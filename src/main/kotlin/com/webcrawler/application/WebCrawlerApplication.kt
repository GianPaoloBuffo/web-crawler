package com.webcrawler.application

import com.webcrawler.domain.*
import com.webcrawler.domain.services.CrawlSummary
import com.webcrawler.domain.services.WebCrawlerService
import com.webcrawler.infrastructure.adapters.ConsoleCrawlResultReporter
import com.webcrawler.infrastructure.adapters.JSoupHtmlParser
import com.webcrawler.infrastructure.adapters.KtorWebPageFetcher
import mu.KotlinLogging

/**
 * Application service that orchestrates the web crawling operation.
 * This is the primary entry point for the application use case.
 */
class WebCrawlerApplication(
    private val config: CrawlerConfig = CrawlerConfig()
) {
    private val logger = KotlinLogging.logger {}
    
    private val webPageFetcher = KtorWebPageFetcher(config)
    private val htmlParser = JSoupHtmlParser()
    private val resultReporter = ConsoleCrawlResultReporter()
    
    private val webCrawlerService = WebCrawlerService(
        webPageFetcher = webPageFetcher,
        htmlParser = htmlParser,
        resultReporter = resultReporter,
        config = config
    )
    
    /**
     * Executes the web crawling operation.
     * 
     * @param startingUrl The URL to start crawling from
     * @return Summary of the crawl operation
     */
    suspend fun crawl(startingUrl: String): CrawlSummary {
        logger.info { "Starting web crawler application" }
        
        return try {
            val crawlUrl = CrawlUrl(startingUrl)
            logger.info { "Validated starting URL: ${crawlUrl.normalizedUrl}" }
            logger.info { "Target domain: ${crawlUrl.domain}" }
            logger.info { "Crawler configuration: $config" }
            
            webCrawlerService.crawlWebsite(crawlUrl)
        } catch (e: Exception) {
            logger.error(e) { "Failed to start crawler" }
            throw e
        }
    }
    
    /**
     * Shuts down the application and releases resources.
     */
    fun shutdown() {
        logger.info { "Shutting down web crawler application" }
        webPageFetcher.close()
    }
}
