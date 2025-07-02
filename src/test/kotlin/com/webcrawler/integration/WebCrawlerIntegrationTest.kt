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

class WebCrawlerIntegrationTest {

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
        assertTrue(summary.totalPages >= 3, "Should crawl at least 3 pages, got ${summary.totalPages}")
        assertTrue(summary.successfulPages >= 2, "Most pages should be successful, got ${summary.successfulPages}") 
        assertTrue(summary.failedPages <= 1, "Should have minimal failures, got ${summary.failedPages}")

        val crawledUrls = captureReporter.results.map { it.url.normalizedUrl }

        assertTrue(crawledUrls.any { it == "$BASE_URL/" }, "Should crawl home page")
        assertTrue(crawledUrls.any { it == "$BASE_URL/about" }, "Should crawl about page")
        assertTrue(crawledUrls.any { it == "$BASE_URL/services" }, "Should crawl services page")

        assertFalse(crawledUrls.any { it.contains("external.com") }, "Should not crawl external domains")
    }
}
