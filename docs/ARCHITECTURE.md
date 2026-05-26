# Mind-Guard System Architecture

## High-Level Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        MIND-GUARD PLATFORM                      │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────┐         ┌──────────────────────────────┐
│   FRONTEND LAYER        │         │    BACKEND LAYER             │
│  (Angular + Material)   │         │  (Spring Boot REST API)      │
├─────────────────────────┤         ├──────────────────────────────┤
│ • Auth Module           │◄───────►│ • Auth Controller            │
│ • Journal Module        │ HTTPS   │ • Journal Service            │
│ • Dashboard Module      │ JWT     │ • Mood Service               │
│ • Mood Tracking        │         │ • Analysis Service           │
│ • Alert Management     │         │ • Alert Service              │
│ • Route Guards         │         │                              │
│ • HTTP Interceptors    │         │ ┌─────────────────────────┐  │
└─────────────────────────┘         │ │  Security Layer         │  │
       ↓                            │ ├─────────────────────────┤  │
  Browser Storage                   │ │ • JWT Validation        │  │
  (JWT Tokens)                      │ • Spring Security        │  │
                                    │ • AES Encryption        │  │
                                    │ • RBAC Filters          │  │
                                    │ └─────────────────────────┘  │
                                    │                              │
                                    │ ┌─────────────────────────┐  │
                                    │ │  Data Access Layer      │  │
                                    │ ├─────────────────────────┤  │
                                    │ │ • JPA/Hibernate         │  │
                                    │ │ • Repositories          │  │
                                    │ │ • Query Optimization    │  │
                                    │ └─────────────────────────┘  │
                                    │                              │
                                    │ ┌─────────────────────────┐  │
                                    │ │  AI Integration Layer   │  │
                                    │ ├─────────────────────────┤  │
                                    │ │ • HF API Client         │  │
                                    │ │ • Model Selection       │  │
                                    │ │ • Result Caching        │  │
                                    │ └─────────────────────────┘  │
└──────────────────────────────────┘
        ↓
┌─────────────────────────────────────┐
│   EXTERNAL SERVICES                 │
├─────────────────────────────────────┤
│ • Hugging Face Inference API        │
│   - Sentiment Analysis              │
│   - Distress Detection              │
│   - Text Summarization              │
│   - Risk Scoring                    │
└─────────────────────────────────────┘
        ↑
        └──────────────────────────────┐
                                       ↓
