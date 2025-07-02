package com.webcrawler.integration

import com.webcrawler.domain.CrawlResult
import com.webcrawler.domain.ports.CrawlResultReporter
import java.util.concurrent.ConcurrentLinkedQueue

class CapturingCrawlResultReporter : CrawlResultReporter {
    
    private val _results = ConcurrentLinkedQueue<CrawlResult>()
    private var _completion: CompletionInfo? = null
    
    val results: List<CrawlResult> get() = _results.toList()
    val completion: CompletionInfo? get() = _completion
    
    override suspend fun reportResult(result: CrawlResult) {
        _results.add(result)
    }
    
    override suspend fun reportCompletion(totalPages: Int, successfulPages: Int, failedPages: Int) {
        _completion = CompletionInfo(totalPages, successfulPages, failedPages)
    }
    
    data class CompletionInfo(
        val totalPages: Int,
        val successfulPages: Int,
        val failedPages: Int
    )
}
