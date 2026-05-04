-- wcms_schema.sql
-- Normalized WCMS database schema in Third Normal Form (3NF)
-- Includes lookup/reference tables, foreign keys, and sample parent table data.

DROP DATABASE IF EXISTS wcms;
CREATE DATABASE wcms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wcms;

-- =====================
-- DDL SECTION
-- =====================

-- Lookup tables
CREATE TABLE role_lookup (
    role_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_key VARCHAR(50) NOT NULL UNIQUE,
    role_label VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE gender_lookup (
    gender_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    gender_key VARCHAR(50) NOT NULL UNIQUE,
    gender_label VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE status_domain (
    status_domain_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    domain_key VARCHAR(50) NOT NULL UNIQUE,
    domain_label VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE status_lookup (
    status_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    status_domain_id INT NOT NULL,
    status_key VARCHAR(50) NOT NULL,
    status_label VARCHAR(100) NOT NULL,
    UNIQUE KEY ux_status_domain_key (status_domain_id, status_key),
    FOREIGN KEY (status_domain_id) REFERENCES status_domain(status_domain_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE complaint_type (
    complaint_type_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type_key VARCHAR(50) NOT NULL UNIQUE,
    type_label VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE report_type (
    report_type_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type_key VARCHAR(50) NOT NULL UNIQUE,
    type_label VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE request_type (
    request_type_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type_key VARCHAR(50) NOT NULL UNIQUE,
    type_label VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE truck_type_lookup (
    truck_type_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    truck_type_key VARCHAR(50) NOT NULL UNIQUE,
    truck_type_label VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE entry_type_lookup (
    entry_type_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    entry_type_key VARCHAR(50) NOT NULL UNIQUE,
    entry_type_label VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- Core tables
CREATE TABLE account (
    account_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status_id INT NOT NULL,
    role_id INT NOT NULL,
    is_barangay_setup_complete TINYINT(1) NOT NULL DEFAULT 0,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role_lookup(role_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE barangay (
    barangay_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_name VARCHAR(255) NOT NULL,
    barangay_household INT NOT NULL DEFAULT 0,
    purok_count INT NOT NULL DEFAULT 0,
    population INT NOT NULL DEFAULT 0,
    contact VARCHAR(50) NULL,
    collection_day VARCHAR(50) NULL,
    status_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE barangay_admin (
    barangay_admin_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NULL,
    account_id INT NOT NULL,
    admin_name VARCHAR(255) NULL,
    age INT NULL,
    gender_id INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY ux_barangay_admin_account (account_id),
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (account_id) REFERENCES account(account_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (gender_id) REFERENCES gender_lookup(gender_id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE announcement (
    announcement_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NULL,
    message TEXT NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    is_archived TINYINT(1) NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    archived_at TIMESTAMP NULL
) ENGINE=InnoDB;

CREATE TABLE complaint (
    complaint_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NULL,
    complaint_type_id INT NULL,
    message TEXT NOT NULL,
    proof LONGBLOB NULL,
    status_id INT NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    is_archived TINYINT(1) NOT NULL DEFAULT 0,
    archived_at TIMESTAMP NULL,
    location VARCHAR(255) NULL,
    response_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (complaint_type_id) REFERENCES complaint_type(complaint_type_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE report (
    report_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NULL,
    report_type_id INT NULL,
    message TEXT NOT NULL,
    proof LONGBLOB NULL,
    status_id INT NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    is_archived TINYINT(1) NOT NULL DEFAULT 0,
    archived_at TIMESTAMP NULL,
    response_message TEXT NULL,
    purok_analytics TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (report_type_id) REFERENCES report_type(report_type_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE request (
    request_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NULL,
    request_type_id INT NULL,
    message TEXT NOT NULL,
    proof LONGBLOB NULL,
    status_id INT NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    is_archived TINYINT(1) NOT NULL DEFAULT 0,
    archived_at TIMESTAMP NULL,
    location VARCHAR(255) NULL,
    response_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (request_type_id) REFERENCES request_type(request_type_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE personnel (
    personnel_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    personnel_name VARCHAR(255) NOT NULL,
    age INT NULL,
    gender_id INT NULL,
    address VARCHAR(255) NULL,
    contact_number VARCHAR(50) NULL,
    team_id INT NULL,
    role_id INT NOT NULL,
    status_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES role_lookup(role_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (gender_id) REFERENCES gender_lookup(gender_id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE truck (
    truck_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(50) NOT NULL UNIQUE,
    truck_type_id INT NOT NULL,
    capacity VARCHAR(100) NULL,
    status_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (truck_type_id) REFERENCES truck_type_lookup(truck_type_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE team (
    team_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(255) NOT NULL,
    leader_id INT NULL,
    driver_id INT NULL,
    truck_id INT NULL,
    status_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (truck_id) REFERENCES truck(truck_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE team_collectors (
    team_id INT NOT NULL,
    personnel_id INT NOT NULL,
    PRIMARY KEY (team_id, personnel_id),
    FOREIGN KEY (team_id) REFERENCES team(team_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (personnel_id) REFERENCES personnel(personnel_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE schedule (
    schedule_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NOT NULL,
    team_id INT NOT NULL,
    schedule_date DATE NOT NULL,
    schedule_time TIME NOT NULL,
    status_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (team_id) REFERENCES team(team_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE purok_checklist (
    checklist_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NOT NULL,
    purok_name VARCHAR(100) NOT NULL,
    is_collected TINYINT(1) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE entry_attachment (
    attachment_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    entry_type_id INT NOT NULL,
    entry_id INT NOT NULL,
    image_blob LONGBLOB NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (entry_type_id) REFERENCES entry_type_lookup(entry_type_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Indexes
CREATE INDEX idx_account_status ON account(status_id);
CREATE INDEX idx_account_role ON account(role_id);
CREATE INDEX idx_barangay_status ON barangay(status_id);
CREATE INDEX idx_complaint_barangay ON complaint(barangay_id);
CREATE INDEX idx_report_barangay ON report(barangay_id);
CREATE INDEX idx_request_barangay ON request(barangay_id);
CREATE INDEX idx_personnel_status ON personnel(status_id);
CREATE INDEX idx_personnel_role ON personnel(role_id);
CREATE INDEX idx_team_status ON team(status_id);
CREATE INDEX idx_schedule_date ON schedule(schedule_date);
CREATE INDEX idx_purok_barangay ON purok_checklist(barangay_id);

-- Add circular foreign key constraints
ALTER TABLE personnel ADD CONSTRAINT fk_personnel_team 
    FOREIGN KEY (team_id) REFERENCES team(team_id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE team ADD CONSTRAINT fk_team_leader 
    FOREIGN KEY (leader_id) REFERENCES personnel(personnel_id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE team ADD CONSTRAINT fk_team_driver 
    FOREIGN KEY (driver_id) REFERENCES personnel(personnel_id) ON DELETE SET NULL ON UPDATE CASCADE;

-- =====================
-- DML USED DAO SECTION
-- =====================
-- ACCOUNT DAO
SELECT a.account_id, a.name, a.email_address, a.password, s.status_label as status, r.role_key as role, a.last_login, a.is_barangay_setup_complete
INSERT INTO account(name, email_address, password, status_id, role_id, is_barangay_setup_complete) VALUES (?, ?, ?, ?, ?, ?)
UPDATE account SET name=?, email_address=?, password=?, status_id=?, role_id=? WHERE account_id=?
UPDATE account SET last_login = NOW() WHERE account_id = ?
UPDATE account SET status_id = ? WHERE account_id = ?
DELETE FROM barangay_admin WHERE account_id = ?
DELETE FROM account WHERE account_id = ?
SELECT COUNT(*) FROM account WHERE email_address = ?
SELECT barangay_admin_id FROM barangay_admin WHERE account_id = ?
UPDATE barangay_admin SET barangay_id = ? WHERE account_id = ?
INSERT INTO barangay_admin (barangay_id, account_id) VALUES (?, ?)
SELECT barangay_admin_id, barangay_id FROM barangay_admin WHERE account_id = ?
UPDATE barangay_admin SET barangay_admin = ?, age = ?, gender_id = ? WHERE account_id = ?
INSERT INTO barangay_admin (barangay_id, account_id, barangay_admin, age, gender_id) VALUES (?, ?, ?, ?, ?)
SELECT barangay_admin_id FROM barangay_admin WHERE account_id = ?
UPDATE barangay_admin SET barangay_id = ? WHERE account_id = ?
SELECT barangay_id FROM barangay WHERE UPPER(TRIM(barangay_name)) = UPPER(TRIM(?))
ALTER TABLE account ADD COLUMN is_barangay_setup_complete BOOLEAN DEFAULT FALSE
UPDATE account SET is_barangay_setup_complete = ? WHERE account_id = ?
SELECT status_id FROM status_lookup WHERE status_key = ?
SELECT role_id FROM role_lookup WHERE role_key = ?
SELECT gender_id FROM gender_lookup WHERE gender_name = ?

-- ANNOUNCEMENT DAO
SELECT * FROM announcement WHERE is_active = 1 AND is_archived = 0 ORDER BY created_at DESC LIMIT 1
SELECT * FROM announcement WHERE is_archived = 1 ORDER BY archived_at DESC, created_at DESC
INSERT INTO announcement (title, message, is_active, is_archived, created_at, expires_at) VALUES (?, ?, 1, 0, CURRENT_TIMESTAMP, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY))
UPDATE announcement SET is_active = 0, is_archived = 1, archived_at = CURRENT_TIMESTAMP WHERE announcement_id = ?
INSERT INTO announcement (title, message, is_active, is_archived, created_at, expires_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
UPDATE announcement SET title = ?, message = ?, is_active = ?, is_archived = ?, expires_at = ? WHERE announcement_id = ?
UPDATE announcement SET is_active = 0, is_archived = 1, archived_at = CURRENT_TIMESTAMP WHERE is_active = 1 AND is_archived = 0 AND expires_at IS NOT NULL AND expires_at <= CURRENT_TIMESTAMP
UPDATE announcement SET is_active = 0, is_archived = 1, archived_at = CURRENT_TIMESTAMP WHERE is_active = 1 AND is_archived = 0
CREATE TABLE announcement (announcement_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255) NULL, message TEXT NOT NULL, is_active TINYINT(1) NOT NULL DEFAULT 1, is_archived TINYINT(1) NOT NULL DEFAULT 0, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, archived_at TIMESTAMP NULL, expires_at TIMESTAMP NULL)

-- ATTACHMENT DAO
DELETE FROM entry_attachment WHERE entry_type = ? AND entry_id = ?
INSERT INTO entry_attachment (entry_type, entry_id, image_blob) VALUES (?, ?, ?)
SELECT attachment_id, entry_type, entry_id, image_blob FROM entry_attachment WHERE entry_type = ? AND entry_id = ? ORDER BY attachment_id ASC
CREATE TABLE entry_attachment (attachment_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, entry_type VARCHAR(30) NOT NULL, entry_id INT NOT NULL, image_blob LONGBLOB NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)

-- BARANGAY DAO
INSERT INTO barangay(barangay_name, barangay_household, purok_count, population, contact, collection_day, status_id) VALUES (?, ?, ?, ?, ?, ?, ?)
UPDATE barangay SET barangay_name = ?, barangay_household = ?, purok_count = ?, population = ?, contact = ?, collection_day = ?, status_id = ? WHERE barangay_id = ?
DELETE FROM barangay WHERE barangay_id = ?
SELECT b.*, sl.status_label as status FROM barangay b LEFT JOIN status_lookup sl ON b.status_id = sl.status_id WHERE b.barangay_id = ?
SELECT b.*, sl.status_label as status FROM barangay b LEFT JOIN status_lookup sl ON b.status_id = sl.status_id ORDER BY b.barangay_name
SELECT b.*, sl.status_label as status FROM barangay b LEFT JOIN status_lookup sl ON b.status_id = sl.status_id WHERE UPPER(TRIM(b.barangay_name)) = UPPER(TRIM(?))
SELECT b.*, sl.status_label as status FROM barangay b LEFT JOIN status_lookup sl ON b.status_id = sl.status_id WHERE b.collection_day = ? ORDER BY b.barangay_name
SELECT COUNT(*) FROM barangay b JOIN status_lookup sl ON b.status_id = sl.status_id WHERE sl.status_key = 'SCHEDULED'
SELECT COUNT(*) FROM barangay
SELECT COALESCE(SUM(barangay_household), 0) FROM barangay
SELECT COALESCE(SUM(population), 0) FROM barangay
ALTER TABLE barangay ADD COLUMN purok_count INT DEFAULT 0
ALTER TABLE barangay ADD COLUMN population INT DEFAULT 0
SELECT status_id FROM status_lookup WHERE status_key = ? AND status_domain_id = 2

-- COMPLAINT
INSERT INTO complaint (barangay_id, complaint_type_id, message, proof, status_id, is_read, is_archived, location, response_message) VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?)
UPDATE complaint SET barangay_id = ?, complaint_type_id = ?, message = ?, proof = ?, status_id = ?, is_read = ?, is_archived = ?, archived_at = ?, location = ?, response_message = ? WHERE complaint_id = ?
DELETE FROM complaint WHERE complaint_id = ?
SELECT c.*, ct.complaint_type_name as type, s.status_name as status, b.barangay_name FROM complaint c LEFT JOIN complaint_type ct ON c.complaint_type_id = ct.complaint_type_id LEFT JOIN status_lookup s ON c.status_id = s.status_id LEFT JOIN barangay b ON c.barangay_id = b.barangay_id WHERE c.complaint_id = ?
SELECT c.*, ct.complaint_type_name as type, s.status_name as status, b.barangay_name FROM complaint c LEFT JOIN complaint_type ct ON c.complaint_type_id = ct.complaint_type_id LEFT JOIN status_lookup s ON c.status_id = s.status_id LEFT JOIN barangay b ON c.barangay_id = b.barangay_id WHERE c.is_archived = 0 ORDER BY c.created_at DESC
SELECT c.*, ct.complaint_type_name as type, s.status_name as status, b.barangay_name FROM complaint c LEFT JOIN complaint_type ct ON c.complaint_type_id = ct.complaint_type_id LEFT JOIN status_lookup s ON c.status_id = s.status_id LEFT JOIN barangay b ON c.barangay_id = b.barangay_id WHERE c.is_archived = 1 ORDER BY c.created_at DESC
SELECT c.*, ct.complaint_type_name as type, s.status_name as status, b.barangay_name FROM complaint c LEFT JOIN complaint_type ct ON c.complaint_type_id = ct.complaint_type_id LEFT JOIN status_lookup s ON c.status_id = s.status_id LEFT JOIN barangay b ON c.barangay_id = b.barangay_id WHERE c.is_archived = 0 AND s.status_name = ? ORDER BY c.complaint_id DESC
SELECT * FROM complaint WHERE is_archived = 0 AND (is_read = FALSE OR is_read IS NULL) ORDER BY complaint_id DESC
UPDATE complaint SET is_read = TRUE WHERE complaint_id = ?
UPDATE complaint SET status_id = ? WHERE complaint_id = ?
SELECT COUNT(*) FROM complaint WHERE is_archived = 0
SELECT COUNT(*) FROM complaint WHERE is_archived = 1
SELECT COUNT(*) FROM complaint c LEFT JOIN status_lookup s ON c.status_id = s.status_id WHERE c.is_archived = 0 AND s.status_name = ?
SELECT COUNT(*) FROM complaint WHERE is_archived = 0 AND (is_read = FALSE OR is_read IS NULL)
UPDATE complaint SET is_archived = 1, archived_at = CURRENT_TIMESTAMP WHERE is_archived = 0 AND created_at <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)
SELECT complaint_type_id FROM complaint_type WHERE complaint_type_name = ?
SELECT status_id FROM status_lookup WHERE status_name = ?

-- HOME DAO
SELECT COUNT(*) AS count FROM schedule WHERE schedule_date = ?
SELECT b.barangay_name FROM schedule s JOIN barangay b ON s.barangay_id = b.barangay_id WHERE s.schedule_date = ? LIMIT 1
SELECT COUNT(*) AS count FROM schedule s JOIN status_lookup sl ON s.status_id = sl.status_id WHERE sl.status_key = 'COMPLETED'
SELECT b.barangay_name FROM schedule s JOIN barangay b ON s.barangay_id = b.barangay_id JOIN status_lookup sl ON s.status_id = sl.status_id WHERE sl.status_key = 'COMPLETED' LIMIT 1
SELECT COUNT(*) AS count FROM schedule s JOIN status_lookup sl ON s.status_id = sl.status_id WHERE s.schedule_date < ? AND sl.status_key != 'COMPLETED'
SELECT b.barangay_name FROM schedule s JOIN barangay b ON s.barangay_id = b.barangay_id JOIN status_lookup sl ON s.status_id = sl.status_id WHERE s.schedule_date < ? AND sl.status_key != 'COMPLETED' LIMIT 1
SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'wcms' AND table_name = 'complaint'
SELECT COUNT(*) AS count FROM complaint WHERE is_read = 0 OR is_read IS NULL

-- NOTIFICATION DAO
SELECT COUNT(*) FROM barangay
SELECT COUNT(*) FROM schedule
SELECT COUNT(*) FROM report

-- PERSONNEL DAO
SELECT p.*, gl.gender_label AS gender, rl.role_label AS role, sl.status_label AS status FROM personnel p LEFT JOIN gender_lookup gl ON p.gender_id = gl.gender_id LEFT JOIN role_lookup rl ON p.role_id = rl.role_id LEFT JOIN status_lookup sl ON p.status_id = sl.status_id ORDER BY p.personnel_name ASC
SELECT p.*, gl.gender_label AS gender, rl.role_label AS role, sl.status_label AS status FROM personnel p LEFT JOIN gender_lookup gl ON p.gender_id = gl.gender_id LEFT JOIN role_lookup rl ON p.role_id = rl.role_id LEFT JOIN status_lookup sl ON p.status_id = sl.status_id WHERE (p.team_name IS NULL OR TRIM(p.team_name) = '') ORDER BY p.personnel_name ASC
SELECT p.*, gl.gender_label AS gender, rl.role_label AS role, sl.status_label AS status FROM personnel p LEFT JOIN gender_lookup gl ON p.gender_id = gl.gender_id LEFT JOIN role_lookup rl ON p.role_id = rl.role_id LEFT JOIN status_lookup sl ON p.status_id = sl.status_id WHERE p.personnel_id = ?
SELECT p.*, gl.gender_label AS gender, rl.role_label AS role, sl.status_label AS status FROM personnel p LEFT JOIN gender_lookup gl ON p.gender_id = gl.gender_id LEFT JOIN role_lookup rl ON p.role_id = rl.role_id LEFT JOIN status_lookup sl ON p.status_id = sl.status_id WHERE rl.role_label = ? OR rl.role_key = UPPER(?) ORDER BY p.personnel_name ASC
INSERT INTO personnel (personnel_name, age, gender_id, address, contact_number, team_id, role_id, status_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
UPDATE personnel SET personnel_name = ?, age = ?, gender_id = ?, address = ?, contact_number = ?, team_id = ?, role_id = ?, status_id = ? WHERE personnel_id = ?
DELETE FROM personnel WHERE personnel_id = ?
UPDATE personnel SET status_id = ? WHERE personnel_id = ?
SELECT COUNT(*) AS count FROM personnel
SELECT COUNT(*) AS count FROM personnel p JOIN status_lookup sl ON p.status_id = sl.status_id WHERE sl.status_key = 'ACTIVE'
SELECT COUNT(*) AS count FROM personnel p JOIN status_lookup sl ON p.status_id = sl.status_id WHERE sl.status_key = 'UNASSIGNED'
SELECT status_id FROM status_lookup WHERE status_key = ? AND status_domain_id = 10
SELECT role_id FROM role_lookup WHERE role_key = ?
SELECT gender_id FROM gender_lookup WHERE gender_key = ?

-- PUROK CHECKLIST DAO
SELECT checklist_id, barangay_id, purok_name, is_collected, updated_at FROM purok_checklist WHERE barangay_id = ? ORDER BY checklist_id ASC
UPDATE purok_checklist SET is_collected = ?, updated_at = CURRENT_TIMESTAMP WHERE checklist_id = ?
SELECT COUNT(*) FROM purok_checklist WHERE barangay_id = ?
INSERT INTO purok_checklist (barangay_id, purok_name, is_collected) VALUES (?, ?, 0)
CREATE TABLE purok_checklist (checklist_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, barangay_id INT NOT NULL, purok_name VARCHAR(100) NOT NULL, is_collected TINYINT(1) NOT NULL DEFAULT 0, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)

-- REPORT DAO
INSERT INTO report (message, proof, status_id, is_read, barangay_id, report_type_id, response_message, purok_analytics, is_archived) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
UPDATE report SET message = ?, proof = ?, status_id = ?, is_read = ?, barangay_id = ?, report_type_id = ?, response_message = ?, purok_analytics = ?, is_archived = ?, archived_at = ? WHERE report_id = ?
DELETE FROM report WHERE report_id = ?
SELECT r.*, rt.report_type_name as type, s.status_name as status, b.barangay_name FROM report r LEFT JOIN report_type rt ON r.report_type_id = rt.report_type_id LEFT JOIN status_lookup s ON r.status_id = s.status_id LEFT JOIN barangay b ON r.barangay_id = b.barangay_id WHERE r.report_id = ?
SELECT r.*, rt.report_type_name as type, s.status_name as status, b.barangay_name FROM report r LEFT JOIN report_type rt ON r.report_type_id = rt.report_type_id LEFT JOIN status_lookup s ON r.status_id = s.status_id LEFT JOIN barangay b ON r.barangay_id = b.barangay_id WHERE r.is_archived = 0 ORDER BY r.created_at DESC
SELECT r.*, rt.report_type_name as type, s.status_name as status, b.barangay_name FROM report r LEFT JOIN report_type rt ON r.report_type_id = rt.report_type_id LEFT JOIN status_lookup s ON r.status_id = s.status_id LEFT JOIN barangay b ON r.barangay_id = b.barangay_id WHERE r.is_archived = 1 ORDER BY CASE WHEN r.archived_at IS NULL THEN 0 ELSE 1 END DESC, r.created_at DESC
SELECT r.*, rt.report_type_name as type, s.status_name as status, b.barangay_name FROM report r LEFT JOIN report_type rt ON r.report_type_id = rt.report_type_id LEFT JOIN status_lookup s ON r.status_id = s.status_id LEFT JOIN barangay b ON r.barangay_id = b.barangay_id WHERE r.is_archived = 0 AND s.status_name = ? ORDER BY r.report_id DESC
SELECT r.*, rt.report_type_name as type, s.status_name as status, b.barangay_name FROM report r LEFT JOIN report_type rt ON r.report_type_id = rt.report_type_id LEFT JOIN status_lookup s ON r.status_id = s.status_id LEFT JOIN barangay b ON r.barangay_id = b.barangay_id WHERE r.is_archived = 0 AND (r.is_read = FALSE OR r.is_read IS NULL) ORDER BY r.report_id DESC
UPDATE report SET is_read = TRUE WHERE report_id = ?
UPDATE report SET status_id = ? WHERE report_id = ?
SELECT COUNT(*) FROM report WHERE is_archived = 0
SELECT COUNT(*) FROM report WHERE is_archived = 1
SELECT COUNT(*) FROM report r LEFT JOIN status_lookup s ON r.status_id = s.status_id WHERE r.is_archived = 0 AND s.status_name = ?
SELECT COUNT(*) FROM report WHERE is_archived = 0 AND (is_read = FALSE OR is_read IS NULL)
UPDATE report SET is_archived = 1, archived_at = CURRENT_TIMESTAMP WHERE is_archived = 0 AND created_at <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)
SELECT report_type_id FROM report_type WHERE report_type_name = ?
SELECT status_id FROM status_lookup WHERE status_name = ?

-- REQUEST DAO
INSERT INTO request (barangay_id, request_type_id, message, proof, status_id, is_read, is_archived, location, response_message) VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?)
UPDATE request SET barangay_id = ?, request_type_id = ?, message = ?, proof = ?, status_id = ?, is_read = ?, is_archived = ?, archived_at = ?, location = ?, response_message = ? WHERE request_id = ?
DELETE FROM request WHERE request_id = ?
SELECT r.*, rt.request_type_name as type, s.status_name as status, b.barangay_name FROM request r LEFT JOIN request_type rt ON r.request_type_id = rt.request_type_id LEFT JOIN status_lookup s ON r.status_id = s.status_id LEFT JOIN barangay b ON r.barangay_id = b.barangay_id WHERE r.request_id = ?
SELECT r.*, rt.request_type_name as type, s.status_name as status, b.barangay_name FROM request r LEFT JOIN request_type rt ON r.request_type_id = rt.request_type_id LEFT JOIN status_lookup s ON r.status_id = s.status_id LEFT JOIN barangay b ON r.barangay_id = b.barangay_id WHERE r.is_archived = 0 ORDER BY r.created_at DESC
SELECT r.*, rt.request_type_name as type, s.status_name as status, b.barangay_name FROM request r LEFT JOIN request_type rt ON r.request_type_id = rt.request_type_id LEFT JOIN status_lookup s ON r.status_id = s.status_id LEFT JOIN barangay b ON r.barangay_id = b.barangay_id WHERE r.is_archived = 1 ORDER BY r.created_at DESC
SELECT r.*, rt.request_type_name as type, s.status_name as status, b.barangay_name FROM request r LEFT JOIN request_type rt ON r.request_type_id = rt.request_type_id LEFT JOIN status_lookup s ON r.status_id = s.status_id LEFT JOIN barangay b ON r.barangay_id = b.barangay_id WHERE r.is_archived = 0 AND s.status_name = ? ORDER BY r.request_id DESC
SELECT * FROM request WHERE is_archived = 0 AND (is_read = FALSE OR is_read IS NULL) ORDER BY request_id DESC
UPDATE request SET is_read = TRUE WHERE request_id = ?
UPDATE request SET status_id = ? WHERE request_id = ?
SELECT COUNT(*) FROM request WHERE is_archived = 0
SELECT COUNT(*) FROM request WHERE is_archived = 1
SELECT COUNT(*) FROM request r LEFT JOIN status_lookup s ON r.status_id = s.status_id WHERE r.is_archived = 0 AND s.status_name = ?
SELECT COUNT(*) FROM request WHERE is_archived = 0 AND (is_read = FALSE OR is_read IS NULL)
UPDATE request SET is_archived = 1, archived_at = CURRENT_TIMESTAMP WHERE is_archived = 0 AND created_at <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)
SELECT request_type_id FROM request_type WHERE request_type_name = ?
SELECT status_id FROM status_lookup WHERE status_name = ?

-- SCHEDULE DAO
SELECT s.schedule_id, s.barangay_id, s.team_id, s.schedule_date, s.schedule_time, sl.status_label as status, b.barangay_name, b.contact, ba.admin_name as barangay_admin, t.team_name, tr.plate_number AS truck_plate_number, ttl.truck_type_label as truck_type FROM schedule s LEFT JOIN status_lookup sl ON s.status_id = sl.status_id LEFT JOIN barangay b ON s.barangay_id = b.barangay_id LEFT JOIN barangay_admin ba ON b.barangay_id = ba.barangay_id LEFT JOIN team t ON s.team_id = t.team_id LEFT JOIN truck tr ON t.truck_id = tr.truck_id LEFT JOIN truck_type_lookup ttl ON tr.truck_type_id = ttl.truck_type_id ORDER BY s.schedule_date ASC, s.schedule_time ASC, b.barangay_name ASC
UPDATE schedule SET team_id = ?, schedule_date = ?, schedule_time = ?, status = ? WHERE schedule_id = ?
INSERT INTO schedule (barangay_id, team_id, schedule_date, schedule_time, status) VALUES (?, ?, ?, ?, ?)
DELETE FROM schedule WHERE schedule_id = ?
SELECT t.team_name, tr.plate_number AS truck_plate_number, ttl.truck_type_label as truck_type, s.schedule_time, sl.status_label as status FROM schedule s LEFT JOIN status_lookup sl ON s.status_id = sl.status_id LEFT JOIN barangay b ON s.barangay_id = b.barangay_id LEFT JOIN team t ON s.team_id = t.team_id LEFT JOIN truck tr ON t.truck_id = tr.truck_id LEFT JOIN truck_type_lookup ttl ON tr.truck_type_id = ttl.truck_type_id WHERE UPPER(TRIM(b.barangay_name)) = UPPER(TRIM(?)) ORDER BY s.schedule_date ASC, s.schedule_time ASC LIMIT 1
SELECT barangay_id FROM barangay WHERE UPPER(TRIM(barangay_name)) = UPPER(TRIM(?))
SELECT team_id FROM team WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?))
SELECT schedule_id FROM schedule WHERE barangay_id = ?
SELECT status_id FROM status_lookup WHERE status_key = ? AND status_domain_id = 8

-- TEAM DAO
SELECT t.*, sl.status_label as status, p.personnel_name AS leader_name, d.personnel_name AS driver_name, tr.plate_number AS truck_plate_number FROM team t LEFT JOIN status_lookup sl ON t.status_id = sl.status_id LEFT JOIN personnel p ON t.leader_id = p.personnel_id LEFT JOIN personnel d ON t.driver_id = d.personnel_id LEFT JOIN truck tr ON t.truck_id = tr.truck_id ORDER BY t.team_name ASC
SELECT t.*, sl.status_label as status, p.personnel_name AS leader_name FROM team t LEFT JOIN status_lookup sl ON t.status_id = sl.status_id LEFT JOIN personnel p ON t.leader_id = p.personnel_id ORDER BY t.team_name ASC
SELECT t.*, sl.status_label as status, p.personnel_name AS leader_name, d.personnel_name AS driver_name FROM team t LEFT JOIN status_lookup sl ON t.status_id = sl.status_id LEFT JOIN personnel p ON t.leader_id = p.personnel_id LEFT JOIN personnel d ON t.driver_id = d.personnel_id ORDER BY t.team_name ASC
SELECT t.*, sl.status_label as status, p.personnel_name AS leader_name, tr.plate_number AS truck_plate_number FROM team t LEFT JOIN status_lookup sl ON t.status_id = sl.status_id LEFT JOIN personnel p ON t.leader_id = p.personnel_id LEFT JOIN truck tr ON t.truck_id = tr.truck_id ORDER BY t.team_name ASC
SELECT t.*, sl.status_label as status, p.personnel_name AS leader_name, d.personnel_name AS driver_name, tr.plate_number AS truck_plate_number FROM team t LEFT JOIN status_lookup sl ON t.status_id = sl.status_id LEFT JOIN personnel p ON t.leader_id = p.personnel_id LEFT JOIN personnel d ON t.driver_id = d.personnel_id LEFT JOIN truck tr ON t.truck_id = tr.truck_id WHERE t.team_id = ?
SELECT t.*, sl.status_label as status, p.personnel_name AS leader_name FROM team t LEFT JOIN status_lookup sl ON t.status_id = sl.status_id LEFT JOIN personnel p ON t.leader_id = p.personnel_id WHERE t.team_id = ?
SELECT t.*, sl.status_label as status, p.personnel_name AS leader_name, d.personnel_name AS driver_name FROM team t LEFT JOIN status_lookup sl ON t.status_id = sl.status_id LEFT JOIN personnel p ON t.leader_id = p.personnel_id LEFT JOIN personnel d ON t.driver_id = d.personnel_id WHERE t.team_id = ?
SELECT t.*, sl.status_label as status, p.personnel_name AS leader_name, tr.plate_number AS truck_plate_number FROM team t LEFT JOIN status_lookup sl ON t.status_id = sl.status_id LEFT JOIN personnel p ON t.leader_id = p.personnel_id LEFT JOIN truck tr ON t.truck_id = tr.truck_id WHERE t.team_id = ?
INSERT INTO team (team_name, leader_id, driver_id, truck_id, status_id) VALUES (?, ?, ?, ?, ?)
INSERT INTO team (team_name, leader_id, status_id) VALUES (?, ?, ?)
UPDATE team SET team_name = ?, leader_id = ?, driver_id = ?, truck_id = ?, status_id = ? WHERE team_id = ?
UPDATE team SET team_name = ?, leader_id = ?, status_id = ? WHERE team_id = ?
DELETE FROM team WHERE team_id = ?
SELECT COUNT(*) AS count FROM team
SELECT COUNT(*) AS count FROM team t JOIN status_lookup sl ON t.status_id = sl.status_id WHERE sl.status_key = 'ACTIVE'
SELECT personnel_id, personnel_name FROM personnel WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?)) AND UPPER(TRIM(role)) = 'DRIVER' ORDER BY personnel_name ASC LIMIT 1
SELECT tc.personnel_id, p.personnel_name FROM team_collectors tc LEFT JOIN personnel p ON tc.personnel_id = p.personnel_id WHERE tc.team_id = ? ORDER BY p.personnel_name ASC
SELECT personnel_id, personnel_name FROM personnel WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?)) AND UPPER(TRIM(role)) = 'COLLECTOR' ORDER BY personnel_name ASC
DELETE FROM team_collectors WHERE team_id = ?
INSERT INTO team_collectors (team_id, personnel_id) VALUES (?, ?)
UPDATE personnel SET team_name = NULL WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?))
UPDATE personnel SET team_name = ? WHERE personnel_id = ?
SELECT COUNT(*) AS count FROM team
ALTER TABLE team ADD COLUMN driver_id INT NULL
ALTER TABLE team ADD COLUMN truck_id INT NULL
ALTER TABLE personnel ADD COLUMN team_name VARCHAR(255)
CREATE TABLE team_collectors (team_id INT NOT NULL, personnel_id INT NOT NULL, PRIMARY KEY (team_id, personnel_id))

-- TRUCK DAO
SELECT t.*, sl.status_label as status, ttl.truck_type_label as truck_type FROM truck t LEFT JOIN status_lookup sl ON t.status_id = sl.status_id LEFT JOIN truck_type_lookup ttl ON t.truck_type_id = ttl.truck_type_id ORDER BY t.plate_number ASC
SELECT t.*, sl.status_label as status, ttl.truck_type_label as truck_type FROM truck t LEFT JOIN status_lookup sl ON t.status_id = sl.status_id LEFT JOIN truck_type_lookup ttl ON t.truck_type_id = ttl.truck_type_id WHERE t.truck_id = ?
INSERT INTO truck (plate_number, truck_type, status) VALUES (?, ?, ?)
INSERT INTO truck (plate_number, truck_type, status, capacity, assigned_barangay) VALUES (?, ?, ?, ?, ?)
UPDATE truck SET plate_number = ?, truck_type = ?, status = ? WHERE truck_id = ?
UPDATE truck SET plate_number = ?, truck_type = ?, status = ?, capacity = ?, assigned_barangay = ? WHERE truck_id = ?
DELETE FROM truck WHERE truck_id = ?
SELECT COUNT(*) as count FROM truck
SELECT COUNT(*) AS count FROM truck t JOIN status_lookup sl ON t.status_id = sl.status_id WHERE sl.status_key = 'ACTIVE'
SELECT t.*, sl.status_label as status, ttl.truck_type_label as truck_type FROM truck t LEFT JOIN status_lookup sl ON t.status_id = sl.status_id LEFT JOIN truck_type_lookup ttl ON t.truck_type_id = ttl.truck_type_id WHERE sl.status_key = 'UNASSIGNED' ORDER BY t.plate_number ASC

-- =======================
-- DML SAMPLE DATA SECTION
-- =======================

-- Roles
INSERT INTO role_lookup (role_id, role_key, role_label) VALUES
(1, 'MENRO', 'MENRO'),
(2, 'BARANGAY', 'Barangay Admin'),
(3, 'PERSONNEL', 'Personnel'),
(4, 'COLLECTOR', 'Collector'),
(5, 'DRIVER', 'Driver');

-- Genders
INSERT INTO gender_lookup (gender_id, gender_key, gender_label) VALUES
(1, 'MALE', 'Male'),
(2, 'FEMALE', 'Female'),
(3, 'OTHER', 'Other');

-- Status domains
INSERT INTO status_domain (status_domain_id, domain_key, domain_label) VALUES
(1, 'ACCOUNT', 'Account Status'),
(2, 'BARANGAY', 'Barangay Status'),
(3, 'COMPLAINT', 'Complaint Status'),
(4, 'REPORT', 'Report Status'),
(5, 'REQUEST', 'Request Status'),
(6, 'TRUCK', 'Truck Status'),
(7, 'TEAM', 'Team Status'),
(8, 'SCHEDULE', 'Schedule Status'),
(9, 'ANNOUNCEMENT', 'Announcement Status'),
(10, 'PERSONNEL', 'Personnel Status');

-- Status values
INSERT INTO status_lookup (status_id, status_domain_id, status_key, status_label) VALUES
(1, 1, 'ACTIVE', 'Active'),
(2, 1, 'INACTIVE', 'Inactive'),
(3, 1, 'SUSPENDED', 'Suspended'),
(4, 2, 'ACTIVE', 'Active'),
(5, 2, 'SCHEDULED', 'Scheduled'),
(6, 2, 'INACTIVE', 'Inactive'),
(7, 3, 'PENDING', 'Pending'),
(8, 3, 'UNDER_REVIEW', 'Under Review'),
(9, 3, 'RESOLVED', 'Resolved'),
(10, 3, 'CLOSED', 'Closed'),
(11, 4, 'PENDING', 'Pending'),
(12, 4, 'COMPLETED', 'Completed'),
(13, 4, 'REJECTED', 'Rejected'),
(14, 5, 'PENDING', 'Pending'),
(15, 5, 'APPROVED', 'Approved'),
(16, 5, 'DENIED', 'Denied'),
(17, 5, 'COMPLETED', 'Completed'),
(18, 6, 'ACTIVE', 'Active'),
(19, 6, 'IN_MAINTENANCE', 'In Maintenance'),
(20, 6, 'DECOMMISSIONED', 'Decommissioned'),
(21, 7, 'ACTIVE', 'Active'),
(22, 7, 'INACTIVE', 'Inactive'),
(23, 7, 'SCHEDULED', 'Scheduled'),
(24, 8, 'SCHEDULED', 'Scheduled'),
(25, 8, 'COMPLETED', 'Completed'),
(26, 8, 'CANCELLED', 'Cancelled'),
(27, 8, 'MISSED', 'Missed'),
(28, 9, 'ACTIVE', 'Active'),
(29, 9, 'ARCHIVED', 'Archived'),
(30, 9, 'EXPIRED', 'Expired'),
(31, 10, 'ACTIVE', 'Active'),
(32, 10, 'UNASSIGNED', 'Unassigned'),
(33, 10, 'ON_DUTY', 'On Duty'),
(34, 10, 'OFF_DUTY', 'Off Duty');

-- Built-in admin account
INSERT INTO account (account_id, name, email_address, password, status_id, role_id, is_barangay_setup_complete)
VALUES (1, 'Menro Admin', 'admin@municipal.gov', 'admin123', 1, 1, 1);

-- Complaint types
INSERT INTO complaint_type (complaint_type_id, type_key, type_label) VALUES
(1, 'WASTE_COLLECTION', 'Waste Collection'),
(2, 'DUMPING', 'Illegal Dumping'),
(3, 'RECYCLING', 'Recycling'),
(4, 'ENVIRONMENTAL', 'Environmental Issue'),
(5, 'OTHER', 'Other');

-- Report types
INSERT INTO report_type (report_type_id, type_key, type_label) VALUES
(1, 'WASTE_REPORT', 'Waste Report'),
(2, 'CLEANUP_REPORT', 'Cleanup Report'),
(3, 'AUDIT_REPORT', 'Audit Report'),
(4, 'MAINTENANCE_REPORT', 'Maintenance Report'),
(5, 'OTHER', 'Other');

-- Request types
INSERT INTO request_type (request_type_id, type_key, type_label) VALUES
(1, 'PICKUP_REQUEST', 'Pickup Request'),
(2, 'DISPOSAL_REQUEST', 'Disposal Request'),
(3, 'CLEANUP_REQUEST', 'Cleanup Request'),
(4, 'REPAIR_REQUEST', 'Repair Request'),
(5, 'OTHER', 'Other');

-- Truck types
INSERT INTO truck_type_lookup (truck_type_id, truck_type_key, truck_type_label) VALUES
(1, 'DUMP_TRUCK', 'Dump Truck'),
(2, 'FLATBED', 'Flatbed Truck'),
(3, 'CRANE', 'Crane Truck'),
(4, 'COMPACTOR', 'Compactor Truck'),
(5, 'UTILITY', 'Utility Truck');

-- Entry types
INSERT INTO entry_type_lookup (entry_type_id, entry_type_key, entry_type_label) VALUES
(1, 'COMPLAINT', 'Complaint'),
(2, 'REPORT', 'Report'),
(3, 'REQUEST', 'Request');

-- =====================
-- SAMPLE DATA SECTION
-- =====================

-- Built-in admin account (AUTO ID)
INSERT INTO account (name, email_address, password, status_id, role_id, is_barangay_setup_complete) VALUES 
('Micko Jay Gonzales', 'mickojay@gmail.com', '123456', 1, 1, 1),
('Barangay Official', 'barangay@gmail.com', 'barangay123', 1, 2, 0),
('Juan Dela Cruz', 'juan1@mail.com', 'pass123', 1, 2, 1),
('Maria Santos', 'maria2@mail.com', 'pass123', 1, 2, 1),
('Pedro Reyes', 'pedro3@mail.com', 'pass123', 1, 2, 1),
('Ana Lopez', 'ana4@mail.com', 'pass123', 1, 2, 1),
('Mark Cruz', 'mark5@mail.com', 'pass123', 1, 2, 1),
('Liza Gomez', 'liza6@mail.com', 'pass123', 1, 2, 1),
('Jose Tan', 'jose7@mail.com', 'pass123', 1, 2, 1),
('Carla Lim', 'carla8@mail.com', 'pass123', 1, 2, 1),
('Ben Garcia', 'ben9@mail.com', 'pass123', 1, 2, 1),
('Nina Flores', 'nina10@mail.com', 'pass123', 1, 2, 1),
('Omar Diaz', 'omar11@mail.com', 'pass123', 1, 2, 1),
('Ella Cruz', 'ella12@mail.com', 'pass123', 1, 2, 1),
('Ryan Bautista', 'ryan13@mail.com', 'pass123', 1, 2, 1),
('Sofia Reyes', 'sofia14@mail.com', 'pass123', 1, 2, 1),
('Luis Mendoza', 'luis15@mail.com', 'pass123', 1, 2, 1),
('Ava Torres', 'ava16@mail.com', 'pass123', 1, 2, 1),
('Noah Ramos', 'noah17@mail.com', 'pass123', 1, 2, 1),
('Mia Fernandez', 'mia18@mail.com', 'pass123', 1, 2, 1);

-- BARANGAY (20)
INSERT INTO barangay (barangay_name, barangay_household, purok_count, population, contact, collection_day, status_id) VALUES
('Barangay 1', 100, 5, 500, '0917000001', 'Monday', 4),
('Barangay 2', 120, 6, 600, '0917000002', 'Tuesday', 4),
('Barangay 3', 90, 4, 450, '0917000003', 'Wednesday', 4),
('Barangay 4', 110, 5, 550, '0917000004', 'Thursday', 4),
('Barangay 5', 130, 6, 650, '0917000005', 'Friday', 4),
('Barangay 6', 95, 4, 480, '0917000006', 'Saturday', 4),
('Barangay 7', 140, 7, 700, '0917000007', 'Monday', 4),
('Barangay 8', 150, 8, 750, '0917000008', 'Tuesday', 4),
('Barangay 9', 85, 4, 420, '0917000009', 'Wednesday', 4),
('Barangay 10', 160, 8, 800, '0917000010', 'Thursday', 4),
('Barangay 11', 170, 9, 850, '0917000011', 'Friday', 4),
('Barangay 12', 180, 10, 900, '0917000012', 'Saturday', 4),
('Barangay 13', 190, 10, 950, '0917000013', 'Monday', 4),
('Barangay 14', 200, 11, 1000, '0917000014', 'Tuesday', 4),
('Barangay 15', 210, 12, 1050, '0917000015', 'Wednesday', 4),
('Barangay 16', 220, 12, 1100, '0917000016', 'Thursday', 4),
('Barangay 17', 230, 13, 1150, '0917000017', 'Friday', 4),
('Barangay 18', 240, 14, 1200, '0917000018', 'Saturday', 4),
('Barangay 19', 250, 15, 1250, '0917000019', 'Monday', 4),
('Barangay 20', 260, 16, 1300, '0917000020', 'Tuesday', 4);

-- BARANGAY ADMIN (20)
INSERT INTO barangay_admin (barangay_id, account_id, admin_name, age, gender_id) VALUES
(1, 2, 'Admin 1', 35, 1),
(2, 3, 'Admin 2', 40, 2),
(3, 4, 'Admin 3', 38, 1),
(4, 5, 'Admin 4', 45, 2),
(5, 6, 'Admin 5', 33, 1),
(6, 7, 'Admin 6', 36, 2),
(7, 8, 'Admin 7', 42, 1),
(8, 9, 'Admin 8', 39, 2),
(9, 10, 'Admin 9', 41, 1),
(10, 11, 'Admin 10', 37, 2),
(11, 12, 'Admin 11', 44, 1),
(12, 13, 'Admin 12', 32, 2),
(13, 14, 'Admin 13', 46, 1),
(14, 15, 'Admin 14', 34, 2),
(15, 16, 'Admin 15', 43, 1),
(16, 17, 'Admin 16', 31, 2),
(17, 18, 'Admin 17', 47, 1),
(18, 19, 'Admin 18', 30, 2),
(19, 20, 'Admin 19', 48, 1),
(20, 1, 'Admin 20', 29, 2);

-- ANNOUNCEMENT (20)
INSERT INTO announcement (title, message, is_active, is_archived, expires_at) VALUES
('Waste Notice 1','Garbage collection schedule update',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 2','Segregation reminder',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 3','Clean-up drive announcement',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 4','Trash pickup delay notice',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 5','Waste disposal guidelines',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 6','Community cleanup program',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 7','Recycling awareness drive',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 8','Waste audit schedule',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 9','Illegal dumping warning',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 10','Garbage truck schedule',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 11','Waste reduction campaign',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 12','Plastic ban reminder',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 13','Street cleaning advisory',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 14','Waste bin distribution',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 15','Clean environment pledge',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 16','Trash segregation enforcement',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 17','Waste management update',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 18','Community cleanup report',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 19','Waste collection change',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
('Waste Notice 20','Emergency waste advisory',1,0,DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY));

-- COMPLAINT (20)
INSERT INTO complaint (barangay_id, complaint_type_id, message, status_id, is_read, is_archived, location, response_message) VALUES
(1, 1, 'Uncollected garbage in the main street', 7, 0, 0, 'Zone 1', NULL),
(2, 2, 'Illegal dumping near the creek', 9, 1, 0, 'Zone 2', 'Under investigation'),
(3, 1, 'Delayed pickup for residential area', 7, 0, 0, 'Zone 3', NULL),
(4, 2, 'Trash accumulation at market entrance', 9, 1, 0, 'Zone 4', 'Resolved by cleanup crew'),
(5, 1, 'Scattered waste on highway sidewalk', 7, 0, 0, 'Zone 5', NULL),
(6, 1, 'Missed collection for Barangay 6', 7, 0, 0, 'Zone 6', NULL),
(7, 3, 'Burning waste complaints in neighborhood', 9, 1, 0, 'Zone 7', 'Responded with advisory'),
(8, 2, 'Blocked drainage due to dumped trash', 7, 0, 0, 'Zone 8', NULL),
(9, 1, 'Accumulated garbage behind shops', 9, 1, 0, 'Zone 9', 'Cleanup scheduled'),
(10, 1, 'Improper segregation of recyclables', 7, 0, 0, 'Zone 10', NULL),
(11, 4, 'Littering issue reported near school', 9, 1, 0, 'Zone 11', 'Enforcement patrol alerted'),
(12, 1, 'Bad odor waste collection area', 7, 0, 0, 'Zone 12', NULL),
(13, 1, 'Street trash piling near junction', 9, 1, 0, 'Zone 13', 'Collection completed'),
(14, 2, 'Plastic buildup along roadside', 7, 0, 0, 'Zone 14', NULL),
(15, 1, 'No pickup reported for complaints', 9, 1, 0, 'Zone 15', 'Pending follow-up'),
(16, 1, 'Overflow bins near market', 7, 0, 0, 'Zone 16', NULL),
(17, 2, 'Illegal disposal behind warehouses', 9, 1, 0, 'Zone 17', 'Investigation ongoing'),
(18, 3, 'Canal dumping observed after storm', 7, 0, 0, 'Zone 18', NULL),
(19, 3, 'Uncollected recyclables at collection point', 9, 1, 0, 'Zone 19', 'Scheduled for pickup'),
(20, 1, 'General waste issue in business district', 7, 0, 0, 'Zone 20', NULL);

-- REPORT (20)
INSERT INTO report (barangay_id, report_type_id, message, status_id, is_read, is_archived, purok_analytics, response_message) VALUES
(1, 1, 'Waste report filed for Barangay 1', 12, 1, 0, 'P1', NULL),
(2, 1, 'Trash collection report for Barangay 2', 12, 1, 0, 'P2', NULL),
(3, 2, 'Segregation compliance report', 11, 0, 0, 'P3', NULL),
(4, 2, 'Street audit report for community cleanup', 12, 1, 0, 'P4', NULL),
(5, 1, 'Garbage monitoring report submitted', 11, 0, 0, 'P5', NULL),
(6, 1, 'Bin usage report for Barangay 6', 12, 1, 0, 'P6', NULL),
(7, 1, 'Dumping incident report near market', 11, 0, 0, 'P7', NULL),
(8, 2, 'Cleanliness audit completed', 12, 1, 0, 'P8', NULL),
(9, 1, 'Pickup efficiency report', 11, 0, 0, 'P9', NULL),
(10, 1, 'Recycling process report', 12, 1, 0, 'P10', NULL),
(11, 1, 'Road waste report issued', 12, 1, 0, 'P11', NULL),
(12, 2, 'Waste audit report for Barangay 12', 11, 0, 0, 'P12', NULL),
(13, 2, 'Management report for waste operations', 12, 1, 0, 'P13', NULL),
(14, 1, 'Overflow report at collection site', 11, 0, 0, 'P14', NULL),
(15, 1, 'Cleanup progress report', 12, 1, 0, 'P15', NULL),
(16, 4, 'Disposal report for scheduled maintenance', 11, 0, 0, 'P16', NULL),
(17, 2, 'Monthly report of Barangay 17', 12, 1, 0, 'P17', NULL),
(18, 1, 'Incident report after storm', 11, 0, 0, 'P18', NULL),
(19, 1, 'Recycling report for urban area', 12, 1, 0, 'P19', NULL),
(20, 2, 'Final audit report submitted', 11, 0, 0, 'P20', NULL);

-- REQUEST (20)
INSERT INTO request (barangay_id, request_type_id, message, status_id, is_read, is_archived, location, response_message) VALUES
(1, 1, 'Garbage collection request for Barangay 1', 14, 0, 0, 'P1', NULL),
(2, 2, 'Extra pickup needed', 15, 1, 0, 'P2', NULL),
(3, 3, 'Community cleanup request', 14, 0, 0, 'P3', NULL),
(4, 1, 'Overflow waste pickup request', 15, 1, 0, 'P4', NULL),
(5, 1, 'Missed pickup request', 14, 0, 0, 'P5', NULL),
(6, 4, 'Waste removal repair request', 15, 1, 0, 'P6', NULL),
(7, 3, 'Street cleanup request', 14, 0, 0, 'P7', NULL),
(8, 1, 'Urgent pickup request', 15, 1, 0, 'P8', NULL),
(9, 2, 'Recycling disposal request', 14, 0, 0, 'P9', NULL),
(10, 1, 'Bulk waste pickup request', 15, 1, 0, 'P10', NULL),
(11, 3, 'Canal cleanup request', 14, 0, 0, 'P11', NULL),
(12, 1, 'Community pickup request', 15, 1, 0, 'P12', NULL),
(13, 2, 'Special disposal request', 14, 0, 0, 'P13', NULL),
(14, 1, 'Delayed pickup request', 15, 1, 0, 'P14', NULL),
(15, 4, 'Drainage cleaning request', 14, 0, 0, 'P15', NULL),
(16, 3, 'Road cleanup request', 15, 1, 0, 'P16', NULL),
(17, 1, 'Emergency waste pickup request', 14, 0, 0, 'P17', NULL),
(18, 2, 'Backlog waste disposal request', 15, 1, 0, 'P18', NULL),
(19, 3, 'Street cleanup request', 14, 0, 0, 'P19', NULL),
(20, 1, 'General pickup request', 15, 1, 0, 'P20', NULL);

-- PERSONNEL (20)
INSERT INTO personnel (personnel_name, age, gender_id, address, contact_number, team_id, role_id, status_id) VALUES
('Collector One', 28, 1, 'Zone 1', '0917110001', NULL, 4, 31),
('Collector Two', 30, 2, 'Zone 2', '0917110002', NULL, 4, 31),
('Collector Three', 29, 1, 'Zone 3', '0917110003', NULL, 4, 31),
('Collector Four', 32, 2, 'Zone 4', '0917110004', NULL, 4, 31),
('Collector Five', 27, 1, 'Zone 5', '0917110005', NULL, 4, 31),
('Collector Six', 33, 2, 'Zone 6', '0917110006', NULL, 4, 31),
('Collector Seven', 31, 1, 'Zone 7', '0917110007', NULL, 4, 31),
('Collector Eight', 26, 2, 'Zone 8', '0917110008', NULL, 4, 31),
('Collector Nine', 34, 1, 'Zone 9', '0917110009', NULL, 4, 31),
('Collector Ten', 28, 2, 'Zone 10', '0917110010', NULL, 4, 31),
('Driver One', 35, 1, 'Depot A', '0917120001', NULL, 5, 31),
('Driver Two', 36, 2, 'Depot B', '0917120002', NULL, 5, 31),
('Driver Three', 38, 1, 'Depot C', '0917120003', NULL, 5, 31),
('Driver Four', 37, 2, 'Depot D', '0917120004', NULL, 5, 31),
('Driver Five', 40, 1, 'Depot E', '0917120005', NULL, 5, 31),
('Driver Six', 39, 2, 'Depot F', '0917120006', NULL, 5, 31),
('Driver Seven', 41, 1, 'Depot G', '0917120007', NULL, 5, 31),
('Driver Eight', 34, 2, 'Depot H', '0917120008', NULL, 5, 31),
('Driver Nine', 42, 1, 'Depot I', '0917120009', NULL, 5, 31),
('Driver Ten', 43, 2, 'Depot J', '0917120010', NULL, 5, 31);

-- TRUCK (20)
INSERT INTO truck (plate_number, truck_type_id, capacity, status_id) VALUES
('TRK-1001', 1, '10 tons', 18),
('TRK-1002', 2, '12 tons', 18),
('TRK-1003', 3, '8 tons', 18),
('TRK-1004', 4, '14 tons', 18),
('TRK-1005', 5, '6 tons', 18),
('TRK-1006', 1, '10 tons', 18),
('TRK-1007', 2, '12 tons', 18),
('TRK-1008', 3, '8 tons', 18),
('TRK-1009', 4, '14 tons', 18),
('TRK-1010', 5, '6 tons', 18),
('TRK-1011', 1, '10 tons', 18),
('TRK-1012', 2, '12 tons', 18),
('TRK-1013', 3, '8 tons', 18),
('TRK-1014', 4, '14 tons', 18),
('TRK-1015', 5, '6 tons', 18),
('TRK-1016', 1, '10 tons', 18),
('TRK-1017', 2, '12 tons', 18),
('TRK-1018', 3, '8 tons', 18),
('TRK-1019', 4, '14 tons', 18),
('TRK-1020', 5, '6 tons', 18);

-- TEAM (20)
INSERT INTO team (team_name, leader_id, driver_id, truck_id, status_id) VALUES
('Team Alpha', NULL, NULL, 1, 21),
('Team Bravo', NULL, NULL, 2, 21),
('Team Charlie', NULL, NULL, 3, 21),
('Team Delta', NULL, NULL, 4, 21),
('Team Echo', NULL, NULL, 5, 21),
('Team Foxtrot', NULL, NULL, 6, 21),
('Team Gamma', NULL, NULL, 7, 21),
('Team Hotel', NULL, NULL, 8, 21),
('Team India', NULL, NULL, 9, 21),
('Team Juliet', NULL, NULL, 10, 21),
('Team Kilo', NULL, NULL, 11, 21),
('Team Lima', NULL, NULL, 12, 21),
('Team Mike', NULL, NULL, 13, 21),
('Team November', NULL, NULL, 14, 21),
('Team Oscar', NULL, NULL, 15, 21),
('Team Papa', NULL, NULL, 16, 21),
('Team Quebec', NULL, NULL, 17, 21),
('Team Romeo', NULL, NULL, 18, 21),
('Team Sierra', NULL, NULL, 19, 21),
('Team Tango', NULL, NULL, 20, 21);

-- SCHEDULE (20)
INSERT INTO schedule (barangay_id, team_id, schedule_date, schedule_time, status_id) VALUES
(1, 1, '2026-05-03', '08:00:00', 24),
(2, 2, '2026-05-03', '09:00:00', 24),
(3, 3, '2026-05-04', '08:00:00', 24),
(4, 4, '2026-05-04', '09:00:00', 24),
(5, 5, '2026-05-05', '08:00:00', 25),
(6, 6, '2026-05-05', '09:00:00', 25),
(7, 7, '2026-05-06', '08:00:00', 24),
(8, 8, '2026-05-06', '09:00:00', 24),
(9, 9, '2026-05-07', '08:00:00', 24),
(10, 10, '2026-05-07', '09:00:00', 26),
(11, 11, '2026-05-08', '08:00:00', 24),
(12, 12, '2026-05-08', '09:00:00', 24),
(13, 13, '2026-05-09', '08:00:00', 24),
(14, 14, '2026-05-09', '09:00:00', 24),
(15, 15, '2026-05-10', '08:00:00', 25),
(16, 16, '2026-05-10', '09:00:00', 25),
(17, 17, '2026-05-11', '08:00:00', 24),
(18, 18, '2026-05-11', '09:00:00', 24),
(19, 19, '2026-05-12', '08:00:00', 24),
(20, 20, '2026-05-12', '09:00:00', 26);

-- PUROK_CHECKLIST (20)
INSERT INTO purok_checklist (barangay_id, purok_name, is_collected) VALUES
(1, 'Purok 1-A', 0),
(2, 'Purok 1-B', 1),
(3, 'Purok 2-A', 0),
(4, 'Purok 2-B', 1),
(5, 'Purok 3-A', 0),
(6, 'Purok 3-B', 1),
(7, 'Purok 4-A', 0),
(8, 'Purok 4-B', 1),
(9, 'Purok 5-A', 0),
(10, 'Purok 5-B', 1),
(11, 'Purok 6-A', 0),
(12, 'Purok 6-B', 1),
(13, 'Purok 7-A', 0),
(14, 'Purok 7-B', 1),
(15, 'Purok 8-A', 0),
(16, 'Purok 8-B', 1),
(17, 'Purok 9-A', 0),
(18, 'Purok 9-B', 1),
(19, 'Purok 10-A', 0),
(20, 'Purok 10-B', 1);

-- Update team assignments for personnel
UPDATE personnel SET team_id = 1 WHERE personnel_id IN (1, 11);
UPDATE personnel SET team_id = 2 WHERE personnel_id IN (2, 12);
UPDATE personnel SET team_id = 3 WHERE personnel_id IN (3, 13);
UPDATE personnel SET team_id = 4 WHERE personnel_id IN (4, 14);
UPDATE personnel SET team_id = 5 WHERE personnel_id IN (5, 15);
UPDATE personnel SET team_id = 6 WHERE personnel_id IN (6, 16);
UPDATE personnel SET team_id = 7 WHERE personnel_id IN (7, 17);
UPDATE personnel SET team_id = 8 WHERE personnel_id IN (8, 18);
UPDATE personnel SET team_id = 9 WHERE personnel_id IN (9, 19);
UPDATE personnel SET team_id = 10 WHERE personnel_id IN (10, 20);

-- Update team leaders and drivers
UPDATE team SET leader_id = 1, driver_id = 11 WHERE team_id = 1;
UPDATE team SET leader_id = 2, driver_id = 12 WHERE team_id = 2;
UPDATE team SET leader_id = 3, driver_id = 13 WHERE team_id = 3;
UPDATE team SET leader_id = 4, driver_id = 14 WHERE team_id = 4;
UPDATE team SET leader_id = 5, driver_id = 15 WHERE team_id = 5;
UPDATE team SET leader_id = 6, driver_id = 16 WHERE team_id = 6;
UPDATE team SET leader_id = 7, driver_id = 17 WHERE team_id = 7;
UPDATE team SET leader_id = 8, driver_id = 18 WHERE team_id = 8;
UPDATE team SET leader_id = 9, driver_id = 19 WHERE team_id = 9;
UPDATE team SET leader_id = 10, driver_id = 20 WHERE team_id = 10;
UPDATE team SET leader_id = 1, driver_id = 11 WHERE team_id = 11;
UPDATE team SET leader_id = 2, driver_id = 12 WHERE team_id = 12;
UPDATE team SET leader_id = 3, driver_id = 13 WHERE team_id = 13;
UPDATE team SET leader_id = 4, driver_id = 14 WHERE team_id = 14;
UPDATE team SET leader_id = 5, driver_id = 15 WHERE team_id = 15;
UPDATE team SET leader_id = 6, driver_id = 16 WHERE team_id = 16;
UPDATE team SET leader_id = 7, driver_id = 17 WHERE team_id = 17;
UPDATE team SET leader_id = 8, driver_id = 18 WHERE team_id = 18;
UPDATE team SET leader_id = 9, driver_id = 19 WHERE team_id = 19;
UPDATE team SET leader_id = 10, driver_id = 20 WHERE team_id = 20;
INSERT INTO team_collectors (team_id, personnel_id) VALUES
(1, 1), (1, 2),
(2, 3), (2, 4),
(3, 5), (3, 6),
(4, 7), (4, 8),
(5, 9), (5, 10),
(6, 1), (6, 3),
(7, 2), (7, 4),
(8, 5), (8, 6),
(9, 7), (9, 8),
(10, 9), (10, 10),
(11, 1), (11, 2),
(12, 3), (12, 4),
(13, 5), (13, 6),
(14, 7), (14, 8),
(15, 9), (15, 10),
(16, 1), (16, 3),
(17, 2), (17, 4),
(18, 5), (18, 6),
(19, 7), (19, 8),
(20, 9), (20, 10);

-- ENTRY_ATTACHMENT (20 sample attachments for complaints, reports, and requests)
INSERT INTO entry_attachment (entry_type_id, entry_id, image_blob) VALUES
(1, 1, NULL), -- Complaint 1 attachment
(1, 2, NULL), -- Complaint 2 attachment
(1, 3, NULL), -- Complaint 3 attachment
(1, 4, NULL), -- Complaint 4 attachment
(1, 5, NULL), -- Complaint 5 attachment
(2, 1, NULL), -- Report 1 attachment
(2, 2, NULL), -- Report 2 attachment
(2, 3, NULL), -- Report 3 attachment
(2, 4, NULL), -- Report 4 attachment
(2, 5, NULL), -- Report 5 attachment
(3, 1, NULL), -- Request 1 attachment
(3, 2, NULL), -- Request 2 attachment
(3, 3, NULL), -- Request 3 attachment
(3, 4, NULL), -- Request 4 attachment
(3, 5, NULL), -- Request 5 attachment
(1, 6, NULL), -- Complaint 6 attachment
(1, 7, NULL), -- Complaint 7 attachment
(2, 6, NULL), -- Report 6 attachment
(2, 7, NULL), -- Report 7 attachment
(3, 6, NULL); -- Request 6 attachment