┌─────────────────────────────────────────────────────────┐
│        DATA PERSISTENCE LAYER                           │
├─────────────────────────────────────────────────────────┤
│ PostgreSQL Database                                     │
│ ├─ Users (Patients, Therapists, Admins)                │
│ ├─ Journal Entries (Encrypted)                         │
│ ├─ Mood Logs                                           │
│ ├─ Analysis Results (Cached)                           │
│ ├─ Alerts & Responses                                  │
│ └─ Therapist-Patient Assignments                       │
└─────────────────────────────────────────────────────────┘
```

## Component Architecture

### 1. Frontend Architecture (Angular)

#### Project Structure
```
frontend/src/
├── app/
│   ├── auth/                      # Authentication module
│   │   ├── components/
│   │   │   ├── login.component.ts
│   │   │   └── register.component.ts
│   │   ├── services/
│   │   │   └── auth.service.ts
│   │   ├── guards/
│   │   │   ├── auth.guard.ts
│   │   │   └── role.guard.ts
│   │   └── auth.module.ts
│   │
│   ├── journal/                   # Journal module
│   │   ├── components/
│   │   │   ├── journal-list.component.ts
│   │   │   ├── journal-form.component.ts
│   │   │   └── journal-detail.component.ts
│   │   ├── services/
│   │   │   └── journal.service.ts
│   │   └── journal.module.ts
│   │
│   ├── mood/                      # Mood tracking module
│   │   ├── components/
│   │   │   ├── mood-log.component.ts
│   │   │   └── mood-chart.component.ts
│   │   ├── services/
│   │   │   └── mood.service.ts
│   │   └── mood.module.ts
│   │
│   ├── dashboard/                 # Therapist dashboard
│   │   ├── components/
│   │   │   ├── patient-list.component.ts
│   │   │   ├── patient-summary.component.ts
│   │   │   └── alert-panel.component.ts
│   │   ├── services/
│   │   │   └── therapist.service.ts
│   │   └── dashboard.module.ts
│   │
│   ├── alerts/                    # Alert management
│   │   ├── components/
│   │   │   ├── alert-list.component.ts
│   │   │   └── alert-detail.component.ts
│   │   ├── services/
│   │   │   └── alert.service.ts
│   │   └── alerts.module.ts
│   │
│   ├── shared/                    # Shared components
│   │   ├── components/
│   │   │   ├── header.component.ts
│   │   │   ├── sidebar.component.ts
│   │   │   └── loader.component.ts
│   │   ├── services/
│   │   │   └── shared.service.ts
│   │   └── shared.module.ts
│   │
│   ├── interceptors/
│   │   ├── auth.interceptor.ts    # Adds JWT to requests
│   │   ├── error.interceptor.ts   # Handles errors & 401s
│   │   └── loading.interceptor.ts # Shows/hides loader
│   │
│   ├── app-routing.module.ts
│   └── app.component.ts
│
├── assets/                        # Static assets
├── environments/
│   ├── environment.ts
│   └── environment.prod.ts
├── styles.scss                    # Global styles
└── main.ts
```

#### Key Angular Services

**AuthService** - Authentication management
```typescript
- login(credentials): Observable<AuthResponse>
- register(userData): Observable<User>
- logout(): void
- refreshToken(): Observable<AuthResponse>
- getCurrentUser(): User | null
- hasRole(role): boolean
```

**JournalService** - Journal operations
```typescript
- createEntry(entry): Observable<JournalEntry>
- getEntries(page, size): Observable<PaginatedResponse>
- getEntryById(id): Observable<JournalEntry>
- updateEntry(id, data): Observable<JournalEntry>
- deleteEntry(id): Observable<void>
```

**MoodService** - Mood tracking
```typescript
- logMood(moodData): Observable<MoodLog>
- getMoodHistory(days): Observable<MoodLog[]>
- getMoodSummary(days): Observable<MoodSummary>
```

**AlertService** - Alert management
```typescript
- getAlerts(filters): Observable<PaginatedResponse>
- acknowledgeAlert(id): Observable<void>
- respondToAlert(id, response): Observable<AlertResponse>
```

#### HTTP Interceptors

**AuthInterceptor**:
- Adds JWT token to Authorization header
- Handles token refresh on 401
- Clears token on logout

**ErrorInterceptor**:
- Intercepts HTTP errors
- Shows error notifications
- Handles specific error codes
- Logs errors for debugging

**LoadingInterceptor**:
- Tracks pending requests
- Shows loading spinner
- Hides spinner when complete

### 2. Backend Architecture (Spring Boot)

#### Package Structure
```
src/main/java/com/mindguard/
├── config/
│   ├── SecurityConfig.java        # Spring Security configuration
│   ├── JwtConfig.java             # JWT settings
│   ├── EncryptionConfig.java      # Encryption bean setup
│   ├── CorsConfig.java            # CORS configuration
│   └── WebConfig.java             # Web MVC configuration
│
├── controller/
│   ├── AuthController.java        # Login, register, refresh
│   ├── JournalController.java     # Journal CRUD operations
│   ├── MoodController.java        # Mood tracking endpoints
│   ├── AnalysisController.java    # AI analysis endpoints
│   ├── AlertController.java       # Alert management
│   ├── TherapistController.java   # Therapist dashboard
│   └── AdminController.java       # Admin endpoints
│
├── service/
│   ├── AuthService.java           # Authentication logic
│   ├── UserService.java           # User management
│   ├── JournalService.java        # Journal business logic
│   ├── MoodService.java           # Mood tracking logic
│   ├── AnalysisService.java       # AI analysis orchestration
│   ├── AlertService.java          # Alert management logic
│   ├── TherapistService.java      # Therapist operations
│   └── EmailService.java          # Email notifications
│
├── repository/
│   ├── UserRepository.java        # JPA repository for users
│   ├── JournalRepository.java     # Journal queries
│   ├── MoodRepository.java        # Mood queries
│   ├── AnalysisRepository.java    # Analysis caching
│   ├── AlertRepository.java       # Alert queries
│   └── TherapistPatientRepository.java
│
├── entity/
│   ├── User.java                  # User entity (Patient/Therapist/Admin)
│   ├── JournalEntry.java          # Journal entry entity
│   ├── MoodLog.java               # Mood log entity
│   ├── JournalAnalysis.java       # AI analysis results
│   ├── Alert.java                 # Alert entity
│   ├── AlertResponse.java         # Therapist responses
│   └── TherapistPatient.java      # Relationship mapping
│
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── JournalEntryRequest.java
│   │   ├── MoodLogRequest.java
│   │   └── AlertResponseRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── JournalEntryResponse.java
│       ├── AnalysisResponse.java
│       ├── AlertResponse.java
│       └── PaginatedResponse.java
│
├── exception/
│   ├── MindGuardException.java    # Base custom exception
│   ├── UnauthorizedException.java # 401 errors
│   ├── ForbiddenException.java    # 403 errors
│   ├── NotFoundException.java     # 404 errors
│   ├── ValidationException.java   # 422 errors
│   └── GlobalExceptionHandler.java # Exception mapper
│
├── security/
│   ├── JwtTokenProvider.java      # JWT token generation/validation
│   ├── JwtAuthenticationFilter.java # JWT request filter
│   ├── JwtAuthenticationEntryPoint.java
│   ├── UserDetailsService.java    # Custom user details
│   └── CustomAuthenticationProvider.java
│
├── workflow/
│   ├── AnalysisWorkflow.java      # AI analysis orchestration
│   ├── AlertEscalationWorkflow.java # Alert decision logic
│   └── NotificationWorkflow.java  # Alert notifications
│
├── client/
│   └── HuggingFaceClient.java     # REST client for HF API
│
├── util/
│   ├── EncryptionUtil.java        # AES encryption/decryption
│   ├── DataMaskingUtil.java       # PII masking for logs
│   ├── DateTimeUtil.java          # Date utilities
│   └── ValidationUtil.java        # Common validation
│
├── validation/
│   ├── EmailValidator.java        # Email validation annotation
│   ├── PasswordValidator.java     # Password strength validation
│   └── FileValidator.java         # File upload validation
│
└── MindGuardApplication.java
```

#### Key Service Methods

**AuthService**:
```java
- authenticate(email, password): AuthResponse
- register(registerRequest): User
- refreshAccessToken(refreshToken): String
- validateToken(token): boolean
- extractUserIdFromToken(token): UUID
```

**AnalysisService**:
```java
- analyzeJournalEntry(journalEntryId): JournalAnalysis
- performSentimentAnalysis(content): SentimentResult
- calculateRiskScore(analysis): Integer
- generateWellnessGuidance(content): String
- shouldCreateAlert(riskScore): boolean
```

**AlertService**:
```java
- createAlert(journalEntry, analysis): Alert
- acknowledgeAlert(alertId, userId): void
- respondToAlert(alertId, response): AlertResponse
- getAlertsForTherapist(therapistId, filters): Page<Alert>
- escalateAlert(alertId): void
```

### 3. Database Schema

#### Core Entities

**Users Table**
```sql
- id (UUID, PK)
- username (VARCHAR, UNIQUE)
- email (VARCHAR, UNIQUE)
- password_hash (VARCHAR)
- first_name, last_name
- role (ENUM: PATIENT, THERAPIST, ADMIN)
- is_active (BOOLEAN)
- is_email_verified (BOOLEAN)
- created_at, updated_at (TIMESTAMP)
- last_login_at (TIMESTAMP)
```

**JournalEntries Table**
```sql
- id (UUID, PK)
- user_id (UUID, FK to Users)
- title (VARCHAR)
- content (TEXT) - plaintext for search/display
- content_encrypted (BYTEA) - encrypted content
- is_shared_with_therapist (BOOLEAN)
- mood_before, mood_after (VARCHAR)
- tags (JSONB array)
- created_at, updated_at (TIMESTAMP)
```

**JournalAnalysis Table**
```sql
- id (UUID, PK)
- journal_entry_id (UUID, FK)
- sentiment (ENUM: POSITIVE, NEUTRAL, NEGATIVE)
- sentiment_score (DECIMAL)
- distress_level (ENUM: LOW, MEDIUM, HIGH)
- risk_score (DECIMAL 0-100)
- self_harm_indicators (BOOLEAN)
- wellness_guidance (TEXT)
- analyzed_at (TIMESTAMP)
```

**Alerts Table**
```sql
- id (UUID, PK)
- journal_entry_id (UUID, FK)
- therapist_id (UUID, FK)
- patient_id (UUID, FK)
- alert_type (ENUM: EMERGENCY, URGENT, STANDARD)
- risk_score (DECIMAL)
- is_acknowledged (BOOLEAN)
- is_resolved (BOOLEAN)
- created_at, updated_at (TIMESTAMP)
```

**MoodLogs Table**
```sql
- id (UUID, PK)
- user_id (UUID, FK)
- mood_level (INTEGER 1-10)
- mood_category (VARCHAR)
- energy_level (INTEGER 1-10)
- notes (TEXT)
- created_at (TIMESTAMP)
```

**TherapistPatients Table**
```sql
- id (UUID, PK)
- therapist_id (UUID, FK)
- patient_id (UUID, FK)
- assigned_at (TIMESTAMP)
- notes (TEXT)
- is_active (BOOLEAN)
```

### 4. AI Workflow Orchestration

#### AnalysisWorkflow Process

```
User Creates Journal Entry
        ↓
