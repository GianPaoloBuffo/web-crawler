package com.webcrawler.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CrawlUrlTest {
    
    @Test
    fun `should create valid URL with https protocol`() {
        val url = CrawlUrl("https://example.com")
        assertEquals("https://example.com/", url.normalizedUrl)
        assertEquals("example.com", url.domain)
    }
    
    @Test
    fun `should create valid URL with http protocol`() {
        val url = CrawlUrl("http://example.com")
        assertEquals("http://example.com/", url.normalizedUrl)
        assertEquals("example.com", url.domain)
    }
    
    @Test
    fun `should add https protocol when missing`() {
        val url = CrawlUrl("example.com")
        assertEquals("https://example.com/", url.normalizedUrl)
        assertEquals("example.com", url.domain)
    }
    
    @Test
    fun `should normalize URL by removing trailing slash`() {
        val url = CrawlUrl("https://example.com/path/")
        assertEquals("https://example.com/path", url.normalizedUrl)
    }
    
    @Test
    fun `should keep single slash for root path`() {
        val url = CrawlUrl("https://example.com/")
        assertEquals("https://example.com/", url.normalizedUrl)
    }
    
    @Test
    fun `should normalize domain to lowercase`() {
        val url = CrawlUrl("https://EXAMPLE.COM/Path")
        assertEquals("https://example.com/Path", url.normalizedUrl)
        assertEquals("example.com", url.domain)
    }
    
    @Test
    fun `should handle URL with path and query parameters`() {
        val url = CrawlUrl("https://example.com/path?param=value")
        assertEquals("https://example.com/path", url.normalizedUrl)
        assertEquals("example.com", url.domain)
    }
    
    @Test
    fun `should handle subdomain`() {
        val url = CrawlUrl("https://sub.example.com")
        assertEquals("https://sub.example.com/", url.normalizedUrl)
        assertEquals("sub.example.com", url.domain)
    }
    
    @Test
    fun `should throw exception for blank URL`() {
        assertThrows<IllegalArgumentException> {
            CrawlUrl("")
        }
    }
    
    @Test
    fun `should throw exception for invalid URL format`() {
        assertThrows<IllegalArgumentException> {
            CrawlUrl("ht tp://invalid url with spaces")
        }
    }
    
    @Test
    fun `should handle URL with port`() {
        val url = CrawlUrl("https://example.com:8080/path")
        assertEquals("https://example.com:8080/path", url.normalizedUrl)
        assertEquals("example.com", url.domain)
    }
    
    @Test
    fun `toString should return normalized URL`() {
        val url = CrawlUrl("https://example.com/")
        assertEquals("https://example.com/", url.toString())
    }
}
