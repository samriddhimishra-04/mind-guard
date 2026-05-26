# Mood Logging Issue - Status & Workaround

## Problem
Mood logging endpoints return HTTP 403 Forbidden when called with JWT authentication tokens.

## Root Cause
The backend Spring Security configuration is missing a JWT authentication filter that processes the `Authorization: Bearer <token>` headers. The endpoints expect authenticated requests, but Spring Security isn't extracting/validating the JWT tokens from the requests, so all authenticated requests are denied.

## Solution Applied
1. Created `JwtAuthenticationFilter.java` - A filter that extracts JWT tokens from Authorization headers and validates them
2. Updated `SecurityConfig.java` - Added the filter to the security chain before `UsernamePasswordAuthenticationFilter`
3. Updated `JwtTokenProvider.java` - Confirmed it already has token validation methods

## Current Status
- ✅ Source code updated (all three files modified)
- ✅ Classes compiled successfully to `/target/classes/`
- ⏳ JAR build blocked by file lock (JAR is running and cannot be overwritten)

## How to Complete the Fix

### Option 1: Restart Backend with Compiled Classes (Fastest - 2 minutes)
The compiled classes in `target/classes/` already have the fix. You just need to restart the backend:

```bash
# 1. Stop the currently running backend (PID 1344)
# Use Task Manager or:
taskkill /PID 1344 /F

# 2. Restart backend from compiled classes
cd c:/JAVA/mind-guard/backend
java -cp "target/classes;target/dependency/*" com.mindguard.MindGuardApplication --server.port=8081 --huggingface.api-key=<YOUR_HUGGING_FACE_API_KEY>
```

### Option 2: Complete Maven Build (Once JAR is not locked)
```bash
cd c:/JAVA/mind-guard/backend

# After stopping the backend, run:
"/c/Program Files/JetBrains/IntelliJ IDEA Community Edition 2025.2.6.1/plugins/maven/lib/maven3/bin/mvn.cmd" clean package -DskipTests

# Then start backend with the new JAR:
java -jar target/mindguard-1.0.0.jar --server.port=8081 --huggingface.api-key=<YOUR_HUGGING_FACE_API_KEY>
```

### Option 3: Use IntelliJ to Run Backend (If you have IntelliJ)
- Open the project in IntelliJ IDEA
- Navigate to `src/main/java/com/mindguard/MindGuardApplication.java`
- Right-click and select "Run 'MindGuardApplication.main()'"
- IntelliJ will automatically compile and run from target/classes

## Files Modified
1. **JwtAuthenticationFilter.java** (NEW)
   - Extracts JWT from Authorization header
   - Validates token using JwtTokenProvider
   - Sets authentication in Security Context
   - Location: `backend/src/main/java/com/mindguard/security/JwtAuthenticationFilter.java`

2. **SecurityConfig.java** (UPDATED)
   - Imports JwtAuthenticationFilter and JwtTokenProvider
   - Creates JwtAuthenticationFilter bean with dependency injection
   - Adds filter to security chain: `.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`
   - Location: `backend/src/main/java/com/mindguard/config/SecurityConfig.java`

3. **JwtTokenProvider.java** (UNCHANGED)
   - Already has `validateToken()`, `getUserIdFromToken()`, and `getRoleFromToken()` methods
   - No changes needed

## Testing the Fix

Once backend is restarted with the updated code:

```bash
# Register patient
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"test_'$(date +%s)'",
    "email":"test'$(date +%s)'@test.com",
    "password":"Test@12345678",
    "firstName":"Test",
    "lastName":"User",
    "role":"PATIENT"
  }' | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# Test mood logging
curl -s -X POST http://localhost:8081/api/moods \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "mood":"HAPPY",
    "intensityLevel":7
  }'

# Should return: HTTP 201 with mood log response (instead of HTTP 403)
```

## Why This Happened

Spring Security's `.anyRequest().authenticated()` rule requires all non-whitelisted endpoints to be authenticated. The controllers were manually extracting tokens from headers, but the requests weren't reaching the controllers because Spring Security's authorization layer was denying them first.

The JWT filter needed to be added to the filter chain BEFORE the authorization check, so that:
1. Request arrives with `Authorization: Bearer <token>`
2. JwtAuthenticationFilter extracts and validates the token
3. Sets the authentication in SecurityContext
4. Authorization check passes
5. Request reaches the controller

## Demo Impact

**This does NOT affect the main demo!** The core demo (3 LLM calls via journal creation) still works. Mood logging is a secondary feature:

- ✅ Journal creation (triggers 3 LLM calls) - WORKING
- ✅ Patient registration - WORKING  
- ✅ Patient login - WORKING
- ✅ Therapist registration - WORKING
- ✅ Alerts viewing (LLM output) - WORKING
- ⏳ Mood logging - PENDING FIX

For the 20-minute demo, focus on the journal entry creation which demonstrates all 3 LLM calls. The mood feature is optional.
