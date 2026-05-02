CREATE DATABASE IF NOT EXISTS wcms;
USE wcms;

-- =========================================================
-- WASTE COLLECTION MANAGEMENT SYSTEM DATABASE SCHEMA
-- Generated from DAO classes and model definitions
-- =========================================================

-- Core authentication table
CREATE TABLE account (
    account_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'Active',
    role VARCHAR(50) DEFAULT 'BARANGAY',
    is_barangay_setup_complete BOOLEAN DEFAULT FALSE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Barangay information
CREATE TABLE barangay (
    barangay_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_name VARCHAR(255) NOT NULL,
    barangay_household INT DEFAULT 0,
    purok_count INT DEFAULT 0,
    population INT DEFAULT 0,
    contact VARCHAR(50) NULL,
    collection_day VARCHAR(50) NULL,
    status VARCHAR(50) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Barangay administration details
CREATE TABLE barangay_admin (
    barangay_admin_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NULL,
    account_id INT NOT NULL,
    barangay_admin VARCHAR(255) NULL,
    age INT NULL,
    gender VARCHAR(50) NULL,
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- Announcements
CREATE TABLE announcement (
    announcement_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NULL,
    message TEXT NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    is_archived TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    archived_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL
);

-- Complaints
CREATE TABLE complaint (
    complaint_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NULL,
    barangay_name VARCHAR(160) NULL,
    type VARCHAR(60) NULL,
    message TEXT NOT NULL,
    proof LONGBLOB NULL,
    status VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_archived TINYINT(1) NOT NULL DEFAULT 0,
    archived_at TIMESTAMP NULL,
    location VARCHAR(255) NULL,
    response_message TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id)
);

-- Reports
CREATE TABLE report (
    report_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NULL,
    barangay_name VARCHAR(160) NULL,
    type VARCHAR(60) NULL,
    message TEXT NOT NULL,
    proof LONGBLOB NULL,
    status VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_archived TINYINT(1) NOT NULL DEFAULT 0,
    archived_at TIMESTAMP NULL,
    response_message TEXT NULL,
    purok_analytics TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id)
);

-- Requests
CREATE TABLE request (
    request_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NULL,
    barangay_name VARCHAR(160) NULL,
    type VARCHAR(60) NULL,
    message TEXT NOT NULL,
    proof LONGBLOB NULL,
    status VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_archived TINYINT(1) NOT NULL DEFAULT 0,
    archived_at TIMESTAMP NULL,
    location VARCHAR(255) NULL,
    response_message TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id)
);

-- Personnel
CREATE TABLE personnel (
    personnel_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    personnel_name VARCHAR(255) NOT NULL,
    age INT NULL,
    gender VARCHAR(50) NULL,
    address VARCHAR(255) NULL,
    contact_number VARCHAR(50) NULL,
    team_name VARCHAR(255) NULL,
    role VARCHAR(50) NULL,
    status VARCHAR(50) DEFAULT 'Active'
);

-- Trucks
CREATE TABLE truck (
    truck_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(50) NOT NULL,
    truck_type VARCHAR(50) NOT NULL,
    capacity VARCHAR(50) NULL,
    assigned_barangay VARCHAR(255) NULL,
    status VARCHAR(50) DEFAULT 'Active',
    assigned_team VARCHAR(255) NULL
);

-- Teams
CREATE TABLE team (
    team_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(255) NOT NULL,
    leader_id INT NULL,
    status VARCHAR(50) DEFAULT 'Active',
    driver_id INT NULL,
    truck_id INT NULL,
    FOREIGN KEY (leader_id) REFERENCES personnel(personnel_id),
    FOREIGN KEY (driver_id) REFERENCES personnel(personnel_id),
    FOREIGN KEY (truck_id) REFERENCES truck(truck_id)
);

-- Team collectors junction table
CREATE TABLE team_collectors (
    team_id INT NOT NULL,
    personnel_id INT NOT NULL,
    PRIMARY KEY (team_id, personnel_id),
    FOREIGN KEY (team_id) REFERENCES team(team_id) ON DELETE CASCADE,
    FOREIGN KEY (personnel_id) REFERENCES personnel(personnel_id) ON DELETE CASCADE
);

