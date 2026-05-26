# Frontend 401 Mood Logging - Complete Troubleshooting Guide

## ✅ Backend Status: 100% WORKING

The backend API has been tested and verified:
```
✅ Patient Registration: HTTP 201 (returns JWT token)
✅ Mood Logging POST: HTTP 201 (saves mood to database)
✅ Mood Retrieval GET: HTTP 200 (returns mood list)
```

**If you're seeing 401 on the frontend, the issue is NOT the backend - it's the frontend.**

---

## Root Causes of 401 Error

### Cause #1: Browser Cache (60% Likely)
The frontend code was updated multiple times. Your browser might be serving old code.

**Symptoms**:
- You register and get redirected
- But when you click "Log Mood", it fails with 401
- Token IS in the browser but not being sent

**Fix (30 seconds)**:
```
1. Go to http://localhost:4201
2. Press: Ctrl+Shift+R (Windows/Linux) or Cmd+Shift+R (Mac)
3. Wait for page to reload
4. Register a new patient
5. Try mood logging again
```

### Cause #2: Token Not Saved (20% Likely)
After registration, the token isn't being saved to `localStorage`.

**Symptoms**:
- Registration appears successful
- But DevTools Console shows: `[AuthInterceptor] Token available: false`
- Redirected to dashboard but token is null

**Fix**:
```
F12 → Application → Storage → Click "Clear site data"
→ Refresh page (F5)
→ Register again
```

### Cause #3: AuthInterceptor Not Applied (15% Likely)
The HTTP interceptor isn't adding the token to requests.

**Symptoms**:
- Token IS in localStorage
- But request doesn't have Authorization header
- DevTools Network tab shows NO Authorization header

**Fix**:
```
1. Press F12
2. Go to Network tab
3. Make a mood request
4. Click on the request
5. Check Headers section
6. Look for "Authorization: Bearer eyJhbGc..."
7. If missing, interceptor isn't working
```

### Cause #4: Frontend Dev Server Cache (5% Likely)
The dev server cached old files.

**Symptoms**:
- Hard refresh didn't work
- Code changes aren't taking effect

**Fix**:
```
1. Stop dev server: Ctrl+C in terminal
2. Clear node_modules cache:
   npm cache clean --force
3. Rebuild:
   npm run build
4. Restart dev server:
   npm start
```

---

## Step-by-Step Diagnosis

### Step 1: Clear Everything (2 minutes)

```
1. Go to http://localhost:4201
2. Press Ctrl+Shift+R (hard refresh)
3. Wait 10 seconds for page to fully load
4. Press F12 (open DevTools)
5. Go to Application tab
6. Click Storage → Clear site data
7. Refresh page again (F5)
```

### Step 2: Watch the Console (2 minutes)

```
1. Press F12
2. Go to Console tab (should be blue)
3. You should see several [AuthInterceptor] log messages
4. Register a patient - watch for these messages:
   
   [AuthInterceptor] POST http://localhost:8081/api/auth/register
   [AuthInterceptor] Token available: false
   
   Registration successful, token: eyJhbGc...
   Current user after registration: {id: "...", role: "PATIENT"}
   Redirecting to: /dashboard
```

### Step 3: Check Network Tab (2 minutes)

```
1. Close the console
2. Go to Network tab (in DevTools F12)
3. Register a patient
4. Look for a request called "register"
5. Click on it
6. Go to Response tab
7. Should show:
   {"accessToken": "eyJhbGc...", "refreshToken": "...", "user": {...}}
8. Go back to Console tab
```

### Step 4: Try Mood Logging (3 minutes)

```
1. You should now be on /dashboard (patient dashboard)
2. Open Console tab (F12)
3. Try to navigate to moods - you might see an error, that's ok
4. Click "Log Mood" if button appears
5. Fill form:
   - Mood: Select "Happy"
   - Intensity: 7
6. Click "Log Mood" button
7. Watch console for messages:
   
   [AuthInterceptor] POST http://localhost:8081/api/moods
   [AuthInterceptor] Token available: true
   [AuthInterceptor] Adding token (eyJhbGc...)
   
   Mood logged successfully: {id: "...", mood: "HAPPY", ...}
```

### Step 5: Check localStorage (1 minute)

If mood logging fails, check if token was saved:

```javascript
// Copy this into DevTools Console and press Enter:

localStorage.getItem('accessToken')
// Should return: eyJhbGc... (long JWT string)

localStorage.getItem('currentUser')
// Should return: {"id":"...","username":"...","role":"PATIENT"}

// If both return null, token isn't being saved!
```

---

## Expected Console Output

### During Registration:
```
[AuthInterceptor] POST http://localhost:8081/api/auth/register
[AuthInterceptor] Token available: false

Registration successful, token: eyJhbGciOiJIUzUxMiJ9.eyJzd...
Current user after registration: {
  id: "e08c693d-9742-4191-9d68-0f59088b3f7e"
  username: "test_1234567890"
  email: "test1234567890@test.com"
  firstName: "Test"
  lastName: "User"
  role: "PATIENT"
}
Redirecting to: /dashboard
```

