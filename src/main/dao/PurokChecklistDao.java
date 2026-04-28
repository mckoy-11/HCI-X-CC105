package main.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import main.database.DbSchemaHelper;
import main.database.SQLConnection;
import main.model.PurokChecklistItem;

public class PurokChecklistDao {

    private static final String TABLE_NAME = "purok_checklist";

    public PurokChecklistDao() {
        ensureSchema();
    }

    public List<PurokChecklistItem> findByBarangay(int barangayId, String barangayName) throws SQLException {
        ensureSchema();
        seedDefaults(barangayId, barangayName);
        List<PurokChecklistItem> results = new ArrayList<PurokChecklistItem>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT checklist_id, barangay_id, purok_name, is_collected, updated_at"
                             + " FROM " + TABLE_NAME + " WHERE barangay_id = ? ORDER BY checklist_id ASC")) {
            ps.setInt(1, barangayId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurokChecklistItem item = new PurokChecklistItem();
                    item.setChecklistId(rs.getInt("checklist_id"));
                    item.setBarangayId(rs.getInt("barangay_id"));
                    item.setBarangayName(barangayName);
                    item.setPurokName(rs.getString("purok_name"));
                    item.setCollected(rs.getBoolean("is_collected"));
                    item.setUpdatedAt(rs.getTimestamp("updated_at"));
                    results.add(item);
                }
            }
        }
        return results;
    }

    public boolean updateCollected(int checklistId, boolean collected) throws SQLException {
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE " + TABLE_NAME + " SET is_collected = ?, updated_at = CURRENT_TIMESTAMP WHERE checklist_id = ?")) {
            ps.setBoolean(1, collected);
            ps.setInt(2, checklistId);
            return ps.executeUpdate() > 0;
        }
    }

    private void seedDefaults(int barangayId, String barangayName) throws SQLException {
        if (barangayId <= 0) {
            return;
        }

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement countPs = conn.prepareStatement(
                     "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE barangay_id = ?")) {
            countPs.setInt(1, barangayId);
            try (ResultSet rs = countPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }

            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO " + TABLE_NAME + " (barangay_id, purok_name, is_collected) VALUES (?, ?, 0)")) {
                for (int i = 1; i <= 7; i++) {
                    insert.setInt(1, barangayId);
                    insert.setString(2, "Purok " + i);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
        }
    }

    private void ensureSchema() {
        try (Connection conn = SQLConnection.getConnection()) {
            if (!DbSchemaHelper.tableExists(conn, TABLE_NAME)) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate(
                            "CREATE TABLE " + TABLE_NAME + " ("
                                    + "checklist_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                                    + "barangay_id INT NOT NULL, "
                                    + "purok_name VARCHAR(100) NOT NULL, "
                                    + "is_collected TINYINT(1) NOT NULL DEFAULT 0, "
                                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)"
                    );
                }
            }
        } catch (SQLException ignored) {
        }
    }
}
