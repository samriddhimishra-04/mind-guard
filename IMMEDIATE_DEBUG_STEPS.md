# Immediate Debug - Follow These Exact Steps

## Step 1: Close Everything & Start Fresh (1 minute)

```
1. Close all browser tabs with localhost:4201
2. Close DevTools if open (F12)
3. Go to http://localhost:4201 (fresh tab)
4. You should see login page
```

## Step 2: Open Console BEFORE Registering (1 minute)

```
1. Press F12 (open DevTools)
2. Click "Console" tab
3. Clear console (⊙ button)
4. You should see a blank console
```

## Step 3: Register & Watch Carefully (2 minutes)

```
1. Click "Sign Up" button
2. Fill form with:
   Username: testdebug001
   Email: testdebug001@test.com
   Password: Debug@12345678
   First Name: Debug
   Last Name: Test
   Role: PATIENT

3. Click "Register" button
4. WATCH CONSOLE - You should see messages appear:
   
   [AuthInterceptor] POST http://localhost:8081/api/auth/register
   [AuthInterceptor] Token available: false
   
   (and possibly more)

5. When page redirects, STOP and DO NOT PROCEED
```

## Step 4: Check if Token Saved (1 minute)

After registration completes, in the console type:

```javascript
localStorage.getItem('accessToken')
```

Press Enter.

**Tell me exactly what you see:**
- Option A: `null` (nothing returned)
- Option B: A long string starting with `eyJhbGc...`
- Option C: Something else (paste it)

**Also type:**
```javascript
localStorage.getItem('currentUser')
```

Tell me what you see.

## Step 5: Check Network Tab (1 minute)

```
1. Go to Network tab (next to Console in DevTools)
2. Clear network log (circle with slash icon)
3. Go back to Console tab
4. Register AGAIN with new email:
   Username: testdebug002
   Email: testdebug002@test.com
   Password: Debug@12345678

5. Go to Network tab
6. Look for "register" request
7. Click on it
8. Go to "Response" tab
9. Take a screenshot of what you see
```

The Response should look like:
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "user": {
    "id": "...",
    "username": "...",
    "email": "...",
    "role": "PATIENT"
  }
}
```

## Step 6: Check What Happens on Dashboard (1 minute)

After registration redirects you:

```
1. You should be on /dashboard (patient dashboard)
2. In console, type:
   localStorage.getItem('accessToken')
   
3. Is it null or does it have a value?
4. Type:
   localStorage.getItem('currentUser')
   
5. Is it null or does it show user info?
```

## Step 7: Try Mood Logging (2 minutes)

```
1. Clear console (⊙ icon)
2. Navigate to /moods (click mood button or type in URL bar)
3. Console should show:
   [AuthInterceptor] GET http://localhost:8081/api/moods
   [AuthInterceptor] Token available: ???
   
4. Tell me: Does it say "true" or "false"?
5. Do you see: "Adding token (eyJhbGc...)" ?
6. Do you see: "Mood logs loaded: [...]" or an error?
```

## What To Report Back

After following these steps, tell me:

1. **After registration**, does `localStorage.getItem('accessToken')` return:
   - [ ] `null` (nothing)
   - [ ] A long string (good!)
   - [ ] Something else

2. **In Network tab**, when you register, does the response include:
   - [ ] `"accessToken": "eyJhbGc..."`?
   - [ ] Or is the response empty/error?

3. **On /moods route**, does console show:
   - [ ] `Token available: true` (good!)
   - [ ] `Token available: false` (bad)
   - [ ] No interceptor messages at all

4. **Paste here** the exact error message from the console

---

## Most Likely Issues

### If token IS null in localStorage
→ Registration is completing but NOT saving the token
→ The `handleAuthSuccess()` method isn't being called
→ Need to check why response isn't storing token

### If token EXISTS but says "Token available: false"
→ Token is saved but `getAccessToken()` returns null
→ This is a bug in the auth service
→ Need to check how service retrieves token

### If "Token available: true" but still 401
→ Token exists and interceptor sees it
→ But either interceptor isn't adding it OR token is invalid
→ Need to check Network tab to see if Authorization header is present

---

## Do This Right Now

1. **Close all tabs**
2. **Open fresh http://localhost:4201**
3. **Press F12**
4. **Register**
5. **Type in console**: `localStorage.getItem('accessToken')`
6. **Tell me if it's null or has a value**

That one test will tell us exactly what's wrong.

---

## Why This Matters

- If token is `null` → Problem is in registration response storage
- If token exists but "Token available: false" → Problem is in auth service getAccessToken()
- If token exists and "Token available: true" → Problem is in interceptor or backend token validation

Each points to a different fix.

**Go ahead and report back with these findings.** 🔍