-- Collection schedules
CREATE TABLE schedule (
    schedule_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NOT NULL,
    team_id INT NOT NULL,
    schedule_date DATE NOT NULL,
    schedule_time TIME NOT NULL,
    status VARCHAR(50) DEFAULT 'Scheduled',
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id),
    FOREIGN KEY (team_id) REFERENCES team(team_id)
);

-- Purok checklist
CREATE TABLE purok_checklist (
    checklist_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NOT NULL,
    purok_name VARCHAR(100) NOT NULL,
    is_collected TINYINT(1) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (barangay_id) REFERENCES barangay(barangay_id)
);

-- Entry attachments
CREATE TABLE entry_attachment (
    attachment_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    entry_type VARCHAR(30) NOT NULL,
    entry_id INT NOT NULL,
    image_blob LONGBLOB NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CREATE TABLE
CREATE TABLE announcement (
    announcement_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NULL,
    message TEXT NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    is_archived TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    archived_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL
);

-- Indexes for better performance
CREATE INDEX idx_account_email ON account(email_address);
CREATE INDEX idx_complaint_status ON complaint(status);
CREATE INDEX idx_complaint_archived ON complaint(is_archived);
CREATE INDEX idx_report_status ON report(status);
CREATE INDEX idx_report_archived ON report(is_archived);
CREATE INDEX idx_request_status ON request(status);
CREATE INDEX idx_request_archived ON request(is_archived);
CREATE INDEX idx_schedule_date ON schedule(schedule_date);
CREATE INDEX idx_purok_checklist_barangay ON purok_checklist(barangay_id);

-- ==========================================================================

-- SELECT (dynamic base)
SELECT a.*
FROM account a

SELECT a.*, ba.barangay_admin_id, ba.age, ba.gender, b.barangay_id, b.barangay_name
FROM account a
LEFT JOIN barangay_admin ba ON a.account_id = ba.account_id
LEFT JOIN barangay b ON ba.barangay_id = b.barangay_id

-- FIND
SELECT a.* FROM account a WHERE a.email_address = ?
SELECT a.* FROM account a WHERE a.account_id = ?
SELECT a.* FROM account a ORDER BY a.account_id ASC
SELECT a.* FROM account a WHERE UPPER(TRIM(a.role)) = UPPER(TRIM(?)) ORDER BY a.account_id ASC

-- INSERT ACCOUNT
INSERT INTO account(name, email_address, password, status, role, is_barangay_setup_complete)
VALUES (?, ?, ?, 'Active', ?, ?)

-- UPDATE ACCOUNT
UPDATE account SET name=?, email_address=?, password=?, status=?, role=? WHERE account_id=?

-- UPDATE LAST LOGIN
UPDATE account SET last_login = NOW() WHERE account_id = ?

-- UPDATE STATUS
UPDATE account SET status = ? WHERE account_id = ?

-- DELETE
DELETE FROM barangay_admin WHERE account_id = ?
DELETE FROM account WHERE account_id = ?

-- EMAIL EXISTS
SELECT COUNT(*) FROM account WHERE email_address = ?

-- BARANGAY ADMIN CHECK
SELECT barangay_admin_id FROM barangay_admin WHERE account_id = ?
SELECT barangay_admin_id, barangay_id FROM barangay_admin WHERE account_id = ?

-- BARANGAY ADMIN UPDATE
UPDATE barangay_admin SET barangay_id = ? WHERE account_id = ?
UPDATE barangay_admin SET barangay_admin = ?, age = ?, gender = ? WHERE account_id = ?

-- BARANGAY ADMIN INSERT
INSERT INTO barangay_admin (barangay_id, account_id) VALUES (?, ?)
INSERT INTO barangay_admin (barangay_id, account_id, barangay_admin, age, gender)
VALUES (?, ?, ?, ?, ?)

-- FIND BARANGAY BY NAME
SELECT barangay_id FROM barangay WHERE UPPER(TRIM(barangay_name)) = UPPER(TRIM(?))

-- ALTER TABLE
ALTER TABLE account ADD COLUMN is_barangay_setup_complete BOOLEAN DEFAULT FALSE

-- UPDATE BARANGAY SETUP FLAG
UPDATE account SET is_barangay_setup_complete = ? WHERE account_id = ?

-- ==========================================================================

-- SELECT ACTIVE ANNOUNCEMENT
SELECT * FROM announcement
WHERE is_active = 1 AND is_archived = 0
ORDER BY created_at DESC
LIMIT 1;

-- SELECT ARCHIVED ANNOUNCEMENTS
SELECT * FROM announcement
WHERE is_archived = 1
ORDER BY archived_at DESC, created_at DESC;

-- INSERT (SAVE AND ACTIVATE)
INSERT INTO announcement
(title, message, is_active, is_archived, created_at, expires_at)
VALUES (?, ?, 1, 0, CURRENT_TIMESTAMP, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY));