JournalService.createEntry()
        ↓
[EVENT] JournalCreatedEvent
        ↓
AnalysisWorkflow.onJournalCreated()
        ├─► HuggingFace Call 1: Sentiment Analysis
        │   └─ Returns: sentiment, sentiment_score
        │
        ├─► HuggingFace Call 2: Zero-Shot Classification
        │   └─ Returns: distress_level, self_harm_indicators
        │
        └─► Calculate Risk Score (Algorithm)
            └─ Input: sentiment, distress, self_harm indicators
            └─ Output: risk_score (0-100)

Risk Score Decision Logic
        ├─ If risk_score > 70
        │  └─ AlertEscalationWorkflow.createEmergencyAlert()
        │     ├─ Create EMERGENCY alert in DB
        │     ├─ HuggingFace Call 3: Generate Therapist Summary
        │     └─ Send real-time notification to assigned therapist
        │
        └─ If risk_score < 70
           ├─ HuggingFace Call 2: Generate Wellness Guidance
           └─ Store as analysis recommendation
```

#### HuggingFace API Calls

**Call 1: Sentiment Analysis**
```
Model: distilbert-base-uncased-finetuned-sst-2-english
Input: Journal entry text
Output: 
{
  "labels": ["NEGATIVE", "POSITIVE"],
  "scores": [0.8, 0.2]
}
Result: NEGATIVE sentiment with 0.8 confidence
```

**Call 2: Zero-Shot Classification (Distress/Self-Harm)**
```
Model: facebook/bart-large-mnli
Input: Journal text
Candidate Labels: ["high distress", "medium distress", "low distress", "mentions self-harm", "no concerning indicators"]
Output: Most likely labels with confidence scores
```

**Call 3: Text Summarization (Wellness Guidance)**
```
Model: facebook/bart-large-cnn
Input: Journal text + sentiment analysis results
Output: 2-3 sentence wellness guidance specific to patient's entry
```

**Risk Scoring Algorithm**
```java
riskScore = (
    sentimentWeight * sentimentScore +        // 40%
    distressWeight * distressScore +          // 40%
    selfHarmWeight * selfHarmIndicators * 100 // 20%
) * 100

