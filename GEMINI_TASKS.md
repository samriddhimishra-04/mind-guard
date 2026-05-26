# Mind-Guard Platform - Development Tasks for Gemini Agent

## Overview
This is a mental health platform with Angular 18 frontend and Spring Boot 3.3 backend. Core authentication, journal entries, mood tracking, AI analysis, and alerts are complete. Below are remaining tasks organized by priority.

---

## PHASE 1: DATABASE & BACKEND FOUNDATION (High Priority)

### Task 1.1: Database Schema Updates
**File:** `backend/src/main/resources/init.sql`
**Status:** Not Started
**Details:**
- Add `alerts` table with relationships to users and journal_entries
- Add `therapist_patients` junction table for therapist-patient assignments
- Create indexes for efficient alert queries:
  * Index on alerts(user_id, status, created_at)
  * Index on alerts(assigned_therapist_id, status)
  * Index on journal_entries(user_id, distress_level)
- Add trigger to auto-update alert updated_at timestamp
- Verify all foreign keys and constraints

**Expected Output:** Updated init.sql with all table definitions, indexes, and sample data

---

### Task 1.2: Create Therapist-Patient Management Backend
**Files to Create:**
- `backend/src/main/java/com/mindguard/entity/TherapistPatient.java`
- `backend/src/main/java/com/mindguard/repository/TherapistPatientRepository.java`
- `backend/src/main/java/com/mindguard/service/TherapistService.java`

**Implementation Details:**
1. **TherapistPatient Entity:**
   - id (UUID, primary key)
   - therapistId (UUID, foreign key to users table where role=THERAPIST)
   - patientId (UUID, foreign key to users table where role=PATIENT)
   - assignedAt (LocalDateTime, auto-timestamp)
   - notes (String, optional)
   - isActive (Boolean, default true)

2. **TherapistPatientRepository:**
   - findByTherapistId(UUID) - list all patients for a therapist
   - findByPatientId(UUID) - list all therapists for a patient
   - existsByTherapistIdAndPatientId(UUID, UUID) - check assignment exists
   - findActiveByTherapistId(UUID) - only active assignments

3. **TherapistService:**
   - assignPatient(therapistId, patientId, notes) - create assignment
   - unassignPatient(therapistId, patientId) - remove assignment
   - getTherapistPatients(therapistId) - list with pagination
   - getPatientTherapists(patientId) - list therapist assignments
   - updateAssignmentNotes(therapistId, patientId, notes)

**Expected Output:** 3 new files with full CRUD functionality and custom queries

---

### Task 1.3: Create Notification Entity & Service
**Files to Create:**
- `backend/src/main/java/com/mindguard/entity/Notification.java`
- `backend/src/main/java/com/mindguard/repository/NotificationRepository.java`
- `backend/src/main/java/com/mindguard/service/NotificationService.java`

**Implementation Details:**
1. **Notification Entity:**
   - id (UUID)
   - userId (UUID, foreign key)
   - type (Enum: ALERT, ENTRY_FLAGGED, THERAPY_MESSAGE)
   - message (String, required)
   - relatedEntityId (UUID, nullable - reference to alert/entry)
   - isRead (Boolean, default false)
   - deliveryMethod (Enum: IN_APP, EMAIL - optional for MVP)
   - createdAt (auto-timestamp)
   - readAt (LocalDateTime, nullable)

2. **NotificationRepository:**
   - findByUserIdOrderByCreatedAtDesc(UUID)
   - findByUserIdAndIsReadFalse(UUID) - unread only
   - findByUserIdAndType(UUID, NotificationType)

3. **NotificationService:**
   - createNotification(userId, type, message, relatedEntityId)
   - markAsRead(userId, notificationId)
   - markAllAsRead(userId)
   - getUserNotifications(userId) - return DTOs
   - deleteNotification(userId, notificationId)
   - triggerAlertNotification(alert) - called from AlertService

**Expected Output:** 3 new files, integration hook in AlertService

---

## PHASE 2: THERAPIST BACKEND ENDPOINTS (High Priority)

### Task 2.1: Create Therapist Controller
**File:** `backend/src/main/java/com/mindguard/controller/TherapistController.java`

