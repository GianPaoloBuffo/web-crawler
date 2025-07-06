package com.webcrawler.integration

import com.webcrawler.application.WebCrawlerApplication
import com.webcrawler.domain.CrawlerConfig
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.*

class EmbeddedWebCrawlerIntegrationTest {

    companion object {
        private lateinit var server: NettyApplicationEngine
        private const val PORT = 8080
        private const val BASE_URL = "http://localhost:$PORT"

        @JvmStatic
        @BeforeAll
        fun startServer() {
            server = embeddedServer(Netty, port = PORT) {
                routing {
                    get("/") {
                        call.respondText("""
                            <!DOCTYPE html>
                            <html>
                            <head><title>Test Website</title></head>
                            <body>
                                <h1>Welcome to Test Website</h1>
                                <nav>
                                    <a href="/about">About Us</a>
                                    <a href="/services">Services</a>
                                    <a href="https://external.com/page">External Link</a>
                                </nav>
                            </body>
                            </html>
                        """.trimIndent(), io.ktor.http.ContentType.Text.Html)
                    }
                    
                    get("/about") {
                        call.respondText("""
                            <!DOCTYPE html>
                            <html>
                            <head><title>About - Test Website</title></head>
                            <body>
                                <h1>About Us</h1>
                                <p>This is our about page.</p>
                                <a href="/">Home</a>
                                <a href="/services">Our Services</a>
                            </body>
                            </html>
                        """.trimIndent(), io.ktor.http.ContentType.Text.Html)
                    }
                    
                    get("/services") {
                        call.respondText("""
                            <!DOCTYPE html>
                            <html>
                            <head><title>Services - Test Website</title></head>
                            <body>
                                <h1>Our Services</h1>
                                <p>We provide excellent services.</p>
                                <a href="/">Home</a>
                            </body>
                            </html>
                        """.trimIndent(), io.ktor.http.ContentType.Text.Html)
                    }
                    
                    // Test page with malformed HTML
                    get("/malformed") {
                        call.respondText("""
                            <!DOCTYPE html>
                            <html>
                            <head><title>Malformed Page</title></head>
                            <body>
                                <h1>Malformed HTML Test</h1>
                                <a href="/about">Valid Link</a>
                                <a href="">Empty Link</a>
                                <a href="   ">Whitespace Link</a>
                                <a>No href attribute</a>
                                <p>Unclosed paragraph
                                <div>Nested without closing</p>
                            </body>
                            <!-- Missing closing html tag -->
                        """.trimIndent(), io.ktor.http.ContentType.Text.Html)
                    }
                    
                    // Test page that returns 404
                    get("/notfound") {
                        call.respond(io.ktor.http.HttpStatusCode.NotFound, "Page not found")
                    }
                    
                    // Test pages with circular references
                    get("/circular1") {
                        call.respondText("""
                            <!DOCTYPE html>
                            <html>
                            <head><title>Circular 1</title></head>
                            <body>
                                <h1>Circular Reference Page 1</h1>
                                <a href="/circular2">Go to Page 2</a>
                            </body>
                            </html>
                        """.trimIndent(), io.ktor.http.ContentType.Text.Html)
                    }
                    
                    get("/circular2") {
                        call.respondText("""
                            <!DOCTYPE html>
                            <html>
                            <head><title>Circular 2</title></head>
                            <body>
                                <h1>Circular Reference Page 2</h1>
                                <a href="/circular1">Go to Page 1</a>
                            </body>
                            </html>
                        """.trimIndent(), io.ktor.http.ContentType.Text.Html)
                    }
                    
                    // Test redirect
                    get("/redirect") {
                        call.respondRedirect("/about", permanent = false)
                    }
                }
            }
            server.start(wait = false)
            Thread.sleep(1000) // Give server time to start
        }

        @JvmStatic
        @AfterAll
        fun stopServer() {
            server.stop(1000, 2000)
        }
    }

