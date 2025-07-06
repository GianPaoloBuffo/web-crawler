package com.webcrawler.infrastructure.adapters

import com.webcrawler.domain.CrawlUrl
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KtorWebPageFetcherTest {

    @Test
    fun `should fetch HTML content successfully`() = runTest {
        // Given
        val mockEngine = MockEngine { request ->
            when (request.url.toString()) {
                "https://example.com/" -> respond(
                    content = "<html><body><h1>Test Page</h1></body></html>",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "text/html")
                )

                else -> respond(
                    content = "Not Found",
                    status = HttpStatusCode.NotFound
                )
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
            }
        }

        val fetcher = object : com.webcrawler.domain.ports.WebPageFetcher {
            override suspend fun fetchPage(url: CrawlUrl): String? {
                return try {
                    val response = httpClient.get(url.normalizedUrl)
                    if (response.status.isSuccess()) {
                        response.bodyAsText()
                    } else null
                } catch (_: Exception) {
                    null
                }
            }
        }

        val url = CrawlUrl("https://example.com/")

        // When
        val content = fetcher.fetchPage(url)

        // Then
        assertEquals("<html><body><h1>Test Page</h1></body></html>", content)

        httpClient.close()
    }

    @Test
    fun `should return null for 404 responses`() = runTest {
        // Given
        val mockEngine = MockEngine { request ->
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound
            )
        }

        val httpClient = HttpClient(mockEngine)

        val fetcher = object : com.webcrawler.domain.ports.WebPageFetcher {
            override suspend fun fetchPage(url: CrawlUrl): String? {
                return try {
                    val response = httpClient.get(url.normalizedUrl)
                    if (response.status.isSuccess()) {
                        response.bodyAsText()
                    } else null
                } catch (_: Exception) {
                    null
                }
            }
        }

        val url = CrawlUrl("https://example.com/not-found")

        // When
        val content = fetcher.fetchPage(url)

        // Then
        assertNull(content)

        httpClient.close()
    }

    @Test
    fun `should handle timeout gracefully`() = runTest {
        // Given
        val mockEngine = MockEngine { request ->
            delay(100) // 100ms delay
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/html")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(HttpTimeout) {
                requestTimeoutMillis = 50 // Shorter than the delay
            }
        }

        val fetcher = object : com.webcrawler.domain.ports.WebPageFetcher {
            override suspend fun fetchPage(url: CrawlUrl): String? {
                return try {
                    val response = httpClient.get(url.normalizedUrl)
                    if (response.status.isSuccess()) {
                        response.bodyAsText()
                    } else null
                } catch (_: Exception) {
                    null
                }
            }
        }

        val url = CrawlUrl("https://example.com/slow")

        // When
        val content = fetcher.fetchPage(url)

        // Then
        assertNull(content)

        httpClient.close()
    }
}
