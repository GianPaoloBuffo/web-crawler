package com.webcrawler.infrastructure.adapters

import com.webcrawler.domain.CrawlResult
import com.webcrawler.domain.CrawlStatus
import com.webcrawler.domain.ports.CrawlResultReporter
import mu.KotlinLogging

class ConsoleCrawlResultReporter : CrawlResultReporter {
    
    private val logger = KotlinLogging.logger {}
    
    override suspend fun reportResult(result: CrawlResult) {
        when (result.status) {
            CrawlStatus.SUCCESS -> {
                println("✓ Crawled: ${result.url.normalizedUrl}")
                if (result.links.isNotEmpty()) {
                    println("  Found ${result.links.size} links:")
                    result.links.forEach { link ->
                        println("    - ${link.normalizedUrl}")
                    }
                } else {
                    println("  No links found")
                }
                println()
            }
            CrawlStatus.FAILED -> {
                println("✗ Failed to crawl: ${result.url.normalizedUrl}")
                result.errorMessage?.let { println("  Error: $it") }
                println()
            }
        }
    }
    
    override suspend fun reportCompletion(totalPages: Int, successfulPages: Int, failedPages: Int) {
        println("=" * 50)
        println("CRAWL COMPLETED")
        println("=" * 50)
        println("Total pages processed: $totalPages")
        println("Successful: $successfulPages")
        println("Failed: $failedPages")
        println("Success rate: ${if (totalPages > 0) (successfulPages * 100) / totalPages else 0}%")
        println("=" * 50)
        
        logger.info { "Crawl completed - Total: $totalPages, Success: $successfulPages, Failed: $failedPages" }
    }
    
    private operator fun String.times(count: Int): String = repeat(count)
}
