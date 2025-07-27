#!/bin/bash

# Security Tests Runner Script
# This script runs all security-related unit tests for the Spring Boot application

echo "🔒 ========================================="
echo "🔒 SECURITY UNIT TESTS RUNNER"
echo "🔒 ========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "PASS")
            echo -e "${GREEN}✅ PASS${NC} - $message"
            ;;
        "FAIL")
            echo -e "${RED}❌ FAIL${NC} - $message"
            ;;
        "INFO")
            echo -e "${BLUE}ℹ️  INFO${NC} - $message"
            ;;
        "WARN")
            echo -e "${YELLOW}⚠️  WARN${NC} - $message"
            ;;
    esac
}

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    print_status "FAIL" "Maven is not installed or not in PATH"
    exit 1
fi

print_status "INFO" "Starting security unit tests..."

# Create test results directory
mkdir -p test-results

# Run tests with detailed output
echo ""
echo "🧪 Running SSRF Protection Tests..."
mvn test -Dtest=SsrfProtectionFilterTest -q
SSRF_RESULT=$?

echo ""
echo "🧪 Running SSRF HTTP Service Tests..."
mvn test -Dtest=SsrfProtectedHttpServiceTest -q
SSRF_SERVICE_RESULT=$?

echo ""
echo "🧪 Running Security Configuration Tests..."
mvn test -Dtest=SecurityConfigTest -q
SECURITY_CONFIG_RESULT=$?

echo ""
echo "🧪 Running Authentication Provider Tests..."
mvn test -Dtest=CustomAuthenticationProviderTest -q
AUTH_PROVIDER_RESULT=$?

echo ""
echo "🧪 Running Auth Controller Tests..."
mvn test -Dtest=AuthControllerTest -q
AUTH_CONTROLLER_RESULT=$?

echo ""
echo "🧪 Running JWT Utility Tests..."
mvn test -Dtest=JwtUtilTest -q
JWT_UTIL_RESULT=$?

echo ""
echo "🧪 Running Rate Limiting Tests..."
mvn test -Dtest=RateLimitFilterTest -q
RATE_LIMIT_RESULT=$?

echo ""
echo "🧪 Running HTTP Logging Tests..."
mvn test -Dtest=HttpBodyLoggingFilterTest -q
HTTP_LOGGING_RESULT=$?

echo ""
echo "🔒 ========================================="
echo "🔒 SECURITY TEST RESULTS SUMMARY"
echo "🔒 ========================================="
echo ""

# Summary of results
TOTAL_TESTS=8
PASSED_TESTS=0

if [ $SSRF_RESULT -eq 0 ]; then
    print_status "PASS" "SSRF Protection Filter Tests"
    ((PASSED_TESTS++))
else
    print_status "FAIL" "SSRF Protection Filter Tests"
fi

if [ $SSRF_SERVICE_RESULT -eq 0 ]; then
    print_status "PASS" "SSRF Protected HTTP Service Tests"
    ((PASSED_TESTS++))
else
    print_status "FAIL" "SSRF Protected HTTP Service Tests"
fi

if [ $SECURITY_CONFIG_RESULT -eq 0 ]; then
    print_status "PASS" "Security Configuration Tests"
    ((PASSED_TESTS++))
else
    print_status "FAIL" "Security Configuration Tests"
fi

if [ $AUTH_PROVIDER_RESULT -eq 0 ]; then
    print_status "PASS" "Authentication Provider Tests"
    ((PASSED_TESTS++))
else
    print_status "FAIL" "Authentication Provider Tests"
fi

if [ $AUTH_CONTROLLER_RESULT -eq 0 ]; then
    print_status "PASS" "Auth Controller Tests"
    ((PASSED_TESTS++))
else
    print_status "FAIL" "Auth Controller Tests"
fi

if [ $JWT_UTIL_RESULT -eq 0 ]; then
    print_status "PASS" "JWT Utility Tests"
    ((PASSED_TESTS++))
else
    print_status "FAIL" "JWT Utility Tests"
fi

if [ $RATE_LIMIT_RESULT -eq 0 ]; then
    print_status "PASS" "Rate Limiting Tests"
    ((PASSED_TESTS++))
else
    print_status "FAIL" "Rate Limiting Tests"
fi

if [ $HTTP_LOGGING_RESULT -eq 0 ]; then
    print_status "PASS" "HTTP Logging Tests"
    ((PASSED_TESTS++))
else
    print_status "FAIL" "HTTP Logging Tests"
fi

echo ""
echo "📊 Test Summary:"
echo "   Total Tests: $TOTAL_TESTS"
echo "   Passed: $PASSED_TESTS"
echo "   Failed: $((TOTAL_TESTS - PASSED_TESTS))"
echo "   Success Rate: $((PASSED_TESTS * 100 / TOTAL_TESTS))%"

echo ""
echo "🔒 ========================================="
echo "🔒 SECURITY FEATURES COVERED"
echo "🔒 ========================================="
echo ""

print_status "INFO" "✅ SSRF Protection (URL validation, pattern detection)"
print_status "INFO" "✅ CSRF Protection (Disabled for JWT REST API)"
print_status "INFO" "✅ JWT Security (Secret validation, token generation)"
print_status "INFO" "✅ Authentication (Account lockout, password strength)"
print_status "INFO" "✅ Rate Limiting (Request throttling)"
print_status "INFO" "✅ Security Headers (CSP, HSTS, etc.)"
print_status "INFO" "✅ CORS Configuration (Origin restrictions)"
print_status "INFO" "✅ Sensitive Data Protection (Logging filters)"

echo ""
echo "🔒 ========================================="
echo "🔒 SECURITY TEST COMPLETION"
echo "🔒 ========================================="

if [ $PASSED_TESTS -eq $TOTAL_TESTS ]; then
    print_status "PASS" "All security tests passed! 🎉"
    echo ""
    echo "🚀 Your application is ready for production deployment with comprehensive security measures."
    exit 0
else
    print_status "FAIL" "Some security tests failed. Please review and fix the issues."
    echo ""
    echo "⚠️  Please address the failing tests before deploying to production."
    exit 1
fi 