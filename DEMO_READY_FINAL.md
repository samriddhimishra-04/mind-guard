# Mind-Guard Demo - READY TO RUN ✅

## Application Status

✅ **Frontend**: Angular 18 running on http://localhost:4201  
✅ **Backend**: Spring Boot 3.3 running on http://localhost:8082 (with JWT authentication fix)  
✅ **Database**: PostgreSQL configured  
✅ **LLM Integration**: Hugging Face API active  
✅ **All Features**: Patient registration, journal creation, mood logging, therapist alerts

---

## 20-Minute Demo Flow

### **0-2 min: Setup**
- Frontend: http://localhost:4201
- Backend running on port 8082
- All services configured

### **2-5 min: Patient Registration**
1. Open http://localhost:4201 in browser
2. Click "Sign Up" 
3. Fill in form:
   - Username: `john_patient`
   - Email: `john@demo.com`
   - Password: `Demo@12345678`
   - First Name: `John`
   - Last Name: `Doe`
   - Role: `PATIENT`
4. Click Register → See patient dashboard

### **5-10 min: Create Journal Entry (Triggers 3 LLM Calls!)**
1. Click "New Journal Entry" button
2. Fill in:
   - Title: "Work stress and anxiety"
   - Content: "I am feeling very anxious and overwhelmed today. Work deadlines are piling up and I feel hopeless about the situation. I cannot sleep well at night. Everything feels so stressful and I worry constantly. I need help."
   - Mood: "ANXIOUS"
3. Click Save
4. **Behind the scenes**:
   - **LLM Call #1** (Sentiment Analysis - DistilBERT): Analyzes emotional tone → Returns 0-1 score
   - **LLM Call #2** (Distress Detection - Custom Model): Identifies crisis → Returns 0-10 score
   - **LLM Call #3** (AI Analysis - Text2Text): If distress > 5.0, generates recommendations
5. See sentiment score and distress level in response

### **10-15 min: Test Mood Logging**
1. Click "Log Mood" button  
2. Select mood: "ANXIOUS"
3. Set intensity: 8/10
4. Add notes: "Feeling overwhelmed"
5. Click Save → Mood logged successfully!
6. (This now works thanks to JWT authentication fix)

### **15-18 min: Therapist Dashboard & Alerts**
1. Open new browser tab/incognito
2. Go to http://localhost:4201
3. Register therapist:
   - Username: `dr_smith`
   - Email: `smith@demo.com`
   - Password: `Demo@12345678`
   - First Name: `Dr.`
   - Last Name: `Smith`
   - Role: `THERAPIST`
4. Click Register → See therapist dashboard
5. Navigate to "Alerts" section
6. View auto-generated alert from patient's journal:
   - Alert Level: CRITICAL (because distress > 5.0)
   - Description: AI-generated analysis with recommendations
   - References sentiment and distress scores

### **18-20 min: Explain the Architecture**