-- INSERT (CUSTOM ADD)
INSERT INTO announcement
(title, message, is_active, is_archived, created_at, expires_at)
VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?);

-- UPDATE (DISMISS ANNOUNCEMENT)
UPDATE announcement
SET is_active = 0,
    is_archived = 1,
    archived_at = CURRENT_TIMESTAMP
WHERE announcement_id = ?;

-- UPDATE (GENERAL UPDATE)
UPDATE announcement
SET title = ?,
    message = ?,
    is_active = ?,
    is_archived = ?,
    expires_at = ?
WHERE announcement_id = ?;

-- UPDATE (ARCHIVE EXPIRED)
UPDATE announcement
SET is_active = 0,
    is_archived = 1,
    archived_at = CURRENT_TIMESTAMP
WHERE is_active = 1
  AND is_archived = 0
  AND expires_at IS NOT NULL
  AND expires_at <= CURRENT_TIMESTAMP;

-- UPDATE (ARCHIVE CURRENT ACTIVE)
UPDATE announcement
SET is_active = 0,
    is_archived = 1,
    archived_at = CURRENT_TIMESTAMP
WHERE is_active = 1
  AND is_archived = 0;

-- ==========================================================================

-- DELETE ATTACHMENTS FOR ENTRY
DELETE FROM entry_attachment
WHERE entry_type = ? AND entry_id = ?;

-- INSERT ATTACHMENT
INSERT INTO entry_attachment (entry_type, entry_id, image_blob)
VALUES (?, ?, ?);

-- SELECT ATTACHMENTS BY ENTRY
SELECT attachment_id, entry_type, entry_id, image_blob
FROM entry_attachment
WHERE entry_type = ? AND entry_id = ?
ORDER BY attachment_id ASC;

-- CREATE TABLE
CREATE TABLE entry_attachment (
    attachment_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    entry_type VARCHAR(30) NOT NULL,
    entry_id INT NOT NULL,
    image_blob LONGBLOB NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================================================

-- INSERT BARANGAY
INSERT INTO barangay
(barangay_name, barangay_household, purok_count, population, contact, collection_day, status)
VALUES (?, ?, ?, ?, ?, ?, ?);

-- UPDATE BARANGAY
UPDATE barangay
SET barangay_name = ?,
    barangay_household = ?,
    purok_count = ?,
    population = ?,
    contact = ?,
    collection_day = ?,
    status = ?
WHERE barangay_id = ?;

-- DELETE BARANGAY
DELETE FROM barangay
WHERE barangay_id = ?;

-- SELECT BY ID
SELECT * FROM barangay
WHERE barangay_id = ?;

-- SELECT ALL
SELECT * FROM barangay
ORDER BY barangay_name;

-- SELECT BY NAME
SELECT * FROM barangay
WHERE UPPER(TRIM(barangay_name)) = UPPER(TRIM(?));

-- SELECT BY COLLECTION DAY
SELECT * FROM barangay
WHERE collection_day = ?
ORDER BY barangay_name;

-- COUNT SCHEDULED BARANGAY
SELECT COUNT(*) FROM barangay
WHERE UPPER(COALESCE(status, '')) = 'SCHEDULED';

-- COUNT TOTAL BARANGAY
SELECT COUNT(*) FROM barangay;

-- SUM HOUSEHOLDS
SELECT COALESCE(SUM(barangay_household), 0) FROM barangay;

-- SUM POPULATION
SELECT COALESCE(SUM(population), 0) FROM barangay;

-- ALTER TABLE (ADD PUROK COUNT)
ALTER TABLE barangay
ADD COLUMN purok_count INT DEFAULT 0;

-- ALTER TABLE (ADD POPULATION)
ALTER TABLE barangay
ADD COLUMN population INT DEFAULT 0;

-- ==========================================================================

-- INSERT COMPLAINT
INSERT INTO complaint
(barangay_id, barangay_name, type, message, proof, status, is_read, is_archived, location, response_message)
VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?);

