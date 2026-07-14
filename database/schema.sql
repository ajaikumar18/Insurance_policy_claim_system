-- Database Schema Definitions for Insurance Policy & Claims Management System

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_enum VARCHAR(50) NOT NULL,
    office_id VARCHAR(100),
    certification_number VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    legal_name VARCHAR(150),
    tax_id VARCHAR(50),
    address TEXT,
    contact_number VARCHAR(50),
    aml_flagged BOOLEAN NOT NULL DEFAULT FALSE,
    aml_status VARCHAR(50) NOT NULL DEFAULT 'CLEARED'
);

CREATE TABLE IF NOT EXISTS policies (
    id SERIAL PRIMARY KEY,
    policy_number VARCHAR(30) UNIQUE NOT NULL,
    policyholder_id INTEGER NOT NULL REFERENCES users(id),
    product_type VARCHAR(100) NOT NULL,
    base_premium DECIMAL(15,2) NOT NULL CHECK (base_premium >= 0),
    active_reserve DECIMAL(15,2) DEFAULT 0.00,
    expiry_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS claims (
    id SERIAL PRIMARY KEY,
    claim_number VARCHAR(30) UNIQUE NOT NULL,
    policy_id INTEGER NOT NULL REFERENCES policies(id),
    reporter_id INTEGER NOT NULL REFERENCES users(id),
    incident_date DATE NOT NULL,
    incident_type VARCHAR(100),
    incident_location VARCHAR(255),
    gross_reserve DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    loss_description TEXT,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS underwrite_overrides (
    id SERIAL PRIMARY KEY,
    policy_number VARCHAR(30) NOT NULL,
    override_type VARCHAR(100) NOT NULL,
    delta_value VARCHAR(100) NOT NULL,
    requested_by VARCHAR(150) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    supervisor_signoff_by VARCHAR(150),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS policy_history (
    id SERIAL PRIMARY KEY,
    policy_id INTEGER NOT NULL REFERENCES policies(id),
    policy_number VARCHAR(30) NOT NULL,
    product_type VARCHAR(100) NOT NULL,
    base_premium DECIMAL(15,2) NOT NULL,
    active_reserve DECIMAL(15,2) DEFAULT 0.00,
    expiry_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_email VARCHAR(150) NOT NULL,
    action VARCHAR(100) NOT NULL,
    target VARCHAR(100) NOT NULL,
    detail TEXT
);
