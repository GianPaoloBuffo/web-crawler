package com.webcrawler

import com.webcrawler.application.WebCrawlerApplication
import com.webcrawler.domain.CrawlerConfig
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.system.exitProcess

/**
 * Main entry point for the Web Crawler application.
 */
object Main {
    private val logger = KotlinLogging.logger {}
    
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            printUsage()
            exitProcess(1)
        }
        
        // Check for help flag first
        if (args.contains("--help")) {
            printUsage()
            exitProcess(0)
        }
        
        val startingUrl = args[0]
        
        // Parse additional configuration from command line arguments
        val config = parseConfig(args)
        
        val application = WebCrawlerApplication(config)
        
        try {
            runBlocking {
                application.crawl(startingUrl)
            }
        } catch (e: Exception) {
            logger.error(e) { "Application failed" }
            exitProcess(1)
        } finally {
            application.shutdown()
        }
    }
    
    private fun parseConfig(args: Array<String>): CrawlerConfig {
        var maxConcurrency = 10
        var requestDelayMillis = 100L
        var timeoutMillis = 30000L
        var maxRetries = 3
        var userAgent = "WebCrawler/1.0"
        
        var i = 1
        while (i < args.size) {
            when (args[i]) {
                "--max-concurrency" -> {
                    if (i + 1 < args.size) {
                        maxConcurrency = args[++i].toIntOrNull() ?: maxConcurrency
                    }
                }
                "--delay" -> {
                    if (i + 1 < args.size) {
                        requestDelayMillis = args[++i].toLongOrNull() ?: requestDelayMillis
                    }
                }
                "--timeout" -> {
                    if (i + 1 < args.size) {
                        timeoutMillis = args[++i].toLongOrNull() ?: timeoutMillis
                    }
                }
                "--max-retries" -> {
                    if (i + 1 < args.size) {
                        maxRetries = args[++i].toIntOrNull() ?: maxRetries
                    }
                }
                "--user-agent" -> {
                    if (i + 1 < args.size) {
                        userAgent = args[++i]
                    }
                }
                "--help" -> {
                    printUsage()
                    exitProcess(0)
                }
            }
            i++
        }
        
        return CrawlerConfig(
            maxConcurrency = maxConcurrency,
            requestDelayMillis = requestDelayMillis,
            timeoutMillis = timeoutMillis,
            maxRetries = maxRetries,
            userAgent = userAgent
        )
    }
    
    private fun printUsage() {
        println("""
            Web Crawler v1.0
            
            Usage: web-crawler <starting-url> [options]
            
            Arguments:
              starting-url          The URL to start crawling from
            
            Options:
              --max-concurrency N   Maximum number of concurrent requests (default: 10)
              --delay N             Delay between requests in milliseconds (default: 100)
              --timeout N           Request timeout in milliseconds (default: 30000)
              --max-retries N       Maximum number of retries per request (default: 3)
              --user-agent STRING   User agent string (default: "WebCrawler/1.0")
              --help                Show this help message
            
            Examples:
              web-crawler https://example.com
              web-crawler https://example.com --max-concurrency 5 --delay 200
        """.trimIndent())
    }
}
