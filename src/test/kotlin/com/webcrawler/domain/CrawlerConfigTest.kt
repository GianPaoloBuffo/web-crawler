package com.webcrawler.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CrawlerConfigTest {
    
    @Test
    fun `should create config with default values`() {
        val config = CrawlerConfig()
        
        assertEquals(10, config.maxConcurrency)
        assertEquals(100L, config.requestDelayMillis)
        assertEquals(30000L, config.timeoutMillis)
        assertEquals(3, config.maxRetries)
        assertEquals("WebCrawler/1.0", config.userAgent)
    }
    
    @Test
    fun `should create config with custom values`() {
        val config = CrawlerConfig(
            maxConcurrency = 5,
            requestDelayMillis = 200L,
            timeoutMillis = 60000L,
            maxRetries = 5,
            userAgent = "CustomCrawler/2.0"
        )
        
        assertEquals(5, config.maxConcurrency)
        assertEquals(200L, config.requestDelayMillis)
        assertEquals(60000L, config.timeoutMillis)
        assertEquals(5, config.maxRetries)
        assertEquals("CustomCrawler/2.0", config.userAgent)
    }
    
    @Test
    fun `should throw exception for negative max concurrency`() {
        assertThrows<IllegalArgumentException> {
            CrawlerConfig(maxConcurrency = -1)
        }
    }
    
    @Test
    fun `should throw exception for zero max concurrency`() {
        assertThrows<IllegalArgumentException> {
            CrawlerConfig(maxConcurrency = 0)
        }
    }
    
    @Test
    fun `should throw exception for negative request delay`() {
        assertThrows<IllegalArgumentException> {
            CrawlerConfig(requestDelayMillis = -1L)
        }
    }
    
    @Test
    fun `should allow zero request delay`() {
        val config = CrawlerConfig(requestDelayMillis = 0L)
        assertEquals(0L, config.requestDelayMillis)
    }
    
    @Test
    fun `should throw exception for negative timeout`() {
        assertThrows<IllegalArgumentException> {
            CrawlerConfig(timeoutMillis = -1L)
        }
    }
    
    @Test
    fun `should throw exception for zero timeout`() {
        assertThrows<IllegalArgumentException> {
            CrawlerConfig(timeoutMillis = 0L)
        }
    }
    
    @Test
    fun `should throw exception for negative max retries`() {
        assertThrows<IllegalArgumentException> {
            CrawlerConfig(maxRetries = -1)
        }
    }
    
    @Test
    fun `should allow zero max retries`() {
        val config = CrawlerConfig(maxRetries = 0)
        assertEquals(0, config.maxRetries)
    }
    
    @Test
    fun `should throw exception for blank user agent`() {
        assertThrows<IllegalArgumentException> {
            CrawlerConfig(userAgent = "")
        }
    }
    
    @Test
    fun `should throw exception for whitespace-only user agent`() {
        assertThrows<IllegalArgumentException> {
            CrawlerConfig(userAgent = "   ")
        }
    }
}