**Endpoints:**
```
GET    /api/therapists/patients
       - Get all assigned patients for logged-in therapist
       - Query params: page, size (pagination)
       - Returns: List of PatientDto with last interaction date

GET    /api/therapists/patients/{patientId}/entries
       - Get recent journal entries for a specific patient
       - Query params: limit=10, offset=0
       - Returns: List of JournalEntryResponse (without sensitive AI data)

GET    /api/therapists/patients/{patientId}/moods
       - Get mood logs for patient over date range
       - Query params: startDate, endDate (ISO format)
       - Returns: List of MoodLogResponse

GET    /api/therapists/alerts
       - Get all alerts for patients assigned to therapist
       - Query params: status, patientId (optional filters)
       - Returns: List of AlertResponse with patient info

GET    /api/therapists/alerts/{alertId}
       - Get specific alert with full context and patient history
       - Returns: AlertDetailDto (includes patient, entry, analysis)

POST   /api/therapists/patients/{patientId}/assign
       - Assign a patient to therapist (with optional notes)
       - Body: { notes: string }
       - Returns: TherapistPatientDto

DELETE /api/therapists/patients/{patientId}/unassign
       - Remove patient assignment

PUT    /api/therapists/alerts/{alertId}/resolve
       - Mark alert as resolved with therapist notes
       - Body: { resolutionNotes: string, recommendation: string }
       - Returns: AlertResponse with updated status
```

**Authorization:** All endpoints require THERAPIST role

**Expected Output:** Complete TherapistController with proper error handling and logging

---

## PHASE 3: FRONTEND - THERAPIST DASHBOARD (Medium-High Priority)

### Task 3.1: Create Therapist Dashboard Component
**Files to Create:**
- `frontend/src/app/components/therapist-dashboard/therapist-dashboard.component.ts`
- `frontend/src/app/components/therapist-dashboard/therapist-dashboard.component.html`
- `frontend/src/app/components/therapist-dashboard/therapist-dashboard.component.scss`

**Features:**
1. Header with therapist name and logout
2. Main navigation tabs:
   - Patients (list of assigned patients)
   - Alerts (critical alerts across all patients)
   - Dashboard (overview stats)

3. Patient List Section:
   - Table/list of assigned patients with:
     * Patient name and ID
     * Last interaction date
     * Active alerts count (with color coding)
     * Action buttons (View, Manage)
   - Search/filter functionality by patient name
   - Sorting options

4. Alerts Section:
   - List of unresolved alerts from all assigned patients
   - Priority colors (critical/high/medium/low)
   - Alert cards showing:
     * Patient name
     * Alert level and description
     * Journal entry title (link to view)
     * Time ago
     * Status badge
   - Bulk actions (acknowledge, resolve)

**Expected Output:** Responsive component with routing

---

### Task 3.2: Create Patient Detail Component
**Files to Create:**
- `frontend/src/app/components/therapist-dashboard/patient-detail/patient-detail.component.ts`
- `frontend/src/app/components/therapist-dashboard/patient-detail/patient-detail.component.html`
- `frontend/src/app/components/therapist-dashboard/patient-detail/patient-detail.component.scss`

**Features:**
1. Patient header with:
   - Patient name, email, assigned date
   - Contact button (placeholder)
   - Unassign button with confirmation

2. Tabs:
   - **Overview:** Key stats (total entries, avg mood, alerts count)
   - **Journal Entries:** List of recent entries with:
     * Title, date, mood
     * Flagged status if high distress
     * Quick view modal
   - **Mood Logs:** Recent moods with mini chart
   - **Alerts:** Alerts specific to this patient with ability to view and resolve
   - **Notes:** Therapist's personal notes about patient

3. Quick Actions:
   - View journal entry (opens modal)
   - Review alert (opens modal with resolution form)
   - Edit personal notes

**Expected Output:** Complete patient detail view with all tabs functional

---

### Task 3.3: Create Alert Resolution Modal Component
**Files to Create:**
- `frontend/src/app/components/alert-resolution-modal/alert-resolution-modal.component.ts`
- `frontend/src/app/components/alert-resolution-modal/alert-resolution-modal.component.html`
- `frontend/src/app/components/alert-resolution-modal/alert-resolution-modal.component.scss`

**Features:**
1. Modal showing full alert context:
   - Alert level, description, triggered by
   - Related journal entry (full text)
   - Previous resolutions (if any)

2. Form fields:
   - Resolution notes (textarea)
   - Recommendation for patient (dropdown):
     * "Continue current treatment"
     * "Increase session frequency"
     * "Consider medication review"
     * "Escalate to specialist"
   - Follow-up date (date picker, optional)

3. Actions:
   - Save and Mark as Resolved button
   - Cancel button

**Expected Output:** Modal component with form validation

---

## PHASE 4: FRONTEND - ENHANCED JOURNAL FEATURES (Medium Priority)

