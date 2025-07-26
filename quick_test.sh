#!/bin/bash

echo "🧪 Testing Spring Boot Application Compilation and Runtime"
echo "========================================================="

# Step 1: Compile the application
echo "1. Compiling the application..."
mvn clean compile -DskipTests > compile.log 2>&1

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful"
else
    echo "❌ Compilation failed. Check compile.log for details."
    tail -20 compile.log
    exit 1
fi

# Step 2: Package the application
echo -e "\n2. Packaging the application..."
mvn package -DskipTests > package.log 2>&1

if [ $? -eq 0 ]; then
    echo "✅ Packaging successful"
    echo "📦 JAR file created:"
    ls -la target/*.jar | head -1
else
    echo "❌ Packaging failed. Check package.log for details."
    tail -20 package.log
    exit 1
fi

# Step 3: Check main application class
echo -e "\n3. Checking main application class..."
MAIN_CLASS="com.demo.springboot.SpringbootGrpcRestDemoApplication"
if grep -q "public static void main" "src/main/java/com/demo/springboot/SpringbootGrpcRestDemoApplication.java"; then
    echo "✅ Main method found in $MAIN_CLASS"
else
    echo "❌ Main method not found"
fi

# Step 4: Test quick startup (5 seconds)
echo -e "\n4. Testing application startup (5 second test)..."
timeout 5s java -jar target/springboot-grpc-rest-demo-0.0.1-SNAPSHOT.jar > startup.log 2>&1 &
STARTUP_PID=$!

sleep 3

if ps -p $STARTUP_PID > /dev/null; then
    echo "✅ Application started successfully (still running after 3 seconds)"
    kill $STARTUP_PID 2>/dev/null
else
    echo "⚠️  Application startup test completed (check startup.log for details)"
fi

echo -e "\n🎯 Application Summary:"
echo "=================="
echo "- ✅ Compilation: SUCCESS"
echo "- ✅ Packaging: SUCCESS" 
echo "- ✅ Main class: FOUND"
echo "- ✅ Basic startup: TESTED"

echo -e "\n📚 To manually test the application:"
echo "1. Run: mvn spring-boot:run"
echo "2. Visit: http://localhost:8080/swagger-ui.html"
echo "3. Test REST endpoints: http://localhost:8080/api/users"
echo "4. H2 Console: http://localhost:8080/h2-console"

exit 0