-- UPDATE COMPLAINT
UPDATE complaint
SET barangay_id = ?,
    barangay_name = ?,
    type = ?,
    message = ?,
    proof = ?,
    status = ?,
    is_read = ?,
    is_archived = ?,
    archived_at = ?,
    location = ?,
    response_message = ?
WHERE complaint_id = ?;

-- DELETE COMPLAINT
DELETE FROM complaint
WHERE complaint_id = ?;

-- SELECT BY ID
SELECT * FROM complaint
WHERE complaint_id = ?;

-- SELECT ALL (NOT ARCHIVED)
SELECT * FROM complaint
WHERE is_archived = 0
ORDER BY created_at DESC;

-- SELECT ARCHIVED
SELECT * FROM complaint
WHERE is_archived = 1
ORDER BY created_at DESC;

-- SELECT BY STATUS
SELECT * FROM complaint
WHERE is_archived = 0 AND status = ?
ORDER BY complaint_id DESC;

-- SELECT UNREAD
SELECT * FROM complaint
WHERE is_archived = 0
  AND (is_read = FALSE OR is_read IS NULL)
ORDER BY complaint_id DESC;

-- MARK AS READ
UPDATE complaint
SET is_read = TRUE
WHERE complaint_id = ?;

-- UPDATE STATUS
UPDATE complaint
SET status = ?
WHERE complaint_id = ?;

-- COUNT NOT ARCHIVED
SELECT COUNT(*) FROM complaint
WHERE is_archived = 0;

-- COUNT ARCHIVED
SELECT COUNT(*) FROM complaint
WHERE is_archived = 1;

-- COUNT BY STATUS
SELECT COUNT(*) FROM complaint
WHERE is_archived = 0 AND status = ?;

-- COUNT UNREAD
SELECT COUNT(*) FROM complaint
WHERE is_archived = 0
  AND (is_read = FALSE OR is_read IS NULL);

-- ARCHIVE EXPIRED (OLDER THAN 7 DAYS)
UPDATE complaint
SET is_archived = 1,
    archived_at = CURRENT_TIMESTAMP
WHERE is_archived = 0
  AND created_at <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY);

-- SELECT BY ARCHIVE FLAG
SELECT * FROM complaint
WHERE is_archived = ?
ORDER BY created_at DESC;

-- ALTER TABLE (DYNAMIC COLUMNS)
ALTER TABLE complaint ADD COLUMN barangay_id INT NULL;
ALTER TABLE complaint ADD COLUMN barangay_name VARCHAR(160) NULL;
ALTER TABLE complaint ADD COLUMN type VARCHAR(60) NULL;
ALTER TABLE complaint ADD COLUMN response_message TEXT NULL;
ALTER TABLE complaint ADD COLUMN is_archived TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE complaint ADD COLUMN archived_at TIMESTAMP NULL;
ALTER TABLE complaint ADD COLUMN location VARCHAR(255) NULL;

-- ==========================================================================

-- TODAY COLLECTION COUNT
SELECT COUNT(*) AS count
FROM schedule
WHERE schedule_date = ?;

-- TODAY COLLECTION BARANGAY
SELECT b.barangay_name
FROM schedule s
JOIN barangay b ON s.barangay_id = b.barangay_id
WHERE s.schedule_date = ?
LIMIT 1;

-- COMPLETED COLLECTION COUNT
SELECT COUNT(*) AS count
FROM schedule
WHERE status = 'Completed';

-- COMPLETED COLLECTION BARANGAY
SELECT b.barangay_name
FROM schedule s
JOIN barangay b ON s.barangay_id = b.barangay_id
WHERE s.status = 'Completed'
LIMIT 1;

-- MISSED COLLECTION COUNT
SELECT COUNT(*) AS count
FROM schedule
WHERE schedule_date < ?
  AND (status != 'Completed' OR status IS NULL);

-- MISSED COLLECTION BARANGAY
SELECT b.barangay_name
FROM schedule s
JOIN barangay b ON s.barangay_id = b.barangay_id
WHERE s.schedule_date < ?
  AND (s.status != 'Completed' OR s.status IS NULL)
