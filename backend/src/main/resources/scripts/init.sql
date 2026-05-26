-- Mind-Guard Database Initialization Script
-- PostgreSQL Database Schema

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Drop existing tables if they exist (for fresh setup)
-- In production, use Flyway/Liquibase for migrations
DROP TABLE IF EXISTS alert_responses CASCADE;
DROP TABLE IF EXISTS alerts CASCADE;
DROP TABLE IF EXISTS journal_analyses CASCADE;
DROP TABLE IF EXISTS journal_entries CASCADE;
DROP TABLE IF EXISTS mood_logs CASCADE;
DROP TABLE IF EXISTS therapist_patients CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('PATIENT', 'THERAPIST', 'ADMIN')),
    phone VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(20),
    is_active BOOLEAN DEFAULT true,
    is_email_verified BOOLEAN DEFAULT false,
    profile_picture_url VARCHAR(500),
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

-- Therapist-Patient Relationship
CREATE TABLE therapist_patients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    therapist_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    patient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    is_active BOOLEAN DEFAULT true,
    UNIQUE(therapist_id, patient_id)
);

-- Journal Entries Table
CREATE TABLE journal_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    content_encrypted BYTEA,  -- Encrypted content
    is_shared_with_therapist BOOLEAN DEFAULT false,
    is_flagged BOOLEAN DEFAULT false,
    tags JSONB,  -- JSON array of tags
    mood_before VARCHAR(50),  -- Pre-entry mood
    mood_after VARCHAR(50),   -- Post-entry mood
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Mood Logs Table
CREATE TABLE mood_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mood_level INTEGER NOT NULL CHECK (mood_level >= 1 AND mood_level <= 10),
    mood_category VARCHAR(50) NOT NULL,  -- Happy, Sad, Angry, Anxious, Calm, etc.
    energy_level INTEGER CHECK (energy_level >= 1 AND energy_level <= 10),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Journal Analyses Table (AI Analysis Results)
CREATE TABLE journal_analyses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    journal_entry_id UUID NOT NULL REFERENCES journal_entries(id) ON DELETE CASCADE,
    sentiment VARCHAR(50) NOT NULL CHECK (sentiment IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE')),
    sentiment_score DECIMAL(3, 2),  -- 0.00 to 1.00
    distress_level VARCHAR(50) NOT NULL CHECK (distress_level IN ('LOW', 'MEDIUM', 'HIGH')),
    distress_score DECIMAL(3, 2),  -- 0.00 to 1.00
    self_harm_indicators BOOLEAN DEFAULT false,
    self_harm_confidence DECIMAL(3, 2),
    risk_score DECIMAL(5, 2) NOT NULL CHECK (risk_score >= 0 AND risk_score <= 100),
    wellness_guidance TEXT,
    analysis_metadata JSONB,  -- Store additional AI model outputs
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    analyzed_by VARCHAR(100)  -- Model version/name
);

-- Alerts Table
CREATE TABLE alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    journal_entry_id UUID NOT NULL REFERENCES journal_entries(id) ON DELETE CASCADE,
    therapist_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    patient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    alert_type VARCHAR(50) NOT NULL CHECK (alert_type IN ('EMERGENCY', 'URGENT', 'STANDARD')),
    risk_score DECIMAL(5, 2),
    alert_message TEXT NOT NULL,
    alert_summary TEXT,
    is_acknowledged BOOLEAN DEFAULT false,
    acknowledged_at TIMESTAMP,
    acknowledged_by UUID REFERENCES users(id),
    is_resolved BOOLEAN DEFAULT false,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Alert Responses Table
CREATE TABLE alert_responses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    alert_id UUID NOT NULL REFERENCES alerts(id) ON DELETE CASCADE,
    therapist_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    response_text TEXT NOT NULL,
    action_taken VARCHAR(255),  -- Action description
    follow_up_required BOOLEAN DEFAULT false,
    follow_up_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Indexes
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_journal_user_date ON journal_entries(user_id, created_at);
CREATE INDEX idx_journal_shared ON journal_entries(is_shared_with_therapist);
CREATE INDEX idx_mood_user_date ON mood_logs(user_id, created_at);
CREATE INDEX idx_analysis_sentiment ON journal_analyses(sentiment);
CREATE INDEX idx_analysis_distress ON journal_analyses(distress_level);
CREATE INDEX idx_alerts_therapy_patient ON alerts(therapist_id, patient_id);
CREATE INDEX idx_alerts_patient ON alerts(patient_id);
CREATE INDEX idx_alert_responses_alert ON alert_responses(alert_id);

-- Create Updated At Trigger Function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply Updated At Triggers
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_journal_entries_updated_at
    BEFORE UPDATE ON journal_entries
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alerts_updated_at
    BEFORE UPDATE ON alerts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alert_responses_updated_at
    BEFORE UPDATE ON alert_responses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Sample Data (for development only)
-- Users
INSERT INTO users (username, email, password_hash, first_name, last_name, role, is_active, is_email_verified)
VALUES
    ('patient1', 'patient1@mindguard.com', '$2a$10$dummy_hash_bcrypt', 'John', 'Doe', 'PATIENT', true, true),
    ('patient2', 'patient2@mindguard.com', '$2a$10$dummy_hash_bcrypt', 'Jane', 'Smith', 'PATIENT', true, true),
    ('therapist1', 'therapist1@mindguard.com', '$2a$10$dummy_hash_bcrypt', 'Dr. Alice', 'Johnson', 'THERAPIST', true, true),
    ('therapist2', 'therapist2@mindguard.com', '$2a$10$dummy_hash_bcrypt', 'Dr. Bob', 'Williams', 'THERAPIST', true, true),
    ('admin', 'admin@mindguard.com', '$2a$10$dummy_hash_bcrypt', 'Admin', 'User', 'ADMIN', true, true)
ON CONFLICT DO NOTHING;

-- Therapist-Patient Assignments
INSERT INTO therapist_patients (therapist_id, patient_id, notes)
SELECT
    (SELECT id FROM users WHERE username = 'therapist1'),
    (SELECT id FROM users WHERE username = 'patient1'),
    'Primary therapist for patient 1'
WHERE NOT EXISTS (
    SELECT 1 FROM therapist_patients
    WHERE therapist_id = (SELECT id FROM users WHERE username = 'therapist1')
    AND patient_id = (SELECT id FROM users WHERE username = 'patient1')
);

INSERT INTO therapist_patients (therapist_id, patient_id, notes)
SELECT
    (SELECT id FROM users WHERE username = 'therapist1'),
    (SELECT id FROM users WHERE username = 'patient2'),
    'Primary therapist for patient 2'
WHERE NOT EXISTS (
    SELECT 1 FROM therapist_patients
    WHERE therapist_id = (SELECT id FROM users WHERE username = 'therapist1')
    AND patient_id = (SELECT id FROM users WHERE username = 'patient2')
);

-- Verify Installation
SELECT 'Mind-Guard Database Schema Initialized Successfully' as status;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as therapist_patient_count FROM therapist_patients;
