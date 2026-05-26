# Mind-Guard Security Review & Best Practices

## Overview
This document outlines the security architecture, vulnerabilities to address, and best practices implemented in the Mind-Guard platform.

## Security Principles

1. **Defense in Depth**: Multiple layers of security controls
2. **Least Privilege**: Users have minimum required permissions
3. **Data Protection**: Encryption at rest and in transit
4. **Authentication & Authorization**: Strong, token-based security
5. **Audit Logging**: Comprehensive logging for compliance

## Authentication & Authorization

### JWT Implementation

**Backend (Spring Security)**
```java
// JWT Token Flow:
1. User login → POST /api/auth/login
2. Server validates credentials → generates JWT
3. JWT includes: userId, role, issued-at, expiration
4. Client stores token in localStorage (with HttpOnly option)
5. Client includes token in Authorization: Bearer <token> header
6. Server validates token signature on each request
7. On expiration → POST /api/auth/refresh returns new token
```

**Security Considerations**:
- JWT_SECRET must be at least 32 characters (256-bit)
- Set to environment variable in production, NEVER hardcode
- Token expiration: 24 hours (access token)
- Refresh token: 7 days, rotated on use
- Use HTTPS-only cookies for token storage in production
- Implement token revocation list for logout

**Frontend (HTTP Interceptor)**
```typescript
// Angular HTTP Interceptor automatically adds JWT to requests
// Handles token refresh on 401 response
// Removes token on logout
```

### Role-Based Access Control (RBAC)

**User Roles**:
```
PATIENT:    Can create/manage own journal entries, view their mood logs, receive wellness guidance
THERAPIST:  Can view assigned patients, respond to alerts, create recommendations
ADMIN:      Can manage users, view system metrics, configure settings
```

**Implementation**:
- Backend: Spring Security with @PreAuthorize annotations
- Frontend: Route guards and component-level permission checks
- Database: Role stored in users table

**Example Backend**:
```java
@PreAuthorize("hasRole('THERAPIST')")
@GetMapping("/patients/{patientId}")
public ResponseEntity<PatientDTO> getPatientSummary(@PathVariable UUID patientId) { ... }
```

## Data Encryption

### AES-256 Encryption

**Use Cases**:
- Journal entry content stored encrypted in database
- Sensitive PII (SSN, medical notes if applicable)
- Password hashing using bcrypt

**Implementation**:
```java
@Component
public class EncryptionUtil {
    private final String ALGORITHM = "AES/GCM/NoPadding";
    
    public String encrypt(String plainText) {
        // Uses ENCRYPTION_KEY from environment
        // Generates random IV for each encryption
        // Returns Base64-encoded ciphertext
    }
    
    public String decrypt(String encryptedText) {
        // Validates GCM authentication tag
        // Returns plaintext
    }
}
```

**Key Management**:
- Encryption key: 32+ characters, stored in environment variables
- Never commit encryption keys to version control
- Rotate keys periodically (implement re-encryption process)
- Store IV (Initialization Vector) with ciphertext

### Password Security

**Requirements**:
- Minimum 12 characters
- Must include uppercase, lowercase, numbers, special characters
- Hash using bcrypt (Spring Security default)
- Salt rounds: 12

**Implementation**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

## Validation & Input Sanitization

### Backend Validation

**DTO Level (Javax Validation)**:
```java
public class JournalEntryDTO {
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255)
    private String title;
    
    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 10000)
    private String content;
    
    @Email(message = "Invalid email format")
    private String sharedEmail;
}
```

**Service Layer**:
- Validate all inputs before processing
- Sanitize text fields to prevent XSS
- Validate enum values
- Check file sizes and types

### Frontend Validation

**Reactive Forms Validation**:
```typescript
const form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(12)]],
    content: ['', [Validators.required, Validators.maxLength(10000)]]
});
```

