# Web Crawler

A production-quality concurrent web crawler written in Kotlin using hexagonal architecture principles.

## Quick Start

1. **Run the demo:**
   ```bash
   ./demo.sh
   ```

2. **Crawl a website:**
   ```bash
   ./gradlew run --args="https://example.com"
   ```

3. **With custom settings:**
   ```bash
   ./gradlew run --args="https://example.com --max-concurrency 5 --delay 200"
   ```

4. **Show help:**
   ```bash
   ./gradlew run --args="--help"
   ```

## Overview

This web crawler visits web pages starting from a given URL and follows links within the same domain. It leverages Kotlin coroutines for concurrent processing and follows SOLID principles with a clean hexagonal architecture.

## Features

- **Concurrent crawling** using Kotlin coroutines
- **Domain-restricted crawling** - stays within the starting domain
- **Hexagonal architecture** for clean separation of concerns
- **Rate limiting** and configurable request delays
- **Robust error handling** with retries
- **Comprehensive logging** with structured output
- **URL normalization** and duplicate detection
- **Configurable timeouts** and retry policies
- **Production-ready** with comprehensive testing

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

### Command Line Options

```bash
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
```

### Examples

**Basic crawling:**
```bash
./gradlew run --args="https://example.com"
```

**With custom settings:**
```bash
./gradlew run --args="https://example.com --max-concurrency 5 --delay 200 --timeout 60000"
```

## Configuration

The crawler can be configured through command-line arguments or by modifying the `CrawlerConfig` class:

- **maxConcurrency**: Number of concurrent requests (default: 10)
- **requestDelayMillis**: Delay between requests in milliseconds (default: 100)
- **timeoutMillis**: Request timeout in milliseconds (default: 30000)
- **maxRetries**: Maximum retry attempts per request (default: 3)
- **userAgent**: User agent string sent with requests (default: "WebCrawler/1.0")

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

### Available Tests

The project includes comprehensive testing code:

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions  
- **Domain Tests**: Test business logic and value objects
- **Infrastructure Tests**: Test external adapters

Test files are located in:
- `src/test/kotlin/com/webcrawler/domain/` - Domain model tests
- `src/test/kotlin/com/webcrawler/infrastructure/` - Infrastructure adapter tests
- `src/test/kotlin/com/webcrawler/application/` - Application service tests

### Manual Testing

You can manually test the application using the demo script:

```bash
./demo.sh
```

Or run individual commands:

```bash
# Test help functionality
./gradlew run --args="--help"

# Test with a simple URL (conservative settings)
./gradlew run --args="example.org --max-concurrency 1 --delay 2000"
```

### Test Coverage

The test code covers:
- URL validation and normalization
- HTML parsing and link extraction  
- Concurrent crawling logic
- Error handling and retries
- Configuration validation

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
- **Memory Usage**: Visited URLs are stored in memory (consider disk-based storage for large crawls)
- **Connection Pooling**: Ktor HTTP client uses connection pooling
- **Duplicate Prevention**: URLs are normalized and deduplicated

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

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

### Why Ktor?
- **Kotlin-first**: Built specifically for Kotlin
- **Coroutines Support**: Native async/await support
- **Lightweight**: Minimal overhead
- **Flexible**: Easy to configure and extend

### Why JSoup?
- **Robust**: Battle-tested HTML parsing library
- **jQuery-like API**: Familiar CSS selector syntax
- **Handles Malformed HTML**: Gracefully handles real-world HTML
- **Link Resolution**: Built-in relative URL resolution
