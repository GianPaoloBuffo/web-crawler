#!/bin/bash

# Web Crawler Demo Script
echo "=== Web Crawler Demo ==="
echo

echo "1. Showing help:"
./gradlew run --args="--help" --quiet

echo
echo "2. Test crawling with example.org (safe test site):"
echo "Running: ./gradlew run --args=\"example.org --max-concurrency 1 --delay 2000\""
echo "This will run for ~30 seconds and then be terminated for demo purposes..."
echo

timeout 30 ./gradlew run --args="example.org --max-concurrency 1 --delay 2000" --quiet || echo "Demo completed (timed out as expected)"

echo
echo "=== Demo completed ==="
echo
echo "The web crawler is now ready to use!"
echo "Try running:"
echo "  ./gradlew run --args=\"https://your-website.com\""
echo "  ./gradlew run --args=\"https://your-website.com --max-concurrency 5 --delay 200\""