**HTML Escaping**:
```html
<!-- Angular automatically escapes {{ }} expressions -->
<div>{{ userInput }}</div>

<!-- For HTML binding (dangerous), use sanitizer -->
<div [innerHTML]="sanitizer.sanitize(SecurityContext.HTML, htmlContent)"></div>
```

## SQL Injection Prevention

**Implementation**:
```java
// ✓ SAFE: Using JPA with parameterized queries
@Query("SELECT j FROM JournalEntry j WHERE j.user.id = :userId")
List<JournalEntry> findByUserId(@Param("userId") UUID userId);

// ✓ SAFE: Repository methods
journalRepository.findByUserIdAndId(userId, journalId);

// ✗ UNSAFE (Never do this):
String query = "SELECT * FROM journal_entries WHERE id = '" + entryId + "'";
```

## CORS Configuration

**Backend (application.yml)**:
```yaml
spring:
  web:
    cors:
      allowed-origins: http://localhost:4200
      allowed-methods: GET,POST,PUT,DELETE,OPTIONS
      allowed-headers: "*"
      allow-credentials: true
      max-age: 3600
```

**Security Considerations**:
- Only allow specific frontend origins in production
- Restrict HTTP methods to necessary ones
- Restrict headers if possible
- Disable in production if not needed
- Use HTTPS only in production

## HTTPS/TLS

**Implementation**:
```yaml
# production environment
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

**Requirements**:
- Use TLS 1.2 or higher
- Valid SSL certificate from trusted CA
- HTTP → HTTPS redirect
- HSTS header enabled
- Cipher suites: TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384

## Audit Logging

**Logged Events**:
- User login/logout (including failures)
- Data access (especially by therapist)
- Alert creation and resolution
- Data modifications with timestamp and user
- Administrative actions

**Implementation**:
```java
@Aspect
@Component
public class AuditLoggingAspect {
    
    @After("@annotation(com.mindguard.annotation.Audit)")
    public void auditLog(JoinPoint joinPoint) {
        String action = joinPoint.getSignature().getName();
        String user = getCurrentUser();
        String timestamp = Instant.now().toString();
        
        auditRepository.log(action, user, timestamp);
    }
}
```

**Storage**:
- Log to file: `logs/mind-guard.log`
- Log to database for querying
- Implement log rotation and archival
- Protect log files from unauthorized access

## API Security

### Rate Limiting
```java
@Configuration
public class RateLimitingConfig {
    
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(100.0); // 100 requests per second
    }
}
```

### API Key Management
- If using API keys (for internal services):
  - Store hashed versions in database
  - Rotate regularly
  - Monitor usage

### Error Handling
```java
// ✓ GOOD: Generic error message
response.setMessage("Login failed. Check credentials.");

// ✗ BAD: Leaking information
response.setMessage("User 'john@example.com' not found in database");
```

## Dependency Security

**Maven**:
```bash
# Check for vulnerable dependencies
mvn org.owasp:dependency-check-maven:check

# Keep dependencies updated
mvn versions:display-dependency-updates
```

**Frontend**:
```bash
# Audit npm dependencies
npm audit

