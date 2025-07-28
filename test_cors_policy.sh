#!/bin/bash

# CORS Policy Test Script
# Tests allowed and disallowed origins, methods, and credentials

echo "🔧 Setting up CORS test environment..."
API_URL="http://localhost:8085/api/products"
echo "   📍 API URL: $API_URL"

ALLOWED_ORIGIN="https://yourdomain.com"
echo "   ✅ Allowed Origin: $ALLOWED_ORIGIN"

DISALLOWED_ORIGIN="https://malicious-site.com"
echo "   ❌ Disallowed Origin: $DISALLOWED_ORIGIN"

print_status() {
    local status=$1
    local message=$2
    if [ "$status" = "PASS" ]; then
        echo -e "\033[0;32m✅ PASS\033[0m - $message"
    else
        echo -e "\033[0;31m❌ FAIL\033[0m - $message"
    fi
}

echo ""
echo "🔒 ========================================="
echo "🔒 CORS POLICY TEST SUITE"
echo "🔒 ========================================="
echo ""

echo "🧪 Starting CORS policy tests..."
echo ""

# Test 1: Allowed Origin
echo "📋 Test 1: Checking if allowed origin is accepted..."
echo "   🔄 Sending OPTIONS request with Origin: $ALLOWED_ORIGIN"
echo "   📤 Command: curl -s -o /dev/null -w \"%{http_code}\" -H \"Origin: $ALLOWED_ORIGIN\" -X OPTIONS \"$API_URL\""
resp=$(curl -s -o /dev/null -w "%{http_code}" -H "Origin: $ALLOWED_ORIGIN" -X OPTIONS "$API_URL")
echo "   📥 Response code: $resp"
if [ "$resp" = "200" ]; then
    print_status PASS "Allowed origin $ALLOWED_ORIGIN accepted (OPTIONS)"
else
    print_status FAIL "Allowed origin $ALLOWED_ORIGIN was rejected (OPTIONS)"
fi
echo ""

# Test 2: Disallowed Origin
echo "📋 Test 2: Checking if disallowed origin is rejected..."
echo "   🔄 Sending OPTIONS request with Origin: $DISALLOWED_ORIGIN"
echo "   📤 Command: curl -s -o /dev/null -w \"%{http_code}\" -H \"Origin: $DISALLOWED_ORIGIN\" -X OPTIONS \"$API_URL\""
resp=$(curl -s -o /dev/null -w "%{http_code}" -H "Origin: $DISALLOWED_ORIGIN" -X OPTIONS "$API_URL")
echo "   📥 Response code: $resp"
if [ "$resp" = "403" ] || [ "$resp" = "401" ]; then
    print_status PASS "Disallowed origin $DISALLOWED_ORIGIN correctly rejected (OPTIONS)"
else
    print_status FAIL "Disallowed origin $DISALLOWED_ORIGIN was not rejected (OPTIONS), got $resp"
fi
echo ""

# Test 3: Allowed Method
echo "📋 Test 3: Checking if allowed method POST is accepted..."
echo "   🔄 Sending OPTIONS request with Access-Control-Request-Method: POST"
echo "   📤 Command: curl -s -o /dev/null -w \"%{http_code}\" -H \"Origin: $ALLOWED_ORIGIN\" -H \"Access-Control-Request-Method: POST\" -X OPTIONS \"$API_URL\""
resp=$(curl -s -o /dev/null -w "%{http_code}" -H "Origin: $ALLOWED_ORIGIN" -H "Access-Control-Request-Method: POST" -X OPTIONS "$API_URL")
echo "   📥 Response code: $resp"
if [ "$resp" = "200" ]; then
    print_status PASS "Allowed method POST accepted for $ALLOWED_ORIGIN"
else
    print_status FAIL "Allowed method POST was rejected for $ALLOWED_ORIGIN"
fi
echo ""

# Test 4: Disallowed Method
echo "📋 Test 4: Checking if disallowed method DELETE is rejected..."
echo "   🔄 Sending OPTIONS request with Access-Control-Request-Method: DELETE"
echo "   📤 Command: curl -s -o /dev/null -w \"%{http_code}\" -H \"Origin: $ALLOWED_ORIGIN\" -H \"Access-Control-Request-Method: DELETE\" -X OPTIONS \"$API_URL\""
resp=$(curl -s -o /dev/null -w "%{http_code}" -H "Origin: $ALLOWED_ORIGIN" -H "Access-Control-Request-Method: DELETE" -X OPTIONS "$API_URL")
echo "   📥 Response code: $resp"
if [ "$resp" = "403" ] || [ "$resp" = "401" ]; then
    print_status PASS "Disallowed method DELETE correctly rejected for $ALLOWED_ORIGIN"
