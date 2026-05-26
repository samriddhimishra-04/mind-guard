# Mind-Guard API Demo Guide
## Demonstrate 3 LLM Calls Via Backend API Only

Since the frontend has build issues, here's how to demonstrate the **3 LLM calls** directly via the API:

---

## 🎯 Demo Setup (5 minutes)

### Prerequisites Running
- ✅ Backend on http://localhost:8081
- ✅ PostgreSQL database connected
- ✅ Hugging Face API active

### Backend Status Check
```bash
curl -s http://localhost:8081/actuator/health
# Response: {"status":"UP"}
```

---

## 📱 Demo Flow (20 minutes)

### Step 1: Register Patient User (2 minutes)

**Command:**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "patient_demo_'$(date +%s)'",
    "email": "patient'$(date +%s)'@demo.com",
    "password": "Patient@12345678",
    "firstName": "John",
    "lastName": "Doe",
    "role": "PATIENT"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": "uuid-here",
    "username": "patient_demo_...",
    "email": "patient...@demo.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "PATIENT"
  },
  "expiresIn": 86400,
  "tokenType": "Bearer"
}
```

**Save the accessToken** - you'll need it for next steps!

---

### Step 2: Create Journal Entry - **TRIGGERS 3 LLM CALLS** (3 minutes)

**Command:**
```bash
TOKEN="<accessToken from Step 1>"

curl -X POST http://localhost:8081/api/journals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Struggling with work stress and anxiety",
    "content": "I am feeling very anxious and overwhelmed today. Work deadlines are piling up and I feel hopeless about the situation. I cannot sleep well at night. Everything feels so stressful and I worry constantly. I need help.",
    "mood": "ANXIOUS"
  }'
```

**What's Happening (Behind the Scenes):**
```
T+0ms:   Request received
T+100ms: LLM CALL #1 starts (Sentiment Analysis)
T+100ms: LLM CALL #2 starts (Distress Detection)
T+600ms: Both calls complete
         ├─ Sentiment Score: 0.92
         └─ Distress Level: 8.2/10
T+650ms: Decision: 8.2 > 5.0? YES → Trigger LLM CALL #3
T+700ms: LLM CALL #3 starts (AI Analysis)
T+1800ms: Call #3 completes with AI-generated text
T+1850ms: Alert created in database
T+1900ms: Response sent to client
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Journal entry created successfully",
  "journalEntry": {
    "id": "entry-uuid",
    "title": "Struggling with work stress...",
    "content": "I am feeling very anxious...",
    "mood": "ANXIOUS",
    "sentimentScore": 0.92,
    "distressLevel": 8.2,
    "createdAt": "2026-05-26T..."
  }
}
```

**Explain To Audience:**
> "Notice the response includes:
> - **sentimentScore: 0.92** ← From LLM Call #1 (Sentiment Analysis)
> - **distressLevel: 8.2** ← From LLM Call #2 (Distress Detection)
> 
> Since distress (8.2) > 5.0, the backend automatically triggered LLM Call #3 to generate insights, which created an alert."

---

### Step 3: View Generated Alert (2 minutes)

**Command (to register therapist):**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "therapist_demo_'$(date +%s)'",
    "email": "therapist'$(date +%s)'@demo.com",
    "password": "Therapist@12345678",
    "firstName": "Dr.",
    "lastName": "Smith",
    "role": "THERAPIST"
  }'
```

**Save the therapist accessToken!**

---

### Step 4: Fetch Alerts (3 minutes)

**Command:**
```bash
THERAPIST_TOKEN="<therapist accessToken from Step 3>"

curl -X GET http://localhost:8081/api/alerts \
  -H "Authorization: Bearer $THERAPIST_TOKEN"
```