LIMIT 1;

-- CHECK IF COMPLAINT TABLE EXISTS
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'wcms'
  AND table_name = 'complaint';

-- UNREAD COMPLAINTS COUNT
SELECT COUNT(*) AS count
FROM complaint
WHERE is_read = 0 OR is_read IS NULL;

-- ==========================================================================

-- COUNT BARANGAY
SELECT COUNT(*) FROM barangay;

-- COUNT SCHEDULE
SELECT COUNT(*) FROM schedule;

-- COUNT REPORT
SELECT COUNT(*) FROM report;

-- ==========================================================================

-- SELECT ALL PERSONNEL
SELECT * FROM personnel
ORDER BY personnel_name ASC;

-- SELECT UNASSIGNED PERSONNEL
SELECT * FROM personnel
WHERE status = 'Unassigned'
ORDER BY personnel_name ASC;

-- SELECT BY ID
SELECT * FROM personnel
WHERE personnel_id = ?;

-- SELECT BY ROLE
SELECT * FROM personnel
WHERE role = ?
ORDER BY personnel_name ASC;

-- INSERT PERSONNEL
INSERT INTO personnel
(personnel_name, age, gender, address, contact_number, team_name, role, status)
VALUES (?, ?, ?, ?, ?, ?, ?, ?);

-- UPDATE PERSONNEL
UPDATE personnel
SET personnel_name = ?,
    age = ?,
    gender = ?,
    address = ?,
    contact_number = ?,
    team_name = ?,
    role = ?,
    status = ?
WHERE personnel_id = ?;

-- DELETE PERSONNEL
DELETE FROM personnel
WHERE personnel_id = ?;

-- UPDATE STATUS
UPDATE personnel
SET status = ?
WHERE personnel_id = ?;

-- COUNT TOTAL PERSONNEL
SELECT COUNT(*) AS count
FROM personnel;

-- COUNT ACTIVE PERSONNEL
SELECT COUNT(*) AS count
FROM personnel
WHERE status = 'Active';

-- COUNT UNASSIGNED PERSONNEL
SELECT COUNT(*) AS count
FROM personnel
WHERE status = 'Unassigned';

-- ==========================================================================

-- SELECT CHECKLIST BY BARANGAY
SELECT checklist_id, barangay_id, purok_name, is_collected, updated_at
FROM purok_checklist
WHERE barangay_id = ?
ORDER BY checklist_id ASC;

-- CHECK IF BARANGAY HAS CHECKLIST ITEMS
SELECT COUNT(*)
FROM purok_checklist
WHERE barangay_id = ?;

-- INSERT DEFAULT PUROK ITEMS
INSERT INTO purok_checklist (barangay_id, purok_name, is_collected)
VALUES (?, ?, 0);

-- UPDATE COLLECTED STATUS
UPDATE purok_checklist
SET is_collected = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE checklist_id = ?;

-- CREATE TABLE
CREATE TABLE purok_checklist (
    checklist_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barangay_id INT NOT NULL,
    purok_name VARCHAR(100) NOT NULL,
    is_collected TINYINT(1) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ==========================================================================

-- BASE SELECT (JOIN BARANGAY)
SELECT r.*, b.barangay_name
FROM report r
LEFT JOIN barangay b ON r.barangay_id = b.barangay_id;

-- SELECT BY ID
SELECT r.*, b.barangay_name
FROM report r
LEFT JOIN barangay b ON r.barangay_id = b.barangay_id
WHERE r.report_id = ?;

-- SELECT ALL (NOT ARCHIVED)
SELECT r.*, b.barangay_name
FROM report r
LEFT JOIN barangay b ON r.barangay_id = b.barangay_id
WHERE r.is_archived = 0
ORDER BY r.report_id DESC;

-- SELECT ARCHIVED
SELECT r.*, b.barangay_name
FROM report r
LEFT JOIN barangay b ON r.barangay_id = b.barangay_id
WHERE r.is_archived = ?
ORDER BY CASE WHEN r.archived_at IS NULL THEN 0 ELSE 1 END DESC,
         r.created_at DESC;

-- SELECT BY STATUS
SELECT r.*, b.barangay_name
FROM report r
LEFT JOIN barangay b ON r.barangay_id = b.barangay_id
WHERE r.is_archived = 0 AND r.status = ?
ORDER BY r.report_id DESC;

-- SELECT UNREAD
SELECT r.*, b.barangay_name
FROM report r
LEFT JOIN barangay b ON r.barangay_id = b.barangay_id
WHERE r.is_archived = 0
  AND (r.is_read = FALSE OR r.is_read IS NULL)
ORDER BY r.report_id DESC;

-- INSERT REPORT
INSERT INTO report
(message, proof, status, is_read, barangay_id, type, response_message, purok_analytics, is_archived)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0);