### Task 4.1: Create Journal Entry Form Component
**Files to Create:**
- `frontend/src/app/components/journal/journal-entry-form/journal-entry-form.component.ts`
- `frontend/src/app/components/journal/journal-entry-form/journal-entry-form.component.html`
- `frontend/src/app/components/journal/journal-entry-form/journal-entry-form.component.scss`

**Features:**
1. Form fields:
   - Title (required, max 500 chars)
   - Content (required, min 10 chars) - textarea or rich text editor
   - Mood selector (visual emoji buttons)
   - Tags input (with autocomplete from previous tags)

2. Layout:
   - Left side: form fields
   - Right side: preview pane showing formatted entry

3. Actions:
   - Save Draft button
   - Publish button (triggers AI analysis)
   - Cancel button

4. Features:
   - Auto-save to draft every 30 seconds
   - Character counter
   - Form validation with error messages
   - Loading state during submission

**Expected Output:** Fully functional form component with service integration

---

### Task 4.2: Create Journal Entry Detail Component
**Files to Create:**
- `frontend/src/app/components/journal/journal-entry-detail/journal-entry-detail.component.ts`
- `frontend/src/app/components/journal/journal-entry-detail/journal-entry-detail.component.html`
- `frontend/src/app/components/journal/journal-entry-detail/journal-entry-detail.component.scss`

**Features:**
1. Full entry display with:
   - Title and creation date
   - Entry content (formatted)
   - Mood indicator with emoji
   - Tags displayed as chips

2. AI Analysis Section:
   - Sentiment score (visual gauge)
   - Distress level (color-coded)
   - AI analysis summary
   - Alert banner if flagged (with link to alert)

3. Edit/Delete Actions:
   - Edit button (opens form)
   - Delete button with confirmation

4. Responsive layout:
   - Mobile-optimized
   - Full screen option

**Expected Output:** Detail view component

---

## PHASE 5: FRONTEND - MOOD ANALYTICS (Medium Priority)

### Task 5.1: Create Mood Chart Component
**Files to Create:**
- `frontend/src/app/components/mood/mood-chart/mood-chart.component.ts`
- `frontend/src/app/components/mood/mood-chart/mood-chart.component.html`
- `frontend/src/app/components/mood/mood-chart/mood-chart.component.scss`

**Dependencies:** Install `ng2-charts` and `chart.js`

**Features:**
1. Chart Options:
   - Line chart: Intensity over time (7 days, 30 days, 90 days)
   - Bar chart: Mood frequency distribution
   - Pie chart: Mood type breakdown

2. Data Filtering:
   - Date range selector
   - Mood type filter

3. Statistics:
   - Most frequent mood
   - Average intensity
   - Trend indicator (improving/stable/declining)

**Expected Output:** Functional chart component with multiple chart types

---

### Task 5.2: Create Mood Statistics Dashboard
**Files to Create:**
- `frontend/src/app/components/mood/mood-statistics/mood-statistics.component.ts`
- `frontend/src/app/components/mood/mood-statistics/mood-statistics.component.html`
- `frontend/src/app/components/mood/mood-statistics/mood-statistics.component.scss`

**Features:**
1. Key Metrics Cards:
   - Total mood logs
   - Current mood streak
   - Highest intensity recorded
   - Most common mood

2. Comparison:
   - Week-over-week trend
   - Month-over-month trend

3. Insights Section:
   - Mood patterns (best time of day, best/worst day)
   - Trigger analysis (most common triggers)

**Expected Output:** Statistics dashboard component

---

## PHASE 6: FRONTEND - ALERT MANAGEMENT (Medium Priority)

### Task 6.1: Create Alert List Component
**Files to Create:**
- `frontend/src/app/components/alert/alert-list/alert-list.component.ts`
- `frontend/src/app/components/alert/alert-list/alert-list.component.html`
- `frontend/src/app/components/alert/alert-list/alert-list.component.scss`

**Features:**
1. Alert List:
   - Display all user's alerts with filters and sorting
   - Color-coded by severity
   - Status badges (new, reviewed, acknowledged, resolved)
   - Related journal entry link

2. Filters:
   - By status (dropdown)
   - By level (critical, high, medium, low)
   - By date range (date picker)

3. Actions per alert:
   - View details (modal)
   - Mark as reviewed
   - Acknowledge
   - Delete

4. Critical Alerts Banner:
   - Always visible at top if critical alerts exist
   - Quick dismiss option

**Expected Output:** Alert management component

---

