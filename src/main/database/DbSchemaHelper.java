package main.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DbSchemaHelper {

    private DbSchemaHelper() {
    }

    public static boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) AS count FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?")) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("count") > 0;
            }
        }
    }

    public static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) AS count FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?")) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("count") > 0;
            }
        }
    }

    public static String getFirstColumn(Connection conn, String tableName, String... candidates) throws SQLException {
        for (String column : candidates) {
            if (columnExists(conn, tableName, column)) {
                return column;
            }
        }
        return null;
    }

    /**
     * Migrates the database from the old denormalized schema to the new normalized schema
     * @param conn
     * @throws java.sql.SQLException
     */
    public static void migrateToNormalizedSchema(Connection conn) throws SQLException {
        System.out.println("Checking database schema...");

        // Check if migration is needed (if lookup tables don't exist)
        if (tableExists(conn, "role_lookup") && tableExists(conn, "status_lookup")) {
            System.out.println("Database already migrated to normalized schema.");
            return;
        }

        System.out.println("Migrating database to normalized schema...");

        // Create lookup tables
        createLookupTables(conn);

        // Populate lookup tables with default data
        populateLookupTables(conn);

        // Migrate existing tables
        migrateAccountTable(conn);
        migrateBarangayTable(conn);
        migratePersonnelTable(conn);
        migrateTruckTable(conn);
        migrateTeamTable(conn);

        System.out.println("Database migration completed successfully.");
    }

    private static void createLookupTables(Connection conn) throws SQLException {
        // Role lookup
        try (PreparedStatement stmt = conn.prepareStatement(
            "CREATE TABLE IF NOT EXISTS role_lookup (" +
            "role_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
            "role_key VARCHAR(50) NOT NULL UNIQUE, " +
            "role_label VARCHAR(100) NOT NULL) ENGINE=InnoDB")) {
            stmt.execute();
        }

        // Gender lookup
        try (PreparedStatement stmt = conn.prepareStatement(
            "CREATE TABLE IF NOT EXISTS gender_lookup (" +
            "gender_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
            "gender_key VARCHAR(50) NOT NULL UNIQUE, " +
            "gender_label VARCHAR(100) NOT NULL) ENGINE=InnoDB")) {
            stmt.execute();
        }

        // Status domain
        try (PreparedStatement stmt = conn.prepareStatement(
            "CREATE TABLE IF NOT EXISTS status_domain (" +
            "status_domain_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
            "domain_key VARCHAR(50) NOT NULL UNIQUE, " +
            "domain_label VARCHAR(100) NOT NULL) ENGINE=InnoDB")) {
            stmt.execute();
        }

        // Status lookup
        try (PreparedStatement stmt = conn.prepareStatement(
            "CREATE TABLE IF NOT EXISTS status_lookup (" +
            "status_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
            "status_domain_id INT NOT NULL, " +
            "status_key VARCHAR(50) NOT NULL, " +
            "status_label VARCHAR(100) NOT NULL, " +
            "UNIQUE KEY ux_status_domain_key (status_domain_id, status_key), " +
            "FOREIGN KEY (status_domain_id) REFERENCES status_domain(status_domain_id) " +
            "ON DELETE RESTRICT ON UPDATE CASCADE) ENGINE=InnoDB")) {
            stmt.execute();
        }

        // Truck type lookup
        try (PreparedStatement stmt = conn.prepareStatement(
            "CREATE TABLE IF NOT EXISTS truck_type_lookup (" +
            "truck_type_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
            "truck_type_key VARCHAR(50) NOT NULL UNIQUE, " +
            "truck_type_label VARCHAR(100) NOT NULL) ENGINE=InnoDB")) {
            stmt.execute();
        }
    }

    private static void populateLookupTables(Connection conn) throws SQLException {
        // Insert status domains
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT IGNORE INTO status_domain (domain_key, domain_label) VALUES (?, ?)")) {
            String[][] domains = {
                {"ACCOUNT", "Account Status"},
                {"BARANGAY", "Barangay Status"},
                {"PERSONNEL", "Personnel Status"},
                {"TRUCK", "Truck Status"},
                {"TEAM", "Team Status"}
            };
            for (String[] domain : domains) {
                stmt.setString(1, domain[0]);
                stmt.setString(2, domain[1]);
                stmt.execute();
            }
        }

        // Insert roles
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT IGNORE INTO role_lookup (role_key, role_label) VALUES (?, ?)")) {
            String[][] roles = {
                {"MENRO", "MENRO Admin"},
                {"BARANGAY", "Barangay Official"}
            };
            for (String[] role : roles) {
                stmt.setString(1, role[0]);
                stmt.setString(2, role[1]);
                stmt.execute();
            }
        }

        // Insert genders
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT IGNORE INTO gender_lookup (gender_key, gender_label) VALUES (?, ?)")) {
            String[][] genders = {
                {"MALE", "Male"},
                {"FEMALE", "Female"}
            };
            for (String[] gender : genders) {
                stmt.setString(1, gender[0]);
                stmt.setString(2, gender[1]);
                stmt.execute();
            }
        }

        // Insert statuses
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT IGNORE INTO status_lookup (status_domain_id, status_key, status_label) " +
            "SELECT d.status_domain_id, ?, ? FROM status_domain d WHERE d.domain_key = ?")) {
            String[][] statuses = {
                {"ACTIVE", "Active", "ACCOUNT"},
                {"INACTIVE", "Inactive", "ACCOUNT"},
                {"ACTIVE", "Active", "BARANGAY"},
                {"INACTIVE", "Inactive", "BARANGAY"},
                {"ACTIVE", "Active", "PERSONNEL"},
                {"INACTIVE", "Inactive", "PERSONNEL"},
                {"UNASSIGNED", "Unassigned", "PERSONNEL"},
                {"AVAILABLE", "Available", "TRUCK"},
                {"IN_USE", "In Use", "TRUCK"},
                {"MAINTENANCE", "Under Maintenance", "TRUCK"},
                {"ACTIVE", "Active", "TEAM"},
                {"INACTIVE", "Inactive", "TEAM"}
            };
            for (String[] status : statuses) {
                stmt.setString(1, status[0]);
                stmt.setString(2, status[1]);
                stmt.setString(3, status[2]);
                stmt.execute();
            }
        }

        // Insert truck types
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT IGNORE INTO truck_type_lookup (truck_type_key, truck_type_label) VALUES (?, ?)")) {
            String[][] truckTypes = {
                {"GARBAGE_TRUCK", "Garbage Truck"},
                {"RECYCLING_TRUCK", "Recycling Truck"},
                {"COMPACTOR_TRUCK", "Compactor Truck"}
            };
            for (String[] type : truckTypes) {
                stmt.setString(1, type[0]);
                stmt.setString(2, type[1]);
                stmt.execute();
            }
        }
    }

    private static void migrateAccountTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "account")) return;

        // Add new columns if they don't exist
        if (!columnExists(conn, "account", "status_id")) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE account ADD COLUMN status_id INT NULL, " +
                "ADD COLUMN role_id INT NULL, " +
                "ADD COLUMN gender_id INT NULL")) {
                stmt.execute();
            }
        }

        // Migrate data
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE account a " +
            "SET a.status_id = (SELECT s.status_id FROM status_lookup s " +
            "JOIN status_domain d ON s.status_domain_id = d.status_domain_id " +
            "WHERE d.domain_key = 'ACCOUNT' AND s.status_key = UPPER(a.status)), " +
            "a.role_id = (SELECT r.role_id FROM role_lookup r WHERE r.role_key = UPPER(a.role)), " +
            "a.gender_id = (SELECT g.gender_id FROM gender_lookup g WHERE g.gender_key = UPPER(a.gender)) " +
            "WHERE a.status_id IS NULL")) {
            stmt.execute();
        }

        // Set defaults for NULL values
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE account SET status_id = 1 WHERE status_id IS NULL")) {
            stmt.execute();
        }
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE account SET role_id = 2 WHERE role_id IS NULL")) {
            stmt.execute();
        }

        // Add foreign keys
        try (PreparedStatement stmt = conn.prepareStatement(
            "ALTER TABLE account " +
            "ADD CONSTRAINT fk_account_status FOREIGN KEY (status_id) REFERENCES status_lookup(status_id), " +
            "ADD CONSTRAINT fk_account_role FOREIGN KEY (role_id) REFERENCES role_lookup(role_id), " +
            "ADD CONSTRAINT fk_account_gender FOREIGN KEY (gender_id) REFERENCES gender_lookup(gender_id)")) {
            stmt.execute();
        }

        // Drop old columns
        if (columnExists(conn, "account", "status")) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE account DROP COLUMN status, DROP COLUMN role, DROP COLUMN gender")) {
                stmt.execute();
            }
        }
    }

    private static void migrateBarangayTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "barangay")) return;

        // Add new column if it doesn't exist
        if (!columnExists(conn, "barangay", "status_id")) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE barangay ADD COLUMN status_id INT NULL")) {
                stmt.execute();
            }
        }

        // Migrate data
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE barangay b " +
            "SET b.status_id = (SELECT s.status_id FROM status_lookup s " +
            "JOIN status_domain d ON s.status_domain_id = d.status_domain_id " +
            "WHERE d.domain_key = 'BARANGAY' AND s.status_key = UPPER(b.status)) " +
            "WHERE b.status_id IS NULL")) {
            stmt.execute();
        }

        // Set default for NULL values
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE barangay SET status_id = 3 WHERE status_id IS NULL")) {
            stmt.execute();
        }

        // Add foreign key
        try (PreparedStatement stmt = conn.prepareStatement(
            "ALTER TABLE barangay " +
            "ADD CONSTRAINT fk_barangay_status FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)")) {
            stmt.execute();
        }

        // Drop old column
        if (columnExists(conn, "barangay", "status")) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE barangay DROP COLUMN status")) {
                stmt.execute();
            }
        }
    }

    private static void migratePersonnelTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "personnel")) return;

        // Add new columns if they don't exist
        if (!columnExists(conn, "personnel", "status_id")) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE personnel ADD COLUMN status_id INT NULL, " +
                "ADD COLUMN role_id INT NULL, " +
                "ADD COLUMN gender_id INT NULL")) {
                stmt.execute();
            }
        }

        // Migrate data
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE personnel p " +
            "SET p.status_id = (SELECT s.status_id FROM status_lookup s " +
            "JOIN status_domain d ON s.status_domain_id = d.status_domain_id " +
            "WHERE d.domain_key = 'PERSONNEL' AND s.status_key = UPPER(p.status)), " +
            "p.role_id = (SELECT r.role_id FROM role_lookup r WHERE r.role_key = UPPER(p.role)), " +
            "p.gender_id = (SELECT g.gender_id FROM gender_lookup g WHERE g.gender_key = UPPER(p.gender)) " +
            "WHERE p.status_id IS NULL")) {
            stmt.execute();
        }

        // Set defaults for NULL values
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE personnel SET status_id = 5 WHERE status_id IS NULL")) {
            stmt.execute();
        }

        // Add foreign keys
        try (PreparedStatement stmt = conn.prepareStatement(
            "ALTER TABLE personnel " +
            "ADD CONSTRAINT fk_personnel_status FOREIGN KEY (status_id) REFERENCES status_lookup(status_id), " +
            "ADD CONSTRAINT fk_personnel_role FOREIGN KEY (role_id) REFERENCES role_lookup(role_id), " +
            "ADD CONSTRAINT fk_personnel_gender FOREIGN KEY (gender_id) REFERENCES gender_lookup(gender_id)")) {
            stmt.execute();
        }

        // Drop old columns
        if (columnExists(conn, "personnel", "status")) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE personnel DROP COLUMN status, DROP COLUMN role, DROP COLUMN gender")) {
                stmt.execute();
            }
        }
    }

    private static void migrateTruckTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "truck")) return;

        // Add new columns if they don't exist
        if (!columnExists(conn, "truck", "truck_type_id")) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE truck ADD COLUMN truck_type_id INT NULL, " +
                "ADD COLUMN status_id INT NULL")) {
                stmt.execute();
            }
        }

        // Migrate data
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE truck t " +
            "SET t.truck_type_id = (SELECT ttl.truck_type_id FROM truck_type_lookup ttl " +
            "WHERE ttl.truck_type_key = UPPER(REPLACE(t.truck_type, ' ', '_'))), " +
            "t.status_id = (SELECT s.status_id FROM status_lookup s " +
            "JOIN status_domain d ON s.status_domain_id = d.status_domain_id " +
            "WHERE d.domain_key = 'TRUCK' AND s.status_key = UPPER(REPLACE(t.status, ' ', '_'))) " +
            "WHERE t.truck_type_id IS NULL")) {
            stmt.execute();
        }

        // Set defaults for NULL values
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE truck SET truck_type_id = 1 WHERE truck_type_id IS NULL")) {
            stmt.execute();
        }
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE truck SET status_id = 7 WHERE status_id IS NULL")) {
            stmt.execute();
        }

        // Add foreign keys
        try (PreparedStatement stmt = conn.prepareStatement(
            "ALTER TABLE truck " +
            "ADD CONSTRAINT fk_truck_type FOREIGN KEY (truck_type_id) REFERENCES truck_type_lookup(truck_type_id), " +
            "ADD CONSTRAINT fk_truck_status FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)")) {
            stmt.execute();
        }

        // Drop old columns
        if (columnExists(conn, "truck", "truck_type")) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE truck DROP COLUMN truck_type, DROP COLUMN status")) {
                stmt.execute();
            }
        }
    }

    private static void migrateTeamTable(Connection conn) throws SQLException {
        if (!tableExists(conn, "team")) return;

        // Add new column if it doesn't exist
        if (!columnExists(conn, "team", "status_id")) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE team ADD COLUMN status_id INT NULL")) {
                stmt.execute();
            }
        }

        // Migrate data
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE team t " +
            "SET t.status_id = (SELECT s.status_id FROM status_lookup s " +
            "JOIN status_domain d ON s.status_domain_id = d.status_domain_id " +
            "WHERE d.domain_key = 'TEAM' AND s.status_key = UPPER(t.status)) " +
            "WHERE t.status_id IS NULL")) {
            stmt.execute();
        }

        // Set default for NULL values
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE team SET status_id = 10 WHERE status_id IS NULL")) {
            stmt.execute();
        }

        // Add foreign key
        try (PreparedStatement stmt = conn.prepareStatement(
            "ALTER TABLE team " +
            "ADD CONSTRAINT fk_team_status FOREIGN KEY (status_id) REFERENCES status_lookup(status_id)")) {
            stmt.execute();
        }

        // Drop old column
        if (columnExists(conn, "team", "status")) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "ALTER TABLE team DROP COLUMN status")) {
                stmt.execute();
            }
        }
    }
}