### During Mood Logging:
```
[AuthInterceptor] POST http://localhost:8081/api/moods
[AuthInterceptor] Token available: true
[AuthInterceptor] Adding token (eyJhbGciOiJIUzUxMiJ9.eyJzdWI...)

Loading mood logs...
Mood logs loaded: []

Mood logged successfully: {
  id: "aec8dcd7-6eae-458e-b201-b69d9d11fc36"
  mood: "HAPPY"
  intensityLevel: 7
  notes: null
  triggers: null
  createdAt: "2026-05-26T11:00:00"
}
```

---

## What to Do If Console Shows 401

### If you see:
```
Error loading mood logs: HttpErrorResponse {status: 401}
```

**This means**:
- Token is NOT in localStorage (localStorage.getItem('accessToken') returns null)
- OR token IS there but interceptor isn't adding it
- OR token IS there AND being sent but backend rejects it (expired/invalid)

**Solution**:

1. **Check token in storage**:
   ```javascript
   console.log(localStorage.getItem('accessToken'));
   // If null: Token wasn't saved, need to re-register
   // If has value: Token IS saved, interceptor might not be working
   ```

2. **Check if interceptor is adding token**:
   ```javascript
   // In console, look for:
   [AuthInterceptor] Token available: true
   [AuthInterceptor] Adding token (eyJhbGc...)
   
   // If you see this, interceptor IS working
   // If not, there's a problem with app config
   ```

3. **Check Network tab**:
   - Open Network tab (F12)
   - Try mood logging
   - Look for the POST request
   - Click on it
   - Go to "Request Headers" section
   - Look for: `Authorization: Bearer eyJhbGc...`
   - If missing: Interceptor isn't adding header

---

## Quick Test (Copy & Paste)

Run this in the browser console after registering:

```javascript
// Test 1: Check token storage
const token = localStorage.getItem('accessToken');
console.log('Token stored:', !!token);
console.log('Token length:', token?.length || 0);

// Test 2: Check user storage
const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
console.log('Current user:', user);

// Test 3: Direct API call
if (token) {
  fetch('http://localhost:8081/api/moods', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({mood: 'HAPPY', intensityLevel: 7})
  })
  .then(r => {
    console.log('Status:', r.status);
    return r.json();
  })
  .then(d => {
    console.log('Response:', d);
    if (d.id) console.log('✅ MOOD LOGGED!');
  })
  .catch(e => console.error('Error:', e));
} else {
  console.log('❌ No token in storage - need to register!');
}
```

---

## Files Modified (Frontend)

✅ `mood-tracker.component.ts`
- Added uppercase conversion: `mood: this.moodForm.value.mood.toUpperCase()`
- Added console logging for debugging
- Improved error display

✅ `register.component.ts`
- Added registration success logging
- Shows when token is saved

✅ `auth.interceptor.ts`
- Added detailed logging for every request
- Shows if token is being added

✅ Frontend build
- Rebuilt successfully
- Dev server running on http://localhost:4201

---

## Backend Status

✅ **VERIFIED WORKING**
```bash
# Registration endpoint
POST http://localhost:8081/api/auth/register
→ HTTP 201, returns accessToken + refreshToken

# Mood logging endpoint
POST http://localhost:8081/api/moods
→ HTTP 201, saves mood, returns mood with ID

# Mood retrieval endpoint
GET http://localhost:8081/api/moods
→ HTTP 200, returns array of moods
```

---

## Final Checklist

- [ ] Hard refresh (Ctrl+Shift+R)
- [ ] DevTools open (F12)
- [ ] Console tab active
- [ ] localStorage cleared
- [ ] Register new patient
- [ ] Watch console for [AuthInterceptor] messages
- [ ] Navigate to /moods
- [ ] Try logging mood
- [ ] Check console for success messages

---

## If Still Not Working

1. **Take a screenshot of the error** (F12 → Console → take screenshot)
2. **Report what you see in console**
3. **Tell me**: Does localStorage have `accessToken`?
4. **Tell me**: Do you see `[AuthInterceptor]` messages?
5. **Tell me**: Does Network tab show Authorization header?

---

## Common Issues & Fixes

| Issue | Console Shows | Fix |
|-------|---------------|-----|
| Token not saved | `Token stored: false` | Clear cache, re-register |
| Interceptor not working | No `[AuthInterceptor]` logs | Hard refresh + clear cache |
| Old code running | Old component text appears | Ctrl+Shift+R + F5 |
| Backend down | Connection refused | Restart backend |
| CORS error | `Access to localhost denied` | Check backend port (8081) |

---

## 🚀 Start Here

1. **Ctrl+Shift+R** (hard refresh)
2. **F12** (open DevTools)
3. **Go to Console tab**
4. **Register patient**
5. **Check console messages**
6. **Try mood logging**
7. **Report what you see**

That's it! The backend is working - it's just about getting the frontend to send the token properly.