### Task 6.2: Create Alert Detail Modal Component
**Files to Create:**
- `frontend/src/app/components/alert/alert-detail-modal/alert-detail-modal.component.ts`
- `frontend/src/app/components/alert/alert-detail-modal/alert-detail-modal.component.html`
- `frontend/src/app/components/alert/alert-detail-modal/alert-detail-modal.component.scss`

**Features:**
1. Display:
   - Full alert description
   - Alert level with color
   - Triggered by information
   - Related journal entry (inline or linked)
   - AI analysis if available
   - Created date and therapist info (if assigned)

2. Actions:
   - Mark as Viewed
   - Acknowledge button
   - Share with therapist button (if not already assigned)
   - Close modal

**Expected Output:** Modal component for alert details

---

## PHASE 7: FRONTEND - USER PROFILE (Lower Priority)

### Task 7.1: Create User Profile Component
**Files to Create:**
- `frontend/src/app/components/user-profile/user-profile.component.ts`
- `frontend/src/app/components/user-profile/user-profile.component.html`
- `frontend/src/app/components/user-profile/user-profile.component.scss`

**Features:**
1. Profile Information:
   - Name, email, role
   - Member since date
   - Profile picture (upload placeholder)
   - Bio/About section

2. Settings Tabs:
   - **Account:** Email, username update
   - **Security:** Password change form
   - **Preferences:** Notification settings, privacy controls
   - **Data:** Export data, GDPR options

3. Password Change Form:
   - Current password (required)
   - New password (with validation)
   - Confirm password
   - Show/hide toggle

**Expected Output:** Profile management component

---

## PHASE 8: FRONTEND - NAVIGATION & UI IMPROVEMENTS (Lower Priority)

### Task 8.1: Create Sidebar Navigation Component
**Files to Create:**
- `frontend/src/app/components/layout/sidebar/sidebar.component.ts`
- `frontend/src/app/components/layout/sidebar/sidebar.component.html`
- `frontend/src/app/components/layout/sidebar/sidebar.component.scss`

**Features:**
1. Navigation Menu:
   - Dashboard
   - Journal
   - Mood Tracker
   - Alerts
   - Profile
   - Logout

2. Responsive:
   - Collapsible on mobile
   - Always visible on desktop
   - Hamburger menu toggle

3. Active Route Highlighting:
   - Visual indicator of current page

**Expected Output:** Responsive navigation component

---

### Task 8.2: Create Toast Notification System
**Files to Create:**
- `frontend/src/app/services/toast.service.ts`
- `frontend/src/app/components/layout/toast-container/toast-container.component.ts`
- `frontend/src/app/components/layout/toast-container/toast-container.component.html`
- `frontend/src/app/components/layout/toast-container/toast-container.component.scss`

**Features:**
1. Toast Service:
   - success(message, duration?)
   - error(message, duration?)
   - warning(message, duration?)
   - info(message, duration?)

2. Toast Container:
   - Display multiple toasts simultaneously
   - Auto-dismiss after duration
   - Manual close button
   - Stack from bottom-right

3. Integration:
   - Use in all forms after submit
   - Display API errors
   - Show action confirmations

**Expected Output:** Complete toast notification system

---

## PHASE 9: TESTING (High Priority)

### Task 9.1: Backend Unit Tests
**Files to Create:**
- `backend/src/test/java/com/mindguard/service/AuthServiceTest.java`
- `backend/src/test/java/com/mindguard/service/JournalServiceTest.java`
- `backend/src/test/java/com/mindguard/service/MoodServiceTest.java`
- `backend/src/test/java/com/mindguard/service/AlertServiceTest.java`
- `backend/src/test/java/com/mindguard/service/TherapistServiceTest.java`

**Test Coverage:**
- Positive cases (happy path)
- Negative cases (errors, validations)
- Edge cases (null values, empty lists)
- Authorization checks (user isolation)

**Framework:** JUnit 5 + Mockito

---

### Task 9.2: Backend Integration Tests
**Files to Create:**
- `backend/src/test/java/com/mindguard/controller/AuthControllerTest.java`
- `backend/src/test/java/com/mindguard/controller/JournalControllerTest.java`
- `backend/src/test/java/com/mindguard/controller/AlertControllerTest.java`
- `backend/src/test/java/com/mindguard/controller/TherapistControllerTest.java`

**Test Coverage:**
- Full request/response cycle
- Status codes verification
- Error response format
- Authorization header validation

**Framework:** Spring Test + MockMvc

---

### Task 9.3: Frontend Unit Tests
**Files to Create:**
- `frontend/src/app/services/auth.service.spec.ts`
- `frontend/src/app/services/journal.service.spec.ts`
- `frontend/src/app/components/login/login.component.spec.ts`
- `frontend/src/app/components/register/register.component.spec.ts`

