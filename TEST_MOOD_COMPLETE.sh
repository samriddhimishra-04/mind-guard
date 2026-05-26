#!/bin/bash

# Complete mood logging test script
# Run this to diagnose what's going wrong

echo "=================================="
echo "MOOD LOGGING DIAGNOSTIC TEST"
echo "=================================="
echo ""

# Check backend
echo "[1/5] Checking backend..."
BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/auth/register)
if [ "$BACKEND_STATUS" != "500" ] && [ "$BACKEND_STATUS" != "400" ]; then
  echo "❌ Backend not responding on port 8081"
  exit 1
fi
echo "✅ Backend is responding"
echo ""

# Check frontend
echo "[2/5] Checking frontend..."
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:4201)
if [ "$FRONTEND_STATUS" != "200" ]; then
  echo "❌ Frontend not responding on port 4201"
  exit 1
fi
echo "✅ Frontend is responding"
echo ""

# Register patient
echo "[3/5] Registering test patient..."
REGISTER=$(curl -s -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\":\"test_$(date +%s)\",
    \"email\":\"test$(date +%s)@test.com\",
    \"password\":\"Test@12345678\",
    \"firstName\":\"Test\",
    \"lastName\":\"User\",
    \"role\":\"PATIENT\"
  }")

TOKEN=$(echo $REGISTER | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Registration failed"
  echo "Response: $REGISTER"
  exit 1
fi

echo "✅ Patient registered successfully"
echo "Token: ${TOKEN:0:50}..."
echo ""

# Test mood logging
echo "[4/5] Testing mood logging (POST)..."
MOOD_POST=$(curl -s -X POST http://localhost:8081/api/moods \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"mood":"HAPPY","intensityLevel":7}')

MOOD_ID=$(echo $MOOD_POST | grep -o '"id":"[^"]*' | cut -d'"' -f4)

if [ -z "$MOOD_ID" ]; then
  echo "❌ Mood logging failed"
  echo "Response: $MOOD_POST"
  exit 1
fi

echo "✅ Mood logged successfully"
echo "Mood ID: $MOOD_ID"
echo ""

# Get mood logs
echo "[5/5] Testing mood retrieval (GET)..."
MOOD_GET=$(curl -s -X GET http://localhost:8081/api/moods \
  -H "Authorization: Bearer $TOKEN")

if echo "$MOOD_GET" | grep -q "$MOOD_ID"; then
  echo "✅ Moods retrieved successfully"
  echo "Mood count: $(echo $MOOD_GET | grep -o '"id"' | wc -l)"
else
  echo "❌ Mood retrieval failed"
  echo "Response: $MOOD_GET"
  exit 1
fi

echo ""
echo "=================================="
echo "✅ ALL TESTS PASSED!"
echo "=================================="
echo ""
echo "Backend is working perfectly."
echo "If frontend still shows 401:"
echo "1. Hard refresh: Ctrl+Shift+R"
echo "2. Open DevTools: F12"
echo "3. Check Console for auth logs"
echo "4. Register and try mood logging"
echo ""
