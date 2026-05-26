# Mind-Guard Quick Setup Commands

Complete command reference for getting Mind-Guard running on your local machine.

## Prerequisites Verification

```bash
# Verify Java 17+
java -version

# Verify Node.js 18+
node -v
npm -v

# Verify PostgreSQL 14+
psql --version
```

---

## Database Setup (PostgreSQL)

### 1. Create Database and User

```bash
# Connect to PostgreSQL default
psql -U postgres

# Inside psql:
CREATE DATABASE mindguard;
CREATE USER mindguard_user WITH PASSWORD 'your_secure_password';
ALTER ROLE mindguard_user CREATEDB;
\q
```

### 2. Initialize Schema

```bash
# Navigate to backend
cd "c:\JAVA\mind-guard\backend"

# Run initialization script
psql -U postgres -d mindguard -f "src/main/resources/scripts/init.sql"

# Verify tables created
psql -U postgres -d mindguard -c "\dt"
```

---

## Backend Setup

### Quick Start

```bash
# Navigate to backend
cd "c:\JAVA\mind-guard\backend"

# Copy environment template
cp .env.example .env

# Edit .env with your values:
# - DB_PASSWORD: your postgres password
# - JWT_SECRET: generate strong random string (32+ chars)
# - ENCRYPTION_KEY: generate strong random string (32+ chars)
# - HUGGINGFACE_API_KEY: from https://huggingface.co/settings/tokens

# Download dependencies
mvn clean install

# Start backend (runs on http://localhost:8080/api)
mvn spring-boot:run
```

### Backend Verification Commands

```bash
# Check if backend is running
curl http://localhost:8080/api/actuator/health

# Expected: {"status":"UP"}

# View logs
# Check console output from mvn spring-boot:run
```

### Backend Build & Package

```bash
# Compile code
mvn compile

# Run unit tests
mvn test

# Generate code coverage report
mvn jacoco:report

# Check code quality
mvn sonar:sonar

# Package as JAR
mvn clean package

# Run built JAR
java -jar target/mindguard-1.0.0.jar
```

---

## Frontend Setup

### Quick Start

```bash
# Navigate to frontend
cd "c:\JAVA\mind-guard\frontend"

# Copy environment template (optional)
cp .env.example .env

# Install dependencies (takes 1-2 minutes)
npm install

# Start development server (opens browser automatically)
ng serve --open

# Or use npm
npm start
```

**Frontend URL**: http://localhost:4200

### Frontend Verification Commands

```bash
# Check Angular version
ng version

# Build for production
ng build --configuration production

# Run unit tests with coverage
ng test --code-coverage

# Run e2e tests
ng e2e

# Lint code
ng lint

# Format code with Prettier
npm run format

# Check formatting without fixing
npm run format:check
```

---

## Testing & Verification

### Test Registration via API

```bash
# Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testpatient",
    "email": "patient@test.com",
    "password": "TestPassword123!@#",
    "firstName": "Test",
    "lastName": "Patient",
    "role": "PATIENT"
  }'

# Expected response includes userId
```

### Test Login via API

```bash
# Login with credentials
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@test.com",
    "password": "TestPassword123!@#"
  }'

# Expected response includes accessToken and refreshToken
# Save accessToken for next request
```

### Test Protected Endpoint

```bash
# Replace {ACCESS_TOKEN} with token from login response
curl -X GET http://localhost:8080/api/journals \
  -H "Authorization: Bearer {ACCESS_TOKEN}" \
  -H "Content-Type: application/json"

# Expected: empty array initially
```

### Test Frontend Login

1. Open http://localhost:4200 in browser
2. Click "Sign Up"
3. Register with:
   - Username: `testuser`
   - Email: `testuser@test.com`
   - Password: `TestPassword123!@#`
   - Role: Patient
4. Verify redirected to dashboard
5. Check browser console (F12) for JWT token

---

## Development Workflow

### Terminal Setup (Recommended)

**Terminal 1: PostgreSQL** (ensure running)
```bash
# Windows: Services → PostgreSQL should be running
# macOS: brew services list | grep postgresql
# Linux: systemctl status postgresql
```

**Terminal 2: Backend**
```bash
cd "c:\JAVA\mind-guard\backend"
mvn spring-boot:run
```

**Terminal 3: Frontend**
```bash
cd "c:\JAVA\mind-guard\frontend"
npm start
```

---

## Git Commands (Version Control)

```bash
# Initialize git repository
cd "c:\JAVA\mind-guard"
git init

# Add all files
git add .

# Create initial commit
git commit -m "Initial Mind-Guard project setup"

# View git status
git status

# View commit history
git log --oneline
```

---

## Docker Setup (Optional - Future)

```bash
# Build Docker image
docker build -f backend/Dockerfile -t mindguard-backend:1.0 .

# Run PostgreSQL in Docker
docker run --name mindguard-db \
  -e POSTGRES_PASSWORD=password \
  -d postgres:14

# Run backend in Docker
docker run --name mindguard-backend \
  -p 8080:8080 \
  --link mindguard-db:db \
  -e DB_HOST=db \
  -d mindguard-backend:1.0

# Run frontend in Docker
docker run --name mindguard-frontend \
  -p 4200:80 \
  -d mindguard-frontend:1.0

# View logs
docker logs -f mindguard-backend
docker logs -f mindguard-frontend
```

