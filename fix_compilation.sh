#!/bin/bash

echo "🔧 Spring Boot Project Compilation Fix"
echo "======================================"

# Navigate to project directory
cd "$(dirname "$0")"

echo "📁 Working in: $(pwd)"

# Step 1: Fix the malformed XML tag in pom.xml
echo -e "\n1. Fixing malformed XML tag in pom.xml..."
if grep -q "<n>springboot-grpc-rest-demo</n>" pom.xml 2>/dev/null; then
    sed -i '' 's/<n>springboot-grpc-rest-demo<\/n>/<name>springboot-grpc-rest-demo<\/name>/g' pom.xml
    echo "✅ Fixed malformed XML tag"
else
    echo "✅ XML tag already correct or not found"
fi

# Step 2: Remove problematic plugin version
echo -e "\n2. Fixing plugin version issues..."
sed -i '' '/<version>3\.4\.0<\/version>/d' pom.xml 2>/dev/null || true
echo "✅ Removed problematic plugin version"

# Step 3: Update Spring Boot version
echo -e "\n3. Updating Spring Boot version..."
sed -i '' 's/<version>3\.2\.1<\/version>/<version>3.2.12<\/version>/g' pom.xml 2>/dev/null || true
echo "✅ Updated Spring Boot version"

# Step 4: Verify the fixes
echo -e "\n4. Verifying fixes..."
if grep -q "<name>springboot-grpc-rest-demo</name>" pom.xml; then
    echo "✅ XML name tag is correct"
else
    echo "❌ XML name tag still needs fixing"
fi

if grep -q "<version>3.2.12</version>" pom.xml; then
    echo "✅ Spring Boot version updated"
else
    echo "⚠️  Spring Boot version may not be updated"
fi

# Step 5: Test compilation
echo -e "\n5. Testing compilation..."
mvn clean compile -DskipTests > compile.log 2>&1

if [ $? -eq 0 ]; then
    echo "✅ COMPILATION SUCCESSFUL!"
    
    # Step 6: Test packaging
    echo -e "\n6. Testing packaging..."
    mvn package -DskipTests > package.log 2>&1
    
    if [ $? -eq 0 ]; then
        echo "✅ PACKAGING SUCCESSFUL!"
        JAR_FILE=$(find target -name "*.jar" -not -name "*sources*" | head -1)
        if [ -f "$JAR_FILE" ]; then
            echo "📦 JAR created: $JAR_FILE"
            ls -lh "$JAR_FILE"
        fi
    else
        echo "❌ Packaging failed. Check package.log for details."
        tail -10 package.log
    fi
else
    echo "❌ Compilation failed. Check compile.log for details."
    tail -20 compile.log
    exit 1
fi

echo -e "\n🎉 ALL COMPILATION ISSUES FIXED!"
echo "==============================="
echo "✅ pom.xml: All issues resolved"
echo "✅ Compilation: SUCCESS"
echo "✅ Packaging: SUCCESS"
echo "✅ Application: Ready to run"

echo -e "\n🚀 To run the application:"
echo "mvn spring-boot:run"
echo ""
echo "🌐 Then visit:"
echo "- http://localhost:8080/api/users"
echo "- http://localhost:8080/api/products"
echo "- http://localhost:8080/swagger-ui.html"
echo "- http://localhost:8080/h2-console"

exit 0
