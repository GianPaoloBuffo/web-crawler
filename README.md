# Web Crawler

This web crawler visits web pages starting from a given URL and follows links within the same domain.

## Features

- **Concurrent crawling** using Kotlin coroutines
- **Domain-restricted crawling** - stays within the starting domain
- **Hexagonal architecture** for clean separation of concerns
- **Rate limiting** and configurable request delays
- **Robust error handling** with retries
- **Comprehensive logging** with structured output
- **URL normalization** and duplicate detection
- **Configurable timeouts** and retry policies

## Architecture

The application follows hexagonal architecture (ports and adapters pattern):

```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                    │
│  ┌─────────────────────────────────────────────────┐    │
│  │            WebCrawlerApplication                │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                       │
│  ┌─────────────────────────────────────────────────┐    │
│  │             WebCrawlerService                   │    │
│  │  ┌─────────────┐  ┌──────────────┐             │    │
│  │  │   CrawlUrl  │  │ CrawlResult  │             │    │
│  │  └─────────────┘  └──────────────┘             │    │
│  └─────────────────────────────────────────────────┘    │
│                       Ports                             │
│  ┌─────────────────────────────────────────────────┐    │
│  │ WebPageFetcher │ HtmlParser │ CrawlResultReporter│    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────┐
│                Infrastructure Layer                     │
│  ┌─────────────────────────────────────────────────┐    │
│  │ KtorWebPageFetcher │ JSoupHtmlParser │ Console...│    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

### Domain Layer
- **CrawlUrl**: Value object representing URLs with validation and normalization
- **CrawlResult**: Result of crawling a single page
- **CrawlerConfig**: Configuration for the crawler
- **WebCrawlerService**: Core business logic for web crawling
- **Ports**: Interfaces defining contracts for external dependencies

### Infrastructure Layer
- **KtorWebPageFetcher**: HTTP client implementation using Ktor
- **JSoupHtmlParser**: HTML parsing implementation using JSoup
- **ConsoleCrawlResultReporter**: Console output implementation

### Application Layer
- **WebCrawlerApplication**: Orchestrates the crawling operation and manages dependencies

## Requirements

- Java 21 or higher
- Kotlin 2.1.21
- Gradle 8.x

## Dependencies

- **Ktor**: HTTP client for web requests
- **JSoup**: HTML parsing and link extraction
- **Kotlinx Coroutines**: Concurrent programming
- **Logback**: Structured logging
- **JUnit 5**: Testing framework
- **MockK**: Mocking framework for tests

## Usage

### Building the Project

```bash
./gradlew build
```

### Running the Crawler

```bash
./gradlew run --args="https://example.com"
```

**With custom settings:**
```bash
./gradlew run --args="https://example.com --max-concurrency 5 --delay 200 --timeout 60000"
```

### Command Line Options

```
Options:
  --max-concurrency N   Maximum number of concurrent requests (default: 10)
  --delay N             Delay between requests in milliseconds (default: 100)
  --timeout N           Request timeout in milliseconds (default: 30000)
  --max-retries N       Maximum number of retries per request (default: 3)
  --user-agent STRING   User agent string (default: "WebCrawler/1.0")
  --help                Show this help message
```

## Domain Restrictions

The crawler only follows links within the same domain as the starting URL:

- ✅ `https://example.com/` → `https://example.com/about`
- ✅ `https://example.com/` → `https://example.com/features/travel`
- ❌ `https://example.com/` → `https://community.example.com/`
- ❌ `https://example.com/` → `https://facebook.com/example`

## Output

The crawler outputs:
- Each URL being crawled with success/failure status
- List of links found on each page
- Final summary with statistics

Example output:
```
✓ Crawled: https://example.com
  Found 3 links:
    - https://example.com/about
    - https://example.com/contact
    - https://example.com/blog

✓ Crawled: https://example.com/about
  Found 1 links:
    - https://example.com/team

==================================================
CRAWL COMPLETED
==================================================
Total pages processed: 4
Successful: 4
Failed: 0
Success rate: 100%
==================================================
```

## Testing

### Running Tests

```bash
./gradlew clean test
```

## Error Handling

The crawler handles various error scenarios gracefully:

- **Network timeouts**: Configurable timeout with retries
- **HTTP errors**: 4xx/5xx responses are logged and skipped
- **Invalid URLs**: Malformed URLs are filtered out
- **Parsing errors**: HTML parsing failures are logged and skipped
- **Rate limiting**: Configurable delays between requests

## Logging

The application uses structured logging with different levels:

- **INFO**: High-level crawling progress and statistics
- **DEBUG**: Detailed request/response information
- **WARN**: Recoverable errors and skipped content
- **ERROR**: Unrecoverable errors

Configure logging levels in `src/main/resources/logback.xml`.

## Performance Considerations

- **Concurrency**: Adjustable concurrent request limit
- **Rate Limiting**: Configurable delay between requests
- **Memory Usage**: Visited URLs are stored in memory (disk-based storage could be implemented for large crawls)
- **Connection Pooling**: Ktor HTTP client uses connection pooling
- **Duplicate Prevention**: URLs are normalized and deduplicated

## Architecture Decisions

### Why Hexagonal Architecture?
- **Testability**: Easy to test business logic in isolation
- **Flexibility**: Easy to swap out infrastructure components
- **Maintainability**: Clear separation of concerns
- **Domain Focus**: Business logic is not dependent on external frameworks

### Why Kotlin Coroutines?
- **Lightweight**: More efficient than traditional threads
- **Structured Concurrency**: Better error handling and cancellation
- **Readable**: Async code looks like synchronous code
- **Native Support**: First-class support in Kotlin
