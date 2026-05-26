# Mind-Guard Demo - Cheat Sheet

## 🎯 The 3 LLM Calls in 30 Seconds

When a patient writes in their journal:

1. **Sentiment Analysis** 📊
   - Detects emotional tone
   - Returns: -1 (sad) to +1 (happy)

2. **Distress Detection** 🚨
   - Finds crisis keywords
   - Returns: 0-10 scale
   - If > 5 → Creates alert

3. **AI Analysis Generation** 💡
   - Generates personalized insights
   - Recommends coping strategies
   - Helps therapist understand patient

---

## 📋 Quick Links

| What | URL | Username |
|------|-----|----------|
| Frontend | http://localhost:4201 | Any |
| Patient Dashboard | http://localhost:4201/dashboard | john_doe |
| Therapist Dashboard | http://localhost:4201/therapist | dr_smith |
| Backend API | http://localhost:8081 | - |

---

## 🎬 Demo Script (2 min)

"Mind-Guard is an AI mental health platform with **3 Hugging Face LLM calls**:

1. **Sentiment Analysis** - Understands emotions
2. **Distress Detection** - Identifies crisis  
3. **AI Analysis** - Generates insights

When a patient writes a journal entry, all 3 run automatically. The therapist sees AI-powered alerts in real-time."

---

## ⚡ 5-Minute Demo

### Minute 1-2: Setup
```
Backend: java -jar target/mindguard-1.0.0.jar --server.port=8081 \
  --huggingface.api-key=<YOUR_HUGGING_FACE_API_KEY>

Frontend: npm start -- --port 4201
```

### Minute 2-3: Patient Registration
- Go to http://localhost:4201
- Register → Fill form → Patient role ✓
- See patient dashboard

### Minute 3-4: Trigger LLM (Create Journal)
- Click "New Journal Entry"
- **Paste this text:**
```
Title: Struggling with stress

Content: I am feeling very anxious and 
overwhelmed today. Work deadlines are 
piling up and I feel hopeless about the 
situation. I cannot sleep well at night.
```
- Mood: ANXIOUS
- Save

**WAIT 2-3 SECONDS** ← 3 LLM calls happening!

### Minute 4-5: Therapist View Alert
- Open incognito window
- Register as therapist → Role: THERAPIST ✓
- See Alerts tab
- **Show the alert with AI analysis**

---

## 🔑 Test Credentials

Create new or use:
```
Patient:
  Email: patient1@test.com
  Password: Patient@12345678

Therapist:
  Email: therapist1@test.com
  Password: Therapist@12345678
```

---

## 📱 Key Buttons

| Button | Location | Action |
|--------|----------|--------|
| New Journal | Patient Dashboard | Creates entry (triggers LLM) |
| Log Mood | Patient Dashboard | Records mood |
| Alerts | Therapist Dashboard | Shows AI alerts |
| Patients | Therapist Dashboard | Shows patient list |

---

## ✅ Success Markers

✓ Patient registers  
✓ Patient dashboard shows  
✓ Journal entry created  
✓ Alert appears on therapist view  
✓ Alert shows AI-generated text  
✓ Both dashboards load  

---

## 🐛 If Something Breaks

| Issue | Fix |
|-------|-----|
| Port in use | Kill process: `lsof -i :8081` then `kill -9 <PID>` |
| Page blank | Clear cache (Ctrl+Shift+Del) then refresh (Ctrl+F5) |
| API error | Check backend logs: `tail -f /tmp/backend.log` |
| No alert | Wait 3 seconds, refresh page |

---

## 📊 Backend Logs Location

Check LLM calls:
```bash
tail -f /tmp/backend.log | grep -i "sentiment\|distress\|analyze"
```

---

## 🎓 What You're Showing

- **Full-stack app** (Angular + Spring Boot + PostgreSQL)
- **LLM Integration** (3 Hugging Face API calls)
- **Role-based dashboards** (Patient vs Therapist)
- **Real-time processing** (Async background tasks)
- **Crisis detection** (Auto-generated alerts)
- **AI insights** (Personalized recommendations)

---

## ⏱️ Timing

- Setup: 2 min
- Demo: 5 min
- Q&A: 13 min
- **Total: 20 min** ✅

---

## 💡 Key Talking Points

> "Three LLM calls happen automatically when patient writes journal:
> 1. Sentiment analysis detects emotional tone
> 2. Distress detection identifies crisis keywords
> 3. AI generates personalized insights for therapist"

> "If distress level goes above 5, alert auto-created"

> "Therapist sees AI-powered recommendations to help patient"

---

## 🎯 Focus Areas

✅ Show patient creating journal (triggers LLM)  
✅ Wait 2-3 seconds (show LLM processing)  
✅ Switch to therapist (show alert)  
✅ Show AI-generated analysis text  
✅ Explain the 3 API calls briefly  

---

## 📸 Screenshots to Show

1. Patient dashboard (colorful cards)
2. Journal entry form (text input)
3. Therapist alert card (AI analysis text)
4. Patients list (shows alerts)

---

**Ready? Go to http://localhost:4201 and start! 🚀**
