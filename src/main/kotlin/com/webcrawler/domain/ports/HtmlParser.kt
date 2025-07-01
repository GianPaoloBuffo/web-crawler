package com.webcrawler.domain.ports

import com.webcrawler.domain.CrawlUrl

interface HtmlParser {
    /**
     * Parses HTML content and extracts all links.
     * 
     * @param html The HTML content to parse
     * @param baseUrl The base URL for resolving relative links
     * @return List of extracted URLs
     */
    fun extractLinks(html: String, baseUrl: CrawlUrl): List<CrawlUrl>
}
