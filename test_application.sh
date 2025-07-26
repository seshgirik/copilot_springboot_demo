#!/bin/bash

echo "🚀 Spring Boot Application Validation Test"
echo "=========================================="

# Check if Java is available
echo "1. Checking Java version..."
java -version 2>&1 | head -1

# Check if Maven is available  
echo -e "\n2. Checking Maven version..."
mvn -version 2>&1 | head -1

# Build the project
echo -e "\n3. Building the project..."
mvn clean package -DskipTests -q

# Check if JAR was created
echo -e "\n4. Checking if JAR was built..."
if [ -f "target/springboot-grpc-rest-demo-0.0.1-SNAPSHOT.jar" ]; then
    echo "✅ JAR file created successfully"
    ls -lh target/*.jar
else
    echo "❌ JAR file not found"
fi

# Test application startup (background)
echo -e "\n5. Testing application startup..."
mvn spring-boot:run > app.log 2>&1 &
APP_PID=$!

# Wait for startup
echo "   Waiting for application to start..."
sleep 25

# Test REST endpoints
echo -e "\n6. Testing REST API endpoints..."

# Test user count endpoint
USER_COUNT=$(curl -s http://localhost:8080/api/users/count 2>/dev/null)
if [ $? -eq 0 ] && [ "$USER_COUNT" -ge 0 ] 2>/dev/null; then
    echo "✅ User API: $USER_COUNT users found"
else
    echo "❌ User API: Failed to connect"
fi

# Test product count endpoint  
PRODUCT_COUNT=$(curl -s http://localhost:8080/api/products/count 2>/dev/null)
if [ $? -eq 0 ] && [ "$PRODUCT_COUNT" -ge 0 ] 2>/dev/null; then
    echo "✅ Product API: $PRODUCT_COUNT products found"
else
    echo "❌ Product API: Failed to connect"
fi

# Test Swagger UI
SWAGGER_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/swagger-ui.html 2>/dev/null)
if [ "$SWAGGER_STATUS" = "200" ]; then
    echo "✅ Swagger UI: Available"
else
    echo "❌ Swagger UI: Not accessible"
fi

# Test H2 Console
H2_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/h2-console 2>/dev/null)
if [ "$H2_STATUS" = "200" ]; then
    echo "✅ H2 Console: Available"
else
    echo "❌ H2 Console: Not accessible"
fi

# Cleanup
echo -e "\n7. Stopping application..."
kill $APP_PID 2>/dev/null
wait $APP_PID 2>/dev/null

echo -e "\n🎯 Test Results Summary:"
echo "========================"
if [ -f "app.log" ]; then
    if grep -q "Started SpringbootGrpcRestDemoApplication" app.log; then
        echo "✅ Application started successfully"
    else
        echo "❌ Application failed to start"
        echo "Error details:"
        tail -10 app.log
    fi
else
    echo "❌ No application log found"
fi

echo -e "\n📝 Access URLs when running:"
echo "   REST API: http://localhost:8080"
echo "   Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   H2 Console: http://localhost:8080/h2-console"
echo "   (Username: sa, Password: password)"
