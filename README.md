# Mind-Guard: AI-Powered Mental Health Platform

A full-stack application demonstrating **3 Hugging Face LLM API calls** for mental health support.

## 🎯 Quick Start

### Systems Running ✅
- **Frontend:** http://localhost:4201
- **Backend:** http://localhost:8081

### Go to Demo
**Open in browser:** http://localhost:4201

## 📖 Documentation Files

Read these in order:

1. **DEMO_START_HERE.md** ← START HERE
   - Quick 20-minute demo walkthrough
   - What to click and when
   - Expected results

2. **COMPLETE_DEMO_GUIDE.md** ← FULL DETAILS
   - Complete system setup
   - Step-by-step demo instructions
   - Detailed LLM call explanations
   - Model dependencies explained
   - Complete data flow diagrams

3. **DEMO_CHEAT_SHEET.md** ← QUICK REFERENCE
   - Print this for easy lookup
   - Key buttons and URLs
   - Troubleshooting tips
   - 2-minute demo script

4. **LLM_DEMO.md** ← TECHNICAL DEEP DIVE
   - LLM architecture overview
   - API request/response examples
   - Prompt construction details
   - How Call #3 depends on #1 & #2

5. **README_DEMO.md** ← ARCHITECTURE
   - System overview
   - Feature list
   - Educational value
   - Full tech stack

## 🧠 The 3 LLM Calls

### Call #1: Sentiment Analysis 📊
- **Model:** DistilBERT
- **Input:** Journal text
- **Output:** Sentiment score (0.92 = very negative)
- **Time:** 500-800ms

### Call #2: Distress Detection 🚨
- **Model:** Custom distress detection
- **Input:** Journal text
- **Output:** Distress level (8.2/10 = critical)
- **Time:** 400-600ms

### Call #3: AI Analysis Generation 💡
- **Model:** Text2Text generation
- **Input:** Journal + Call#1 result (0.92) + Call#2 result (8.2)
- **Output:** AI-generated insights
- **Time:** 1000-2000ms
- **Note:** Depends on Call#1 & Call#2 outputs

## 📱 Demo Flow

1. **Patient registers** → Sees personal dashboard
2. **Patient creates journal entry** → 3 LLM calls trigger
3. **Wait 2-3 seconds** → All processing happens
4. **Therapist registers** → Sees alert with AI analysis
5. **Alert shows:**
   - Sentiment score (0.92)
   - Distress level (8.2)
   - AI-generated recommendations

## 🎯 Key Features

✅ Role-based dashboards (Patient vs Therapist)  
✅ Real-time LLM processing  
✅ Automatic alert generation  
✅ AI-powered mental health insights  
✅ Crisis detection (distress > 5.0)  
✅ JWT authentication  
✅ Responsive design  

## 🏗️ Tech Stack

**Frontend:**
- Angular 18
- TypeScript
- SCSS styling
- JWT authentication

**Backend:**
- Spring Boot 3.3
- Java 21
- PostgreSQL
- Hugging Face API integration

**LLM Integration:**
- Hugging Face API
- 3 different models
- Async processing
- Result caching in DB

## 📊 Success Metrics

✅ Patient dashboard loads  
✅ Journal entry saves  
✅ All 3 LLM calls execute  
✅ Therapist sees alert  
✅ AI analysis displays  
✅ Scores show correctly  

## 🚀 Ready to Demo?

1. Read **DEMO_START_HERE.md**
2. Open http://localhost:4201
3. Follow the steps
4. Show the 3 LLM calls working together

## 📞 Support

- Frontend issues: Check browser console (F12)
- Backend issues: Check logs: `tail -f /tmp/backend.log`
- LLM issues: Verify API key is active
- Database issues: Check PostgreSQL connection

---

**Created for demonstration of LLM integration in healthcare applications**