-- UPDATE REPORT
UPDATE report
SET message = ?,
    proof = ?,
    status = ?,
    is_read = ?,
    barangay_id = ?,
    type = ?,
    response_message = ?,
    purok_analytics = ?,
    is_archived = ?,
    archived_at = ?
WHERE report_id = ?;

-- DELETE REPORT
DELETE FROM report
WHERE report_id = ?;

-- MARK AS READ
UPDATE report
SET is_read = TRUE
WHERE report_id = ?;

-- UPDATE STATUS
UPDATE report
SET status = ?
WHERE report_id = ?;

-- ARCHIVE EXPIRED (OLDER THAN 7 DAYS)
UPDATE report
SET is_archived = 1,
    archived_at = CURRENT_TIMESTAMP
WHERE is_archived = 0
  AND created_at <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY);

-- COUNT TOTAL (NOT ARCHIVED)
SELECT COUNT(*)
FROM report
WHERE is_archived = 0;

-- COUNT ARCHIVED
SELECT COUNT(*)
FROM report
WHERE is_archived = 1;

-- COUNT BY STATUS
SELECT COUNT(*)
FROM report
WHERE is_archived = 0 AND status = ?;

-- COUNT UNREAD
SELECT COUNT(*)
FROM report
WHERE is_archived = 0
  AND (is_read = FALSE OR is_read IS NULL);

-- ALTER TABLE (DYNAMIC COLUMNS)
ALTER TABLE report ADD COLUMN type VARCHAR(60) NULL;
ALTER TABLE report ADD COLUMN response_message TEXT NULL;
ALTER TABLE report ADD COLUMN purok_analytics TEXT NULL;
ALTER TABLE report ADD COLUMN is_archived TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE report ADD COLUMN archived_at TIMESTAMP NULL;

-- ==========================================================================

-- INSERT REQUEST
INSERT INTO request
(barangay_id, barangay_name, type, message, proof, status, is_read, is_archived, location, response_message)
VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?);

-- UPDATE REQUEST
UPDATE request
SET barangay_id = ?,
    barangay_name = ?,
    type = ?,
    message = ?,
    proof = ?,
    status = ?,
    is_read = ?,
    is_archived = ?,
    archived_at = ?,
    location = ?,
    response_message = ?
WHERE request_id = ?;

-- DELETE REQUEST
DELETE FROM request
WHERE request_id = ?;

-- SELECT BY ID
SELECT * FROM request
WHERE request_id = ?;

-- SELECT ALL (NOT ARCHIVED)
SELECT * FROM request
WHERE is_archived = 0
ORDER BY created_at DESC;

-- SELECT ARCHIVED
SELECT * FROM request
WHERE is_archived = 1
ORDER BY created_at DESC;

-- SELECT BY STATUS
SELECT * FROM request
WHERE is_archived = 0 AND status = ?
ORDER BY request_id DESC;

-- SELECT UNREAD
SELECT * FROM request
WHERE is_archived = 0
  AND (is_read = FALSE OR is_read IS NULL)
ORDER BY request_id DESC;

-- MARK AS READ
UPDATE request
SET is_read = TRUE
WHERE request_id = ?;

-- UPDATE STATUS
UPDATE request
SET status = ?
WHERE request_id = ?;

-- COUNT TOTAL (NOT ARCHIVED)
SELECT COUNT(*)
FROM request
WHERE is_archived = 0;

-- COUNT ARCHIVED
SELECT COUNT(*)
FROM request
WHERE is_archived = 1;

-- COUNT BY STATUS
SELECT COUNT(*)
FROM request
WHERE is_archived = 0 AND status = ?;

-- COUNT UNREAD
SELECT COUNT(*)
FROM request
WHERE is_archived = 0
  AND (is_read = FALSE OR is_read IS NULL);

