#!/bin/bash

echo "🔧 Spring Boot Compilation Diagnostic"
echo "===================================="

cd "$(dirname "$0")"

echo "📁 Working directory: $(pwd)"

# Check Java version
echo -e "\n1. Java Version:"
java -version 2>&1 | head -3

# Check Maven version  
echo -e "\n2. Maven Version:"
mvn -version 2>&1 | head -1

# Check if pom.xml exists and is valid
echo -e "\n3. POM.xml Validation:"
if [ -f "pom.xml" ]; then
    echo "✅ pom.xml exists"
    # Try to validate XML
    if command -v xmllint >/dev/null 2>&1; then
        if xmllint --noout pom.xml 2>/dev/null; then
            echo "✅ pom.xml is valid XML"
        else
            echo "❌ pom.xml has XML syntax errors"
            xmllint --noout pom.xml
        fi
    fi
else
    echo "❌ pom.xml not found"
fi

# Check critical source files
echo -e "\n4. Source Files Check:"
critical_files=(
    "src/main/java/com/demo/springboot/SpringbootGrpcRestDemoApplication.java"
    "src/main/java/com/demo/springboot/controller/ProductController.java"
    "src/main/java/com/demo/springboot/service/ProductService.java"
    "src/main/java/com/demo/springboot/entity/Product.java"
    "src/main/java/com/demo/springboot/repository/ProductRepository.java"
)

for file in "${critical_files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file (missing)"
    fi
done

# Clean build attempt
echo -e "\n5. Clean Build Test:"
mvn clean > clean.log 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Clean successful"
else
    echo "❌ Clean failed"
    tail -5 clean.log
fi

# Compilation attempt
echo -e "\n6. Compilation Test:"
mvn compile > compile.log 2>&1
compile_result=$?

if [ $compile_result -eq 0 ]; then
    echo "✅ COMPILATION SUCCESSFUL!"
    echo "📦 JAR packaging test..."
    mvn package -DskipTests > package.log 2>&1
    if [ $? -eq 0 ]; then
        echo "✅ PACKAGING SUCCESSFUL!"
        echo "🎉 Project is ready to run!"
    else
        echo "❌ Packaging failed"
        echo "Last 10 lines of package.log:"
        tail -10 package.log
    fi
else
    echo "❌ COMPILATION FAILED"
    echo -e "\n🔍 Compilation Errors:"
    echo "===================="
    
    # Show compilation errors
    grep -A5 -B5 "ERROR" compile.log || tail -20 compile.log
    
    echo -e "\n🛠️ Common Issues and Fixes:"
    echo "- Check if all dependencies are downloaded"
    echo "- Verify Java version (needs Java 17+)"
    echo "- Check for syntax errors in source files"
    echo "- Ensure proper package declarations"
    
    # Check for specific error patterns
    if grep -q "package does not exist" compile.log; then
        echo "❌ Missing package dependencies detected"
    fi
    
    if grep -q "cannot find symbol" compile.log; then
        echo "❌ Missing class or method references detected"
    fi
    
    if grep -q "unmappable character" compile.log; then
        echo "❌ File encoding issues detected"
    fi
fi

echo -e "\n📋 Summary:"
echo "- Java: $(java -version 2>&1 | head -1 | cut -d' ' -f3 | tr -d '\"')"
echo "- Maven: $(mvn -version 2>&1 | head -1 | awk '{print $3}')"
echo "- Compilation: $([ $compile_result -eq 0 ] && echo 'SUCCESS' || echo 'FAILED')"

exit $compile_result
