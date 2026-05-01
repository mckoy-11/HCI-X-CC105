CREATE DATABASE IF NOT EXISTS wcms;
USE wcms;

-- =========================================================
-- 1. AUTH / IDENTITY LAYER
-- =========================================================

CREATE TABLE accounts (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE account_roles (
    account_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (account_id, role_id),

    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

-- =========================================================
-- 2. STATUS SYSTEM (FULLY NORMALIZED - NO ENUMS)
-- =========================================================

CREATE TABLE status_domains (
    status_domain_id INT AUTO_INCREMENT PRIMARY KEY,
    domain_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE statuses (
    status_id INT AUTO_INCREMENT PRIMARY KEY,
    status_domain_id INT NOT NULL,
    status_name VARCHAR(50) NOT NULL,

    UNIQUE (status_domain_id, status_name),

    FOREIGN KEY (status_domain_id)
        REFERENCES status_domains(status_domain_id)
        ON DELETE CASCADE
);

-- =========================================================
-- 3. BARANGAY MODULE
-- =========================================================

CREATE TABLE collection_days (
    collection_day_id INT AUTO_INCREMENT PRIMARY KEY,
    day_name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE barangays (
    barangay_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    household_count INT DEFAULT 0,
    contact_number VARCHAR(50),
    collection_day_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (collection_day_id)
        REFERENCES collection_days(collection_day_id)
);

CREATE TABLE barangay_admins (
    barangay_admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    barangay_id BIGINT NOT NULL UNIQUE,
    account_id BIGINT NOT NULL UNIQUE,

    FOREIGN KEY (barangay_id) REFERENCES barangays(barangay_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- =========================================================
-- 4. PERSONNEL MODULE
-- =========================================================

CREATE TABLE genders (
    gender_id INT AUTO_INCREMENT PRIMARY KEY,
    gender_name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE personnel_roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE personnel (
    personnel_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    age INT,
    gender_id INT,
    address VARCHAR(255),
    contact_number VARCHAR(50),
    role_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (role_id) REFERENCES personnel_roles(role_id),
    FOREIGN KEY (gender_id) REFERENCES genders(gender_id)
);

-- =========================================================
-- 5. TRANSPORT MODULE
-- =========================================================

CREATE TABLE truck_types (
    truck_type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE trucks (
    truck_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(30) NOT NULL UNIQUE,
    truck_type_id INT NOT NULL,
    status_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (truck_type_id) REFERENCES truck_types(truck_type_id),
    FOREIGN KEY (status_id) REFERENCES statuses(status_id)
);

-- =========================================================
-- 6. TEAM SYSTEM
-- =========================================================

CREATE TABLE teams (
    team_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(80) NOT NULL UNIQUE,
    status_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (status_id) REFERENCES statuses(status_id)
);

CREATE TABLE team_member_roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE team_members (
    team_id BIGINT NOT NULL,
    personnel_id BIGINT NOT NULL,
    member_role_id INT NOT NULL,

    PRIMARY KEY (team_id, personnel_id),

    FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (personnel_id) REFERENCES personnel(personnel_id) ON DELETE CASCADE,
    FOREIGN KEY (member_role_id) REFERENCES team_member_roles(role_id)
);

-- =========================================================
-- 7. SCHEDULING SYSTEM
-- =========================================================

CREATE TABLE schedules (
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    barangay_id BIGINT NOT NULL,
    team_id BIGINT,
    schedule_datetime DATETIME NOT NULL,
    status_id INT NOT NULL,

    UNIQUE (barangay_id, schedule_datetime),

    FOREIGN KEY (barangay_id) REFERENCES barangays(barangay_id),
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (status_id) REFERENCES statuses(status_id)
);

-- =========================================================
-- 8. TICKET SYSTEM (COMPLAINT / REPORT / REQUEST UNIFIED)
-- =========================================================

CREATE TABLE ticket_types (
    ticket_type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE tickets (
    ticket_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_type_id INT NOT NULL,
    account_id BIGINT,
    barangay_id BIGINT,
    message TEXT,
    proof BLOB,
    location VARCHAR(255),
    is_read BOOLEAN DEFAULT FALSE,
    status_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (ticket_type_id) REFERENCES ticket_types(ticket_type_id),
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL,
    FOREIGN KEY (barangay_id) REFERENCES barangays(barangay_id) ON DELETE SET NULL,
    FOREIGN KEY (status_id) REFERENCES statuses(status_id)
);

================================================================================
WCMS SAMPLE DATA - SQL INSERT STATEMENTS
================================================================================
NOTE: These are sample inserts. Adjust column names and values based on your 
      actual database schema. The queries are designed to work with the schema
      inferred from the DAO classes.

================================================================================
BARANGAY TABLE - 10 Sample Records
================================================================================

INSERT INTO barangay (barangay_name, barangay_household, contact, collection_day, status) VALUES
('Bagong Silang', 250, '09123456789', 'Monday', 'Active'),
('Santo Niño', 180, '09123456790', 'Tuesday', 'Active'),
('San Jose', 320, '09123456791', 'Wednesday', 'Active'),
('Maliksi', 210, '09123456792', 'Thursday', 'Active'),
('Poblacion', 450, '09123456793', 'Friday', 'Active'),
('San Roque', 190, '09123456794', 'Monday', 'Active'),
('Imus', 280, '09123456795', 'Tuesday', 'Active'),
('Bayan', 310, '09123456796', 'Wednesday', 'Active'),
('Luzon', 165, '09123456797', 'Thursday', 'Active'),
('Visayas', 230, '09123456798', 'Friday', 'Active');

================================================================================
TRUCK TABLE - 10 Sample Records
================================================================================

INSERT INTO truck (plate_number, truck_type, capacity, status, assigned_barangay, assigned_team) VALUES
('TRK-001', 'Compactor', '5000 kg', 'Active', 'Bagong Silang', 'Team Alpha'),
('TRK-002', 'Dump Truck', '3000 kg', 'Active', 'Santo Niño', 'Team Bravo'),
('TRK-003', 'Garbage Truck', '4500 kg', 'Active', 'San Jose', 'Team Charlie'),
('TRK-004', 'Container Carrier', '8000 kg', 'Maintenance', NULL, NULL),
('TRK-005', 'Compactor', '5500 kg', 'Active', 'Poblacion', 'Team Delta'),
('TRK-006', 'Dump Truck', '2800 kg', 'Active', 'San Roque', 'Team Echo'),
('TRK-007', 'Garbage Truck', '4200 kg', 'Inactive', NULL, NULL),
('TRK-008', 'Compactor', '5200 kg', 'Active', 'Imus', 'Team Foxtrot'),
('TRK-009', 'Dump Truck', '3200 kg', 'Active', 'Bayan', 'Team Golf'),
('TRK-010', 'Other', '2500 kg', 'Decommissioned', NULL, NULL);

================================================================================
PERSONNEL TABLE - 10 Sample Records
================================================================================

INSERT INTO personnel (personnel_name, age, gender, address, contact_number, team_name, role, status) VALUES
('Juan dela Cruz', 35, 'Male', '123 Main St, Bagong Silang', '09991234567', 'Team Alpha', 'Driver', 'Active'),
('Maria Santos', 28, 'Female', '456 Oak Ave, Santo Niño', '09991234568', 'Team Alpha', 'Collector', 'Active'),
('Pedro Garcia', 42, 'Male', '789 Pine St, San Jose', '09991234569', 'Team Bravo', 'Driver', 'Active'),
('Ana Reyes', 31, 'Female', '321 Elm St, Maliksi', '09991234570', 'Team Bravo', 'Collector', 'Active'),
('Luis Moreno', 38, 'Male', '654 Maple Rd, Poblacion', '09991234571', 'Team Charlie', 'Driver', 'Active'),
('Carmen Lopez', 26, 'Female', '987 Cedar Ln, San Roque', '09991234572', 'Team Charlie', 'Collector', 'Active'),
('Josefino Wong', 45, 'Male', '147 Birch Blvd, Imus', '09991234573', 'Team Delta', 'Driver', 'Active'),
('Theresa Cu', 29, 'Female', '258 Walnut Ave, Bayan', '09991234574', 'Team Delta', 'Collector', 'Active'),
('Ricardo Tan', 33, 'Male', '369 Cherry St, Luzon', '09991234575', 'Team Echo', 'Driver', 'Active'),
('Felisa Kim', 27, 'Female', '741 Spruce Rd, Visayas', '09991234576', 'Team Echo', 'Collector', 'Active');

================================================================================
TEAM TABLE - 10 Sample Records
================================================================================

INSERT INTO team (team_name, leader_id, driver_id, truck_id, status) VALUES
-- Note: leader_id and driver_id should reference valid personnel_id values
('Team Alpha', 1, 2, 1, 'Active'),
('Team Bravo', 3, 4, 2, 'Active'),
('Team Charlie', 5, 6, 3, 'Active'),
('Team Delta', 7, 8, 5, 'Active'),
('Team Echo', 9, 10, 6, 'Active'),
('Team Foxtrot', NULL, NULL, 8, 'Active'),
('Team Golf', NULL, NULL, 9, 'Inactive'),
('Team Hotel', NULL, NULL, NULL, 'Inactive'),
('Team India', NULL, NULL, NULL, 'Inactive'),
('Team Juliet', NULL, NULL, NULL, 'Active');

================================================================================
TEAM_COLLECTORS TABLE (Optional) - Sample Records
================================================================================

-- If using team_collectors junction table:
INSERT INTO team_collectors (team_id, personnel_id) VALUES
(1, 2),   -- Team Alpha has collector Maria Santos
(1, 12),  -- Additional collector
(2, 4),   -- Team Bravo has collector Ana Reyes
(2, 13),  -- Additional collector
(3, 6),   -- Team Charlie has collector Carmen Lopez
(4, 8),   -- Team Delta has collector Theresa Cu
(5, 10);   -- Team Echo has collector Felisa Kim

-- Additional personnel for teams (collector/helper roles)
INSERT INTO personnel (personnel_name, age, gender, address, contact_number, team_name, role, status) VALUES
('Mario Hernandez', 29, 'Male', '111 Oak St, Bagong Silang', '09991234577', 'Team Alpha', 'Helper', 'Active'),
('Patricia Devera', 24, 'Female', '222 Pine St, Santo Niño', '09991234578', 'Team Bravo', 'Helper', 'Active'),
('Francis Valero', 32, 'Male', '333 Maple Ave, San Jose', '09991234579', 'Team Charlie', 'Helper', 'Active'),
('Janice Marcelo', 27, 'Female', '444 Cedar Ln, Maliksi', '09991234580', 'Team Delta', 'Helper', 'Active'),
('Michael Reyes', 30, 'Male', '555 Elm St, Poblacion', '09991234581', 'Team Echo', 'Helper', 'Active'),
('Jessica Lee', 25, 'Female', '666 Birch Blvd, Imus', '09991234582', 'Team Foxtrot', 'Driver', 'Active'),
('Kevin Salazar', 34, 'Male', '777 Walnut Ave, Bayan', '09991234583', 'Team Foxtrot', 'Collector', 'Active'),
('Angela Torres', 28, 'Female', '888 Cherry St, Luzon', '09991234584', 'Team Golf', 'Driver', 'Active'),
('Daniel Mendoza', 36, 'Male', '999 Spruce Rd, Visayas', '09991234585', 'Team Golf', 'Collector', 'Active'),
('Grace Chua', 26, 'Female', '100 Ash Ln, Bagong Silang', '09991234586', 'Team Hotel', 'Driver', 'Inactive');

================================================================================
SCHEDULE TABLE - 10 Sample Records
================================================================================

INSERT INTO schedule (barangay_id, team_id, schedule_date, schedule_time, status) VALUES
-- Note: barangay_id and team_id should reference valid IDs
(1, 1, '2024-01-15', '08:00:00', 'Scheduled'),
(2, 2, '2024-01-16', '08:30:00', 'Scheduled'),
(3, 3, '2024-01-17', '09:00:00', 'Confirmed'),
(4, 4, '2024-01-18', '08:00:00', 'Scheduled'),
(5, 5, '2024-01-19', '08:30:00', 'Scheduled'),
(6, 1, '2024-01-22', '09:00:00', 'Pending'),
(7, 2, '2024-01-23', '08:00:00', 'Scheduled'),
(8, 3, '2024-01-24', '08:30:00', 'Pending'),
(9, 4, '2024-01-25', '09:00:00', 'Scheduled'),
(10, 5, '2024-01-26', '08:00:00', 'Completed');

================================================================================
ACCOUNT TABLE - Sample Records (if exists)
================================================================================

-- Sample accounts for login (if using separate account table)
-- INSERT INTO account (username, password, role, status) VALUES
-- ('admin', 'admin123', 'Admin', 'Active'),
-- ('user1', 'user123', 'User', 'Active');

================================================================================
ANNOUNCEMENT TABLE - Sample Records
================================================================================

INSERT INTO announcement (title, message, is_active, is_archived, created_at, expires_at) VALUES
('Collection Schedule Update', 'Effective next week, collection in Bagong Silang will be at 8:00 AM', 1, 0, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY)),
('Holiday Notice', 'No collection on Christmas Day', 0, 0, NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY)),
('New Truck Route', 'Team Alpha will cover Santo Niño area starting next month', 1, 0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));

================================================================================
SAMPLE DATA NOTES:
================================================================================

1. The INSERT statements above assume these tables exist in your database:
   - barangay
   - truck
   - personnel
   - team
   - team_collectors (optional junction table)
   - schedule
   - announcement

2. Foreign key relationships:
   - team.leader_id -> personnel.personnel_id
   - team.driver_id -> personnel.personnel_id
   - team.truck_id -> truck.truck_id
   - schedule.barangay_id -> barangay.barangay_id
   - schedule.team_id -> team.team_id
   - team_collectors.team_id -> team.team_id
   - team_collectors.personnel_id -> personnel.personnel_id

3. Before running these inserts:
   - Make sure the tables exist (run CREATE TABLE statements if needed)
   - Adjust column names to match your actual schema
   - Ensure foreign key references are valid
   - Run in order: barangay -> truck -> personnel -> team -> team_collectors -> schedule

4. Sample data is designed to be consistent:
   - Trucks are assigned to teams
   - Personnel are assigned to teams
   - Schedules reference valid barangay and team IDs

================================================================================
END OF SAMPLE DATA
================================================================================
