package com.webcrawler.domain.services

import com.webcrawler.domain.*
import com.webcrawler.domain.ports.CrawlResultReporter
import com.webcrawler.domain.ports.HtmlParser
import com.webcrawler.domain.ports.WebPageFetcher
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Domain service that implements the web crawler business logic.
 * This is the core of our hexagonal architecture.
 */
class WebCrawlerService(
    private val webPageFetcher: WebPageFetcher,
    private val htmlParser: HtmlParser,
    private val resultReporter: CrawlResultReporter,
    private val config: CrawlerConfig = CrawlerConfig()
) {
    private val logger = KotlinLogging.logger {}
    
    /**
     * Crawls a website starting from the given URL, staying within the same domain.
     * 
     * @param startingUrl The URL to start crawling from
     * @return Summary of the crawl operation
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun crawlWebsite(startingUrl: CrawlUrl): CrawlSummary {
        logger.info { "Starting crawl from: ${startingUrl.normalizedUrl}" }
        
        val targetDomain = startingUrl.domain
        val visitedUrls = ConcurrentHashMap<String, Boolean>()
        val urlQueue = Channel<CrawlUrl>(Channel.UNLIMITED)
        val semaphore = Semaphore(config.maxConcurrency)
        
        val successfulPages = AtomicInteger(0)
        val failedPages = AtomicInteger(0)
        val totalPages = AtomicInteger(0)
        
        // Add starting URL to queue
        urlQueue.trySend(startingUrl)
        visitedUrls[startingUrl.normalizedUrl] = true
        
        return coroutineScope {
            val crawlerJobs = mutableListOf<Job>()
            
            // Launch crawler workers
            repeat(config.maxConcurrency) { workerId ->
                val job = launch {
                    crawlWorker(
                        workerId = workerId,
                        targetDomain = targetDomain,
                        urlQueue = urlQueue,
                        visitedUrls = visitedUrls,
                        semaphore = semaphore,
                        successfulPages = successfulPages,
                        failedPages = failedPages,
                        totalPages = totalPages
                    )
                }
                crawlerJobs.add(job)
            }
            
            // Wait for all workers to complete (when queue is empty and no workers are active)
            while (crawlerJobs.any { it.isActive }) {
                delay(100)
                // If queue is empty and no worker is currently processing, we're done
                if (urlQueue.isEmpty && semaphore.availablePermits == config.maxConcurrency) {
                    break
                }
            }
            
            // Cancel any remaining jobs
            crawlerJobs.forEach { it.cancel() }
            
            val summary = CrawlSummary(
                totalPages = totalPages.get(),
                successfulPages = successfulPages.get(),
                failedPages = failedPages.get()
            )
            
            resultReporter.reportCompletion(
                totalPages = summary.totalPages,
                successfulPages = summary.successfulPages,
                failedPages = summary.failedPages
            )
            
            logger.info { "Crawl completed: $summary" }
            summary
        }
    }
    
    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun crawlWorker(
        workerId: Int,
        targetDomain: String,
        urlQueue: Channel<CrawlUrl>,
        visitedUrls: ConcurrentHashMap<String, Boolean>,
        semaphore: Semaphore,
        successfulPages: AtomicInteger,
        failedPages: AtomicInteger,
        totalPages: AtomicInteger
    ) {
        logger.debug { "Worker $workerId started" }
        
        while (!urlQueue.isClosedForReceive) {
            val url = urlQueue.tryReceive().getOrNull() ?: break
            
            semaphore.acquire()
            try {
                val result = crawlSinglePage(url, targetDomain)
                resultReporter.reportResult(result)
                
                when (result.status) {
                    CrawlStatus.SUCCESS -> {
                        successfulPages.incrementAndGet()
                        // Add new URLs to queue
                        result.links.forEach { link ->
                            if (visitedUrls.putIfAbsent(link.normalizedUrl, true) == null) {
                                urlQueue.trySend(link)
                            }
                        }
                    }
                    CrawlStatus.FAILED -> failedPages.incrementAndGet()
                    CrawlStatus.SKIPPED -> { /* Don't count skipped pages */ }
                }
                
                totalPages.incrementAndGet()
                
                // Respect rate limiting
                if (config.requestDelayMillis > 0) {
                    delay(config.requestDelayMillis)
                }
            } finally {
                semaphore.release()
            }
        }
        
        logger.debug { "Worker $workerId finished" }
    }
    
    private suspend fun crawlSinglePage(url: CrawlUrl, targetDomain: String): CrawlResult {
        logger.debug { "Crawling: ${url.normalizedUrl}" }
        
        return try {
            val html = webPageFetcher.fetchPage(url)
                ?: return CrawlResult(url, emptyList(), CrawlStatus.FAILED, "Failed to fetch page content")
            
            val extractedLinks = htmlParser.extractLinks(html, url)
            val sameDomainLinks = extractedLinks.filter { it.domain == targetDomain }
            
            CrawlResult(url, sameDomainLinks, CrawlStatus.SUCCESS)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to crawl ${url.normalizedUrl}" }
            CrawlResult(url, emptyList(), CrawlStatus.FAILED, e.message)
        }
    }
}

/**
 * Summary of a crawl operation.
 */
data class CrawlSummary(
    val totalPages: Int,
    val successfulPages: Int,
    val failedPages: Int
)
