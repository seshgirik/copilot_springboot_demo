#!/bin/bash

echo "🔧 Fixing pom.xml malformed XML tag"
echo "=================================="

# Fix the malformed XML tag
if [ -f "pom.xml" ]; then
    echo "📝 Fixing <n> tag to <name> tag in pom.xml..."
    
    # Create a backup
    cp pom.xml pom.xml.backup
    
    # Fix the malformed tag
    sed -i '' 's/<n>springboot-grpc-rest-demo<\/n>/<name>springboot-grpc-rest-demo<\/name>/g' pom.xml
    
    # Verify the fix
    if grep -q "<name>springboot-grpc-rest-demo</name>" pom.xml; then
        echo "✅ Successfully fixed the malformed XML tag"
    else
        echo "❌ Failed to fix the XML tag, restoring backup"
        mv pom.xml.backup pom.xml
        exit 1
    fi
    
    # Test compilation
    echo -e "\n🧪 Testing compilation..."
    mvn clean compile > test_compile.log 2>&1
    
    if [ $? -eq 0 ]; then
        echo "✅ COMPILATION SUCCESSFUL!"
        rm pom.xml.backup test_compile.log
        
        # Test packaging
        echo "📦 Testing packaging..."
        mvn package -DskipTests > test_package.log 2>&1
        if [ $? -eq 0 ]; then
            echo "✅ PACKAGING SUCCESSFUL!"
            rm test_package.log
        else
            echo "⚠️  Packaging had issues, but compilation works"
        fi
    else
        echo "❌ Compilation still failing. Check test_compile.log for details:"
        tail -10 test_compile.log
        exit 1
    fi
else
    echo "❌ pom.xml not found"
    exit 1
fi

echo -e "\n🎉 pom.xml has been fixed and project compiles successfully!"
