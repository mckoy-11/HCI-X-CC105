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
import main.model.Complaint;

public class ComplaintDao {

    private static final String TABLE_NAME = "complaint";

    public ComplaintDao() {
        ensureSchema();
    }

    public boolean save(Complaint complaint) throws SQLException {
        ensureSchema();
        archiveExpired();
        String sql = "INSERT INTO " + TABLE_NAME
                + " (barangay_id, barangay_name, type, message, proof, status, is_read, is_archived, location, response_message)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, complaint, false);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        complaint.setComplaintId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean update(Complaint complaint) throws SQLException {
        ensureSchema();
        archiveExpired();
        String sql = "UPDATE " + TABLE_NAME
                + " SET barangay_id = ?, barangay_name = ?, type = ?, message = ?, proof = ?, status = ?,"
                + " is_read = ?, is_archived = ?, archived_at = ?, location = ?, response_message = ?"
                + " WHERE complaint_id = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, complaint, true);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE complaint_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Complaint findById(int id) throws SQLException {
        archiveExpired();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE complaint_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Complaint> findAll() throws SQLException {
        return findByArchive(false);
    }

    public List<Complaint> findArchived() throws SQLException {
        return findByArchive(true);
    }

    public List<Complaint> findByStatus(String status) throws SQLException {
        archiveExpired();
        List<Complaint> results = new ArrayList<Complaint>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM " + TABLE_NAME + " WHERE is_archived = 0 AND status = ? ORDER BY complaint_id DESC")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }
        }
        return results;
    }

    public List<Complaint> findUnread() throws SQLException {
        archiveExpired();
        List<Complaint> results = new ArrayList<Complaint>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM " + TABLE_NAME + " WHERE is_archived = 0 AND (is_read = FALSE OR is_read IS NULL) ORDER BY complaint_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(map(rs));
            }
        }
        return results;
    }

    public boolean markAsRead(int id) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE " + TABLE_NAME + " SET is_read = TRUE WHERE complaint_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE " + TABLE_NAME + " SET status = ? WHERE complaint_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int getTotalCount() throws SQLException {
        archiveExpired();
        return count("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE is_archived = 0");
    }

    public int getArchivedCount() throws SQLException {
        archiveExpired();
        return count("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE is_archived = 1");
    }

    public int getCountByStatus(String status) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE is_archived = 0 AND status = ?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int getUnreadCount() throws SQLException {
        archiveExpired();
        return count("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE is_archived = 0 AND (is_read = FALSE OR is_read IS NULL)");
    }

    public void archiveExpired() throws SQLException {
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE " + TABLE_NAME
                             + " SET is_archived = 1, archived_at = CURRENT_TIMESTAMP"
                             + " WHERE is_archived = 0 AND created_at <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)")) {
            ps.executeUpdate();
        }
    }

    private List<Complaint> findByArchive(boolean archived) throws SQLException {
        archiveExpired();
        List<Complaint> results = new ArrayList<Complaint>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM " + TABLE_NAME + " WHERE is_archived = ? ORDER BY created_at DESC")) {
            ps.setBoolean(1, archived);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }
        }
        return results;
    }

    private int count(String sql) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void bind(PreparedStatement ps, Complaint complaint, boolean includeId) throws SQLException {
        ps.setInt(1, complaint.getBarangayId());
        ps.setString(2, complaint.getBarangayName());
        ps.setString(3, complaint.getType());
        ps.setString(4, complaint.getMessage());
        ps.setBytes(5, complaint.getProof());
        ps.setString(6, complaint.getStatus() == null ? "Under Review" : complaint.getStatus());
        ps.setBoolean(7, complaint.isRead());
        if (includeId) {
            ps.setBoolean(8, complaint.isArchived());
            ps.setTimestamp(9, complaint.getArchivedAt());
            ps.setString(10, complaint.getLocation());
            ps.setString(11, complaint.getResponseMessage());
            ps.setInt(12, complaint.getComplaintId());
        } else {
            ps.setString(8, complaint.getLocation());
            ps.setString(9, complaint.getResponseMessage());
        }
    }

    private Complaint map(ResultSet rs) throws SQLException {
        Complaint complaint = new Complaint();
        complaint.setComplaintId(rs.getInt("complaint_id"));
        complaint.setBarangayId(rs.getInt("barangay_id"));
        complaint.setBarangayName(rs.getString("barangay_name"));
        complaint.setType(rs.getString("type"));
        complaint.setMessage(rs.getString("message"));
        complaint.setProof(rs.getBytes("proof"));
        complaint.setStatus(rs.getString("status"));
        complaint.setRead(rs.getBoolean("is_read"));
        complaint.setArchived(rs.getBoolean("is_archived"));
        complaint.setCreatedAt(rs.getTimestamp("created_at"));
        complaint.setArchivedAt(rs.getTimestamp("archived_at"));
        complaint.setLocation(rs.getString("location"));
        complaint.setResponseMessage(rs.getString("response_message"));
        return complaint;
    }

    private void ensureSchema() {
        try (Connection conn = SQLConnection.getConnection()) {
            safeAddColumn(conn, "barangay_id", "INT NULL");
            safeAddColumn(conn, "barangay_name", "VARCHAR(160) NULL");
            safeAddColumn(conn, "type", "VARCHAR(60) NULL");
            safeAddColumn(conn, "response_message", "TEXT NULL");
            safeAddColumn(conn, "is_archived", "TINYINT(1) NOT NULL DEFAULT 0");
            safeAddColumn(conn, "archived_at", "TIMESTAMP NULL");
            safeAddColumn(conn, "location", "VARCHAR(255) NULL");
        } catch (SQLException ignored) {
        }
    }

    private void safeAddColumn(Connection conn, String columnName, String definition) throws SQLException {
        if (!DbSchemaHelper.columnExists(conn, TABLE_NAME, columnName)) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + columnName + " " + definition);
            } catch (SQLException ignored) {
            }
        }
    }
}
