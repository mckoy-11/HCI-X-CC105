-- =========================================================
-- SAMPLE DATA FOR WCMS DATABASE
-- Test Accounts
-- =========================================================

USE wcms;

-- Insert sample accounts
INSERT INTO account (name, email_address, password, status, role, is_barangay_setup_complete) VALUES
('Municipal Admin', 'admin@municipal.gov', 'admin123', 'Active', 'MENRO', TRUE),
('Barangay Official', 'barangay@gmail.com', 'barangay123', 'Active', 'BARANGAY', FALSE);

-- Verify inserts
SELECT * FROM account;
