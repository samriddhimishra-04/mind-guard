#!/bin/bash

echo "Stopping frontend dev server..."
ps aux | grep "ng serve" | grep -v grep | awk '{print $2}' | xargs kill -9 2>/dev/null || echo "No running server found"

sleep 2

echo ""
echo "Clearing node_modules cache..."
cd c:/JAVA/mind-guard/frontend
npm cache clean --force 2>/dev/null || echo "Cache clean skipped"

echo ""
echo "Starting fresh dev server on port 4201..."
npm start -- --port 4201 &

echo ""
echo "Waiting for server to start (30 seconds)..."
sleep 30

echo ""
echo "Testing frontend..."
curl -s http://localhost:4201 | head -5

echo ""
echo "✅ Frontend dev server restarted"
echo "   URL: http://localhost:4201"
echo ""
