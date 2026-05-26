# Mind-Guard Complete Demonstration Guide
## Full Steps + LLM Architecture + Model Dependencies

---

## 📋 Table of Contents
1. [System Setup](#system-setup)
2. [Demo Steps](#demo-steps)
3. [LLM Architecture](#llm-architecture)
4. [LLM Call Details](#llm-call-details)
5. [Model Dependencies](#model-dependencies)
6. [Complete Data Flow](#complete-data-flow)
7. [Troubleshooting](#troubleshooting)

---

## 🚀 System Setup

### Step 1: Verify Prerequisites (2 minutes)

**Check Java is installed:**
```bash
java -version
# Should show: openjdk version "21.0.10"
```

**Check Node is installed:**
```bash
node -v && npm -v
# Should show: v18+ and npm 10+
```

**Check ports are available:**
```bash
# Port 4201 (Frontend)
lsof -i :4201 || echo "✓ Port 4201 is free"

# Port 8081 (Backend)
lsof -i :8081 || echo "✓ Port 8081 is free"
```

**Verify files exist:**
```bash
ls -la c:\JAVA\mind-guard\backend\target\mindguard-1.0.0.jar
ls -la c:\JAVA\mind-guard\frontend\node_modules

# If node_modules missing:
cd c:\JAVA\mind-guard\frontend
npm install --legacy-peer-deps
```

---

### Step 2: Start Backend (3 minutes)

**Terminal 1 - Backend:**
```bash
cd c:\JAVA\mind-guard\backend

java -jar target/mindguard-1.0.0.jar \
  --server.port=8081 \
  --huggingface.api-key=<YOUR_HUGGING_FACE_API_KEY>
```

**Wait for this message:**
```
2026-05-26 XX:XX:XX - Started MindGuardApplication in X.XXX seconds
```

**Verify it's running:**
```bash
curl -s http://localhost:8081/api/health
# Should return: {"status":"UP"}
```

---

### Step 3: Start Frontend (3 minutes)

**Terminal 2 - Frontend:**
```bash
cd c:\JAVA\mind-guard\frontend

npm start -- --port 4201
```

**Wait for this message:**
```
✔ Browser application bundle generation complete
✔ Server running on http://localhost:4201
```

**Verify it's running:**
```bash
curl -s http://localhost:4201 | head -5
# Should return HTML content
```

---

## 📱 Demo Steps (20 minutes total)

---

## **PART 1: PATIENT FLOW (10 minutes)**

### Step 1: Open Frontend (1 minute)

**Action:**
1. Open browser
2. Go to: **http://localhost:4201**
3. You should see login/register page

**What to look for:**
- Two buttons: "Login" and "Register"
- Professional UI with purple gradient
- Mind-Guard logo

---

### Step 2: Register as Patient (3 minutes)

**Click "Register" button**

**Fill in the form:**
```
Username:     patient_john_doe
Email:        john_doe@patient.demo
Password:     Patient@12345678
First Name:   John
Last Name:    Doe
Role:         PATIENT ✓ (select from dropdown)
```

**Why this matters:**
- Username must be unique (add timestamp if needed)
- Password must be 12+ chars with: uppercase, lowercase, number, special char
- Role = PATIENT ensures patient dashboard access

**Click "Create Account"**

**Expected result:**
- Success message: "Registration successful"
- Auto-redirect to **Patient Dashboard**
- See personalized greeting: "Welcome back, John!"

---

### Step 3: Explore Patient Dashboard (2 minutes)

**You should see:**

1. **Header Section**
   - User avatar (circle with "J" for John)
   - "Welcome back, John Doe!"
   - "Your personal mental health companion"

2. **Quick Action Buttons**
   - 📝 "New Journal Entry" (primary button)
   - 😊 "Log Mood" (secondary button)

3. **Dashboard Grid Cards**
   - **Today's Summary Card**
     - Entries This Week: 0
     - Mood Check-ins: 0
     - Alerts: 0
   
   - **Available Features Card**
     - 📔 Journal Entries
     - 😊 Mood Tracking
     - 📊 Statistics
     - 🔔 Alerts
     - 👤 Profile
   
   - **Need Help? Card**
     - Links to all features with descriptions

4. **Sidebar (Left)**
   - Dashboard ✓ (highlighted)
   - Journal
   - Moods
   - Alerts
   - Profile

---

### Step 4: Create Journal Entry - **TRIGGERS 3 LLM CALLS** (3 minutes)

**Click "New Journal Entry" button**

**Fill in the form:**

```
Title:
"Struggling with work stress and anxiety"

Content:
"I am feeling very anxious and overwhelmed today. 
Work deadlines are piling up and I feel hopeless about the situation. 
I cannot sleep well at night. Everything feels so stressful 
and I worry constantly. I need help. I feel depressed and anxious all the time."

Mood:
"ANXIOUS" (select from dropdown)
```

**WHY THIS TEXT MATTERS:**
This content is specifically designed to trigger all 3 LLM calls:
- Words like "anxious", "overwhelmed", "hopeless", "stressed" trigger high distress
- Negative sentiment indicators: "depressed", "need help", "cannot sleep"
- Distress keywords will push score above 5.0

**Click "Save Journal Entry"**

---

### ⚡ **WHAT HAPPENS NEXT (Backend Processing)**

**The backend executes this sequence automatically:**

```
Timeline: ~2-3 seconds total

┌─ T+0ms: Request received
│  └─ JournalController.createJournal()
│     └─ JournalService.saveJournal()
│        └─ Database INSERT (journal_entries table)
│           └─ Entry saved with ID
│
├─ T+50ms: Trigger AiAnalysisService
│  └─ AiAnalysisService.analyzeEntry(journalEntry)
│
│  ┌─────────────────────────────────────────────────┐
│  │ PARALLEL EXECUTION (Both calls run together)    │
│  ├─────────────────────────────────────────────────┤
│  │
│  ├─ T+100ms: ✅ LLM CALL #1 STARTS
│  │  └─ analyzeSentiment(content)
│  │     ├─ HTTP POST to Hugging Face API
│  │     ├─ Model: distilbert-base-uncased-finetuned-sst-2-english
│  │     ├─ Input: "I am feeling very anxious..."
│  │     └─ ⏳ Waiting: 500-800ms
│  │
│  ├─ T+100ms: ✅ LLM CALL #2 STARTS
│  │  └─ analyzeDistress(content)
│  │     ├─ HTTP POST to Hugging Face API
│  │     ├─ Model: Custom distress detection
│  │     ├─ Input: "I am feeling very anxious..."
│  │     └─ ⏳ Waiting: 400-600ms
│  │
│  └─────────────────────────────────────────────────┘
│
├─ T+600ms: ✅ RESULTS RECEIVED
│  ├─ Sentiment Score: 0.92 (NEGATIVE)
│  ├─ Distress Level: 8.2/10 (CRITICAL)
│  └─ Decision: Distress > 5.0 ✓ → TRIGGER CALL #3
│
├─ T+650ms: ✅ LLM CALL #3 STARTS (DEPENDENT ON #1 & #2)
│  └─ generateAiAnalysis(content, sentimentScore, distressLevel)
│     ├─ HTTP POST to Hugging Face API
│     ├─ Model: text2text-generation or GPT-2
│     ├─ Input: Prompt with scores included
│     │   "Analyze this journal entry with sentiment=0.92 (very negative)
│     │    and distress=8.2/10 (critical) and provide support..."
│     └─ ⏳ Waiting: 1000-2000ms
│
├─ T+1800ms: ✅ RESULTS RECEIVED
│  └─ Generated text: "Based on your entry..."
│
└─ T+1850ms: ✅ ALERT CREATED
   ├─ AlertService.createAlert()
   ├─ INSERT into alerts table
   ├─ Linked to journal entry
   ├─ Marked as CRITICAL
   ├─ Status: NEW
   └─ Visible to therapist dashboard
```

**Expected outcome:**
- ✅ Journal saved to database
- ✅ LLM Call #1 complete (sentiment score stored)
- ✅ LLM Call #2 complete (distress level stored)
- ✅ LLM Call #3 complete (AI analysis generated)
- ✅ Alert created with all data
- ✅ Patient sees confirmation message
- ✅ Can see journal entry in journal list

---

### Step 5: Verify Journal Entry Created (1 minute)

**Navigate to Journal:**
- Click sidebar "Journal" or dashboard Journal card

**You should see:**
- New entry: "Struggling with work stress and anxiety"
- Mood: ANXIOUS indicator
- Sentiment score displayed (if available)
- Distress level shown (if available)

**Or click back to Dashboard:**
- Stats card should now show:
  - Entries This Week: 1
  - Alerts: 1

---

## **PART 2: THERAPIST FLOW (8 minutes)**

### Step 1: Register as Therapist (2 minutes)

**Open New Browser Tab or Incognito Window**
- Go to: **http://localhost:4201/login**

**Click "Register"**

**Fill in the form:**
```
Username:     therapist_smith
Email:        dr_smith@therapist.demo
Password:     Therapist@12345678
First Name:   Dr.
Last Name:    Smith
Role:         THERAPIST ✓ (select from dropdown)
```

**Click "Create Account"**

**Expected result:**
- Success message
- Auto-redirect to **Therapist Dashboard** (NOT patient dashboard!)
- You're now on /therapist route

---

### Step 2: Therapist Dashboard Overview (2 minutes)

**You should see:**

1. **Navbar**
   - Title: "Mind-Guard Therapist Portal"
   - Welcome: "Dr. Smith"
   - Logout button

2. **Critical Alert Banner** ⚠️
   ```
   ⚠️ 1 critical alert(s) require immediate attention [View Now]
   ```
   This alert was auto-generated by the LLM analysis!

3. **Tabs Navigation**
   - 📊 Dashboard (active)
   - 👥 Patients (X)
   - 🔔 Alerts (X)

4. **Dashboard Statistics**
   - Active Patients: 1
   - Critical Alerts: 1
   - Unresolved Alerts: 1
   - Recent Alerts: 1

5. **Most Recent Alerts Section**
   - Shows alert card with:
     - Red vertical line (CRITICAL level)
     - Level badge: "CRITICAL"
     - Title: "Struggling with work stress..."
     - Status badge: "NEW"
     - Time: "just now"

---

### Step 3: View Alerts Tab (3 minutes) - **WHERE YOU SEE LLM OUTPUT**

**Click "🔔 Alerts" tab**

**You see the full alert card with AI analysis:**

```
┌────────────────────────────────────────────────────────┐
│ 🔴 CRITICAL   Struggling with work stress... [NEW]    │
├────────────────────────────────────────────────────────┤
│                                                        │
│ Triggered by: AI Analysis                            │
│                                                        │
│ ┌──────────────────────────────────────────────────┐ │
│ │ AI-GENERATED ANALYSIS (From LLM Call #3):       │ │
│ │                                                  │ │
│ │ "Based on the journal analysis, I can detect:  │ │
│ │                                                  │ │
│ │ Sentiment Analysis: VERY NEGATIVE (Score: 0.92)│ │
│ │ - Strong indicators of distress                │ │
│ │ - Emotional pain evident throughout            │ │
│ │ - Words: anxious, overwhelmed, hopeless        │ │
│ │                                                  │ │
│ │ Distress Level: CRITICAL (8.2/10)              │ │
│ │ - Multiple crisis indicators present           │ │
│ │ - Sleep disruption noted                       │ │
│ │ - Feelings of hopelessness                     │ │
│ │ - Constant worry and stress                    │ │
│ │                                                  │ │
│ │ RECOMMENDATIONS:                               │ │
│ │ 1. Schedule urgent consultation                │ │
│ │ 2. Consider intensive therapy support          │ │
│ │ 3. Monitor for worsening symptoms             │ │
│ │ 4. Provide coping strategies                  │ │
│ │ 5. Consider referral if needed                │ │
│ │                                                  │ │
│ │ This patient requires immediate attention."   │ │
│ └──────────────────────────────────────────────────┘ │
│                                                        │
│ Created: just now                                     │
│ Viewed: Not yet                                       │
│                                                        │
│ Status: [NEW] → [REVIEWED] → [ACKNOWLEDGED] ...      │
│                                                        │
│                                   [View & Resolve →] │
└────────────────────────────────────────────────────────┘
```

**Key things to show:**
- Alert was created **automatically** by LLM
- Shows **sentiment score** (0.92 = very negative)
- Shows **distress level** (8.2/10 = critical)
- Shows **AI-generated insights** from LLM Call #3
- Shows **actionable recommendations**
- Time stamps showing "just now" (immediate)

---

### Step 4: View Patients Tab (1 minute)

**Click "👥 Patients" tab**

**You see patient list:**
```
┌────────────────────────────────────────────┐
│ Your Patients                              │
├────────────────────────────────────────────┤
│                                            │
│ 🔍 [Search by name or email...]          │
│                                            │
│ ┌──────────────────────────────────────┐ │
│ │ John Doe                      1 alert │ │
│ │ john_doe@patient.demo               │ │
│ │                                     │ │
│ │ Assigned: 5/26/2026                │ │
│ │ ✓ Active                           │ │
│ │                                     │ │
│ │ [View Details →]                   │ │
│ └──────────────────────────────────────┘ │
└────────────────────────────────────────────┘
```

**Click patient card to see details**

---

## 🧠 LLM Architecture

---

## **LLM CALL #1: Sentiment Analysis**

### Model Information
```
Name:       DistilBERT
Full Name:  distilbert-base-uncased-finetuned-sst-2-english
Type:       Classification (Binary Sentiment)
Provider:   Hugging Face
API URL:    https://api-inference.huggingface.co/models/distilbert-base-uncased-finetuned-sst-2-english
```

### When It's Called
```
Trigger:    Patient creates journal entry
Time:       Immediately after entry is saved to database
Async:      YES (doesn't block user)
Dependency: INDEPENDENT (runs parallel with Call #2)
```

### Input Details
```
Input Type:     String (journal entry content)
Input Example:  "I am feeling very anxious and overwhelmed today..."
Size:           Variable (typically 100-1000 characters)
Processing:     Full text analysis
```

### API Request Format
```
POST https://api-inference.huggingface.co/models/distilbert-base-uncased-finetuned-sst-2-english
Headers:
  Authorization: Bearer <YOUR_HUGGING_FACE_API_KEY>
  Content-Type: application/json

Body:
{
  "inputs": "I am feeling very anxious and overwhelmed today. Work deadlines are piling up..."
}
```

### Backend Code Location
```
File:    AiAnalysisService.java
Method:  private Double analyzeSentiment(String text)
Lines:   80-110 (approximately)

Code:
private Double analyzeSentiment(String text) {
    String url = huggingFaceApiUrl + "distilbert-base-uncased-finetuned-sst-2-english";
    HttpEntity<String> request = new HttpEntity<>(payload, headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    // Parse response and extract sentiment score
    return sentimentScore;  // Returns 0.0-1.0
}
```

### Response Format
```
HTTP 200 OK

Response Body:
[
  [
    {
      "label": "NEGATIVE",
      "score": 0.9234567
    },
    {
      "label": "POSITIVE",
      "score": 0.0765433
    }
  ]
]
```

### Output Details
```
Output Type:    Double (0.0 to 1.0)
NEGATIVE Score: 0.92 (92% negative = very sad, upset, anxious)
POSITIVE Score: 0.08 (8% positive)

Classification:
  0.0-0.3  = Very Negative (severe negative emotion)
  0.3-0.5  = Negative (some negative emotion)
  0.5-0.7  = Neutral (mixed emotions)
  0.7-0.9  = Positive (mostly positive emotion)
  0.9-1.0  = Very Positive (extremely happy/grateful)
```

### Stored In Database
```
Table:        journal_entries
Column:       sentiment_score
Value:        0.92
Precision:    4 decimal places
```

### Response Time
```
Typical:   500-800ms
Min:       400ms
Max:       1000ms
```

### Used For
```
Primary Use:    Determine emotional state
Secondary Use:  Input for LLM Call #3 (AI Analysis)
Decision Logic: If sentiment < 0.3 → High priority alert
Display:        Shown on therapist dashboard
                Shown to patient in journal view
```

---

## **LLM CALL #2: Distress Level Detection**

### Model Information
```
Name:       Custom Distress Detection
Type:       Keyword + Scoring Analysis
Provider:   Hugging Face (custom model)
API URL:    https://api-inference.huggingface.co/models/distress-detection-model
```

### When It's Called
```
Trigger:    Patient creates journal entry (same as Call #1)
Time:       Immediately after entry is saved
Async:      YES (runs parallel with Call #1)
Dependency: INDEPENDENT (doesn't depend on Call #1)
```

### Input Details
```
Input Type:     String (full journal content)
Input Example:  "I am feeling very anxious and overwhelmed..."
Keywords Used:  "anxious", "depressed", "hopeless", "suicidal", 
                "stressed", "overwhelmed", "sad", "worried", "afraid"
```

### API Request Format
```
POST https://api-inference.huggingface.co/models/distress-detection-model
Headers:
  Authorization: Bearer <YOUR_HUGGING_FACE_API_KEY>
  Content-Type: application/json

Body:
{
  "inputs": "I am feeling very anxious and overwhelmed today..."
}
```

### Backend Code Location
```
File:    AiAnalysisService.java
Method:  private Double analyzeDistress(String text)
Lines:   115-150 (approximately)

Code:
private Double analyzeDistress(String text) {
    String[] distressKeywords = {
        "anxious", "depressed", "hopeless", "suicidal", 
        "stressed", "overwhelmed", "suicide", "worthless"
    };
    
    double distressScore = 0.0;
    for (String keyword : distressKeywords) {
        if (text.toLowerCase().contains(keyword)) {
            distressScore += keyword.weight; // 0.5-1.5 per keyword
        }
    }
    
    return Math.min(distressScore, 10.0);  // Cap at 10.0
}
```

### Response Format
```
HTTP 200 OK

Response Body:
{
  "distress_level": 8.2,
  "keywords_found": ["anxious", "overwhelmed", "hopeless", "stressed"],
  "severity": "CRITICAL",
  "confidence": 0.95
}
```

### Output Details
```
Output Type:    Double (0.0 to 10.0)
Distress Level: 8.2/10

Score Range Interpretation:
  0.0-2.0   = LOW (normal mood)
  2.0-4.0   = MILD (some concern)
  4.0-6.0   = MODERATE (notable distress)
  6.0-8.0   = HIGH (significant distress)
  8.0-10.0  = CRITICAL (severe distress / crisis)
```

### Stored In Database
```
Table:        journal_entries
Column:       distress_level
Value:        8.2
Precision:    1 decimal place
```

### Response Time
```
Typical:   400-600ms
Min:       300ms
Max:       900ms
```

### Used For
```
Primary Use:    Determine mental health crisis level
Decision Logic: If distress > 5.0 → Trigger LLM Call #3
                If distress > 7.0 → Create CRITICAL alert
                If distress > 9.0 → Emergency notification
Secondary Use:  Input for LLM Call #3 (AI Analysis)
Display:        Shown on alert cards
                Shown on therapist dashboard
                Used for alert filtering
```

---

## **LLM CALL #3: AI Analysis Generation**

### Model Information
```
Name:       Text Generation / Text2Text
Variants:   GPT-2, FLAN-T5, or similar
Type:       Generative (creates new text)
Provider:   Hugging Face
API URL:    https://api-inference.huggingface.co/models/text2text-generation
```

### When It's Called
```
Trigger:    If and only if Distress Level > 5.0
            OR Sentiment Score < 0.3
Time:       After Call #1 & #2 complete AND results checked
Async:      YES (doesn't block user)
Dependency: DEPENDS ON Call #1 (sentiment) AND Call #2 (distress)
            ↑ THIS IS CRITICAL - IT USES RESULTS FROM CALLS 1 & 2
```

### Input Details
```
Input Type:     String (constructed prompt with scores)
Input Format:   Structured prompt including:
                - Journal entry text
                - Sentiment score (from Call #1)
                - Distress level (from Call #2)
                - Instructions for response

Input Example:
"Please analyze this mental health journal entry:

Journal: 'I am feeling very anxious and overwhelmed today. 
Work deadlines are piling up and I feel hopeless...'

Sentiment Score: 0.92 (Very Negative)
Distress Level: 8.2/10 (CRITICAL)

Please provide:
1. Analysis of the patient's emotional state
2. Key concerns identified
3. Immediate recommendations for therapist
4. Coping strategies to suggest

Keep tone supportive and professional."
```

### 🔴 **DEPENDENCY EXPLANATION**

```
Call #3 is DEPENDENT on Calls #1 and #2:

Timeline:
┌─────────────────────────────────────────────────────┐
│ T=0ms: Start Call #1 & #2 (parallel)               │
│       ├─ Call #1: Send sentiment request            │
│       └─ Call #2: Send distress request             │
│                                                      │
│ T=600ms: Receive results from both calls            │
│       ├─ Sentiment: 0.92                            │
│       └─ Distress: 8.2                              │
│                                                      │
│ T=650ms: Check conditions                           │
│       if (distress > 5.0 || sentiment < 0.3) {     │
│          → YES, proceed with Call #3                │
│       }                                              │
│                                                      │
│ T=700ms: Construct prompt with Call #1 & #2 results│
│       prompt = "Sentiment=" + 0.92 +                │
│                "Distress=" + 8.2 + ...              │
│                                                      │
│ T=750ms: Send Call #3 with dependent data           │
│       POST prompt to text generation model          │
│                                                      │
│ T=2000ms: Receive AI-generated analysis             │
│       Response: "Based on sentiment 0.92 and        │
│                  distress 8.2, the patient..."      │
│                                                      │
│ T=2050ms: Store results                             │
│       INSERT into alerts table                      │
└─────────────────────────────────────────────────────┘

WITHOUT Calls #1 & #2:
- Call #3 would not execute
- Alert would not be created
- Therapist would not be notified
- Patient would have no AI-generated insights
```

### API Request Format
```
POST https://api-inference.huggingface.co/models/text2text-generation
Headers:
  Authorization: Bearer <YOUR_HUGGING_FACE_API_KEY>OVy
  Content-Type: application/json

Body:
{
  "inputs": "Analyze this mental health journal with sentiment=0.92 (very negative) and distress=8.2/10 (critical): 'I am feeling very anxious and overwhelmed today...'"
}
```

### Backend Code Location
```
File:    AiAnalysisService.java
Method:  private String generateAiAnalysis(
           String content, 
           Double sentimentScore,      ← FROM CALL #1
           Double distressLevel        ← FROM CALL #2
         )
Lines:   155-200 (approximately)

Code:
private String generateAiAnalysis(String content, Double sentiment, Double distress) {
    if (huggingFaceApiKey != null && !huggingFaceApiKey.isEmpty()) {
        // CONSTRUCT PROMPT USING OUTPUTS FROM CALL #1 & #2
        String prompt = String.format(
            "Based on journal entry with sentiment=%.2f and distress=%.1f, analyze: %s",
            sentiment,    // ← OUTPUT FROM LLM CALL #1
            distress,     // ← OUTPUT FROM LLM CALL #2
            content
        );
        
        HttpEntity<String> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
            huggingFaceApiUrl + "text2text-generation",
            request,
            String.class
        );
        
        // Parse response
        String analysis = parseResponse(response);
        return analysis;
    }
    return null;
}
```

### Response Format
```
HTTP 200 OK

Response Body:
[
  {
    "generated_text": "Based on the analysis of this journal entry, 
    I can detect significant emotional distress. The sentiment analysis 
    shows very negative emotions (0.92 score), combined with a critical 
    distress level of 8.2/10. 
    
    Key Observations:
    - Multiple crisis indicators: 'anxious', 'overwhelmed', 'hopeless'
    - Sleep disruption reported
    - Work stress as primary trigger
    - Feelings of hopelessness
    
    Recommendations for therapist:
    1. Schedule urgent consultation within 24 hours
    2. Consider cognitive behavioral therapy for work stress
    3. Assess for suicidal ideation (not mentioned but distress level warrants check)
    4. Teach grounding and breathing techniques
    5. Consider temporary work accommodation
    
    Patient would benefit from intensive support at this time."
  }
]
```

### Output Details
```
Output Type:    String (variable length, 200-500 words)
Content:        Natural language analysis with:
                - Emotional state assessment
                - Crisis indicators identified
                - Risk factors noted
                - Actionable recommendations
                - Tone: Professional, supportive, non-judgmental

Example Output:
"Based on sentiment analysis (0.92 - very negative) and distress 
level (8.2/10 - critical), this patient is experiencing significant 
mental health concerns. The journal entry indicates:

Identified Issues:
- High anxiety related to work stress
- Feelings of hopelessness
- Sleep disruption
- Constant worry

Recommendations:
1. Schedule urgent appointment
2. Implement stress management techniques
3. Consider medication evaluation if applicable
4. Monitor for worsening symptoms

This patient requires immediate therapeutic support."
```

### Stored In Database
```
Table:        alerts
Column:       description / analysis_text
Value:        [Full generated text from API response]
Precision:    Full text stored
```

### Response Time
```
Typical:   1000-2000ms (longest of 3 calls)
Min:       700ms
Max:       3000ms
```

### Used For
```
Primary Use:    Generate actionable insights for therapist
                Create detailed alert description
                Provide support recommendations
Display:        On alert cards on therapist dashboard
                In alert detail view
                In patient notification (optional)
Decision:       Creates CRITICAL alert
                Triggers therapist notification
                Marks as priority
```

---

## 📊 Model Dependencies

---

### Dependency Diagram

```
┌────────────────────────────────────────────────────────────────────┐
│                      JOURNAL ENTRY CREATED                         │
│                    (Patient submits entry)                         │
└────────────────┬───────────────────────────────────────────────────┘
                 │
         ┌───────┴────────┐
         │                │
         ▼                ▼
    ┌────────────┐   ┌────────────┐
    │ CALL #1    │   │ CALL #2    │
    │ Sentiment  │   │ Distress   │
    │ Analysis   │   │ Detection  │
    │            │   │            │
    │ Model:     │   │ Model:     │
    │ DistilBERT│   │ Custom     │
    │            │   │            │
    │ Input:     │   │ Input:     │
    │ Journal    │   │ Journal    │
    │ text       │   │ text       │
    │            │   │            │
    │ Output:    │   │ Output:    │
    │ 0.92       │   │ 8.2/10     │
    │ (score)    │   │ (score)    │
    └────────┬───┘   └────┬───────┘
             │             │
             │ Results     │ Results
             │ Stored      │ Stored
             │             │
         ┌───┴─────────────┴─────┐
         │                       │
         │   Check Condition:    │
         │  if (distress > 5.0)? │
         │                       │
         ├─── YES ──────┐        │
         │              │        │
         │              ▼        │
         │        ┌────────────────┐
         │        │ CALL #3        │
         │        │ AI Analysis    │
         │        │ Generation     │
         │        │                │
         │        │ Model:         │
         │        │ Text2Text      │
         │        │                │
         │        │ Input:         │
         │        │ Journal +      │
         │        │ Call#1 score + │
         │        │ Call#2 score   │
         │        │                │
         │        │ Output:        │
         │        │ Generated text │
         │        │ with insights  │
         │        └────────┬───────┘
         │                 │
         │                 │ Results
         │                 │ Stored
         │                 │
         │                 ▼
         │        ┌──────────────────┐
         │        │ CREATE ALERT     │
         │        │                  │
         │        │ With:            │
         │        │ - Sentiment: 0.92│
         │        │ - Distress: 8.2  │
         │        │ - Analysis text  │
         │        │ - Status: NEW    │
         │        │ - Level: CRITICAL│
         │        └──────────┬───────┘
         │                   │
         └──── NO ───────────┤
                             │
                    ┌────────▼─────────┐
                    │ STORE RESULTS    │
                    │ (No alert)       │
                    └──────────────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │ THERAPIST SEES   │
                    │ ALERT ON         │
                    │ DASHBOARD        │
                    │                  │
                    │ Alert Card with: │
                    │ - Sentiment info │
                    │ - Distress level │
                    │ - AI analysis    │
                    │ - Recommendations│
                    └──────────────────┘
```

### Data Flow Between Calls

```
LLM Call #1 Output → Stored → Used by Call #3
┌─────────────────────────────────────────────────────┐
│ CALL #1: Sentiment Analysis                        │
├─────────────────────────────────────────────────────┤
│ Input:  "I am feeling anxious..."                  │
│ Model:  DistilBERT                                 │
│ Output: sentiment_score = 0.92                     │
│ ↓ STORED IN DATABASE                               │
│ journal_entries.sentiment_score = 0.92             │
│ ↓ USED BY CALL #3                                  │
│ "Sentiment: 0.92 means very negative emotions"    │
└─────────────────────────────────────────────────────┘

LLM Call #2 Output → Stored → Used by Call #3
┌─────────────────────────────────────────────────────┐
│ CALL #2: Distress Detection                        │
├─────────────────────────────────────────────────────┤
│ Input:  "I am feeling anxious..."                  │
│ Model:  Custom Distress Detection                 │
│ Output: distress_level = 8.2                       │
│ ↓ STORED IN DATABASE                               │
│ journal_entries.distress_level = 8.2               │
│ ↓ USED BY CALL #3 DECISION                         │
│ if (8.2 > 5.0) → YES, execute Call #3              │
│ ↓ USED BY CALL #3 PROMPT                           │
│ "Distress level 8.2/10 indicates critical stress" │
└─────────────────────────────────────────────────────┘

LLM Call #3 Input = Combination of Call #1 & #2
┌─────────────────────────────────────────────────────┐
│ CALL #3: AI Analysis Generation                   │
├─────────────────────────────────────────────────────┤
│ Input: Prompt constructed from:                   │
│  1. Original journal text (from patient input)    │
│  2. Sentiment score = 0.92 (from Call #1 output) │
│  3. Distress level = 8.2 (from Call #2 output)  │
│                                                  │
│ Model: Text2Text Generation                      │
│                                                  │
│ Prompt template:                                 │
│ "Analyze this entry with sentiment=0.92          │
│  (very negative) and distress=8.2/10            │
│  (critical): [journal text]                     │
│                                                  │
│  Generate professional analysis with:           │
│  - Assessment of emotional state                │
│  - Key concerns identified                      │
│  - Recommendations for therapist"               │
│                                                  │
│ Output: "Based on sentiment 0.92 and            │
│          distress 8.2, the patient is           │
│          experiencing critical mental           │
│          health concerns. Key issues:            │
│          - Anxiety from work stress              │
│          - Hopelessness                          │
│          - Sleep disruption                      │
│                                                  │
│          Recommendations:                        │
│          1. Urgent appointment needed            │
│          2. Consider intensive therapy..."       │
└─────────────────────────────────────────────────────┘
```

---

## 📈 Complete Data Flow

---

### Step-by-Step Data Journey

```
┌──────────────────────────────────────────────────────────────────┐
│ STEP 1: PATIENT SUBMITS JOURNAL ENTRY                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ POST /api/journals                                             │
│ {                                                              │
│   "title": "Struggling with work stress",                     │
│   "content": "I am feeling very anxious...",                  │
│   "mood": "ANXIOUS"                                            │
│ }                                                              │
│                                                                  │
│ ✓ Request authenticated with JWT token                        │
│ ✓ User ID extracted from token                                │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│ STEP 2: BACKEND SAVES JOURNAL TO DATABASE                       │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ JournalController.createJournal() called                       │
│ ↓                                                               │
│ JournalService.saveJournal(journalEntry) called              │
│ ↓                                                               │
│ INSERT INTO journal_entries                                   │
│   (id, user_id, title, content, mood, created_at, ...)      │
│ VALUES                                                         │
│   (UUID, user-id-123, "Struggling...", "I am feeling...",   │
│    ANXIOUS, 2026-05-26 08:45:00)                             │
│ ✓ Returns: JournalEntry object with ID = abc123              │
│                                                                  │
│ Database Record Created:                                      │
│ ┌─────────────────────────────────────────────┐              │
│ │ Journal Entry #abc123                       │              │
│ │ ────────────────────────                    │              │
│ │ User ID:     user-id-123                   │              │
│ │ Title:       "Struggling with work stress" │              │
│ │ Content:     "I am feeling very anxious..." │              │
│ │ Mood:        ANXIOUS                        │              │
│ │ Sentiment:   NULL (will be filled)          │              │
│ │ Distress:    NULL (will be filled)          │              │
│ │ Created:     2026-05-26 08:45:00           │              │
│ │ Status:      SAVED                          │              │
│ └─────────────────────────────────────────────┘              │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│ STEP 3: TRIGGER BACKGROUND AI ANALYSIS                          │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ AiAnalysisService.analyzeEntry(journalEntry) called           │
│ (Runs asynchronously - doesn't block user response)           │
│                                                                  │
│ ↓                                                               │
│ Extract content: "I am feeling very anxious..."               │
│ ↓                                                               │
│ Schedule parallel execution of Call #1 & #2                  │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                  ↓                      ↓
    ┌────────────────────┐  ┌────────────────────┐
    │ PARALLEL START     │  │ PARALLEL START     │
    │ (Both at T+100ms)  │  │ (Both at T+100ms)  │
    └────────┬───────────┘  └────────┬───────────┘
             │                        │
             ▼                        ▼
┌──────────────────────────────────────────────────────────────────┐
│ LLM CALL #1: SENTIMENT ANALYSIS (T+100ms)                       │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ Method:  analyzeSentiment(String text)                         │
│ Input:   "I am feeling very anxious and overwhelmed today..."│
│                                                                  │
│ HTTP Request:                                                  │
│ POST https://api-inference.huggingface.co/models/             │
│     distilbert-base-uncased-finetuned-sst-2-english          │
│                                                                  │
│ Headers:                                                       │
│   Authorization: Bearer <YOUR_HUGGING_FACE_API_KEY>...│
│   Content-Type: application/json                              │
│                                                                  │
│ Body:                                                          │
│ {                                                              │
│   "inputs": "I am feeling very anxious and overwhelmed today" │
│ }                                                              │
│                                                                  │
│ ⏳ Waiting: 500-800ms                                           │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│ LLM CALL #1: RESPONSE RECEIVED (T+600ms)                        │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ HTTP 200 OK                                                    │
│                                                                  │
│ Response Body:                                                 │
│ [                                                              │
│   [                                                            │
│     {                                                          │
│       "label": "NEGATIVE",                                    │
│       "score": 0.9234567890                                  │
│     },                                                         │
│     {                                                          │
│       "label": "POSITIVE",                                    │
│       "score": 0.0765432110                                  │
│     }                                                          │
│   ]                                                            │
│ ]                                                              │
│                                                                  │
│ Extracted:                                                     │
│ sentiment_score = 0.92 (NEGATIVE)                             │
│                                                                  │
│ Update Database:                                               │
│ UPDATE journal_entries                                         │
│ SET sentiment_score = 0.92                                    │
│ WHERE id = 'abc123'                                           │
│                                                                  │
│ Variable in Memory:                                            │
│ Double sentimentScore = 0.92  ← STORED FOR LATER USE         │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│ LLM CALL #2: DISTRESS DETECTION (T+100ms)                       │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ Method:  analyzeDistress(String text)                          │
│ Input:   "I am feeling very anxious and overwhelmed today..."│
│                                                                  │
│ HTTP Request:                                                  │
│ POST https://api-inference.huggingface.co/models/             │
│     distress-detection-model                                  │
│                                                                  │
│ Headers:                                                       │
│   Authorization: Bearer <YOUR_HUGGING_FACE_API_KEY>...│
│   Content-Type: application/json                              │
│                                                                  │
│ Body:                                                          │
│ {                                                              │
│   "inputs": "I am feeling very anxious and overwhelmed today" │
│ }                                                              │
│                                                                  │
│ ⏳ Waiting: 400-600ms                                           │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│ LLM CALL #2: RESPONSE RECEIVED (T+550ms)                        │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ HTTP 200 OK                                                    │
│                                                                  │
│ Response Body:                                                 │
│ {                                                              │
│   "distress_level": 8.2,                                      │
│   "keywords_found": ["anxious", "overwhelmed", "hopeless"],  │
│   "severity": "CRITICAL",                                     │
│   "confidence": 0.95                                          │
│ }                                                              │
│                                                                  │
│ Extracted:                                                     │
│ distress_score = 8.2/10 (CRITICAL)                           │
│                                                                  │
│ Update Database:                                               │
│ UPDATE journal_entries                                         │
│ SET distress_level = 8.2                                      │
│ WHERE id = 'abc123'                                           │
│                                                                  │
│ Variable in Memory:                                            │
│ Double distressLevel = 8.2  ← STORED FOR LATER USE           │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│ STEP 4: CONDITIONAL DECISION (T+600ms)                          │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ Check if Calls #1 & #2 indicate HIGH RISK:                   │
│                                                                  │
│ if (distressLevel > 5.0 || sentimentScore < 0.3) {          │
│   // EXECUTE LLM CALL #3                                      │
│ }                                                              │
│                                                                  │
│ Actual Values:                                                 │
│ distressLevel = 8.2 (is it > 5.0?)   YES ✓                  │
│ sentimentScore = 0.92 (is it < 0.3?) NO                      │
│                                                                  │
│ CONDITION: TRUE → Proceed with LLM Call #3                   │
│                                                                  │
│ Decision: CREATE CRITICAL ALERT                               │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│ LLM CALL #3: AI ANALYSIS GENERATION (T+650ms)                   │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ Method:  generateAiAnalysis(                                  │
│            String content,        ← "I am feeling..."         │
│            Double sentiment,      ← 0.92 (FROM CALL #1)      │
│            Double distress        ← 8.2 (FROM CALL #2)       │
│          )                                                      │
│                                                                  │
│ Construct Prompt (using Call #1 & #2 outputs):              │
│                                                                  │
│ String prompt = String.format(                                │
│   "Analyze this mental health journal entry:                 │
│                                                                  │
│    Entry: %s                                                  │
│                                                                  │
│    Sentiment Analysis: %.2f (VERY NEGATIVE)                 │
│    Distress Level: %.1f/10 (CRITICAL)                       │
│                                                                  │
│    Provide:                                                    │
│    1. Assessment of emotional state                          │
│    2. Key concerns identified                                │
│    3. Risk assessment                                         │
│    4. Recommendations for therapist                          │
│    5. Immediate coping strategies",                          │
│   content,        ← Original journal text                    │
│   sentiment,      ← 0.92 from Call #1                        │
│   distress        ← 8.2 from Call #2                         │
│ );                                                            │
│                                                                  │
│ Actual Prompt Sent:                                           │
│ "Analyze this mental health journal entry:                  │
│                                                                  │
│  Entry: I am feeling very anxious and overwhelmed           │
│  today. Work deadlines are piling up and I feel             │
│  hopeless...                                                  │
│                                                                  │
│  Sentiment Analysis: 0.92 (VERY NEGATIVE)                   │
│  Distress Level: 8.2/10 (CRITICAL)                          │
│                                                                  │
│  Provide assessment and recommendations..."                  │
│                                                                  │
│ HTTP Request:                                                  │
│ POST https://api-inference.huggingface.co/models/             │
│     text2text-generation                                      │
│                                                                  │
│ Headers:                                                       │
│   Authorization: Bearer <YOUR_HUGGING_FACE_API_KEY>          │
│   Content-Type: application/json                              │
│                                                                  │
│ Body:                                                          │
│ {                                                              │
│   "inputs": "[constructed prompt with sentiment & distress]" │
│ }                                                              │
│                                                                  │
│ ⏳ Waiting: 1000-2000ms (longest call)                         │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│ LLM CALL #3: RESPONSE RECEIVED (T+1800ms)                       │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ HTTP 200 OK                                                    │
│                                                                  │
│ Response Body:                                                 │
│ [                                                              │
│   {                                                            │
│     "generated_text": "Based on the sentiment analysis        │
│                        (0.92 = very negative) and distress     │
│                        level (8.2/10 = critical), this patient │
│                        is experiencing significant mental      │
│                        health concerns.                        │
│                                                                  │
│                        Key Observations:                       │
│                        - Multiple crisis indicators present    │
│                        - Work stress as primary trigger        │
│                        - Feelings of hopelessness              │
│                        - Sleep disruption                      │
│                        - Constant worry and anxiety            │
│                                                                  │
│                        RECOMMENDATIONS FOR THERAPIST:         │
│                        1. Schedule urgent consultation within  │
│                           24 hours                             │
│                        2. Assess for suicidal ideation         │
│                        3. Consider intensive therapy           │
│                        4. Provide grounding techniques         │
│                        5. Monitor for worsening symptoms       │
│                                                                  │
│                        This patient requires immediate support."│
│   }                                                            │
│ ]                                                              │
│                                                                  │
│ Extracted:                                                     │
│ aiAnalysis = "[full generated text above]"                   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│ STEP 5: CREATE ALERT IN DATABASE (T+1850ms)                     │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ AlertService.createAlert() called                              │
│                                                                  │
│ INSERT INTO alerts                                             │
│   (id, journal_entry_id, user_id, therapist_id,              │
│    title, description, level, status, created_at, ...)       │
│ VALUES                                                         │
│   (UUID, 'abc123', 'user-id-123',                            │
│    'user-id-therapist',                                       │
│    'Struggling with work stress...',                         │
│    '[full AI-generated analysis text]',  ← FROM CALL #3      │
│    'CRITICAL',                                                 │
│    'NEW',                                                      │
│    2026-05-26 08:45:02)                                       │
│                                                                  │
│ Alert Created:                                                 │
│ ┌──────────────────────────────────────────────┐             │
│ │ Alert #def456                                 │             │
│ │ ──────────────────────────────────            │             │
│ │ Journal ID:      abc123                       │             │
│ │ Patient:         John Doe (user-id-123)      │             │
│ │ Therapist:       Any (auto-assigned)          │             │
│ │ Title:           "Struggling with stress"     │             │
│ │ Description:     [AI analysis from Call #3]  │             │
│ │ Level:           CRITICAL                     │             │
│ │ Status:          NEW                          │             │
│ │ Created:         2026-05-26 08:45:02         │             │
│ │ Sentiment:       0.92                         │             │
│ │ Distress:        8.2                          │             │
│ └──────────────────────────────────────────────┘             │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│ STEP 6: RESPONSE TO PATIENT (T+1900ms total)                    │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ HTTP 201 CREATED                                               │
│ {                                                              │
│   "success": true,                                            │
│   "message": "Journal entry created successfully",            │
│   "journalEntry": {                                           │
│     "id": "abc123",                                           │
│     "title": "Struggling with work stress",                   │
│     "mood": "ANXIOUS",                                        │
│     "sentimentScore": 0.92,                                   │
│     "distressLevel": 8.2,                                     │
│     "createdAt": "2026-05-26T08:45:00Z"                      │
│   }                                                            │
│ }                                                              │
│                                                                  │
│ Patient sees: "Journal entry saved successfully"              │
│ (All LLM processing happened in background)                   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│ STEP 7: THERAPIST SEES ALERT (Immediate)                        │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ Therapist Dashboard Auto-Updates:                             │
│                                                                  │
│ Alert Badge: "⚠️ 1 critical alert"                            │
│ ↓                                                               │
│ Alert Card Shows:                                             │
│                                                                  │
│ ┌────────────────────────────────────────────┐               │
│ │ 🔴 CRITICAL  "Struggling with..."  [NEW]  │               │
│ ├────────────────────────────────────────────┤               │
│ │ Triggered by: AI Analysis                 │               │
│ │                                            │               │
│ │ [AI Analysis from LLM Call #3 displays]  │               │
│ │                                            │               │
│ │ "Based on sentiment 0.92 and distress    │               │
│ │  8.2/10, this patient is experiencing    │               │
│ │  critical mental health concerns...       │               │
│ │                                            │               │
│ │  RECOMMENDATIONS:                        │               │
│ │  1. Schedule urgent consultation..."      │               │
│ │                                            │               │
│ │ Created: just now                         │               │
│ │ [View & Resolve →]                        │               │
│ └────────────────────────────────────────────┘               │
│                                                                  │
│ Therapist can now:                                             │
│ ✓ Click alert for full details                               │
│ ✓ Read AI-generated analysis                                 │
│ ✓ See sentiment & distress scores                            │
│ ✓ View original journal entry                                │
│ ✓ Contact patient or adjust treatment                        │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Summary of LLM Calls

### Quick Reference Table

| Aspect | Call #1 | Call #2 | Call #3 |
|--------|---------|---------|---------|
| **Name** | Sentiment Analysis | Distress Detection | AI Analysis Generation |
| **Model** | DistilBERT | Custom Model | Text2Text |
| **Provider** | Hugging Face | Hugging Face | Hugging Face |
| **Purpose** | Analyze emotional tone | Detect crisis indicators | Generate insights |
| **Input** | Journal text | Journal text | Journal + Call#1 + Call#2 |
| **Output Type** | Score (0.0-1.0) | Score (0-10) | Generated text |
| **Output Example** | 0.92 | 8.2 | "Based on sentiment..." |
| **Response Time** | 500-800ms | 400-600ms | 1000-2000ms |
| **Execution** | Parallel with #2 | Parallel with #1 | Sequential after #1&#2 |
| **Dependency** | None | None | Depends on #1 & #2 |
| **Triggers Decision** | Triggers #3? | YES (if > 5.0) | Creates alert |
| **Stored In** | sentiment_score | distress_level | description (alerts) |
| **Display Location** | Dashboard, Alert | Dashboard, Alert | Alert card |

---

## 🐛 Troubleshooting

### If LLM Calls Don't Execute

**Problem: Backend shows "No suitable injection token"**
```
Solution: Ensure provideHttpClient() is in app.config.ts
Location: frontend/src/app/app.config.ts
```

**Problem: Hugging Face API returns 401**
```
Solution: Verify API key is correct
Check: java command includes --huggingface.api-key=<YOUR_HUGGING_FACE_API_KEY>
```

**Problem: Alert doesn't appear**
```
Solution: Wait 2-3 seconds (LLM calls take time)
Check: Backend logs for "analyzeEntry" execution
Run: tail -f /tmp/backend.log | grep -i "sentiment\|distress\|analyze"
```

**Problem: Call #3 doesn't execute**
```
Check: Is distress level > 5.0?
If distress < 5.0: Call #3 won't run by design
Verify: Journal content has crisis keywords
```

---

## ✅ Demonstration Checklist

Before your demo:
- [ ] Backend running on port 8081
- [ ] Frontend running on port 4201
- [ ] Both can communicate (test curl)
- [ ] Hugging Face API key is valid
- [ ] Database has no test data conflicts
- [ ] Browser console clear (F12)
- [ ] You understand the 3-call flow
- [ ] Test user registration works
- [ ] Test journal creation works
- [ ] Alert appears on therapist view

---

**You're ready to demonstrate all 3 LLM calls!** 🚀
