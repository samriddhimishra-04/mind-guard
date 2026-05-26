# Mind-Guard REST API Documentation

## Base URL
```
Development: http://localhost:8080/api
Production: https://api.mindguard.com/api
```

## Authentication

All endpoints (except `/auth/register` and `/auth/login`) require JWT bearer token in Authorization header:

```
Authorization: Bearer <jwt_token>
```

## Response Format

### Success Response (2xx)
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "message": "Operation successful"
  },
  "timestamp": "2026-05-25T10:30:00Z"
}
```

### Error Response (4xx, 5xx)
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "email",
        "message": "Invalid email format"
      }
    ]
  },
  "timestamp": "2026-05-25T10:30:00Z"
}
```

## HTTP Status Codes
- `200 OK` - Request succeeded
- `201 Created` - Resource created successfully
- `204 No Content` - Request succeeded, no response body
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Authenticated but not authorized
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `422 Unprocessable Entity` - Validation error
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Server error

## API Endpoints

---

## Authentication Endpoints

### Register User
```
POST /auth/register
```

**Request Body**:
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePassword123!@#",
  "firstName": "John",
  "lastName": "Doe",
  "role": "PATIENT",
  "dateOfBirth": "1990-05-15"
}
```

**Response** (201):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "johndoe",
    "email": "john@example.com",
    "role": "PATIENT",
    "createdAt": "2026-05-25T10:30:00Z"
  }
}
```

**Validations**:
- Username: 3-100 characters, alphanumeric + underscore
- Email: Valid email format, unique
- Password: Minimum 12 characters, uppercase, lowercase, number, special character
- Role: PATIENT or THERAPIST only (ADMIN via admin panel)

---

### Login
```
POST /auth/login
```

**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "SecurePassword123!@#"
}
```

**Response** (200):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "johndoe",
      "email": "john@example.com",
      "role": "PATIENT",
      "firstName": "John",
      "lastName": "Doe"
    },
    "expiresIn": 86400,
    "tokenType": "Bearer"
  }
}
```

---

### Refresh Token
```
POST /auth/refresh
```

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response** (200):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

---

### Logout
```
POST /auth/logout
```

**Request Headers**: Authorization required

**Response** (204): No Content

---

## Journal Entry Endpoints

### Create Journal Entry
```
POST /journals
```

**Request Body**:
```json
{
  "title": "Today's reflections",
  "content": "Had a challenging day at work...",
  "moodBefore": "ANXIOUS",
  "moodAfter": "CALM",
  "isSharedWithTherapist": true,
  "tags": ["work", "anxiety", "coping"]
}
```

**Response** (201):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Today's reflections",
    "createdAt": "2026-05-25T10:30:00Z",
    "analysis": null
  }
}
```

---

### Get Journal Entries
```
GET /journals?page=0&size=10&sort=createdAt,desc
```

**Query Parameters**:
- `page` (default: 0) - Page number (0-indexed)
- `size` (default: 20) - Items per page
- `sort` (default: createdAt,desc) - Sort field and direction
- `sharedOnly` (optional) - Filter to therapist-shared entries only

**Response** (200):
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "title": "Today's reflections",
        "summary": "Had a challenging day...",
        "moodBefore": "ANXIOUS",
        "moodAfter": "CALM",
        "isSharedWithTherapist": true,
        "analysis": {
          "sentiment": "NEUTRAL",
          "riskScore": 35
        },
        "createdAt": "2026-05-25T10:30:00Z",
        "tags": ["work", "anxiety"]
      }
    ],
    "totalElements": 42,
    "totalPages": 5,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

---

### Get Journal Entry by ID
```
GET /journals/{id}
```

**Path Parameters**:
- `id` (required) - Journal entry UUID

**Response** (200):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Today's reflections",
    "content": "Had a challenging day at work... [full content]",
    "moodBefore": "ANXIOUS",
    "moodAfter": "CALM",
    "isSharedWithTherapist": true,
    "analysis": {
      "sentiment": "NEUTRAL",
      "distressLevel": "LOW",
      "selfHarmIndicators": false,
      "riskScore": 35,
      "wellnessGuidance": "Consider taking a break and practicing deep breathing..."
    },
    "createdAt": "2026-05-25T10:30:00Z",
    "updatedAt": "2026-05-25T14:45:00Z",
    "tags": ["work", "anxiety", "coping"]
  }
}
```

