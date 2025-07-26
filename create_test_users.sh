#!/bin/bash

echo "🧑‍💼 User Creation Test Script"
echo "=============================="
echo ""

# Check if application is running
if ! curl -s http://localhost:8085/api/users > /dev/null 2>&1; then
    echo "❌ Application not running on port 8085. Please start it first:"
    echo "   mvn spring-boot:run"
    exit 1
fi

echo "✅ Application is running!"
echo ""

echo "📝 Creating test users..."
echo ""

# Create User 1
echo "👤 Creating User 1: Alice Johnson"
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice.johnson@example.com",
    "phone": "555-0123"
  }' | jq .

echo ""
echo ""

# Create User 2
echo "👤 Creating User 2: Bob Smith"
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bob Smith",
    "email": "bob.smith@example.com",
    "phone": "555-0456"
  }' | jq .

echo ""
echo ""

# Create User 3
echo "👤 Creating User 3: Carol Williams"
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Carol Williams",
    "email": "carol.williams@example.com",
    "phone": "555-0789"
  }' | jq .

echo ""
echo ""

# Create User 4
echo "👤 Creating User 4: David Brown"
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "David Brown",
    "email": "david.brown@example.com",
    "phone": "555-1234"
  }' | jq .

echo ""
echo ""

# Create User 5
echo "👤 Creating User 5: Emma Davis"
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Emma Davis",
    "email": "emma.davis@example.com",
    "phone": "555-5678"
  }' | jq .

echo ""
echo ""

echo "📊 Getting all users:"
curl -s http://localhost:8085/api/users | jq .

echo ""
echo ""

echo "🔢 Total user count:"
curl -s http://localhost:8085/api/users/count

echo ""
echo ""
echo "✅ User creation test completed!"
echo ""
echo "💡 You can now test other operations:"
echo "   • GET user by ID: curl http://localhost:8085/api/users/1"
echo "   • Search by name: curl 'http://localhost:8085/api/users/search/name?name=alice'"
echo "   • Update user: curl -X PUT http://localhost:8085/api/users/1 -H 'Content-Type: application/json' -d '{...}'"
echo "   • Delete user: curl -X DELETE http://localhost:8085/api/users/1"
