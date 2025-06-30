package com.webcrawler.infrastructure.adapters

import com.webcrawler.domain.CrawlUrl
import com.webcrawler.domain.ports.HtmlParser
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Infrastructure adapter that implements HtmlParser using JSoup.
 */
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