---

### Update Journal Entry
```
PUT /journals/{id}
```

**Path Parameters**:
- `id` (required) - Journal entry UUID

**Request Body**:
```json
{
  "title": "Updated title",
  "content": "Updated content...",
  "moodAfter": "CALM",
  "isSharedWithTherapist": true,
  "tags": ["updated", "tags"]
}
```

**Response** (200): Updated entry data

---

### Delete Journal Entry
```
DELETE /journals/{id}
```

**Response** (204): No Content

---

## Mood Tracking Endpoints

### Log Mood
```
POST /moods
```

**Request Body**:
```json
{
  "moodLevel": 7,
  "moodCategory": "HAPPY",
  "energyLevel": 8,
  "notes": "Good day overall"
}
```

**Response** (201):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440002",
    "moodLevel": 7,
    "moodCategory": "HAPPY",
    "energyLevel": 8,
    "createdAt": "2026-05-25T10:30:00Z"
  }
}
```

**Mood Categories**: HAPPY, SAD, ANGRY, ANXIOUS, CALM, NEUTRAL, FRUSTRATED, HOPEFUL

---

### Get Mood History
```
GET /moods?days=30&page=0&size=20
```

**Query Parameters**:
- `days` (default: 7) - Number of days to retrieve
- `page` (default: 0) - Page number
- `size` (default: 20) - Items per page

**Response** (200):
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440002",
        "moodLevel": 7,
        "moodCategory": "HAPPY",
        "energyLevel": 8,
        "notes": "Good day overall",
        "createdAt": "2026-05-25T10:30:00Z"
      }
    ],
    "totalElements": 15,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

---

### Get Mood Summary Statistics
```
GET /moods/summary?days=30
```

**Response** (200):
```json
{
  "success": true,
  "data": {
    "period": "2026-04-25 to 2026-05-25",
    "averageMoodLevel": 6.2,
    "averageEnergyLevel": 6.8,
    "moodDistribution": {
      "HAPPY": 8,
      "CALM": 7,
      "NEUTRAL": 10,
      "ANXIOUS": 3,
      "SAD": 2
    },
    "moodTrend": "IMPROVING",
    "lowestMoodDate": "2026-05-10",
    "highestMoodDate": "2026-05-23"
  }
}
```

---

## AI Analysis Endpoints

### Analyze Journal Entry
```
POST /analysis/analyze
```

**Request Body**:
```json
{
  "journalEntryId": "550e8400-e29b-41d4-a716-446655440001"
}
```

**Response** (200):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440003",
    "journalEntryId": "550e8400-e29b-41d4-a716-446655440001",
    "sentiment": "NEGATIVE",
    "sentimentScore": 0.75,
    "distressLevel": "MEDIUM",
    "distressScore": 0.65,
    "selfHarmIndicators": false,
    "riskScore": 52,
    "wellnessGuidance": "Consider reaching out to your support network...",
    "analyzedAt": "2026-05-25T10:30:00Z"
  }
}
```

**Risk Scoring**:
- 0-30: Low risk (wellness guidance only)
- 31-70: Medium risk (standard notification to therapist)
- 71-100: High risk (emergency alert to therapist)

---

### Get Analysis Results
```
GET /analysis/{journalEntryId}
```

**Response** (200): Analysis data (same as above)

---

## Alert Endpoints (Therapist)

### Get Alerts for Therapist
```
GET /alerts?page=0&size=20&status=unresolved
```

**Query Parameters**:
- `page` (default: 0) - Page number
- `size` (default: 20) - Items per page
- `status` (optional) - Filter by status: all, unresolved, acknowledged
- `patientId` (optional) - Filter by patient UUID

**Required Role**: THERAPIST

**Response** (200):
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440004",
        "alertType": "EMERGENCY",
        "riskScore": 78,
        "patient": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "firstName": "John",
          "lastName": "Doe"
        },
        "journal": {
          "id": "550e8400-e29b-41d4-a716-446655440001",
          "title": "Today's reflections",
          "summary": "Had a challenging day..."
        },
        "alertMessage": "Patient showing high distress indicators...",
        "isAcknowledged": false,
        "isResolved": false,
        "createdAt": "2026-05-25T10:30:00Z"
      }
    ],
    "totalElements": 5,
    "unreadCount": 3
  }
}
```

---

### Acknowledge Alert
```
PUT /alerts/{id}/acknowledge
```

**Required Role**: THERAPIST

**Response** (200):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440004",
    "isAcknowledged": true,
    "acknowledgedAt": "2026-05-25T10:35:00Z"
  }
}
```

