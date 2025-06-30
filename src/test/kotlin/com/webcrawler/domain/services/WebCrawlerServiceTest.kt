package com.webcrawler.domain.services

import com.webcrawler.domain.CrawlStatus
import com.webcrawler.domain.CrawlUrl
import com.webcrawler.domain.CrawlerConfig
import com.webcrawler.domain.ports.CrawlResultReporter
import com.webcrawler.domain.ports.HtmlParser
import com.webcrawler.domain.ports.WebPageFetcher
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WebCrawlerServiceTest {
    
    private lateinit var mockWebPageFetcher: WebPageFetcher
    private lateinit var mockHtmlParser: HtmlParser
    private lateinit var mockResultReporter: CrawlResultReporter
    private lateinit var webCrawlerService: WebCrawlerService
    
    @BeforeEach
    fun setup() {
        mockWebPageFetcher = mockk()
        mockHtmlParser = mockk()
        mockResultReporter = mockk()
        
        webCrawlerService = WebCrawlerService(
            webPageFetcher = mockWebPageFetcher,
            htmlParser = mockHtmlParser,
            resultReporter = mockResultReporter,
            config = CrawlerConfig(maxConcurrency = 2, requestDelayMillis = 0)
        )
        
        // Default mock behaviors
        coEvery { mockResultReporter.reportResult(any()) } just Runs
        coEvery { mockResultReporter.reportCompletion(any(), any(), any()) } just Runs
    }
    
    @Test
    fun `should crawl single page successfully`() = runTest {
        val startUrl = CrawlUrl("https://example.com")
        val html = "<html><body><a href='https://example.com/page1'>Link 1</a></body></html>"
        val extractedLinks = listOf(CrawlUrl("https://example.com/page1"))
        
        coEvery { mockWebPageFetcher.fetchPage(startUrl) } returns html
        every { mockHtmlParser.extractLinks(html, startUrl) } returns extractedLinks
        
        coEvery { mockWebPageFetcher.fetchPage(CrawlUrl("https://example.com/page1")) } returns null
        
        val summary = webCrawlerService.crawlWebsite(startUrl)
        
        assertEquals(2, summary.totalPages)
        assertEquals(1, summary.successfulPages)
        assertEquals(1, summary.failedPages)
        
        coVerify { mockWebPageFetcher.fetchPage(startUrl) }
        coVerify { mockWebPageFetcher.fetchPage(CrawlUrl("https://example.com/page1")) }
        verify { mockHtmlParser.extractLinks(html, startUrl) }
        coVerify { mockResultReporter.reportCompletion(2, 1, 1) }
    }
    
    @Test
    fun `should filter links to same domain only`() = runTest {
        val startUrl = CrawlUrl("https://example.com")
        val html = """
            <html><body>
                <a href='https://example.com/page1'>Same domain</a>
                <a href='https://other.com/page1'>Different domain</a>
                <a href='https://sub.example.com/page1'>Subdomain</a>
            </body></html>
        """.trimIndent()
        
        val extractedLinks = listOf(
            CrawlUrl("https://example.com/page1"),
            CrawlUrl("https://other.com/page1"),
            CrawlUrl("https://sub.example.com/page1")
        )
        
        coEvery { mockWebPageFetcher.fetchPage(startUrl) } returns html
        every { mockHtmlParser.extractLinks(html, startUrl) } returns extractedLinks
        
        // Only the same domain link should be crawled
        coEvery { mockWebPageFetcher.fetchPage(CrawlUrl("https://example.com/page1")) } returns null
        
        webCrawlerService.crawlWebsite(startUrl)
        
        // Verify only same domain links are processed
        coVerify(exactly = 1) { mockWebPageFetcher.fetchPage(CrawlUrl("https://example.com/page1")) }
        coVerify(exactly = 0) { mockWebPageFetcher.fetchPage(CrawlUrl("https://other.com/page1")) }
        coVerify(exactly = 0) { mockWebPageFetcher.fetchPage(CrawlUrl("https://sub.example.com/page1")) }
    }
    
    @Test
    fun `should handle fetch failures gracefully`() = runTest {
        val startUrl = CrawlUrl("https://example.com")
        
        coEvery { mockWebPageFetcher.fetchPage(startUrl) } returns null
        
        val summary = webCrawlerService.crawlWebsite(startUrl)
        
        assertEquals(1, summary.totalPages)
        assertEquals(0, summary.successfulPages)
        assertEquals(1, summary.failedPages)
        
        coVerify { mockResultReporter.reportResult(match { it.status == CrawlStatus.FAILED }) }
    }
    
    @Test
    fun `should handle parsing exceptions gracefully`() = runTest {
        val startUrl = CrawlUrl("https://example.com")
        val html = "<html><body>Some content</body></html>"
        
        coEvery { mockWebPageFetcher.fetchPage(startUrl) } returns html
        every { mockHtmlParser.extractLinks(html, startUrl) } throws RuntimeException("Parse error")
        
        val summary = webCrawlerService.crawlWebsite(startUrl)
        
        assertEquals(1, summary.totalPages)
        assertEquals(0, summary.successfulPages)
        assertEquals(1, summary.failedPages)
        
        coVerify { mockResultReporter.reportResult(match { it.status == CrawlStatus.FAILED }) }
    }
    
    @Test
    fun `should not crawl duplicate URLs`() = runTest {
        val startUrl = CrawlUrl("https://example.com")
        val html = """
            <html><body>
                <a href='https://example.com/page1'>Link 1</a>
                <a href='https://example.com/page1'>Duplicate Link</a>
            </body></html>
        """.trimIndent()
        
        val extractedLinks = listOf(
            CrawlUrl("https://example.com/page1"),
            CrawlUrl("https://example.com/page1") // Duplicate
        )
        
        coEvery { mockWebPageFetcher.fetchPage(startUrl) } returns html
        every { mockHtmlParser.extractLinks(html, startUrl) } returns extractedLinks
        
        coEvery { mockWebPageFetcher.fetchPage(CrawlUrl("https://example.com/page1")) } returns null
        
        val summary = webCrawlerService.crawlWebsite(startUrl)
        
        // Should only fetch the duplicate URL once
        coVerify(exactly = 1) { mockWebPageFetcher.fetchPage(CrawlUrl("https://example.com/page1")) }
        assertEquals(2, summary.totalPages) // start page + unique page1
    }
    
    @Test
    fun `should report all results correctly`() = runTest {
        val startUrl = CrawlUrl("https://example.com")
        val html = "<html><body><a href='https://example.com/page1'>Link</a></body></html>"
        
        coEvery { mockWebPageFetcher.fetchPage(startUrl) } returns html
        every { mockHtmlParser.extractLinks(html, startUrl) } returns listOf(CrawlUrl("https://example.com/page1"))
        
        coEvery { mockWebPageFetcher.fetchPage(CrawlUrl("https://example.com/page1")) } returns null
        
        webCrawlerService.crawlWebsite(startUrl)
        
        coVerify(exactly = 2) { mockResultReporter.reportResult(any()) }
        coVerify { mockResultReporter.reportCompletion(any(), any(), any()) }
    }
}