    @Test
    fun `should crawl basic website structure`() = runBlocking {
        // Given
        val startUrl = BASE_URL
        
        val config = CrawlerConfig(
            maxConcurrency = 2,
            requestDelayMillis = 100,
            timeoutMillis = 5000,
            maxRetries = 1
        )

        val captureReporter = CapturingCrawlResultReporter()
        val application = WebCrawlerApplication(config, captureReporter)

        // When
        val summary = application.crawl(startUrl)

        // Then
        val crawledUrls = captureReporter.results.map { it.url.normalizedUrl }

        assertTrue(summary.totalPages == 3, "Should crawl 3 pages, got ${summary.totalPages}")
        assertTrue(crawledUrls.any { it == "$BASE_URL/" }, "Should crawl home page")
        assertTrue(crawledUrls.any { it == "$BASE_URL/about" }, "Should crawl about page")
        assertTrue(crawledUrls.any { it == "$BASE_URL/services" }, "Should crawl services page")

        assertFalse(crawledUrls.any { it.contains("external.com") }, "Should not crawl external domains")
    }

    @Test
    fun `should handle malformed HTML gracefully`() = runBlocking {
        // Given
        val startUrl = "$BASE_URL/malformed"
        
        val config = CrawlerConfig(
            maxConcurrency = 1,
            requestDelayMillis = 50,
            timeoutMillis = 3000
        )

        val captureReporter = CapturingCrawlResultReporter()
        val application = WebCrawlerApplication(config, captureReporter)

        // When
        val summary = application.crawl(startUrl)

        // Then
        assertTrue(summary.totalPages == 4, "Should process the malformed page")
        assertTrue(summary.successfulPages == 4, "Should handle malformed HTML gracefully")
        
        val result = captureReporter.results.first()
        assertTrue(result.links.isNotEmpty(), "Should extract valid links from malformed HTML")
        
        // Should only extract the valid link, not empty/whitespace ones
        val extractedUrls = result.links.map { it.normalizedUrl }
        assertTrue(extractedUrls.any { it.endsWith("/about") }, "Should extract valid link")
        // JSoup should properly filter out empty/whitespace links, so we don't expect them
        assertTrue(extractedUrls.none { it.isEmpty() || it.isBlank() }, "Should filter out empty/whitespace links")
    }

    @Test
    fun `should handle HTTP error responses`() = runBlocking {
        // Given
        val startUrl = "$BASE_URL/notfound"
        
        val config = CrawlerConfig(
            maxConcurrency = 1,
            requestDelayMillis = 50,
            timeoutMillis = 3000
        )

        val captureReporter = CapturingCrawlResultReporter()
        val application = WebCrawlerApplication(config, captureReporter)

        // When
        val summary = application.crawl(startUrl)

        // Then
        assertEquals(1, summary.totalPages, "Should attempt to crawl the 404 page")
        assertEquals(1, summary.failedPages, "Should record 404 as failed")
        assertEquals(0, summary.successfulPages, "Should have no successful pages")
    }

    @Test
    fun `should handle circular references without infinite loops`() = runBlocking {
        // Given
        val startUrl = "$BASE_URL/circular1"
        
        val config = CrawlerConfig(
            maxConcurrency = 1,
            requestDelayMillis = 50,
            timeoutMillis = 5000
        )

        val captureReporter = CapturingCrawlResultReporter()
        val application = WebCrawlerApplication(config, captureReporter)

        // When
        val summary = application.crawl(startUrl)

        // Then
        assertTrue(summary.totalPages == 2, "Should crawl both circular pages")
        assertTrue(summary.successfulPages == 2, "Should successfully handle circular references")
        
        val crawledUrls = captureReporter.results.map { it.url.normalizedUrl }
        assertTrue(crawledUrls.any { it.endsWith("/circular1") }, "Should crawl circular1 page")
        assertTrue(crawledUrls.any { it.endsWith("/circular2") }, "Should crawl circular2 page")
        
        // Should not crawl the same page multiple times
        val uniqueUrls = crawledUrls.distinct()
        assertEquals(crawledUrls.size, uniqueUrls.size, "Should not crawl duplicate URLs")
    }

    @Test
    fun `should handle redirects appropriately`() = runBlocking {
        // Given
        val startUrl = "$BASE_URL/redirect"

        val config = CrawlerConfig(
            maxConcurrency = 1,
            requestDelayMillis = 50,
            timeoutMillis = 3000
        )

        val captureReporter = CapturingCrawlResultReporter()
        val application = WebCrawlerApplication(config, captureReporter)

        // When
        application.crawl(startUrl)

        // Then
        val crawledUrls = captureReporter.results.map { it.url.normalizedUrl }
        assertTrue(crawledUrls.any { it.endsWith("/about") }, "Should successfully follow redirects to final destination")
    }
}
