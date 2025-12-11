#!/bin/bash

# Quick test script for Throwlytics Backend
# This script helps you quickly test the API endpoints

BASE_URL="http://localhost:8080"
PYTHON_URL="http://localhost:8000"

echo "🚀 Throwlytics Backend Quick Test"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if services are running
echo "📋 Checking services..."
echo ""

# Check Python service
echo -n "Python service (port 8000): "
if curl -s "$PYTHON_URL/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Running${NC}"
else
    echo -e "${RED}✗ Not running${NC}"
    echo "   Start it with: cd /Users/agamjotsingh/dev/ThrowlyticsYoloModel && uvicorn video_processing_service:app --reload --port 8000"
    exit 1
fi

# Check Spring Boot backend
echo -n "Spring Boot backend (port 8080): "
if curl -s "$BASE_URL/api/video/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Running${NC}"
else
    echo -e "${RED}✗ Not running${NC}"
    echo "   Start it with: cd /Users/agamjotsingh/Downloads/ThrowlyticsBackend && mvn spring-boot:run"
    exit 1
fi

echo ""
echo "✅ All services are running!"
echo ""

# Test signup
echo "📝 Step 1: Creating test user..."
SIGNUP_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test'$(date +%s)'@example.com",
    "password": "password123"
  }')

if echo "$SIGNUP_RESPONSE" | grep -q "userId"; then
    echo -e "${GREEN}✓ User created${NC}"
    EMAIL=$(echo "$SIGNUP_RESPONSE" | grep -o '"email":"[^"]*"' | cut -d'"' -f4)
else
    echo -e "${YELLOW}⚠ User might already exist, trying login...${NC}"
    EMAIL="test@example.com"
fi

echo ""

# Test login
echo "🔐 Step 2: Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$EMAIL\",
    \"password\": \"password123\"
  }")

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Login failed${NC}"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Login successful${NC}"
echo "Token: ${TOKEN:0:50}..."
echo ""

# Test video upload (if video file provided)
if [ -n "$1" ] && [ -f "$1" ]; then
    echo "📤 Step 3: Uploading video..."
    echo "File: $1"
    
    UPLOAD_RESPONSE=$(curl -s -X POST "$BASE_URL/api/video/upload" \
      -H "Authorization: Bearer $TOKEN" \
      -F "file=@$1")
    
    if echo "$UPLOAD_RESPONSE" | grep -q "throwId"; then
        echo -e "${GREEN}✓ Video uploaded successfully${NC}"
        echo "Response: $UPLOAD_RESPONSE" | jq '.' 2>/dev/null || echo "$UPLOAD_RESPONSE"
    else
        echo -e "${RED}✗ Upload failed${NC}"
        echo "Response: $UPLOAD_RESPONSE"
    fi
    echo ""
else
    echo "⏭ Step 3: Skipping video upload (no file provided)"
    echo "   To test upload: ./quick-test.sh /path/to/video.mp4"
    echo ""
fi

# Test get history
echo "📜 Step 4: Getting throw history..."
HISTORY_RESPONSE=$(curl -s -X GET "$BASE_URL/api/video/history" \
  -H "Authorization: Bearer $TOKEN")

if echo "$HISTORY_RESPONSE" | grep -q "throwId"; then
    echo -e "${GREEN}✓ History retrieved${NC}"
    echo "$HISTORY_RESPONSE" | jq '.' 2>/dev/null || echo "$HISTORY_RESPONSE"
else
    echo -e "${YELLOW}⚠ No history found (this is normal for new users)${NC}"
fi

echo ""
echo "✅ Testing complete!"
echo ""
echo "💡 Tips:"
echo "   - Use Postman for more detailed testing (see TESTING_GUIDE.md)"
echo "   - Check logs for detailed error messages"
echo "   - Verify files in uploads/ directory"

