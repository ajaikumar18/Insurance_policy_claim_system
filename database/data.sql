-- Seed data for testing
-- All passwords are 'password123' (BCrypt hashed)
INSERT INTO users (username, email, password_hash, role_enum, office_id, certification_number, is_active, legal_name, tax_id, address, contact_number)
VALUES
('admin', 'admin@ipcms.local', '$2a$10$7qB2zBqFw.mH5F2YJqf.U.E/bT0Yh5m3B3/lM.z9oN.XgC0LwX2qK', 'ADMIN', 'Lagos', 'SYS-2024-001', TRUE, 'System Admin', 'TAX-001', '123 Admin St', '1234567890'),
('underwriter', 'underwriter@ipcms.local', '$2a$10$7qB2zBqFw.mH5F2YJqf.U.E/bT0Yh5m3B3/lM.z9oN.XgC0LwX2qK', 'UNDERWRITER', 'London', 'UW-2018-003', TRUE, 'Peter Hawthorne', 'TAX-002', '456 Underwriter Rd', '1234567890'),
('claims_handler', 'claims_handler@ipcms.local', '$2a$10$7qB2zBqFw.mH5F2YJqf.U.E/bT0Yh5m3B3/lM.z9oN.XgC0LwX2qK', 'CLAIMS_HANDLER', 'Stockholm', 'CH-2020-018', TRUE, 'Stefan Lindqvist', 'TAX-003', '789 Handler Ave', '1234567890'),
('agent', 'agent@ipcms.local', '$2a$10$7qB2zBqFw.mH5F2YJqf.U.E/bT0Yh5m3B3/lM.z9oN.XgC0LwX2qK', 'AGENT', 'Dakar', 'AG-2023-029', TRUE, 'Amara Diallo', 'TAX-004', '101 Agent Blvd', '1234567890'),
('policyholder', 'policyholder@ipcms.local', '$2a$10$7qB2zBqFw.mH5F2YJqf.U.E/bT0Yh5m3B3/lM.z9oN.XgC0LwX2qK', 'POLICYHOLDER', 'Singapore', 'PH-2024-055', TRUE, 'John Meridian', 'TAX-005', '202 Policyholder Lane', '1234567890')
ON CONFLICT (email) DO NOTHING;
