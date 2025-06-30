package com.webcrawler.domain

/**
 * Configuration for the web crawler.
 */
data class CrawlerConfig(
    val maxConcurrency: Int = 10,
    val requestDelayMillis: Long = 100,
    val timeoutMillis: Long = 30000,
    val maxRetries: Int = 3,
    val userAgent: String = "WebCrawler/1.0"
) {
    init {
        require(maxConcurrency > 0) { "Max concurrency must be positive" }
        require(requestDelayMillis >= 0) { "Request delay cannot be negative" }
        require(timeoutMillis > 0) { "Timeout must be positive" }
        require(maxRetries >= 0) { "Max retries cannot be negative" }
        require(userAgent.isNotBlank()) { "User agent cannot be blank" }
    }
}
