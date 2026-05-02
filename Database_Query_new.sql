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