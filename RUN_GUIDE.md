# Mind-Guard - Quick Start Guide

## Prerequisites
- Java 21 ✅
- Node.js 18+ ✅
- Angular CLI ✅
- PostgreSQL 18 ✅
- Git Bash ✅

## Database Setup

```bash
# 1. Start PostgreSQL (should be running already)
# 2. Create database and initialize schema
export PGPASSWORD=root
psql -U postgres -h localhost -d mindguard -f backend/src/main/resources/scripts/init.sql
```

## Backend (Spring Boot)

```bash
# Start backend on port 8081
cd backend
bash start.sh

# The backend will be available at:
# http://localhost:8081/api
```

**start.sh includes:**
- JDBC URL: `jdbc:postgresql://localhost:5432/mindguard`
- Username: `postgres`
- Password: `root`
- Port: `8081`

## Frontend (Angular)

```bash
# Start frontend on port 4201
cd frontend
npm start -- --port 4201

# The frontend will be available at:
# http://localhost:4201
```

**Frontend configuration:**
- API Base URL: `http://localhost:8081/api`
- Environment: development

## Test the System

### 1. Frontend Access
```bash
curl -s http://localhost:4201 | grep "<title>"
# Should return: <title>Mind-Guard</title>
```

### 2. Backend Connectivity  
```bash
curl -s http://localhost:8081/api/actuator/health
# Should return health status JSON
```

### 3. Database Connection
```bash
export PGPASSWORD=root
psql -U postgres -h localhost -d mindguard -c "SELECT COUNT(*) FROM users;"
# Should return: 0 (initially)
```

## Expected Behavior When Working

1. **Frontend loads** → Shows login page
2. **Click Register** → Fill in patient/therapist details
3. **Submit** → Calls POST /api/auth/register
4. **Login** → Calls POST /api/auth/login, returns JWT token
5. **Dashboard** → Shows patient/therapist interface based on role
6. **Create Entry** → Journal entries stored in database
7. **View Stats** → Statistics calculated and displayed

## Troubleshooting

### Port Already in Use
```bash
# Find and kill process on port
netstat -ano | grep 8081
taskkill /PID <PID> /F
```

### Database Connection Failed
```bash
# Test PostgreSQL directly
export PGPASSWORD=root
psql -U postgres -h localhost -d mindguard -c "\dt"
# Should show: users, therapist_patients, journal_entries, etc.
```

### Frontend Not Loading
```bash
# Check if npm server is running
netstat -ano | grep 4201

# Rebuild if needed
cd frontend
npm install
npm start -- --port 4201
```

### Clear Cache
```bash
# Clear browser cache
# Clear localStorage
cd frontend && npm cache clean --force
```

## Project Structure

```
mind-guard/
├── backend/
│   ├── src/main/java/com/mindguard/
│   │   ├── controller/        (8 REST controllers)
│   │   ├── service/           (6 services)
│   │   ├── repository/        (6 repositories)
│   │   ├── entity/            (6 JPA entities)
│   │   ├── dto/               (13 request/response DTOs)
│   │   ├── security/          (JWT & auth)
│   │   └── MindGuardApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml    (main config)
│   │   ├── application-dev.yml (dev profile)
│   │   └── scripts/init.sql   (DB schema)
│   ├── pom.xml
│   └── start.sh              (startup script)
│
├── frontend/
│   ├── src/app/
│   │   ├── components/        (8 components)
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   ├── dashboard/
│   │   │   ├── layout/
│   │   │   │   ├── sidebar/
│   │   │   │   └── toast-container/
│   │   │   └── therapist-dashboard/
│   │   ├── services/          (5 services)
│   │   │   ├── auth.service.ts
│   │   │   ├── journal.service.ts
│   │   │   ├── mood.service.ts
│   │   │   ├── therapist.service.ts
│   │   │   ├── statistics.service.ts
│   │   │   ├── notification.service.ts
│   │   │   └── toast.service.ts
│   │   ├── guards/            (Auth guards)
│   │   ├── interceptors/      (HTTP interceptors)
│   │   ├── app.routes.ts
│   │   └── app.component.ts
│   ├── angular.json
│   ├── tsconfig.json
│   ├── package.json
│   └── .env                   (environment variables)
│
├── docs/
│   ├── TIER1_TIER2_SUMMARY.md
│   ├── TIER3_COMPLETE.md
│   ├── DATABASE_SETUP_COMPLETE.md
│   └── [other documentation]
│
└── README.md
```

## API Examples

### Register
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "JohnPassword@123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "PATIENT"
  }'
```

### Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "JohnPassword@123"
  }'
```

### Create Journal Entry (requires JWT token)
```bash
curl -X POST http://localhost:8081/api/journals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "title": "My First Entry",
    "content": "Today was a good day...",
    "tags": ["positive", "reflection"]
  }'
```

## Useful Commands

```bash
# View backend logs
tail -f backend/logs/backend.log

# Check all running Java processes
ps aux | grep java

# Stop all Java processes
taskkill /F /IM java.exe

# Clear npm cache
npm cache clean --force

# Rebuild frontend
cd frontend && npm install && npm start -- --port 4201

# Rebuild backend (requires Lombok fixes)
cd backend && mvn clean install -DskipTests

# Check PostgreSQL status
psql -U postgres -c "SELECT version();"

# Backup database
export PGPASSWORD=root
pg_dump -U postgres mindguard > mindguard_backup.sql

# Restore database
export PGPASSWORD=root
psql -U postgres mindguard < mindguard_backup.sql
```

## Current Status

✅ **Frontend:** Running on port 4201  
✅ **Backend:** Running on port 8081  
✅ **Database:** Running on port 5432  
⚠️ **Database Connection:** Debugging (see VERIFICATION_STATUS.md)

## Support

See VERIFICATION_STATUS.md for detailed troubleshooting and known issues.