-- ARCHIVE EXPIRED (OLDER THAN 7 DAYS)
UPDATE request
SET is_archived = 1,
    archived_at = CURRENT_TIMESTAMP
WHERE is_archived = 0
  AND created_at <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY);

-- SELECT BY ARCHIVE FLAG
SELECT * FROM request
WHERE is_archived = ?
ORDER BY created_at DESC;

-- ALTER TABLE (DYNAMIC COLUMNS)
ALTER TABLE request ADD COLUMN barangay_id INT NULL;
ALTER TABLE request ADD COLUMN barangay_name VARCHAR(160) NULL;
ALTER TABLE request ADD COLUMN type VARCHAR(60) NULL;
ALTER TABLE request ADD COLUMN response_message TEXT NULL;
ALTER TABLE request ADD COLUMN is_archived TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE request ADD COLUMN archived_at TIMESTAMP NULL;
ALTER TABLE request ADD COLUMN location VARCHAR(255) NULL;

-- ==========================================================================

-- SELECT ALL SCHEDULES (FULL JOINED DATA)
SELECT s.schedule_id,
       s.barangay_id,
       s.team_id,
       s.schedule_date,
       s.schedule_time,
       s.status,
       b.barangay_name,
       b.contact,
       ba.barangay_admin,
       t.team_name,
       tr.plate_number AS truck_plate_number,
       tr.truck_type,
       tr.assigned_team
FROM schedule s
LEFT JOIN barangay b ON s.barangay_id = b.barangay_id
LEFT JOIN barangay_admin ba ON b.barangay_id = ba.barangay_id
LEFT JOIN team t ON s.team_id = t.team_id
LEFT JOIN truck tr
    ON (t.truck_id = tr.truck_id
        OR UPPER(TRIM(tr.assigned_team)) = UPPER(TRIM(t.team_name)))
ORDER BY s.schedule_date ASC,
         s.schedule_time ASC,
         b.barangay_name ASC;

-- SELECT CURRENT COLLECTION INFO (BY BARANGAY NAME)
SELECT t.team_name,
       tr.plate_number AS truck_plate_number,
       tr.truck_type,
       s.schedule_time,
       s.status
FROM schedule s
LEFT JOIN barangay b ON s.barangay_id = b.barangay_id
LEFT JOIN team t ON s.team_id = t.team_id
LEFT JOIN truck tr
    ON (t.truck_id = tr.truck_id
        OR UPPER(TRIM(tr.assigned_team)) = UPPER(TRIM(t.team_name)))
WHERE UPPER(TRIM(b.barangay_name)) = UPPER(TRIM(?))
ORDER BY s.schedule_date ASC,
         s.schedule_time ASC
LIMIT 1;

-- FIND BARANGAY ID
SELECT barangay_id
FROM barangay
WHERE UPPER(TRIM(barangay_name)) = UPPER(TRIM(?));

-- FIND TEAM ID
SELECT team_id
FROM team
WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?));

-- FIND SCHEDULE ID BY BARANGAY
SELECT schedule_id
FROM schedule
WHERE barangay_id = ?;

-- UPDATE SCHEDULE
UPDATE schedule
SET team_id = ?,
    schedule_date = ?,
    schedule_time = ?,
    status = ?
WHERE schedule_id = ?;

-- INSERT SCHEDULE
INSERT INTO schedule
(barangay_id, team_id, schedule_date, schedule_time, status)
VALUES (?, ?, ?, ?, ?);

-- DELETE SCHEDULE
DELETE FROM schedule
WHERE schedule_id = ?;

-- ==========================================================================

ALTER TABLE team ADD COLUMN IF NOT EXISTS driver_id INT NULL;

ALTER TABLE team ADD COLUMN IF NOT EXISTS truck_id INT NULL;

ALTER TABLE truck ADD COLUMN IF NOT EXISTS assigned_team VARCHAR(120) NULL;

CREATE TABLE IF NOT EXISTS team_collectors (
    team_id INT NOT NULL,
    personnel_id INT NOT NULL,
    PRIMARY KEY (team_id, personnel_id)
);

-- Get all teams
SELECT t.*, p.personnel_name AS leader_name,
       d.personnel_name AS driver_name,
       tr.plate_number AS truck_plate_number