**Expected Response (shows LLM Call #3 output):**
```json
[
  {
    "id": "alert-uuid",
    "title": "Struggling with work stress...",
    "description": "Based on sentiment analysis (0.92 - very negative) and distress level (8.2/10 - critical), this patient is experiencing significant mental health concerns. The journal entry indicates:\n\nIdentified Issues:\n- High anxiety related to work stress\n- Feelings of hopelessness\n- Sleep disruption\n- Constant worry\n\nRecommendations:\n1. Schedule urgent appointment\n2. Implement stress management techniques\n3. Consider medication evaluation\n4. Monitor for worsening symptoms\n\nThis patient requires immediate therapeutic support.",
    "level": "CRITICAL",
    "status": "NEW",
    "createdAt": "2026-05-26T..."
  }
]
```

**Point Out:**
> "This **description** field contains the AI-generated analysis from LLM Call #3.
> 
> It used:
> - Sentiment score (0.92) from Call #1
> - Distress level (8.2) from Call #2
> 
> To generate personalized recommendations for the therapist."

---

## 🧠 Explain the 3 LLM Calls

### **Call #1: Sentiment Analysis**
```
Input:  Journal text ("I am feeling very anxious...")
Model:  DistilBERT (Hugging Face)
Output: 0.92 (scale: 0=negative, 1=positive)
Time:   500-800ms
```

### **Call #2: Distress Detection**
```
Input:  Journal text ("I am feeling very anxious...")
Model:  Custom distress detection (Hugging Face)
Output: 8.2/10 (scale: 0=no distress, 10=severe)
Time:   400-600ms
Decision: if (8.2 > 5.0) → Execute Call #3
```

### **Call #3: AI Analysis Generation**
```
Input:  Prompt with sentiment (0.92) + distress (8.2) + journal text
Model:  Text2Text generation (Hugging Face)
Output: Generated text with recommendations
Time:   1000-2000ms
DEPENDS ON: Call #1 & #2 outputs
```

---

## 📊 Demo Script (Key Points)

> "Mind-Guard demonstrates 3 Hugging Face LLM calls working together:
> 
> When a patient writes a journal entry:
> 
> 1. **Sentiment Analysis** (Call #1) analyzes emotional tone
>    - Result: 0.92 (very negative)
> 
> 2. **Distress Detection** (Call #2) identifies crisis keywords
>    - Result: 8.2/10 (critical)
>    - Decision: if > 5.0 → trigger Call #3
> 
> 3. **AI Analysis** (Call #3) uses both scores to generate insights
>    - Input: Journal + 0.92 + 8.2
>    - Output: Personalized recommendations
> 
> All three calls execute in ~2-3 seconds, automatically alerting the therapist.
> 
> This shows how multiple LLM models can work together to create intelligent mental health support."

---

## 📋 Complete API Endpoints Reference

### Authentication
```bash
# Register
POST /api/auth/register
Body: {username, email, password, firstName, lastName, role}

# Login
POST /api/auth/login
Body: {email, password}
```

### Journal Entries (Triggers LLM)
```bash
# Create (triggers all 3 LLM calls!)
POST /api/journals
Auth: Bearer token
Body: {title, content, mood}

# Get all entries
GET /api/journals
Auth: Bearer token

# Get specific entry
GET /api/journals/{id}
Auth: Bearer token
```

### Alerts (Shows LLM Output)
```bash
# Get all alerts
GET /api/alerts
Auth: Bearer token

# Get specific alert
GET /api/alerts/{id}
Auth: Bearer token
```

### Moods
```bash
# Log mood (optional, can skip in demo)
POST /api/moods
Auth: Bearer token
Body: {mood, intensity}
```

---

## ✅ Success Indicators

✅ Patient registers successfully  
✅ Journal entry created with scores  
✅ Alert appears with AI analysis  
✅ All 3 LLM calls shown in response  
✅ Sentiment score (0.92) visible  
✅ Distress level (8.2) visible  
✅ AI-generated text in alert description  

---

## 🎯 Why This Works Better Than UI

1. **Shows Real API Responses** - audience sees actual data
2. **Demonstrates LLM Outputs** - sentiment and distress scores visible
3. **Shows Dependencies** - Call #3 uses outputs from #1 & #2
4. **Backend Logs Show Processing** - can show real-time LLM calls
5. **No UI Build Issues** - pure API demonstration

---

## 📺 Full Demo (20 minutes)

- **0-2 min:** Explain architecture (3 LLM calls)
- **2-5 min:** Register patient (Step 1)
- **5-8 min:** Create journal (Step 2) - show JSON with scores
- **8-10 min:** Register therapist (Step 3)
- **10-13 min:** Fetch alerts (Step 4) - show AI analysis
- **13-20 min:** Explain each LLM call + Q&A

---

**You're ready to demo the 3 LLM calls via API!** 🚀
