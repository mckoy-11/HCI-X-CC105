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

/* =========================
   ACCOUNT (20)
========================= */
INSERT INTO account (name, email_address, password, status, role, is_barangay_setup_complete)
VALUES
('Juan Dela Cruz','juan1@mail.com','pass123','Active','BARANGAY',1),
('Maria Santos','maria2@mail.com','pass123','Active','BARANGAY',1),
('Pedro Reyes','pedro3@mail.com','pass123','Active','BARANGAY',1),
('Ana Lopez','ana4@mail.com','pass123','Active','BARANGAY',1),
('Mark Cruz','mark5@mail.com','pass123','Active','BARANGAY',1),
('Liza Gomez','liza6@mail.com','pass123','Active','BARANGAY',1),
('Jose Tan','jose7@mail.com','pass123','Active','BARANGAY',1),
('Carla Lim','carla8@mail.com','pass123','Active','BARANGAY',1),
('Ben Garcia','ben9@mail.com','pass123','Active','BARANGAY',1),
('Nina Flores','nina10@mail.com','pass123','Active','BARANGAY',1),
('Omar Diaz','omar11@mail.com','pass123','Active','BARANGAY',1),
('Ella Cruz','ella12@mail.com','pass123','Active','BARANGAY',1),
('Ryan Bautista','ryan13@mail.com','pass123','Active','BARANGAY',1),
('Sofia Reyes','sofia14@mail.com','pass123','Active','BARANGAY',1),
('Luis Mendoza','luis15@mail.com','pass123','Active','BARANGAY',1),
('Ava Torres','ava16@mail.com','pass123','Active','BARANGAY',1),
('Noah Ramos','noah17@mail.com','pass123','Active','BARANGAY',1),
('Mia Fernandez','mia18@mail.com','pass123','Active','BARANGAY',1),
('Ethan Castillo','ethan19@mail.com','pass123','Active','BARANGAY',1),
('Chloe Navarro','chloe20@mail.com','pass123','Active','BARANGAY',1);


/* =========================
   BARANGAY (20)
========================= */
INSERT INTO barangay (barangay_name, barangay_household, purok_count, population, contact, collection_day)
VALUES
('Barangay 1',100,5,500,'0917000001','Monday'),
('Barangay 2',120,6,600,'0917000002','Tuesday'),
('Barangay 3',90,4,450,'0917000003','Wednesday'),
('Barangay 4',110,5,550,'0917000004','Thursday'),
('Barangay 5',130,6,650,'0917000005','Friday'),
('Barangay 6',95,4,480,'0917000006','Saturday'),
('Barangay 7',140,7,700,'0917000007','Monday'),
('Barangay 8',150,8,750,'0917000008','Tuesday'),
('Barangay 9',85,4,420,'0917000009','Wednesday'),
('Barangay 10',160,8,800,'0917000010','Thursday'),
('Barangay 11',170,9,850,'0917000011','Friday'),
('Barangay 12',180,10,900,'0917000012','Saturday'),
('Barangay 13',190,10,950,'0917000013','Monday'),
('Barangay 14',200,11,1000,'0917000014','Tuesday'),
('Barangay 15',210,12,1050,'0917000015','Wednesday'),
('Barangay 16',220,12,1100,'0917000016','Thursday'),
('Barangay 17',230,13,1150,'0917000017','Friday'),
('Barangay 18',240,14,1200,'0917000018','Saturday'),
('Barangay 19',250,15,1250,'0917000019','Monday'),
('Barangay 20',260,16,1300,'0917000020','Tuesday');


/* =========================
   BARANGAY ADMIN (20)
========================= */
INSERT INTO barangay_admin (barangay_id, account_id, barangay_admin, age, gender)
VALUES
(1,1,'Admin 1',35,'Male'),
(2,2,'Admin 2',40,'Female'),
(3,3,'Admin 3',38,'Male'),
(4,4,'Admin 4',45,'Female'),
(5,5,'Admin 5',33,'Male'),
(6,6,'Admin 6',36,'Female'),
(7,7,'Admin 7',42,'Male'),
(8,8,'Admin 8',39,'Female'),
(9,9,'Admin 9',41,'Male'),
(10,10,'Admin 10',37,'Female'),
(11,11,'Admin 11',44,'Male'),
(12,12,'Admin 12',32,'Female'),
(13,13,'Admin 13',46,'Male'),
(14,14,'Admin 14',34,'Female'),
(15,15,'Admin 15',43,'Male'),
(16,16,'Admin 16',31,'Female'),
(17,17,'Admin 17',47,'Male'),
(18,18,'Admin 18',30,'Female'),
(19,19,'Admin 19',48,'Male'),
(20,20,'Admin 20',29,'Female');


/* =========================
   ANNOUNCEMENT (20)
========================= */
INSERT INTO announcement (title, message)
VALUES
('Waste Notice 1','Garbage collection schedule update'),
('Waste Notice 2','Segregation reminder'),
('Waste Notice 3','Clean-up drive announcement'),
('Waste Notice 4','Trash pickup delay notice'),
('Waste Notice 5','Waste disposal guidelines'),
('Waste Notice 6','Community cleanup program'),
('Waste Notice 7','Recycling awareness drive'),
('Waste Notice 8','Waste audit schedule'),
('Waste Notice 9','Illegal dumping warning'),
('Waste Notice 10','Garbage truck schedule'),
('Waste Notice 11','Waste reduction campaign'),
('Waste Notice 12','Plastic ban reminder'),
('Waste Notice 13','Street cleaning advisory'),
('Waste Notice 14','Waste bin distribution'),
('Waste Notice 15','Clean environment pledge'),
('Waste Notice 16','Trash segregation enforcement'),
('Waste Notice 17','Waste management update'),
('Waste Notice 18','Community cleanup report'),
('Waste Notice 19','Waste collection change'),
('Waste Notice 20','Emergency waste advisory');


