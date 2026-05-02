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
import main.model.Request;

public class RequestDao {

    private static final String TABLE_NAME = "request";

    public RequestDao() {
        ensureSchema();
    }

    public boolean save(Request request) throws SQLException {
        ensureSchema();
        archiveExpired();
        String sql = "INSERT INTO " + TABLE_NAME
                + " (barangay_id, request_type_id, message, proof, status_id, is_read, is_archived, location, response_message)"
                + " VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, request, false);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        request.setRequestId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean update(Request request) throws SQLException {
        ensureSchema();
        archiveExpired();
        String sql = "UPDATE " + TABLE_NAME
                + " SET barangay_id = ?, request_type_id = ?, message = ?, proof = ?, status_id = ?,"
                + " is_read = ?, is_archived = ?, archived_at = ?, location = ?, response_message = ?"
                + " WHERE request_id = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, request, true);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE request_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Request findById(int id) throws SQLException {
        archiveExpired();
        String sql = "SELECT r.*, rt.request_type_name as type, s.status_name as status, b.barangay_name " +
                     "FROM " + TABLE_NAME + " r " +
                     "LEFT JOIN request_type rt ON r.request_type_id = rt.request_type_id " +
                     "LEFT JOIN status_lookup s ON r.status_id = s.status_id " +
                     "LEFT JOIN barangay b ON r.barangay_id = b.barangay_id " +
                     "WHERE r.request_id = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Request> findAll() throws SQLException {
        return findByArchive(false);
    }

    public List<Request> findArchived() throws SQLException {
        return findByArchive(true);
    }

    public List<Request> findByStatus(String status) throws SQLException {
        archiveExpired();
        List<Request> results = new ArrayList<Request>();
        String sql = "SELECT r.*, rt.request_type_name as type, s.status_name as status, b.barangay_name " +
                     "FROM " + TABLE_NAME + " r " +
                     "LEFT JOIN request_type rt ON r.request_type_id = rt.request_type_id " +
                     "LEFT JOIN status_lookup s ON r.status_id = s.status_id " +
                     "LEFT JOIN barangay b ON r.barangay_id = b.barangay_id " +
                     "WHERE r.is_archived = 0 AND s.status_name = ? ORDER BY r.request_id DESC";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }
        }
        return results;
    }

    public List<Request> findUnread() throws SQLException {
        archiveExpired();
        List<Request> results = new ArrayList<Request>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM " + TABLE_NAME + " WHERE is_archived = 0 AND (is_read = FALSE OR is_read IS NULL) ORDER BY request_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(map(rs));
            }
        }
        return results;
    }

    public boolean markAsRead(int id) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE " + TABLE_NAME + " SET is_read = TRUE WHERE request_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE " + TABLE_NAME + " SET status_id = ? WHERE request_id = ?")) {
            ps.setInt(1, getStatusId(status));
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
                     "SELECT COUNT(*) FROM " + TABLE_NAME + " r " +
                     "LEFT JOIN status_lookup s ON r.status_id = s.status_id " +
                     "WHERE r.is_archived = 0 AND s.status_name = ?")) {
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

    private List<Request> findByArchive(boolean archived) throws SQLException {
        archiveExpired();
        List<Request> results = new ArrayList<Request>();
        String sql = "SELECT r.*, rt.request_type_name as type, s.status_name as status, b.barangay_name " +
                     "FROM " + TABLE_NAME + " r " +
                     "LEFT JOIN request_type rt ON r.request_type_id = rt.request_type_id " +
                     "LEFT JOIN status_lookup s ON r.status_id = s.status_id " +
                     "LEFT JOIN barangay b ON r.barangay_id = b.barangay_id " +
                     "WHERE r.is_archived = ? ORDER BY r.created_at DESC";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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

    private void bind(PreparedStatement ps, Request request, boolean includeId) throws SQLException {
        ps.setInt(1, request.getBarangayId());
        ps.setInt(2, getRequestTypeId(request.getType()));
        ps.setString(3, request.getMessage());
        ps.setBytes(4, request.getProof());
        ps.setInt(5, getStatusId(request.getStatus() != null ? request.getStatus() : "Under Review"));
        ps.setBoolean(6, request.isRead());
        if (includeId) {
            ps.setBoolean(7, request.isArchived());
            ps.setTimestamp(8, request.getArchivedAt());
            ps.setString(9, request.getLocation());
            ps.setString(10, request.getResponseMessage());
            ps.setInt(11, request.getRequestId());
        } else {
            ps.setString(7, request.getLocation());
            ps.setString(8, request.getResponseMessage());
        }
    }

    private Request map(ResultSet rs) throws SQLException {
        Request request = new Request();
        request.setRequestId(rs.getInt("request_id"));
        request.setBarangayId(rs.getInt("barangay_id"));
        request.setBarangayName(rs.getString("barangay_name"));
        request.setType(rs.getString("type"));
        request.setMessage(rs.getString("message"));
        request.setProof(rs.getBytes("proof"));
        request.setStatus(rs.getString("status"));
        request.setRead(rs.getBoolean("is_read"));
        request.setArchived(rs.getBoolean("is_archived"));
        request.setCreatedAt(rs.getTimestamp("created_at"));
        request.setArchivedAt(rs.getTimestamp("archived_at"));
        request.setLocation(rs.getString("location"));
        request.setResponseMessage(rs.getString("response_message"));
        return request;
    }

    private void ensureSchema() {
        try (Connection conn = SQLConnection.getConnection()) {
            safeAddColumn(conn, "barangay_id", "INT NULL");
            safeAddColumn(conn, "request_type_id", "INT NULL");
            safeAddColumn(conn, "message", "TEXT NULL");
            safeAddColumn(conn, "proof", "LONGBLOB NULL");
            safeAddColumn(conn, "status_id", "INT NULL");
            safeAddColumn(conn, "is_read", "TINYINT(1) NOT NULL DEFAULT 0");
            safeAddColumn(conn, "is_archived", "TINYINT(1) NOT NULL DEFAULT 0");
            safeAddColumn(conn, "archived_at", "TIMESTAMP NULL");
            safeAddColumn(conn, "location", "VARCHAR(255) NULL");
            safeAddColumn(conn, "response_message", "TEXT NULL");
            safeAddColumn(conn, "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
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

    private Integer getStatusId(String statusName) throws SQLException {
        if (statusName == null || statusName.trim().isEmpty()) return 7; // Default to "Under Review"
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT status_id FROM status_lookup WHERE status_name = ?")) {
            ps.setString(1, statusName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("status_id") : 7;
            }
        }
    }

    private Integer getRequestTypeId(String typeName) throws SQLException {
        if (typeName == null || typeName.trim().isEmpty()) return 1; // Default to first type
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT request_type_id FROM request_type WHERE request_type_name = ?")) {
            ps.setString(1, typeName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("request_type_id") : 1;
            }
        }
    }
}
