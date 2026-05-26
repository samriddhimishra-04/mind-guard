# Mind-Guard Complete Setup Guide

Complete step-by-step instructions to get the Mind-Guard platform running locally.

## Prerequisites

### Required Software
- **Java Development Kit (JDK) 17+**
  - Download from: https://www.oracle.com/java/technologies/downloads/
  - Verify: `java -version`

- **Node.js 18+ and npm**
  - Download from: https://nodejs.org/
  - Verify: `node -v && npm -v`

- **PostgreSQL 14+**
  - Download from: https://www.postgresql.org/download/
  - Verify: `psql --version`

- **Git**
  - Download from: https://git-scm.com/
  - Verify: `git --version`

- **Maven 3.8+** (Optional, bundled with most IDEs)
  - Download from: https://maven.apache.org/download.cgi
  - Verify: `mvn -v`

### Required Accounts
- **Hugging Face Account** (for AI API)
  - Sign up: https://huggingface.co/
  - Generate API key: Settings → Access Tokens

### Recommended Tools
- **IDE**: IntelliJ IDEA, VS Code, or Eclipse
- **API Testing**: Postman or Insomnia
- **Database Client**: DBeaver or pgAdmin

---

## Part 1: Database Setup

### 1. Start PostgreSQL

**Windows (if using PostgreSQL installer)**:
```bash
# PostgreSQL typically starts as a Windows service automatically
# Verify it's running:
netstat -ano | findstr :5432
```

**macOS/Linux**:
```bash
# Start PostgreSQL service
brew services start postgresql

# Or using systemctl
sudo systemctl start postgresql
```

### 2. Create Database User (Optional)

```bash
# Connect to PostgreSQL as default user
psql -U postgres

# Create mindguard user with password
CREATE USER mindguard_user WITH PASSWORD 'your_secure_password';

# Grant privileges
ALTER ROLE mindguard_user CREATEDB;

# Exit psql
\q
```

### 3. Create Database

```bash
# Using psql
psql -U postgres -c "CREATE DATABASE mindguard;"

# Or using createdb utility
createdb -U postgres mindguard
```

### 4. Initialize Schema

```bash
# Navigate to backend directory
cd "c:\JAVA\mind-guard\backend"

# Run initialization script
psql -U postgres -d mindguard -f "src/main/resources/scripts/init.sql"

# Verify schema was created
psql -U postgres -d mindguard -c "\dt"
```

**Expected Output** (tables created):
```
             List of relations
 Schema |          Name          | Type
--------+------------------------+-------
 public | alerts                 | table
 public | alert_responses        | table
 public | journal_analyses       | table
 public | journal_entries        | table
 public | mood_logs              | table
 public | therapist_patients     | table
 public | users                  | table
```

---

## Part 2: Backend Setup

### 1. Navigate to Backend Directory

```bash
cd "c:\JAVA\mind-guard\backend"
```

### 2. Create Environment File

```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your values
# Windows: notepad .env
# macOS/Linux: nano .env
```

**Update these values**:
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mindguard
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password

JWT_SECRET=your_very_secure_jwt_secret_key_min_32_chars_123456789
HUGGINGFACE_API_KEY=hf_your_actual_api_key_from_hugging_face
ENCRYPTION_KEY=your_encryption_key_min_32_chars_abcdefghijklmnop
```

### 3. Download Dependencies

```bash
# Maven will download all dependencies listed in pom.xml
mvn clean install

# This may take 2-5 minutes on first run
```

### 4. Verify Build

```bash
# Compile the project
mvn compile

# Expected output: BUILD SUCCESS
```

### 5. Run Database Migrations (if using Flyway/Liquibase)

```bash
# Currently using manual scripts, but ready for migration tool integration
# Coming in next release
```

### 6. Start Backend Server

**Option A: Using Maven**
```bash
mvn spring-boot:run
```

**Option B: Using IDE**
1. Open project in IntelliJ/Eclipse
2. Locate `MindGuardApplication.java` or main class
3. Right-click → Run

**Expected Output**:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/

Tomcat started on port(s): 8080 (http) with context path: '/api'
Started MindGuardApplication in 3.456 seconds
```

### 7. Verify Backend is Running

```bash
# In a new terminal/PowerShell window:
curl http://localhost:8080/api/actuator/health

# Expected response:
# {"status":"UP"}
```

---

## Part 3: Frontend Setup

### 1. Navigate to Frontend Directory

```bash
cd "c:\JAVA\mind-guard\frontend"
```

### 2. Create Environment File

```bash
# Copy the example environment file
cp .env.example .env

# Edit .env (optional, defaults are fine for local development)
```

### 3. Install Dependencies

```bash
# Install npm packages (takes 1-2 minutes)
npm install

# Verify Angular CLI is installed
ng version
```

**Expected Output**:
```
     _                      _                 ____ _     ___
    / \   _ __   __ _ _   _| | __ _ _ __     / ___| |   |_ _|
   / △ \  | '_ \ / _` | | | | |/ _` | '__|   | |   | |    | |
  / ___ \ | | | | (_| | | | | | (_| | |      | |___| |___ | |
 /_/   \_\|_| |_|\__, |\___|_|_|\__,_|_|       \____|_____|___|
                  |___/

Angular CLI: 18.0.0
Node: 18.x.x
Package Manager: npm x.x.x
```

### 4. Update API Configuration

**Edit `src/environments/environment.ts`**:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

**Edit `src/environments/environment.prod.ts`**:
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.mindguard.com/api'
};
```

### 5. Start Frontend Development Server

```bash
# Starts dev server and opens browser automatically
ng serve --open

# Or manually:
npm start

# Server runs at: http://localhost:4200
```

**Expected Output**:
```
✔ Compiled successfully.

Application bundle generation complete.