FROM team t
LEFT JOIN personnel p ON t.leader_id = p.personnel_id
LEFT JOIN personnel d ON t.driver_id = d.personnel_id
LEFT JOIN truck tr ON t.truck_id = tr.truck_id
ORDER BY t.team_name ASC;

-- Get team by ID
SELECT t.*, p.personnel_name AS leader_name,
       d.personnel_name AS driver_name,
       tr.plate_number AS truck_plate_number
FROM team t
LEFT JOIN personnel p ON t.leader_id = p.personnel_id
LEFT JOIN personnel d ON t.driver_id = d.personnel_id
LEFT JOIN truck tr ON t.truck_id = tr.truck_id
WHERE t.team_id = ?;

-- Count all teams
SELECT COUNT(*) AS count FROM team;

-- Count active teams
SELECT COUNT(*) AS count FROM team WHERE status = 'Active';

-- Get collectors (link table)
SELECT tc.personnel_id, p.personnel_name
FROM team_collectors tc
LEFT JOIN personnel p ON tc.personnel_id = p.personnel_id
WHERE tc.team_id = ?
ORDER BY p.personnel_name ASC;

-- Get collectors from personnel fallback
SELECT personnel_id, personnel_name
FROM personnel
WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?))
  AND UPPER(TRIM(role)) = 'COLLECTOR'
ORDER BY personnel_name ASC;

-- Get driver fallback
SELECT personnel_id, personnel_name
FROM personnel
WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?))
  AND UPPER(TRIM(role)) = 'DRIVER'
ORDER BY personnel_name ASC
LIMIT 1;

-- Get truck fallback
SELECT truck_id, plate_number
FROM truck
WHERE UPPER(TRIM(assigned_team)) = UPPER(TRIM(?))
ORDER BY plate_number ASC
LIMIT 1;

INSERT INTO team (team_name, leader_id, driver_id, truck_id, status)
VALUES (?, ?, ?, ?, ?);

INSERT INTO team_collectors (team_id, personnel_id)
VALUES (?, ?);

UPDATE team
SET team_name = ?, leader_id = ?, driver_id = ?, truck_id = ?, status = ?
WHERE team_id = ?;

UPDATE personnel
SET team_name = NULL
WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?));

UPDATE personnel
SET team_name = ?
WHERE personnel_id = ?;

UPDATE truck
SET assigned_team = NULL
WHERE truck_id = ?;

UPDATE truck
SET assigned_team = NULL
WHERE UPPER(TRIM(assigned_team)) = UPPER(TRIM(?));

UPDATE truck
SET assigned_team = ?
WHERE truck_id = ?;

DELETE FROM team_collectors WHERE team_id = ?;

DELETE FROM team WHERE team_id = ?;

-- ==========================================================================

ALTER TABLE truck ADD COLUMN IF NOT EXISTS capacity VARCHAR(100) NULL;

ALTER TABLE truck ADD COLUMN IF NOT EXISTS truck_capacity VARCHAR(100) NULL;

ALTER TABLE truck ADD COLUMN IF NOT EXISTS assigned_barangay VARCHAR(150) NULL;

ALTER TABLE truck ADD COLUMN IF NOT EXISTS barangay_assigned VARCHAR(150) NULL;

ALTER TABLE truck ADD COLUMN IF NOT EXISTS barangay_name VARCHAR(150) NULL;

ALTER TABLE truck ADD COLUMN IF NOT EXISTS assigned_team VARCHAR(120) NULL;

-- Get all trucks
SELECT * FROM truck
ORDER BY plate_number ASC;

-- Get truck by ID
SELECT * FROM truck
WHERE truck_id = ?;

-- Count all trucks
SELECT COUNT(*) AS count FROM truck;

-- Count active trucks
SELECT COUNT(*) AS count FROM truck WHERE status = 'Active';

INSERT INTO truck (plate_number, truck_type, status, capacity, assigned_barangay)
VALUES (?, ?, ?, ?, ?);

UPDATE truck
SET plate_number = ?, truck_type = ?, status = ?, capacity = ?, assigned_barangay = ?
WHERE truck_id = ?;

DELETE FROM truck
WHERE truck_id = ?;

-- ==========================================================================
-- ==========================================================================
-- ==========================================================================
-- ==========================================================================