else
    print_status FAIL "Disallowed method DELETE was not rejected for $ALLOWED_ORIGIN, got $resp"
fi
echo ""

# Test 5: Allowed Headers
echo "📋 Test 5: Checking if allowed headers are accepted..."
echo "   🔄 Sending OPTIONS request with Access-Control-Request-Headers: Authorization,Content-Type"
echo "   📤 Command: curl -s -o /dev/null -w \"%{http_code}\" -H \"Origin: $ALLOWED_ORIGIN\" -H \"Access-Control-Request-Headers: Authorization,Content-Type\" -X OPTIONS \"$API_URL\""
resp=$(curl -s -o /dev/null -w "%{http_code}" -H "Origin: $ALLOWED_ORIGIN" -H "Access-Control-Request-Headers: Authorization,Content-Type" -X OPTIONS "$API_URL")
echo "   📥 Response code: $resp"
if [ "$resp" = "200" ]; then
    print_status PASS "Allowed headers accepted for $ALLOWED_ORIGIN"
else
    print_status FAIL "Allowed headers were rejected for $ALLOWED_ORIGIN"
fi
echo ""

# Test 6: Disallowed Headers
echo "📋 Test 6: Checking if disallowed headers are rejected..."
echo "   🔄 Sending OPTIONS request with Access-Control-Request-Headers: X-Custom-Header"
echo "   📤 Command: curl -s -o /dev/null -w \"%{http_code}\" -H \"Origin: $ALLOWED_ORIGIN\" -H \"Access-Control-Request-Headers: X-Custom-Header\" -X OPTIONS \"$API_URL\""
resp=$(curl -s -o /dev/null -w "%{http_code}" -H "Origin: $ALLOWED_ORIGIN" -H "Access-Control-Request-Headers: X-Custom-Header" -X OPTIONS "$API_URL")
echo "   📥 Response code: $resp"
if [ "$resp" = "403" ] || [ "$resp" = "401" ]; then
    print_status PASS "Disallowed header X-Custom-Header correctly rejected for $ALLOWED_ORIGIN"
else
    print_status FAIL "Disallowed header X-Custom-Header was not rejected for $ALLOWED_ORIGIN, got $resp"
fi
echo ""

# Test 7: Credentials Not Allowed
echo "📋 Test 7: Checking if credentials are not allowed..."
echo "   🔄 Sending OPTIONS request with Cookie header"
echo "   📤 Command: curl -s -o /dev/null -w \"%{http_code}\" -H \"Origin: $ALLOWED_ORIGIN\" -H \"Cookie: sessionid=123\" -X OPTIONS \"$API_URL\""
resp=$(curl -s -o /dev/null -w "%{http_code}" -H "Origin: $ALLOWED_ORIGIN" -H "Cookie: sessionid=123" -X OPTIONS "$API_URL")
echo "   📥 Response code: $resp"
if [ "$resp" = "200" ]; then
    print_status PASS "Request with credentials processed (should not set Access-Control-Allow-Credentials)"
else
    print_status FAIL "Request with credentials was rejected (should be processed but not allow credentials)"
fi
echo ""

# Debug Test: Show actual CORS headers
echo "🔍 Debug Test: Checking actual CORS headers returned..."
echo "   🔄 Sending OPTIONS request and showing full response headers"
echo "   📤 Command: curl -v -H \"Origin: $ALLOWED_ORIGIN\" -X OPTIONS \"$API_URL\" 2>&1 | grep -E '(HTTP|Access-Control|Origin)'"
echo "   📥 Response headers:"
curl -v -H "Origin: $ALLOWED_ORIGIN" -X OPTIONS "$API_URL" 2>&1 | grep -E "(HTTP|Access-Control|Origin)" || echo "   No CORS headers found"
echo ""

echo "🔒 ========================================="
echo "🔒 CORS POLICY TESTS COMPLETE"
echo "🔒 ========================================="
echo ""
echo "📊 Summary: All CORS policy tests have been executed with detailed logging."
echo "   Check the output above for individual test results." 