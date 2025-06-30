package com.webcrawler.infrastructure.adapters

import com.webcrawler.domain.CrawlUrl
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JSoupHtmlParserTest {
    
    private val parser = JSoupHtmlParser()
    
    @Test
    fun `should extract absolute links from HTML`() {
        val html = """
            <html>
                <body>
                    <a href="https://example.com/page1">Absolute Link 1</a>
                    <a href="https://example.com/page2">Absolute Link 2</a>
                </body>
            </html>
        """.trimIndent()
        
        val baseUrl = CrawlUrl("https://example.com")
        val links = parser.extractLinks(html, baseUrl)
        
        assertEquals(2, links.size)
        assertTrue(links.contains(CrawlUrl("https://example.com/page1")))
        assertTrue(links.contains(CrawlUrl("https://example.com/page2")))
    }
    
    @Test
    fun `should convert relative links to absolute`() {
        val html = """
            <html>
                <body>
                    <a href="/page1">Relative Link 1</a>
                    <a href="/subfolder/page2">Relative Link 2</a>
                    <a href="page3">Relative Link 3</a>
                </body>
            </html>
        """.trimIndent()
        
        val baseUrl = CrawlUrl("https://example.com")
        val links = parser.extractLinks(html, baseUrl)
        
        assertEquals(3, links.size)
        assertTrue(links.contains(CrawlUrl("https://example.com/page1")))
        assertTrue(links.contains(CrawlUrl("https://example.com/subfolder/page2")))
        assertTrue(links.contains(CrawlUrl("https://example.com/page3")))
    }
    
    @Test
    fun `should handle links with different protocols`() {
        val html = """
            <html>
                <body>
                    <a href="https://example.com/secure">HTTPS Link</a>
                    <a href="http://example.com/insecure">HTTP Link</a>
                    <a href="mailto:test@example.com">Email Link</a>
                    <a href="javascript:void(0)">JavaScript Link</a>
                </body>
            </html>
        """.trimIndent()
        
        val baseUrl = CrawlUrl("https://example.com")
        val links = parser.extractLinks(html, baseUrl)
        
        // Should include HTTP and HTTPS links, but filter out non-HTTP protocols
        val httpLinks = links.filter { it.normalizedUrl.startsWith("http") }
        assertEquals(2, httpLinks.size)
        assertTrue(links.any { it.normalizedUrl == "https://example.com/secure" })
        assertTrue(links.any { it.normalizedUrl == "http://example.com/insecure" })
    }
    
    @Test
    fun `should remove duplicates`() {
        val html = """
            <html>
                <body>
                    <a href="https://example.com/page1">Link 1</a>
                    <a href="https://example.com/page1">Duplicate Link 1</a>
                    <a href="/page1">Relative to same page</a>
                </body>
            </html>
        """.trimIndent()
        
        val baseUrl = CrawlUrl("https://example.com")
        val links = parser.extractLinks(html, baseUrl)
        
        assertEquals(1, links.size)
        assertEquals("https://example.com/page1", links[0].normalizedUrl)
    }
    
    @Test
    fun `should ignore links without href attribute`() {
        val html = """
            <html>
                <body>
                    <a href="https://example.com/page1">Valid Link</a>
                    <a>No href attribute</a>
                    <a href="">Empty href</a>
                    <a href="   ">Whitespace href</a>
                </body>
            </html>
        """.trimIndent()
        
        val baseUrl = CrawlUrl("https://example.com")
        val links = parser.extractLinks(html, baseUrl)
        
        assertEquals(1, links.size)
        assertEquals("https://example.com/page1", links[0].normalizedUrl)
    }
    
    @Test
    fun `should handle malformed HTML gracefully`() {
        val html = """
            <html>
                <body>
                    <a href="https://example.com/page1">Valid Link</a>
                    <a href="not-a-valid-url">Relative URL</a>
                    <div>Some other content</div>
                </body>
            </html>
        """.trimIndent()
        
        val baseUrl = CrawlUrl("https://example.com")
        val links = parser.extractLinks(html, baseUrl)
        
        // Should extract valid links including relative URLs resolved to absolute
        assertEquals(2, links.size)
        assertTrue(links.any { it.normalizedUrl == "https://example.com/page1" })
        assertTrue(links.any { it.normalizedUrl == "https://example.com/not-a-valid-url" })
    }
    
    @Test
    fun `should return empty list for HTML with no links`() {
        val html = """
            <html>
                <body>
                    <h1>Title</h1>
                    <p>Some content without links</p>
                    <div>More content</div>
                </body>
            </html>
        """.trimIndent()
        
        val baseUrl = CrawlUrl("https://example.com")
        val links = parser.extractLinks(html, baseUrl)
        
        assertTrue(links.isEmpty())
    }
    
    @Test
    fun `should handle empty HTML`() {
        val html = ""
        
        val baseUrl = CrawlUrl("https://example.com")
        val links = parser.extractLinks(html, baseUrl)
        
        assertTrue(links.isEmpty())
    }
}