**Test Coverage:**
- Service methods (login, register, logout)
- Form validation
- Component rendering
- User interactions (form submission)

**Framework:** Jasmine + Karma

---

### Task 9.4: End-to-End Testing Setup
**Files to Create:**
- `e2e/auth.e2e.spec.ts` (login/register flow)
- `e2e/journal.e2e.spec.ts` (create/view entries)
- `e2e/mood.e2e.spec.ts` (log moods)
- `e2e/therapist.e2e.spec.ts` (therapist workflows)

**Test Scenarios:**
1. User Registration → Login → Create Journal Entry → View Alert
2. Therapist Assigns Patient → Views Entries → Resolves Alert
3. Mood Logging → View Charts → Alert Generated

**Framework:** Playwright or Cypress

---

## PHASE 10: DEPLOYMENT & SECURITY (High Priority)

### Task 10.1: Docker Configuration
**Files to Create:**
- `Dockerfile` (for backend)
- `frontend.dockerfile` (for frontend)
- `docker-compose.yml` (orchestration)
- `.dockerignore`

**Configuration:**
- Multi-stage build for efficiency
- Environment variable support
- Health checks
- Volume mounts for data persistence

---

### Task 10.2: Environment Configuration
**Files to Update/Create:**
- `backend/src/main/resources/application-prod.yml`
- `backend/src/main/resources/application-dev.yml`
- `frontend/environments/environment.prod.ts`
- `frontend/environments/environment.ts`

**Configuration:**
- Database connection settings
- API base URLs
- JWT secret (secure for prod)
- Hugging Face API key handling
- Logging levels

---

### Task 10.3: API Documentation (Swagger)
**Files to Create:**
- `backend/src/main/java/com/mindguard/config/SwaggerConfig.java`

**Configuration:**
- Swagger annotations on all controllers
- API title, description, version
- Security scheme documentation
- Model definitions

**Expected Output:** Accessible at `/swagger-ui.html`

---

### Task 10.4: Security Hardening
**Implementation:**
1. Add CORS configuration for production domains
2. Implement CSRF protection tokens
3. Add rate limiting on auth endpoints (max 5 attempts/minute)
4. Implement request validation interceptor
5. Add security headers (X-Content-Type-Options, X-Frame-Options, CSP)
6. Configure session timeout (30 minutes)
7. Add audit logging for sensitive operations
8. Implement request/response encryption for sensitive data (optional)

---

## PHASE 11: DOCUMENTATION (Medium Priority)

### Task 11.1: Update README
**File:** `README.md`
**Content:**
- Project overview
- Tech stack
- Setup instructions (dev environment)
- Running the application (backend + frontend)
- API documentation link
- Deployment instructions
- Contributing guidelines
- License

---

### Task 11.2: Create API Documentation
**File:** `docs/API_DOCUMENTATION.md`
**Content:**
- All endpoints organized by resource
- Request/response examples
- Error codes and meanings
- Authentication details
- Rate limiting information
- Pagination guidelines

---

### Task 11.3: Create Architecture Decision Records
**Files to Create:**
- `docs/adr/0001-jwt-token-strategy.md`
- `docs/adr/0002-ai-integration-approach.md`
- `docs/adr/0003-role-based-access-control.md`

---

## PRIORITY RANKING FOR GEMINI

**TIER 1 (Do First - Core Functionality):**
1. Task 1.1 - Database schema updates
2. Task 1.2 - Therapist-patient management backend
3. Task 2.1 - Therapist controller endpoints
4. Task 4.1 - Journal entry form
5. Task 6.1 - Alert list component

**TIER 2 (Do Next - MVP Complete):**
6. Task 3.1 - Therapist dashboard
7. Task 3.2 - Patient detail component
8. Task 5.1 - Mood chart component
9. Task 9.4 - End-to-end testing

**TIER 3 (Do Later - Polish & Security):**
10. Task 10.1 - Docker configuration
11. Task 10.4 - Security hardening
12. Task 11.1 - Update README
13. All remaining tasks

---

## Success Criteria

- [ ] All TIER 1 tasks completed and tested
- [ ] Backend API endpoints respond correctly with proper status codes
- [ ] Frontend components render without errors
- [ ] User can complete full workflow: Register → Create Entry → Get Alert → Therapist Reviews
- [ ] No console errors in browser or server logs
- [ ] All services properly wired with dependency injection
- [ ] Proper error handling and user feedback throughout
- [ ] Database properly populated with schema and indexes
