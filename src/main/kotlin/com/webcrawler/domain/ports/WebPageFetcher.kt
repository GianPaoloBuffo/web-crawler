package com.webcrawler.domain.ports

import com.webcrawler.domain.CrawlUrl

interface WebPageFetcher {
    /**
     * Fetches the HTML content of a web page.
     * 
     * @param url The URL to fetch
     * @return The HTML content of the page, or null if the page could not be fetched
     * @throws Exception if there's an error fetching the page
     */
    suspend fun fetchPage(url: CrawlUrl): String?
}
