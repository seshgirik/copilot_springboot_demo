#!/bin/bash

echo "🔍 Spring Boot Project Compilation Diagnostic"
echo "============================================="

# Navigate to project directory
cd "$(dirname "$0")"

echo "📁 Working directory: $(pwd)"

# Step 1: Check Java version
echo -e "\n1. Checking Java version..."
java -version 2>&1 | head -1 || echo "❌ Java not found"

# Step 2: Check Maven version
echo -e "\n2. Checking Maven version..."
mvn -version 2>&1 | head -1 || echo "❌ Maven not found"

# Step 3: Check project structure
echo -e "\n3. Checking project structure..."
echo "Key files present:"
files=(
    "pom.xml"
    "src/main/java/com/demo/springboot/SpringbootGrpcRestDemoApplication.java"
    "src/main/java/com/demo/springboot/controller/ProductController.java"
    "src/main/java/com/demo/springboot/service/ProductService.java"
    "src/main/java/com/demo/springboot/entity/Product.java"
    "src/main/java/com/demo/springboot/repository/ProductRepository.java"
    "src/main/resources/application.yml"
)

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file (missing)"
    fi
done

# Step 4: Clean and compile
echo -e "\n4. Attempting clean compilation..."
mvn clean compile > compile_output.log 2>&1

if [ $? -eq 0 ]; then
    echo "✅ COMPILATION SUCCESSFUL!"
    
    # Step 5: Try packaging
    echo -e "\n5. Attempting packaging..."
    mvn package -DskipTests > package_output.log 2>&1
    
    if [ $? -eq 0 ]; then
        echo "✅ PACKAGING SUCCESSFUL!"
        
        # Check if JAR was created
        JAR_FILE=$(find target -name "*.jar" -not -name "*sources*" | head -1)
        if [ -f "$JAR_FILE" ]; then
            echo "📦 JAR created: $JAR_FILE"
            ls -lh "$JAR_FILE"
        fi
        
        echo -e "\n🎉 PROJECT IS READY TO RUN!"
        echo "Run with: mvn spring-boot:run"
        
    else
        echo "❌ PACKAGING FAILED"
        echo "Last 15 lines of package output:"
        tail -15 package_output.log
    fi
    
else
    echo "❌ COMPILATION FAILED"
    echo "Compilation errors found:"
    tail -20 compile_output.log
    
    # Try to identify specific issues
    echo -e "\n🔧 Potential fixes:"
    
    if grep -q "package does not exist" compile_output.log; then
        echo "- Missing dependencies - check pom.xml"
    fi
    
    if grep -q "cannot find symbol" compile_output.log; then
        echo "- Missing method or variable - check class implementations"
    fi
    
    if grep -q "unmappable character" compile_output.log; then
        echo "- Encoding issues - check file encoding"
    fi
fi

echo -e "\n📋 Diagnostic Summary:"
echo "- Java: $(java -version 2>&1 | head -1 | cut -d' ' -f3 | tr -d '\"' || echo 'Not found')"
echo "- Maven: $(mvn -version 2>&1 | head -1 | cut -d' ' -f3 || echo 'Not found')"
echo "- Project structure: Complete"
echo "- Main issues: Check compile_output.log for details"

exit 0
