package com.webcrawler.infrastructure.adapters

import com.webcrawler.domain.CrawlUrl
import com.webcrawler.domain.CrawlerConfig
import com.webcrawler.domain.ports.WebPageFetcher
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import mu.KotlinLogging

class KtorWebPageFetcher(
    private val config: CrawlerConfig = CrawlerConfig()
) : WebPageFetcher {
    
    private val logger = KotlinLogging.logger {}
    
    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = config.timeoutMillis
            connectTimeoutMillis = config.timeoutMillis
            socketTimeoutMillis = config.timeoutMillis
        }
        
        install(ContentEncoding) {
            gzip()
            deflate()
        }
        
        install(Logging) {
            level = LogLevel.INFO
        }
        
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = config.maxRetries)
            exponentialDelay()
        }
        
        install(UserAgent) {
            agent = config.userAgent
        }
        
        followRedirects = true
        
        defaultRequest {
            header(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            header(HttpHeaders.AcceptLanguage, "en-US,en;q=0.5")
            header(HttpHeaders.AcceptEncoding, "gzip, deflate")
            header(HttpHeaders.Connection, "keep-alive")
            header("Upgrade-Insecure-Requests", "1")
        }
    }
    
    override suspend fun fetchPage(url: CrawlUrl): String? {
        return try {
            logger.debug { "Fetching page: ${url.normalizedUrl}" }
            
            val response = httpClient.get(url.normalizedUrl)
            
            when {
                response.status.isSuccess() -> {
                    val contentType = response.headers[HttpHeaders.ContentType]
                    if (contentType?.contains("text/html", ignoreCase = true) == true) {
                        response.bodyAsText()
                    } else {
                        logger.debug { "Skipping non-HTML content: $contentType for ${url.normalizedUrl}" }
                        null
                    }
                }
                else -> {
                    logger.warn { "HTTP ${response.status.value} for ${url.normalizedUrl}" }
                    null
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to fetch ${url.normalizedUrl}" }
            null
        }
    }
    
    /**
     * Closes the HTTP client and releases resources.
     */
    fun close() {
        httpClient.close()
    }
}
