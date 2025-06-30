import com.webcrawler.infrastructure.adapters.JSoupHtmlParser
import com.webcrawler.domain.CrawlUrl

fun main() {
    val parser = JSoupHtmlParser()
    val html = """
        <html>
            <body>
                <a href="https://example.com/secure">HTTPS Link</a>
                <a href="http://example.com/insecure">HTTP Link</a>
                <a href="mailto:test@example.com">Email Link</a>
                <a href="javascript:void(0)">JavaScript Link</a>
            </body>
        </html>
    """.trimIndent()
    
    val baseUrl = CrawlUrl("https://example.com")
    val links = parser.extractLinks(html, baseUrl)
    
    println("Total links extracted: ${links.size}")
    links.forEachIndexed { index, link ->
        println("$index: ${link.normalizedUrl}")
    }
    
    val httpLinks = links.filter { it.normalizedUrl.startsWith("http") }
    println("HTTP links: ${httpLinks.size}")
    httpLinks.forEach { println("  - ${it.normalizedUrl}") }
}
