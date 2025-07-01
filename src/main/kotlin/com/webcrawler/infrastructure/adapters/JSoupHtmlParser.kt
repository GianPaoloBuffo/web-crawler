package com.webcrawler.infrastructure.adapters

import com.webcrawler.domain.CrawlUrl
import com.webcrawler.domain.ports.HtmlParser
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class JSoupHtmlParser : HtmlParser {
    
    private val logger = KotlinLogging.logger {}
    
    override fun extractLinks(html: String, baseUrl: CrawlUrl): List<CrawlUrl> {
        return try {
            val document: Document = Jsoup.parse(html, baseUrl.normalizedUrl)
            
            val links = document.select("a[href]")
                .mapNotNull { element ->
                    val originalHref = element.attr("href").trim()
                    // Skip empty or whitespace-only hrefs
                    if (originalHref.isBlank()) {
                        return@mapNotNull null
                    }
                    
                    val href = element.attr("abs:href")
                    if (href.isNotBlank() && (href.startsWith("http://") || href.startsWith("https://"))) {
                        try {
                            CrawlUrl(href)
                        } catch (_: Exception) {
                            logger.debug { "Invalid URL found: $href" }
                            null
                        }
                    } else null
                }
                .distinct()
            
            logger.debug { "Extracted ${links.size} links from ${baseUrl.normalizedUrl}" }
            links
        } catch (e: Exception) {
            logger.warn(e) { "Failed to parse HTML for ${baseUrl.normalizedUrl}" }
            emptyList()
        }
    }
}

/**
 * JSoup's behavior:
 * val doc = Jsoup.parse(html, "https://example.com")
 *
 * For <a href="">
 * element.attr("href")      Returns: ""
 * element.attr("abs:href")  Returns: "https://example.com" (incorrect)
 *
 * For <a href="   ">
 * element.attr("href")      Returns: "   "
 * element.attr("abs:href")  Returns: "https://example.com" (incorrect)
 *
 * For <a href="/path">
 * element.attr("href")      Returns: "/path"
 * element.attr("abs:href")  Returns: "https://example.com/path" (correct!)
 */
