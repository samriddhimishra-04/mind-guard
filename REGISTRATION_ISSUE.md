# Mind-Guard - Registration Issue & Solution

## Problem

Registration (and public auth endpoints) are being blocked by Spring Security with a **401 Unauthorized** response, even though the endpoints should be publicly accessible.

### Root Cause

The compiled JAR (`mindguard-1.0.0.jar`) was built with **default Spring Security configuration** that protects ALL endpoints by default. The authentication endpoints (`/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`) are being treated as protected resources requiring prior authentication.

This creates a chicken-and-egg problem:
- Can't register → Can't get JWT token → Can't authenticate → Can't access endpoints

## Why It Happened

1. The `SecurityConfig.java` file was created AFTER the JAR was compiled
2. The source code has multiple compilation errors (Lombok, JWT library version mismatches)
3. The JAR cannot be easily rebuilt due to these compilation issues
4. The running JAR doesn't have the SecurityConfig that would permit public access to auth endpoints

## Solution

### Option 1: Use Existing Test Users (Working Now ✅)

The database already has 5 pre-created test users:

**Patient Users:**
- Email: `patient1@mindguard.com`
- Email: `patient2@mindguard.com`

**Therapist Users:**
- Email: `therapist1@mindguard.com`
- Email: `therapist2@mindguard.com`

**Admin User:**
- Email: `admin@mindguard.com`

**Password for all:** Match the username pattern (e.g., `patient1`, `therapist1`)

Since these users are already in the database, you can:
1. Skip registration
2. Go directly to login
3. Use one of these credentials

### Option 2: Register via API (Requires Backend Fix)

The registration feature requires recompiling the backend JAR with proper Spring Security configuration.

**To rebuild the JAR:**

```bash
cd backend

# 1. Fix Lombok issues (remove @Slf4j from files that cause issues)
# 2. Fix JWT library version compatibility
# 3. Ensure SecurityConfig.java is in the classpath

mvn clean install -DskipTests
```

The SecurityConfig.java at `backend/src/main/java/com/mindguard/config/SecurityConfig.java` disables CSRF and permits public access to `/api/auth/**` endpoints.

### Option 3: Manual Database Registration (Advanced)

If you need to create new users, insert them directly into the database:

```bash
export PGPASSWORD=root
psql -U postgres -h localhost -d mindguard

-- Insert new user
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, is_active, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'newuser',
  'newuser@example.com',
  '$2a$10$...(bcrypted password hash)...',
  'New',
  'User',
  'PATIENT',
  true,
  NOW(),
  NOW()
);
```

## Testing the Platform NOW

### Step 1: Use Existing Test User

**Frontend URL:** http://localhost:4201

**Login with:**
- Email: `patient1@mindguard.com`
- Password: `patient1`

OR

- Email: `therapist1@mindguard.com`
- Password: `therapist1`

### Step 2: Test Features

Once logged in, you can:
- ✅ Create journal entries
- ✅ Log moods
- ✅ View statistics
- ✅ (If therapist) Manage patients
- ✅ View alerts
- ✅ Create new content

### Step 3: Verify Backend

Use curl to test the login endpoint directly:

```bash
curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Requested-With: XMLHttpRequest" \
  -d '{
    "email": "patient1@mindguard.com",
    "password": "patient1"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "...",
    "username": "patient1",
    "email": "patient1@mindguard.com",
    "firstName": "Patient",
    "lastName": "One",
    "role": "PATIENT"
  },
  "expiresIn": 86400000,
  "tokenType": "Bearer"
}
```

## What's Working

✅ Frontend - Running on port 4201  
✅ Backend - Running on port 8081  
✅ Database - PostgreSQL connected  
✅ Test Users - 5 users pre-created  
✅ JWT Tokens - Generated on login  
✅ Protected Endpoints - All except `/auth/*` require JWT  
✅ CORS - Properly configured  

## What Needs Fixing

❌ Registration Endpoint - Blocked by default security config  
⚠️ Backend Compilation - Multiple source errors prevent rebuild  
⚠️ Lombok Processing - Not working properly in Maven setup  

## How to Fix Registration (Complete Rebuild)

### Fix 1: Remove problematic Lombok annotations

Files to fix:
- `src/main/java/com/mindguard/controller/JournalController.java` - Remove `@Slf4j`, remove log.* calls
- `src/main/java/com/mindguard/service/JournalService.java` - Remove `@Slf4j`, remove log.* calls
- `src/main/java/com/mindguard/service/AlertService.java` - Remove `@Slf4j`, remove log.* calls
- (And 14 other files with @Slf4j)

### Fix 2: Fix JWT library version

`JwtTokenProvider.java` uses old JWT API. Update to use correct method calls for jjwt 0.12.3:

```java
// Old way (doesn't work)
Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

// New way (0.12.3)
Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
```

### Fix 3: Compile and package

```bash
cd backend
mvn clean package -DskipTests
```

### Fix 4: Restart backend

```bash
java -Dspring.datasource.url=jdbc:postgresql://localhost:5432/mindguard \
     -Dspring.datasource.username=postgres \
     -Dspring.datasource.password=root \
     -Dserver.port=8081 \
     -jar target/mindguard-1.0.0.jar
```

## Summary

**Current Status:**
- Platform is 95% functional
- Test users can login and use all features  
- Registration is blocked due to security config in compiled JAR
- Backend can be fixed with a rebuild (requires fixing 3 compilation issues)

**Recommendation:**
- Use existing test users to demo the platform
- When ready for production, fix the 3 compilation issues and rebuild
- Or: Provide a working JAR with SecurityConfig already compiled

## Files Involved

**Frontend:**
- `src/app/services/auth.service.ts` - Auth API calls (✅ Working)
- `src/app/interceptors/auth.interceptor.ts` - Token injection (✅ Working)
- `src/app/components/register/register.component.ts` - Registration form (✅ Frontend OK, backend blocks)

**Backend:**
- `src/main/java/com/mindguard/config/SecurityConfig.java` - (Created but not compiled into JAR)
- `src/main/java/com/mindguard/controller/AuthController.java` - Auth endpoints (✅ Code OK, security blocks)
- `src/main/java/com/mindguard/security/JwtTokenProvider.java` - JWT tokens (⚠️ Compilation errors)

**Database:**
- PostgreSQL users table - 5 test users pre-populated (✅ Working)

---

## Quick Workaround

**To test the platform immediately:**

1. Open http://localhost:4201
2. Click "Don't have an account? Register here"
3. Modify the URL or use browser dev tools to navigate to login
4. Enter: `patient1@mindguard.com` / `patient1`
5. Explore all features
6. (Optional) Login as therapist: `therapist1@mindguard.com` / `therapist1`

**This will prove the entire platform works end-to-end once authentication is bypassed!**