/* =========================
   COMPLAINT (WASTE ONLY - 20)
========================= */
INSERT INTO complaint (barangay_id, barangay_name, type, message, status)
VALUES
(1,'Barangay 1','Waste','Uncollected garbage','Pending'),
(2,'Barangay 2','Garbage','Overflowing bins','Resolved'),
(3,'Barangay 3','Waste','Delayed pickup','Pending'),
(4,'Barangay 4','Trash','Illegal dumping','Resolved'),
(5,'Barangay 5','Waste','Scattered trash','Pending'),
(6,'Barangay 6','Garbage','Missed collection','Pending'),
(7,'Barangay 7','Waste','Burning waste','Resolved'),
(8,'Barangay 8','Trash','Blocked drainage','Pending'),
(9,'Barangay 9','Waste','Accumulated garbage','Resolved'),
(10,'Barangay 10','Garbage','Improper segregation','Pending'),
(11,'Barangay 11','Waste','Littering issue','Resolved'),
(12,'Barangay 12','Garbage','Bad odor waste','Pending'),
(13,'Barangay 13','Waste','Street trash','Resolved'),
(14,'Barangay 14','Trash','Plastic buildup','Pending'),
(15,'Barangay 15','Waste','No pickup','Resolved'),
(16,'Barangay 16','Garbage','Overflow bins','Pending'),
(17,'Barangay 17','Waste','Illegal disposal','Resolved'),
(18,'Barangay 18','Trash','Canal dumping','Pending'),
(19,'Barangay 19','Waste','Uncollected recyclables','Resolved'),
(20,'Barangay 20','Garbage','General waste issue','Pending');


/* =========================
   REPORT (WASTE ONLY - 20)
========================= */
INSERT INTO report (barangay_id, barangay_name, type, message, status, purok_analytics)
VALUES
(1,'Barangay 1','Waste','Waste report','Completed','P1'),
(2,'Barangay 2','Garbage','Trash report','Completed','P2'),
(3,'Barangay 3','Waste','Segregation report','Pending','P3'),
(4,'Barangay 4','Trash','Street audit','Completed','P4'),
(5,'Barangay 5','Waste','Garbage monitoring','Pending','P5'),
(6,'Barangay 6','Garbage','Bin usage report','Completed','P6'),
(7,'Barangay 7','Waste','Dumping report','Pending','P7'),
(8,'Barangay 8','Trash','Cleanliness audit','Completed','P8'),
(9,'Barangay 9','Waste','Pickup report','Pending','P9'),
(10,'Barangay 10','Garbage','Recycling report','Completed','P10'),
(11,'Barangay 11','Waste','Road waste report','Completed','P11'),
(12,'Barangay 12','Trash','Waste audit','Pending','P12'),
(13,'Barangay 13','Waste','Management report','Completed','P13'),
(14,'Barangay 14','Garbage','Overflow report','Pending','P14'),
(15,'Barangay 15','Waste','Cleanup report','Completed','P15'),
(16,'Barangay 16','Trash','Disposal report','Pending','P16'),
(17,'Barangay 17','Waste','Monthly report','Completed','P17'),
(18,'Barangay 18','Garbage','Incident report','Pending','P18'),
(19,'Barangay 19','Waste','Recycling report','Completed','P19'),
(20,'Barangay 20','Trash','Final audit','Pending','P20');


/* =========================
   REQUEST (WASTE ONLY - 20)
========================= */
INSERT INTO request (barangay_id, barangay_name, type, message, status, location)
VALUES
(1,'Barangay 1','Waste Pickup','Garbage collection request','Pending','P1'),
(2,'Barangay 2','Waste Disposal','Extra pickup request','Approved','P2'),
(3,'Barangay 3','Waste Cleanup','Community cleanup','Pending','P3'),
(4,'Barangay 4','Garbage Pickup','Overflow waste','Approved','P4'),
(5,'Barangay 5','Waste Pickup','Missed pickup','Pending','P5'),
(6,'Barangay 6','Trash Removal','Waste removal request','Approved','P6'),
(7,'Barangay 7','Waste Cleanup','Street cleanup','Pending','P7'),
(8,'Barangay 8','Garbage Pickup','Urgent pickup','Approved','P8'),
(9,'Barangay 9','Waste Disposal','Recycling pickup','Pending','P9'),
(10,'Barangay 10','Waste Pickup','Bulk waste request','Approved','P10'),
(11,'Barangay 11','Trash Cleanup','Canal cleanup','Pending','P11'),
(12,'Barangay 12','Garbage Pickup','Community pickup','Approved','P12'),
(13,'Barangay 13','Waste Disposal','Special disposal','Pending','P13'),
(14,'Barangay 14','Waste Pickup','Delayed pickup','Approved','P14'),
(15,'Barangay 15','Trash Removal','Street cleanup','Pending','P15'),
(16,'Barangay 16','Waste Cleanup','Drainage cleaning','Approved','P16'),
(17,'Barangay 17','Garbage Pickup','Emergency pickup','Pending','P17'),
(18,'Barangay 18','Waste Disposal','Backlog waste','Approved','P18'),
(19,'Barangay 19','Trash Cleanup','Road cleanup','Pending','P19'),
(20,'Barangay 20','Waste Pickup','General pickup','Approved','P20');