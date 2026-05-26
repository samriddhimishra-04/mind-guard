# What To Do Right Now - 1 Minute Quick Start

## The Issue
You see **HTTP 401 Unauthorized** when trying to log mood on the frontend.

## The Reason
The backend is 100% working. Your **browser is sending old code** that doesn't send the token.

## The Fix (90 seconds)

### Step 1: Hard Refresh (30 seconds)
```
1. Go to http://localhost:4201
2. Press: Ctrl+Shift+R (or Cmd+Shift+R on Mac)
3. Wait for page to fully load
```

### Step 2: Clear Local Storage (30 seconds)
```
1. Press F12 (opens DevTools)
2. Go to "Application" tab
3. Click "Storage" in left sidebar
4. Click "Clear site data" button
5. Refresh page (F5)
```

### Step 3: Register & Test (30 seconds)
```
1. Click "Sign Up"
2. Fill form:
   Username: testuser_001
   Email: test@example.com
   Password: Test@12345678 (must be 12+ chars with uppercase, lowercase, number, special char)
   First Name: Test
   Last Name: User
   Role: PATIENT
3. Click Register
4. Click "Log Mood"
5. Select mood: Happy
6. Set intensity: 7
7. Click "Log Mood"
```

## If It Still Doesn't Work

### Check Console Logs (1 minute)

```
1. Press F12
2. Go to Console tab
3. Look for these messages after registering:

   [AuthInterceptor] POST http://localhost:8081/api/auth/register
   Registration successful, token: eyJhbGc...
   Redirecting to: /dashboard
   
   [AuthInterceptor] POST http://localhost:8081/api/moods
   [AuthInterceptor] Token available: true
   [AuthInterceptor] Adding token (eyJhbGc...)
   
   Mood logged successfully: {id: "...", mood: "HAPPY", ...}
```

### If You Don't See These Messages

**Your localStorage isn't saving the token:**

In the console, type:
```javascript
localStorage.getItem('accessToken')
```

- If it returns `null` → Token not being saved, try:
  - Harder refresh: Ctrl+F5
  - Then clear all site data again
  - Re-register

- If it returns a long string → Token IS saved, but something else is wrong

### If You See 401 Still

Tell me:
1. **What messages are in the console?** (screenshot the console)
2. **What does `localStorage.getItem('accessToken')` return?** (null or a long string?)
3. **Do you see `[AuthInterceptor]` messages?** (yes or no?)

---

## Backend Commands (If Needed)

### Check if Backend is Running
```bash
curl http://localhost:8081/api/auth/register
# Should respond (not connection error)
```

### Restart Backend
```bash
# From c:/JAVA/mind-guard/backend folder:
java -cp "target/classes:target/dependency/*" com.mindguard.MindGuardApplication \
  --server.port=8081 \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/mind_guard \
  --spring.datasource.username=postgres \
  --spring.datasource.password=postgres \
  --huggingface.api.key=<YOUR_HUGGING_FACE_API_KEY>
```

---

## Expected Result

After hard refresh + register:

```
✅ Patient registered successfully
✅ Redirected to patient dashboard
✅ Click "Log Mood" works
✅ Select mood and click button
✅ Mood logged successfully - NO 401 ERROR
✅ Mood appears in "Recent Mood Logs"
```

---

## Demo Readiness

Once mood logging works:
1. ✅ Create a journal entry (triggers 3 LLM calls)
2. ✅ Register therapist account
3. ✅ Show auto-generated alerts

**Everything else already works!**

---

## TL;DR

1. **Ctrl+Shift+R** (hard refresh)
2. **F12 → Application → Clear site data**
3. **Register new patient**
4. **Try mood logging**
5. **Check console for logs**

That's it. 90 seconds.

If still not working, you need to check:
- What's in console?
- What does `localStorage.getItem('accessToken')` return?
- Does the Network tab show Authorization header?

Then report back with those details.

---

## Key Point

**The backend API works 100%. This is purely a frontend token-sending issue.**

The fix has been applied (mood case conversion, logging added). Just need to get fresh code in your browser.

Hard refresh almost always solves it. 🚀
