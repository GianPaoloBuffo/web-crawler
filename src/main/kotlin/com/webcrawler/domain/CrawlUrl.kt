package com.webcrawler.domain

/**
 * Represents a URL with validation and normalization capabilities.
 */
data class CrawlUrl(val value: String) {
    
    init {
        require(value.isNotBlank()) { "URL cannot be blank" }
        require(isValidUrl(value)) { "Invalid URL format: $value" }
    }
    
    val domain: String
        get() = extractDomain(value)
    
    val normalizedUrl: String
        get() = normalizeUrl(value)
    
    private fun isValidUrl(url: String): Boolean {
        return try {
            val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url
            
            java.net.URI(normalizedUrl).toURL()
            true
        } catch (_: Exception) {
            false
        }
    }
    
    private fun extractDomain(url: String): String {
        val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else url
        
        return try {
            val uri = java.net.URI(normalizedUrl)
            uri.host.lowercase()
        } catch (e: Exception) {
            throw IllegalArgumentException("Cannot extract domain from URL: $url", e)
        }
    }
    
    private fun normalizeUrl(url: String): String {
        val withProtocol = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else url
        
        return try {
            val uri = java.net.URI(withProtocol)
            val normalizedPath = when {
                uri.path.isNullOrEmpty() -> "/"
                uri.path.endsWith("/") && uri.path.length > 1 -> uri.path.dropLast(1)
                else -> uri.path
            }
            
            val hostWithPort = if (uri.port != -1) {
                "${uri.host.lowercase()}:${uri.port}"
            } else {
                uri.host.lowercase()
            }
            
            "${uri.scheme}://$hostWithPort$normalizedPath"
        } catch (_: Exception) {
            withProtocol
        }
    }
    
    override fun toString(): String = normalizedUrl
}