Initial Chunk Files   | Names         | Size
main.js              | main         | 234.56 kB

Build at: 2026-05-25T10:30:00.000Z - Hash: abc123def456 - Time: 3456ms

Angular Live Development Server is listening on localhost:4200
Open your browser on http://localhost:4200/
✔ Compiled successfully.
```

### 6. Verify Frontend is Running

- Browser should automatically open to `http://localhost:4200`
- You should see the Mind-Guard login page
- Check browser console (F12) for any errors

---

## Part 4: Testing the Complete Setup

### 1. Test User Registration

1. Navigate to `http://localhost:4200`
2. Click "Sign Up" or similar
3. Register with:
   - Username: `testpatient`
   - Email: `patient@test.com`
   - Password: `TestPassword123!@#`
   - Role: Patient

### 2. Test User Login

1. Use credentials from registration
2. Should see patient dashboard
3. Check browser → Application → Local Storage for JWT token

### 3. Test Backend API Directly

```bash
# Register user via API
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPassword123!@#",
    "firstName": "Test",
    "lastName": "User",
    "role": "PATIENT"
  }'

# Login and get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPassword123123!@#"
  }' | jq '.data.accessToken'

# Use token to access protected endpoint
curl -X GET http://localhost:8080/api/journals \
  -H "Authorization: Bearer {your_access_token}"
```

### 4. Test Database

```bash
# Connect to database
psql -U postgres -d mindguard

# Check users table
SELECT username, email, role FROM users;

# Check journal entries
SELECT * FROM journal_entries;

# Exit
\q
```

---

## Part 5: Development Workflow

### Starting All Services (Recommended Setup)

**Terminal 1: PostgreSQL**
```bash
# Ensure PostgreSQL is running
# Windows: Services → PostgreSQL should be running
# macOS/Linux: brew services list | grep postgresql
```

**Terminal 2: Backend**
```bash
cd "c:\JAVA\mind-guard\backend"
mvn spring-boot:run
# Runs on http://localhost:8080/api
```

**Terminal 3: Frontend**
```bash
cd "c:\JAVA\mind-guard\frontend"
npm start
# Opens http://localhost:4200
```

### Common Development Commands

**Backend**:
```bash
# Run tests
mvn test

# Check code coverage
mvn jacoco:report

# Build WAR/JAR for deployment
mvn clean package

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**Frontend**:
```bash
# Run unit tests
ng test

# Run e2e tests
ng e2e

# Build for production
ng build --configuration production

# Lint code
ng lint
```

### Debugging

**Backend (IntelliJ IDEA/VS Code)**:
1. Set breakpoints in Java code
2. Run in Debug mode: Right-click → Debug
3. Browser pauses at breakpoints

**Frontend (Chrome DevTools)**:
1. Press F12 to open DevTools
2. Sources tab → set breakpoints
3. Step through TypeScript code

---

## Part 6: Production Deployment Checklist

Before deploying to production:

### Backend
- [ ] Update `application-prod.yml` with production database
- [ ] Change `JWT_SECRET` to a strong 32+ character string
- [ ] Set `ENCRYPTION_KEY` to a secure value
- [ ] Update `CORS_ALLOWED_ORIGINS` to production frontend domain
- [ ] Disable debug logging: `logging.level.com.mindguard=WARN`
- [ ] Enable SSL/TLS certificate
- [ ] Set up automated database backups
- [ ] Configure monitoring and alerting

### Frontend
- [ ] Update API endpoint in `environment.prod.ts`
- [ ] Build production bundle: `ng build --configuration production`
- [ ] Enable analytics and monitoring
- [ ] Set up CDN for static assets
- [ ] Configure security headers

### General
- [ ] Run security tests: `mvn sonar:sonar`
- [ ] Run dependency checks: `mvn org.owasp:dependency-check-maven:check`
- [ ] Update documentation with deployment URLs
- [ ] Set up CI/CD pipeline (GitHub Actions, Jenkins, etc.)
- [ ] Configure Hugging Face API rate limits and monitoring

---

## Troubleshooting

### PostgreSQL Connection Issues

```bash
# Check if PostgreSQL is running
psql -U postgres -c "SELECT 1"

# If connection refused:
# Windows: Check Services → PostgreSQL-x64
# macOS: brew services restart postgresql
# Linux: sudo systemctl restart postgresql

# Verify connection details in .env file
# Default: localhost:5432, user: postgres
```

### Backend Won't Start

```bash
# Check Java version
java -version  # Should be 17+

# Check port 8080 is available
netstat -ano | findstr :8080

# If in use, change port in application.yml:
# server.port: 8081

# Check for compilation errors
mvn clean compile

# View full error log
mvn spring-boot:run 2>&1 | tee build.log
```

### Frontend Compilation Errors

```bash
# Clear node_modules and reinstall
rm -r node_modules package-lock.json
npm install

# Check Angular version
ng version

# Try cache clearing
npm cache clean --force

# If still issues, check Node version
node -v  # Should be 18+
```

### Hugging Face API Errors

```bash
# Verify API key is set
echo $HUGGINGFACE_API_KEY  # Should print your key

# Test API key with curl
curl https://api-inference.huggingface.co/models/distilbert-base-uncased-finetuned-sst-2-english \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"inputs":"I am happy"}'

# Check API key in backend logs
# Should not appear in logs for security
```

---

## Next Steps

1. **Review API Documentation**: See `docs/API.md`
2. **Understand Architecture**: See `docs/ARCHITECTURE.md`
3. **Security Best Practices**: See `SECURITY_REVIEW.md`
4. **Database Schema**: See `docs/DATABASE.md`
5. **AI Workflow**: See `docs/AI_WORKFLOW.md`

---

**Last Updated**: 2026-05-25
**Version**: 1.0.0