---

### Respond to Alert
```
POST /alerts/{id}/response
```

**Required Role**: THERAPIST

**Request Body**:
```json
{
  "responseText": "Called patient, they are safe. Will follow up next week.",
  "actionTaken": "Phone consultation scheduled",
  "followUpRequired": true,
  "followUpDate": "2026-05-30"
}
```

**Response** (201):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440005",
    "alertId": "550e8400-e29b-41d4-a716-446655440004",
    "responseText": "Called patient, they are safe...",
    "createdAt": "2026-05-25T10:35:00Z"
  }
}
```

---

## Therapist Dashboard Endpoints

### Get Assigned Patients List
```
GET /therapist/patients?page=0&size=20
```

**Query Parameters**:
- `page` (default: 0) - Page number
- `size` (default: 20) - Items per page
- `searchTerm` (optional) - Search patient name

**Required Role**: THERAPIST

**Response** (200):
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "firstName": "John",
        "lastName": "Doe",
        "currentMood": "CALM",
        "moodLevel": 6,
        "lastEntryDate": "2026-05-25T10:30:00Z",
        "riskStatus": "MEDIUM",
        "activeAlerts": 1,
        "assignedDate": "2026-01-15T00:00:00Z"
      }
    ],
    "totalElements": 8,
    "currentPage": 0
  }
}
```

---

### Get Patient Summary
```
GET /therapist/patients/{patientId}/summary
```

**Path Parameters**:
- `patientId` (required) - Patient UUID

**Required Role**: THERAPIST

**Response** (200):
```json
{
  "success": true,
  "data": {
    "patient": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com"
    },
    "moodTrend": {
      "averageMoodLevel": 6.5,
      "trend": "STABLE",
      "moodHistoryChart": []
    },
    "recentEntries": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "title": "Today's reflections",
        "createdAt": "2026-05-25T10:30:00Z",
        "analysis": {
          "sentiment": "NEUTRAL",
          "riskScore": 35
        }
      }
    ],
    "activeAlerts": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440004",
        "alertType": "URGENT",
        "riskScore": 62
      }
    ],
    "wellnessRecommendations": [
      "Continue meditation practice",
      "Increase physical activity"
    ],
    "lastReviewDate": "2026-05-20T14:00:00Z"
  }
}
```

---

## Rate Limiting

All endpoints are rate-limited to prevent abuse:

**Limits**:
- Unauthenticated endpoints (login, register): 10 requests per minute
- Authenticated endpoints: 100 requests per minute
- File upload endpoints: 5 requests per minute

**Rate Limit Headers**:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1653544200
```

**Rate Limit Exceeded** (429):
```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests. Try again in 60 seconds."
  }
}
```

---

## Pagination

Most list endpoints support pagination:

```
GET /endpoint?page=0&size=20&sort=createdAt,desc
```

**Response includes**:
- `content`: Array of items
- `totalElements`: Total number of items
- `totalPages`: Total number of pages
- `currentPage`: Current page number
- `pageSize`: Items per page

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| VALIDATION_ERROR | 400 | Request validation failed |
| UNAUTHORIZED | 401 | Missing or invalid authentication |
| FORBIDDEN | 403 | User lacks required permissions |
| NOT_FOUND | 404 | Requested resource not found |
| CONFLICT | 409 | Resource already exists |
| RATE_LIMIT_EXCEEDED | 429 | Too many requests |
| INTERNAL_ERROR | 500 | Server error |
| SERVICE_UNAVAILABLE | 503 | External service unavailable (e.g., Hugging Face API) |

---

## Testing the API

### Using curl
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePassword123!@#"
  }'

# Create journal entry (using token from login)
curl -X POST http://localhost:8080/api/journals \
  -H "Authorization: Bearer {access_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My entry",
    "content": "Content here..."
  }'
```

### Using Postman
1. Import the API collection from `/docs/postman-collection.json`
2. Set environment variables: `base_url`, `access_token`
3. Create auth flow to automatically refresh tokens

---

**Last Updated**: 2026-05-25
**API Version**: 1.0.0
