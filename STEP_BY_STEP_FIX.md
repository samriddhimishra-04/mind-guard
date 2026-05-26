# Step-by-Step Fix for 401 Error

## The Root Cause

The 401 error means the Authorization header is **NOT** being sent with requests. This happens because **the token is not in localStorage**.

The token only gets to localStorage if `AuthService.handleAuthSuccess()` is called when you register.

## How to Fix It - Follow These Exact Steps

### STEP 1: Verify Registration Works (2 minutes)

Go to http://localhost:4201

```
1. Click "Sign Up"
2. Register with:
   Username: testfinal
   Email: testfinal@test.com
   Password: Final@12345678
   First Name: Test
   Last Name: Final
   Role: PATIENT

3. Click Register button
4. Watch what happens:
   - Should show loading spinner
   - Should complete quickly
   - Should redirect to /dashboard
5. You should now see the patient dashboard

If it redirects, registration likely worked!
```

### STEP 2: Check Browser DevTools - Console (1 minute)

```
1. Open DevTools: F12
2. Click Console tab
3. You should see messages like:
   
   [AuthInterceptor] POST http://localhost:8081/api/auth/register
   [AuthService] handleAuthSuccess called
   Registration successful, token: eyJhbGc...
   Redirecting to: /dashboard

4. Take a screenshot of these messages
```

If you DON'T see `[AuthService] handleAuthSuccess called`:
- The registration response didn't trigger authentication
- This is the bug

### STEP 3: Check localStorage (1 minute)

Still in DevTools Console, type:

```javascript
localStorage.getItem('accessToken')
```

Press Enter.

**What you should see:**
- A long string starting with `eyJhbGc...` ✅
- Or `null` ❌

**Report to me:**
- What did it return?
- Was it null or a JWT token?

### STEP 4: Check Network Headers (2 minutes)

```
1. Go to Network tab (in DevTools)
2. Clear network log (circle with slash icon)
3. Refresh page or try clicking "Log Mood"
4. Look for a request to /api/moods
5. Click on it
6. Go to "Headers" section
7. Scroll to "Request Headers"
8. Look for: Authorization: Bearer...

Take a screenshot
```

**What you should see:**
- `Authorization: Bearer eyJhbGc...` ✅
- Nothing (no Authorization header) ❌

### STEP 5: If Authorization Header is Missing

If the header is missing, it means the interceptor isn't working or the token isn't being retrieved.

Try this in the console:

```javascript
// Check if interceptor is even running
// Any request from now on should log [AuthInterceptor] messages

// Make a test request
fetch('http://localhost:8081/api/moods', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
  }
})
.then(r => {
  console.log('Status:', r.status);
  return r.json();
})
.then(d => console.log('Response:', d))
.catch(e => console.error('Error:', e));
```

**If this returns moods successfully:**
- Backend works fine
- Problem is Angular's interceptor
- Solution: Restart dev server

**If this also returns 401:**
- Token in localStorage is invalid
- Solution: Need to re-register

---

## Common Issues & Solutions

### Issue #1: Token is NULL in localStorage

**Problem:** After registration, `localStorage.getItem('accessToken')` returns `null`

**Cause:** Registration response didn't have `accessToken` field, OR response didn't trigger `handleAuthSuccess()`

**Solution:**
```
1. Check browser console for error messages
2. Check Network tab - what does register response show?
3. Look for error in the Response tab of the register request
4. If error: Backend rejected registration
5. Check password requirements:
   - Min 12 characters
   - Uppercase letter
   - Lowercase letter
   - Number
   - Special character (@$!%*?&)
```

**Try registering again with:**
```
Username: test001
Email: test001@test.com
Password: Test@12345678 (has all requirements)
```

### Issue #2: Token EXISTS but NOT in requests

**Problem:** `localStorage.getItem('accessToken')` returns a long token, but Authorization header is missing from Network tab

**Cause:** AuthInterceptor isn't adding the header

**Solution:**
```
1. Check if [AuthInterceptor] messages appear in console
2. If not: Interceptor code isn't loaded
3. Hard refresh: Ctrl+Shift+R
4. Close DevTools and reopen: F12
5. If still nothing: Restart dev server

# Restart dev server:
cd c:/JAVA/mind-guard/frontend
npm cache clean --force
npm start -- --port 4201
# Wait 30 seconds
# Hard refresh: Ctrl+Shift+R
```

### Issue #3: Both Token AND Header Present, Still 401

**Problem:** Token is in localStorage, Authorization header is in Network tab, but still getting 401

**Cause:** Token is expired or invalid, OR backend issue

**Solution:**
```
1. Check token isn't expired:
   # Token expires after 24 hours
   # If you registered more than 24 hours ago, it's expired
2. Re-register to get new token
3. Check backend logs for error messages:
   tail -50 /tmp/backend.log | grep -i "401\|error"
```

---

## What to Do Right Now

### Option A: Quick Test (2 minutes)

1. Open console: F12 → Console
2. Type: `localStorage.getItem('accessToken')`
3. Tell me: Is it `null` or a JWT string?

### Option B: Complete Diagnosis (5 minutes)

Follow STEPS 1-5 above and report:
1. Do you see `[AuthService]` messages in console?
2. Is there an Authorization header in Network tab?
3. What does `localStorage.getItem('accessToken')` return?

These three answers tell us exactly what's wrong.

### Option C: Nuclear Reset (3 minutes)

```bash
# Kill dev server
ps aux | grep "ng serve\|node" | grep -v grep | awk '{print $2}' | xargs kill -9

# Clean and restart
cd c:/JAVA/mind-guard/frontend
rm -rf node_modules/.cache 2>/dev/null || true
npm cache clean --force
npm start -- --port 4201

# Wait 30 seconds for rebuild
# Then: Ctrl+Shift+R in browser
# Register and test again
```

---

## Expected Console Output When It Works

```
[AuthInterceptor] POST http://localhost:8081/api/auth/register
[AuthInterceptor] Token available: false

[AuthService] handleAuthSuccess called
[AuthService] Response: {accessToken: "eyJhbGc...", ...}
[AuthService] Token saved: true
[AuthService] Stored token: eyJhbGc...

Registration successful, token: eyJhbGc...
Redirecting to: /dashboard

[AuthInterceptor] GET http://localhost:8081/api/moods
[AuthInterceptor] Token available: true
[AuthInterceptor] Adding token (eyJhbGc...)

Mood logs loaded: []
```

If you see `[AuthService] handleAuthSuccess called` and `[AuthInterceptor] Token available: true`, it's working!

---

## Report Format

When you come back, tell me:

1. **localStorage.getItem('accessToken')** returns:
   - null
   - JWT string
   - Something else

2. **Console shows** `[AuthService] handleAuthSuccess called`:
   - Yes
   - No

3. **Network tab shows** Authorization header:
   - Yes
   - No
   - Can't find the request

4. **Browser version**: (in case there's a cache issue)

These 4 answers = exact diagnosis! 🔍