sentimentScore: 1.0 (NEGATIVE) to 0.0 (POSITIVE)
distressScore: 1.0 (HIGH) to 0.0 (LOW)
selfHarmWeight multiplier: 1.0 if true, 0.0 if false

Range: 0-100
- 0-30: LOW RISK
- 31-70: MEDIUM RISK
- 71-100: HIGH RISK (EMERGENCY)
```

### 5. Security Architecture

#### Authentication Flow
```
1. User submits credentials → LoginRequest
2. AuthService.authenticate()
   ├─ Validate password (bcrypt)
   ├─ Generate JWT (access + refresh)
   └─ Return AuthResponse with tokens

3. Frontend stores token in localStorage
4. Each request includes token in Authorization header
5. JwtAuthenticationFilter validates token
6. Spring Security context set with user details
7. @PreAuthorize annotations check permissions
```

#### Encryption Strategy
```
Sensitive Data (PII, Journal Content):
├─ At Rest: AES-256 GCM encryption
│  ├─ IV: Randomly generated for each encryption
│  ├─ Key: ENCRYPTION_KEY (32+ chars) from environment
│  └─ Storage: BYTEA column in PostgreSQL
│
└─ In Transit: HTTPS/TLS 1.2+
   └─ All API calls encrypted

Password Storage:
├─ Algorithm: bcrypt
├─ Salt rounds: 12
└─ Never transmitted or logged
```

#### Data Masking Strategy
```java
For Audit Logs:
- User identifiers: masked to "***.***"
- Email addresses: masked to "user***@***.com"
- Journal content summaries: limited to first 50 chars
- Timestamps preserved for forensics
```

## Data Flow Diagrams

### Journal Entry Creation & Analysis Flow

```
┌─ Patient App
│  └─ Creates journal entry
│     └─ HTTP POST /api/journals
│
└─ Backend API
   ├─ JwtAuthenticationFilter validates token
   ├─ JournalController.createEntry()
   ├─ JournalService.createEntry()
   │  ├─ Encrypt content (AES-256)
   │  ├─ Store in PostgreSQL
   │  └─ Publish JournalCreatedEvent
   │
   ├─ @EventListener onJournalCreated()
   ├─ AnalysisWorkflow.startAnalysis()
   │
   ├─ HuggingFace API Call 1 (Sentiment)
   ├─ HuggingFace API Call 2 (Distress Detection)
   ├─ AnalysisWorkflow.calculateRiskScore()
   │
   ├─ IF risk_score > 70
   │  ├─ AlertService.createAlert()
   │  ├─ HuggingFace API Call 3 (Summary)
   │  ├─ Send WebSocket notification to therapist
   │  └─ Store in Alerts table
   │
   └─ Response to patient with analysis metadata