---

## Debugging

### Backend Debugging in IDE

```bash
# IntelliJ IDEA
1. Open project
2. Locate MindGuardApplication.java
3. Right-click → Debug
4. Set breakpoints in code
5. Pauses at breakpoints when requests hit endpoint

# VS Code
1. Install Extension Pack for Java
2. Create .vscode/launch.json:
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Spring Boot App",
      "type": "java",
      "name": "Spring Boot",
      "request": "launch",
      "cwd": "${workspaceFolder}/backend",
      "mainClass": "com.mindguard.MindGuardApplication",
      "projectName": "mindguard",
      "args": ""
    }
  ]
}
```

### Frontend Debugging

```bash
# Chrome DevTools (F12)
1. Open http://localhost:4200
2. Press F12 to open Developer Tools
3. Sources tab → set breakpoints in TypeScript code
4. Step through code with debugger

# Debugging localStorage
Open Console and run:
localStorage.getItem('mindguard_token')
localStorage.getItem('mindguard_refresh_token')
```

### Database Debugging

```bash
# Connect to PostgreSQL
psql -U postgres -d mindguard

# View all users
SELECT id, username, email, role FROM users;

# View journal entries
SELECT id, title, created_at FROM journal_entries LIMIT 10;

# View analysis results
SELECT ja.id, ja.sentiment, ja.risk_score, ja.analyzed_at 
FROM journal_analyses ja 
ORDER BY analyzed_at DESC LIMIT 10;

# View alerts
SELECT id, alert_type, risk_score, is_acknowledged 
FROM alerts 
ORDER BY created_at DESC;

# Count total entries
SELECT COUNT(*) FROM journal_entries;

# Exit
\q
```

---

## Common Issues & Solutions

### Port Already in Use

```bash
# Backend port 8080 in use
netstat -ano | findstr :8080
taskkill /PID {PID} /F

# Frontend port 4200 in use
netstat -ano | findstr :4200
taskkill /PID {PID} /F

# Or change ports:
# Backend: src/main/resources/application.yml → server.port: 8081
# Frontend: ng serve --port 4300
```

### PostgreSQL Connection Failed

```bash
# Check PostgreSQL is running
psql -U postgres -c "SELECT 1"

# If not running, start it:
# Windows: Services → Start PostgreSQL service
# macOS: brew services start postgresql
# Linux: sudo systemctl start postgresql

# Verify credentials in .env file
# DB_USERNAME=postgres
# DB_PASSWORD=your_password
```

### JWT Errors

```bash
# JWT_SECRET not set or too short
# Requirements: at least 32 characters, stored in .env

# Generate strong JWT secret:
# Linux/macOS: openssl rand -base64 32
# Windows: Use online generator or: certutil -encodehex -f -

# Token expired
# Frontend automatically refreshes tokens
# Check interceptor in src/app/interceptors/auth.interceptor.ts
```

### Hugging Face API Errors

```bash
# API key not valid
# Check: https://huggingface.co/settings/tokens
# Ensure token has enough API credits
# API key should be in .env: HUGGINGFACE_API_KEY

# Rate limiting
# HF API has per-minute limits
# Check console logs for rate limit errors
# Implement request queuing if needed
```

### Frontend Compilation Errors

```bash
# Clear dependencies and reinstall
rm -rf node_modules package-lock.json
npm install

# Update Angular CLI
npm install -g @angular/cli@latest

# Check Node version (should be 18+)
node -v

# Clear Angular cache
ng cache clean
# Or: rm -rf .angular/cache
```

---

## Environment Variables Cheat Sheet

### Backend (.env)

```
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mindguard
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Server
SERVER_PORT=8080
ADMIN_PASSWORD=admin123

# Security
JWT_SECRET=your_jwt_secret_32_chars_min
ENCRYPTION_KEY=your_encryption_key_32_chars_min

# AI
HUGGINGFACE_API_KEY=<YOUR_HUGGING_FACE_API_KEY>

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:4200

# Environment
ENVIRONMENT=development
```

### Frontend (.env)

```
# API Configuration
NG_APP_API_URL=http://localhost:8080/api
NG_APP_API_TIMEOUT=30000

# Authentication
NG_APP_TOKEN_STORAGE_KEY=mindguard_token
NG_APP_REFRESH_TOKEN_STORAGE_KEY=mindguard_refresh_token

# Features
NG_APP_ENABLE_ANALYTICS=false
NG_APP_ENABLE_SENTRY=false

# Environment
NG_APP_ENVIRONMENT=development
NG_APP_VERSION=1.0.0
```

---

## Useful Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Angular Docs**: https://angular.io/docs
- **PostgreSQL Docs**: https://www.postgresql.org/docs/
- **Hugging Face API**: https://huggingface.co/docs/inference-endpoints
- **JWT Best Practices**: https://tools.ietf.org/html/rfc8949
- **OWASP Security**: https://owasp.org/www-project-top-ten/

---

## Next Steps

1. Follow [docs/SETUP.md](docs/SETUP.md) for detailed setup
2. Review [docs/API.md](docs/API.md) for API endpoints
3. Check [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for system design
4. Read [SECURITY_REVIEW.md](SECURITY_REVIEW.md) before production
5. Review [README.md](README.md) for project overview

---

**Last Updated**: 2026-05-25
**Version**: 1.0.0