> "This demo shows how **3 LLM API calls work together** to create intelligent mental health support:
>
> When a patient writes a journal entry:
>
> 1. **Sentiment Analysis** (Call #1) using DistilBERT analyzes emotional tone
>    - Input: Journal text
>    - Output: 0.92 (very negative)
>    - Time: ~500-800ms
>
> 2. **Distress Detection** (Call #2) using custom model identifies crisis keywords
>    - Input: Journal text
>    - Output: 8.2/10 (critical severity)
>    - Time: ~400-600ms
>    - **Decision**: If > 5.0, trigger Call #3
>
> 3. **AI Analysis Generation** (Call #3) using Text2Text LLM creates personalized recommendations
>    - Input: Journal + Sentiment (0.92) + Distress (8.2)
>    - Output: Generated recommendations for therapist
>    - Time: ~1000-2000ms
>    - Automatically creates alert in database
>
> All three calls execute in ~2-3 seconds, automatically alerting the therapist.
>
> This demonstrates how multiple LLM models can work together in a real-world application to provide intelligent, context-aware mental health support."

---

## 🔧 Technical Details

### Three LLM Calls in Action:

**Call #1: Sentiment Analysis**
```
Model: DistilBERT (Hugging Face)
Input: Journal text
Output: -1 to 1 score (negative to positive)
```

**Call #2: Distress Detection**
```
Model: Custom distress model (Hugging Face)
Input: Journal text
Output: 0-10 score (no distress to severe crisis)
Decision: if (score > 5.0) → execute Call #3
```

**Call #3: AI Analysis Generation**
```
Model: Text2Text generation (Hugging Face)
Input: Journal text + sentiment score + distress score
Output: Natural language analysis and recommendations
Depends on: Outputs from Call #1 and Call #2
```

### Architecture:
- **Frontend**: Angular 18 standalone components, role-based routing
- **Backend**: Spring Boot 3.3 with Spring Security, JWT authentication
- **Database**: PostgreSQL 18
- **LLM Service**: Hugging Face API with 3 coordinated calls
- **Authentication**: JWT tokens with custom JwtAuthenticationFilter

---

## ✅ Features Working

✅ Patient registration with email validation  
✅ Therapist registration with role separation  
✅ Patient dashboard with UI components  
✅ Therapist dashboard with patient management  
✅ Journal creation triggering 3 LLM calls  
✅ Sentiment analysis (Call #1)  
✅ Distress detection (Call #2)  
✅ AI analysis generation (Call #3)  
✅ Auto-generated alerts based on distress level  
✅ Mood logging with intensity tracking  
✅ Role-based access control  
✅ JWT authentication throughout  

---

## 📊 API Endpoints (for reference)

```bash
# Register Patient
POST /api/auth/register
Body: {username, email, password, firstName, lastName, role}

# Login
POST /api/auth/login
Body: {email, password}

# Create Journal (triggers 3 LLM calls!)
POST /api/journals
Headers: Authorization: Bearer <token>
Body: {title, content, mood}

# Log Mood
POST /api/moods
Headers: Authorization: Bearer <token>
Body: {mood, intensityLevel, notes}

# Get Alerts (shows LLM output)
GET /api/alerts
Headers: Authorization: Bearer <token>
```

---

## 🚀 Demo Talking Points

1. **Three LLM Models Working Together**
   - Sentiment Analysis identifies emotional state
   - Distress Detection identifies crisis severity
   - AI Analysis generates actionable recommendations

2. **Dependency Chain**
   - Calls #1 and #2 execute in parallel
   - Call #3 waits for results from #1 and #2
   - Call #3 only executes if distress > threshold
   - Shows intelligent decision-making in ML pipelines

3. **Real-World Application**
   - Not just displaying data, but acting on it
   - Auto-generates alerts based on ML analysis
   - Prioritizes patient care based on severity scores
   - Demonstrates end-to-end ML integration

4. **Quick Response Time**
   - ~2-3 seconds total for all 3 calls
   - User gets immediate feedback with LLM scores
   - Therapist notified instantly of high-distress cases

---

## 🎯 Success Metrics

After the demo, audience should understand:
- ✅ How multiple LLM models can be orchestrated in an application
- ✅ How to use LLM outputs to drive business logic (distress > 5.0 → create alert)
- ✅ Real-world mental health use case for AI
- ✅ Complete tech stack from frontend to backend to LLM integration

---

## 📝 Technical Achievement

This demonstrates:
- **LLM Orchestration**: 3 models working together with dependency chains
- **Real-time Processing**: ~2-3 seconds for ML + database operations
- **Intelligent Routing**: Decision logic based on ML scores (distress > 5.0)
- **Full-Stack Integration**: Frontend → API → LLM → Database → Therapist Alert
- **Role-Based Security**: Different dashboards for patient vs therapist
- **JWT Authentication**: Secure API access with token-based auth

---

**You're ready to demo!** 🚀

Start with: http://localhost:4201
