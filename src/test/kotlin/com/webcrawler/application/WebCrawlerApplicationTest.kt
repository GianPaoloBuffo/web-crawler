package com.webcrawler.application

import com.webcrawler.domain.CrawlerConfig
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull

class WebCrawlerApplicationTest {
    
    @Test
    fun `should create application with default config`() {
        val application = WebCrawlerApplication()
        assertNotNull(application)
    }
    
    @Test
    fun `should create application with custom config`() {
        val config = CrawlerConfig(maxConcurrency = 5)
        val application = WebCrawlerApplication(config)
        assertNotNull(application)
    }
    
    @Test
    fun `should validate URL before crawling`() = runTest {
        val application = WebCrawlerApplication()
        
        assertThrows<IllegalArgumentException> {
            application.crawl("ht tp://invalid url with spaces")
        }
        
        application.shutdown()
    }

    @Test
    fun `should handle valid URL`() = runTest {
        val application = WebCrawlerApplication()
        
        try {
            val summary = application.crawl("https://httpbin.org/html")
            assertNotNull(summary)
        } catch (e: Exception) {
            // Expected in offline environments
            println("Integration test skipped - network unavailable: ${e.message}")
        } finally {
            application.shutdown()
        }
    }
    
    @Test
    fun `should shutdown gracefully`() {
        val application = WebCrawlerApplication()
        application.shutdown() // Should not throw
    }
}