# Fix vulnerabilities
npm audit fix
```

## HIPAA Compliance (Mental Health Data)

**Key Requirements**:
1. **Access Control**
   - Only authorized personnel can access patient data
   - Role-based access implemented

2. **Audit Controls**
   - All access logged with timestamps
   - Implement audit trail review process

3. **Integrity Controls**
   - Data integrity checks (digital signatures for modifications)
   - Prevent unauthorized alterations
   - Maintain version history

4. **Transmission Security**
   - HTTPS only
   - TLS 1.2+
   - Encrypt data at rest (AES-256)

5. **Encryption**
   - Patient journal entries: encrypted at rest
   - Database: Use PostgreSQL full-disk encryption
   - Backups: Encrypted archives

6. **Breach Notification**
   - Implement incident response plan
   - Document and respond to breaches
   - Notify affected parties within required timeframe

## Security Testing

### OWASP Top 10 Checklist

- [ ] **A01:2021 – Broken Access Control**
  - Test unauthorized access to endpoints
  - Verify RBAC enforcement
  
- [ ] **A02:2021 – Cryptographic Failures**
  - Verify encryption implementation
  - Test with tools like Burp Suite
  
- [ ] **A03:2021 – Injection**
  - SQL injection testing
  - Command injection testing
  
- [ ] **A04:2021 – Insecure Design**
  - Review threat models
  - Test authentication flow
  
- [ ] **A05:2021 – Security Misconfiguration**
  - Check default credentials
  - Verify security headers
  
- [ ] **A06:2021 – Vulnerable & Outdated Components**
  - Run dependency checks
  - Keep libraries updated
  
- [ ] **A07:2021 – Identification & Authentication Failures**
  - Test weak passwords
  - Test session hijacking
  
- [ ] **A08:2021 – Software & Data Integrity Failures**
  - Verify signed dependencies
  - Check package authenticity
  
- [ ] **A09:2021 – Logging & Monitoring Failures**
  - Test logging functionality
  - Verify alerts work
  
- [ ] **A10:2021 – Server-Side Request Forgery (SSRF)**
  - Test API calls to HuggingFace
  - Verify URL validation

### Security Testing Commands

```bash
# Backend security testing
mvn sonar:sonar

# OWASP Dependency Check
mvn org.owasp:dependency-check-maven:check

# Angular security audit
ng lint --security

# Penetration testing baseline
# Use tools: Burp Suite, OWASP ZAP, Postman
```

## Environmental Security

### Development Environment
```
Features:
- Debug logging enabled
- CORS allows all origins (for testing)
- In-memory H2 database
- JWT expiration: 24 hours
- No SSL required
```

### Production Environment
```
Features:
- Debug logging disabled
- CORS restricted to specific origin
- PostgreSQL with SSL
- JWT expiration: depends on security policy
- SSL/TLS required (HTTPS only)
- Environment-specific secrets vault
```

## Incident Response Plan

1. **Detection**
   - Monitor audit logs
   - Set up alerts for suspicious activity
   - Review access patterns regularly

2. **Containment**
   - Revoke compromised tokens immediately
   - Disable compromised user accounts
   - Isolate affected systems if needed

3. **Eradication**
   - Identify root cause
   - Patch vulnerabilities
   - Rotate credentials

4. **Recovery**
   - Restore from clean backups
   - Verify system integrity
   - Monitor for recurrence

5. **Documentation**
   - Log all incident details
   - Notify affected parties (HIPAA requirement)
   - Prepare post-incident report

## Security Checklist Before Production

- [ ] JWT_SECRET changed and stored in environment variable
- [ ] Database password strong and stored in environment
- [ ] ENCRYPTION_KEY generated and stored securely
- [ ] CORS origins restricted to production frontend domain
- [ ] HTTPS/SSL certificate installed and valid
- [ ] Database backups automated and encrypted
- [ ] Audit logging enabled for all sensitive operations
- [ ] Dependency vulnerabilities checked and resolved
- [ ] Rate limiting enabled on API endpoints
- [ ] Error messages don't leak sensitive information
- [ ] Password policy enforced (12+ chars, complexity)
- [ ] All test data removed from production
- [ ] Admin credentials changed from defaults
- [ ] Security headers configured (HSTS, CSP, X-Frame-Options)
- [ ] API endpoints properly documented with auth requirements
- [ ] Penetration testing completed
- [ ] Security training provided to development team
- [ ] Incident response plan documented and tested

## References

- [OWASP Top 10](https://owasp.org/Top10/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Angular Security Guide](https://angular.io/guide/security)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8949)
- [HIPAA Security Rule](https://www.hhs.gov/hipaa/for-professionals/security/index.html)
- [PostgreSQL Security](https://www.postgresql.org/docs/current/sql-syntax.html)

---

**Last Updated**: 2026-05-25
**Version**: 1.0.0
**Reviewed By**: Security Team
