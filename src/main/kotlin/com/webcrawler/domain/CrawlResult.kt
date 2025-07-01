package com.webcrawler.domain

data class CrawlResult(
    val url: CrawlUrl,
    val links: List<CrawlUrl>,
    val status: CrawlStatus,
    val errorMessage: String? = null
)

enum class CrawlStatus {
    SUCCESS,
    FAILED,
}
