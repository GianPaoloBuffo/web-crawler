package com.webcrawler.domain

// Things to talk about:
// url normalisation

data class CrawlUrl(val value: String) {
    
    init {
        require(value.isNotBlank()) { "URL cannot be blank" }
        require(isValidUrl(value)) { "Invalid URL format: $value" }
    }
    
    val domain: String
        get() = extractDomain(value)
    
    val normalizedUrl: String
        get() = normalizeUrl(value)
    
    private fun isValidUrl(url: String): Boolean = try {
        val normalizedUrl = url.withProtocol()
        java.net.URI(normalizedUrl).toURL()
        true
    } catch (_: Exception) {
        false
    }
    
    private fun extractDomain(url: String): String = try {
        val uri = java.net.URI(normalizedUrl.withProtocol())
        uri.host.lowercase()
    } catch (e: Exception) {
        throw IllegalArgumentException("Cannot extract domain from URL: $url", e)
    }
    
    private fun normalizeUrl(url: String): String {
        val urlWithProtocol = url.withProtocol()
        
        return try {
            val uri = java.net.URI(urlWithProtocol)
            val normalizedPath = when {
                uri.path.isNullOrEmpty() -> "/"
                uri.path.endsWith("/") && uri.path.length > 1 -> uri.path.dropLast(1) // user/about/ -> user/about
                else -> uri.path
            }
            
            val hostWithPort = if (uri.port != -1) {
                "${uri.host.lowercase()}:${uri.port}"
            } else {
                uri.host.lowercase()
            }
            
            "${uri.scheme}://$hostWithPort$normalizedPath"
        } catch (_: Exception) {
            urlWithProtocol
        }
    }

    private fun String.withProtocol(): String = if (!startsWith("http://") && !startsWith("https://")) {
        "https://$this"
    } else this

    override fun toString(): String = normalizedUrl
}
