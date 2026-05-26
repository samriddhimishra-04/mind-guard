# Debugging the 401 Mood Logging Issue

## What's Happening

You're getting a **401 Unauthorized** response when trying to log mood. This means:
- ✅ Your request IS reaching the backend
- ❌ The JWT token is NOT being sent OR is invalid
- ❌ Spring Security is rejecting the request

## Backend Status: WORKING ✅

The backend API works perfectly:
```bash
# Registration works
curl -X POST http://localhost:8081/api/auth/register ...
→ HTTP 201, returns accessToken

# Mood logging works WITH token
curl -X POST http://localhost:8081/api/moods \
  -H "Authorization: Bearer <TOKEN>" ...
→ HTTP 201, mood saved

# GET moods works WITH token
curl -X GET http://localhost:8081/api/moods \
  -H "Authorization: Bearer <TOKEN>" ...
→ HTTP 200, returns mood list
```

## Frontend Status: NEEDS DEBUGGING

The issue is on the frontend. One of these is happening:

### Issue #1: Token Not Stored After Registration
**Cause**: `localStorage` not saving the token after registration  
**Symptoms**: 
- You register and get redirected
- But `localStorage` doesn't have `accessToken`
- AuthInterceptor has no token to send

### Issue #2: AuthInterceptor Not Working
**Cause**: Interceptor not adding token to HTTP requests  
**Symptoms**:
- Token IS in localStorage
- But AuthInterceptor doesn't add it to requests
- Backend gets request without Authorization header

### Issue #3: Browser Cache
**Cause**: Old frontend code running  
**Symptoms**:
- You made changes but they're not taking effect
- Browser is serving cached version

## How to Diagnose

### Step 1: Hard Refresh (Fastest - 30 seconds)
1. Go to http://localhost:4201
2. Press **Ctrl+Shift+R** (or Cmd+Shift+R on Mac)
3. Close DevTools and reopen (F12)
4. Register again
5. Check console for new log messages

### Step 2: Open Browser Console (F12)

1. Press **F12** (Developer Tools)
2. Go to **Console** tab
3. Register a patient
4. **Look for these log messages**:

```javascript
[AuthInterceptor] POST http://localhost:8081/api/auth/register
[AuthInterceptor] Token available: false

Registration successful, token: eyJhbGc...
Current user after registration: {id: "...", username: "...", role: "PATIENT"}
Redirecting to: /dashboard

[AuthInterceptor] GET http://localhost:8081/api/moods
[AuthInterceptor] Token available: true
[AuthInterceptor] Adding token (eyJhbGc...)

Mood logs loaded: [...]
```

**If you see these messages, mood logging is WORKING!**

### Step 3: Check localStorage

In browser console, type:
```javascript
localStorage.getItem('accessToken')
// Should return: eyJhbGc... (long JWT token)

localStorage.getItem('currentUser')
// Should return: {"id":"...","username":"...","role":"PATIENT"}
```

**If empty, token isn't being saved after registration.**

### Step 4: Check Network Tab

1. Press F12
2. Go to **Network** tab
3. Register a patient
4. Click on the **register request** → check Response tab
5. Should show: `{"accessToken":"eyJhb...", ...}`
6. Close DevTools and reopen (sometimes fixes caching)
7. Try mood logging again

## What to Check

### ❌ Problem #1: Token Not in localStorage
**Fix**: Clear browser data and re-register
```
F12 → Application → Storage → Clear site data
→ Refresh page
→ Register again
```

### ❌ Problem #2: AuthInterceptor Logs Missing
**Fix**: Check that the interceptor file has the new logging code
```bash
grep -n "AuthInterceptor" c:/JAVA/mind-guard/frontend/src/app/interceptors/auth.interceptor.ts
# Should show console.log statements
```

### ❌ Problem #3: Old Frontend Code Running
**Fix**: Hard refresh + clear browser cache
```
Ctrl+Shift+R
OR
F12 → Application → Clear site data → Refresh
```

## Expected Behavior After Fix

### Registration Flow
1. Fill registration form
2. Click "Register"
3. **Console shows**:
   ```
   [AuthInterceptor] POST http://localhost:8081/api/auth/register
   Registration successful, token: eyJhbGc...
   Current user after registration: {role: "PATIENT"}
   Redirecting to: /dashboard
   ```
4. Get redirected to dashboard
5. Token saved to localStorage

### Mood Logging Flow
1. Click "Log Mood" button
2. Fill form (Mood: HAPPY, Intensity: 7)
3. Click "Log Mood" button
4. **Console shows**:
   ```
   [AuthInterceptor] POST http://localhost:8081/api/moods
   [AuthInterceptor] Token available: true
   [AuthInterceptor] Adding token (eyJhbGc...)
   Mood logged successfully: {id: "...", mood: "HAPPY", ...}
   ```
5. Form resets
6. Mood appears in "Recent Mood Logs"

## Quick Test (Copy & Paste into Console)

After registering, go to F12 → Console and paste:

```javascript
// Check if token exists
const token = localStorage.getItem('accessToken');
console.log('Token in localStorage:', !!token);

// Check if user exists
const user = JSON.parse(localStorage.getItem('currentUser'));
console.log('Current user:', user);

// Try a direct mood request
fetch('http://localhost:8081/api/moods', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify({mood: 'HAPPY', intensityLevel: 7})
}).then(r => r.json()).then(d => console.log('Response:', d))
```

If this works, the API is fine and it's a frontend integration issue.

## Files Recently Modified

- ✅ `mood-tracker.component.ts` - Added logging + uppercase conversion
- ✅ `register.component.ts` - Added logging  
- ✅ `auth.interceptor.ts` - Added detailed logging
- ✅ Frontend build - Completed successfully

## Next Steps

1. **Hard refresh**: Ctrl+Shift+R
2. **Open console**: F12
3. **Register**: Fill form and submit
4. **Check logs**: Look for messages in console
5. **Report findings**: Tell me what you see in console

---

## If Still Stuck

Run these bash commands and share the output:

```bash
# Check if backend is running
curl http://localhost:8081/api/auth/register

# Check if frontend is running
curl http://localhost:4201 | head -5

# Check network connectivity
ping -c 3 localhost

# Check port usage
netstat -ano | grep 8081
netstat -ano | grep 4201
```

---

**Everything should work now. The 401 error usually means either:**
1. Token not saved → Clear cache and re-register
2. Interceptor not working → Hard refresh
3. Browser cache → Ctrl+Shift+R

Try the hard refresh first! 🚀
