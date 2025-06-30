package com.webcrawler.domain

/**
 * Represents the result of crawling a single page.
 */
data class CrawlResult(
    val url: CrawlUrl,
    val links: List<CrawlUrl>,
    val status: CrawlStatus,
    val errorMessage: String? = null
)

/**
 * Enum representing the status of a crawl operation.
 */
enum class CrawlStatus {
    SUCCESS,
    FAILED,
    SKIPPED
}