```

### Therapist Alert Response Flow

```
┌─ Therapist Dashboard
│  └─ Views emergency alert
│     └─ Clicks "Respond to Alert"
│        └─ HTTP POST /api/alerts/{id}/response
│
└─ Backend API
   ├─ JwtAuthenticationFilter validates therapist JWT
   ├─ @PreAuthorize("hasRole('THERAPIST')")
   ├─ AlertController.respondToAlert()
   ├─ AlertService.addResponse()
   │  ├─ Create AlertResponse record
   │  ├─ Update Alert.is_acknowledged
   │  └─ Send notification back to patient
   │
   └─ Update patient's alert status in real-time
      └─ Patient sees therapist has responded
```

## Technology Stack Details

### Frontend Stack
- **Angular 18**: Latest stable version with Ivy compiler
- **RxJS**: Reactive programming with Observables
- **Angular Material**: Pre-built accessible components
- **Reactive Forms**: Type-safe form handling
- **Router**: Client-side routing with guards
- **HttpClient**: HTTP requests with interceptors
- **jwt-decode**: Token payload extraction

### Backend Stack
- **Spring Boot 3.3**: Latest stable version
- **Spring Security 6**: Authentication & authorization
- **Spring Data JPA**: ORM with Hibernate
- **PostgreSQL 14**: Production-grade relational database
- **JJWT**: JWT library for token management
- **Lombok**: Reduce boilerplate code
- **Validation API**: Input validation annotations

### Database
- **PostgreSQL**: ACID compliance, JSON support
- **Connection Pooling**: HikariCP (20 max connections)
- **Encryption**: PostgreSQL full-disk encryption (optional)
- **Backups**: Automated daily dumps with retention

### External APIs
- **Hugging Face Inference API**: Only external AI service
- **No Docker/Local Models**: Keep deployment simple

## Performance Considerations

### Caching Strategy
```java
// Analysis results cached for 24 hours
@Cacheable(value = "journalAnalysis", key = "#journalId")
public JournalAnalysis getAnalysis(UUID journalId) { ... }

// User details cached during request
SecurityContextHolder.getContext().getAuthentication()

// Mood statistics cached for 1 hour
@Cacheable(value = "moodStats", key = "#userId")
public MoodSummary getMoodSummary(UUID userId) { ... }
```

### Database Optimization
- Indexes on: user_id, created_at, role, is_active
- Pagination enforced for list endpoints (max 100 items)
- Lazy loading for JPA relationships
- Connection pool: 20 max, 5 min idle

### API Rate Limiting
```
Unauthenticated: 10 req/min
Authenticated: 100 req/min
File uploads: 5 req/min
```

---

**Last Updated**: 2026-05-25
**Version**: 1.0.0
