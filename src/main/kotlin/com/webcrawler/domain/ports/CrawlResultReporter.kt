package com.webcrawler.domain.ports

import com.webcrawler.domain.CrawlResult

interface CrawlResultReporter {
    /**
     * Reports the result of crawling a single page.
     * 
     * @param result The crawl result to report
     */
    suspend fun reportResult(result: CrawlResult)
    
    /**
     * Reports the completion of the entire crawl operation.
     * 
     * @param totalPages Total number of pages crawled
     * @param successfulPages Number of pages crawled successfully
     * @param failedPages Number of pages that failed to crawl
     */
    suspend fun reportCompletion(totalPages: Int, successfulPages: Int, failedPages: Int)
}
