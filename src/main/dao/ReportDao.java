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
import main.model.Report;

public class ReportDao {

    private static final String TABLE_NAME = "report";

    public ReportDao() {
        ensureSchema();
    }

    public boolean save(Report report) throws SQLException {
        ensureSchema();
        archiveExpired();
        String sql = "INSERT INTO " + TABLE_NAME
                + " (message, proof, status, is_read, barangay_id, type, response_message, purok_analytics, is_archived)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, report, false);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        report.setReportId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean update(Report report) throws SQLException {
        ensureSchema();
        archiveExpired();
        String sql = "UPDATE " + TABLE_NAME
                + " SET message = ?, proof = ?, status = ?, is_read = ?, barangay_id = ?,"
                + " type = ?, response_message = ?, purok_analytics = ?, is_archived = ?, archived_at = ?"
                + " WHERE report_id = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, report, true);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE report_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Report findById(int id) throws SQLException {
        ensureSchema();
        archiveExpired();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(baseQuery() + " WHERE r.report_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Report> findAll() throws SQLException {
        return findByArchive(false);
    }

    public List<Report> findArchived() throws SQLException {
        return findByArchive(true);
    }

    public List<Report> findByStatus(String status) throws SQLException {
        ensureSchema();
        archiveExpired();
        List<Report> results = new ArrayList<Report>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(baseQuery() + " WHERE r.is_archived = 0 AND r.status = ? ORDER BY r.report_id DESC")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }
        }
        return results;
    }

    public List<Report> findUnread() throws SQLException {
        ensureSchema();
        archiveExpired();
        List<Report> results = new ArrayList<Report>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(baseQuery() + " WHERE r.is_archived = 0 AND (r.is_read = FALSE OR r.is_read IS NULL) ORDER BY r.report_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(map(rs));
            }
        }
        return results;
    }

    public boolean markAsRead(int id) throws SQLException {
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE " + TABLE_NAME + " SET is_read = TRUE WHERE report_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE " + TABLE_NAME + " SET status = ? WHERE report_id = ?")) {
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
        ensureSchema();
        archiveExpired();
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

    private List<Report> findByArchive(boolean archived) throws SQLException {
        ensureSchema();
        archiveExpired();
        List<Report> results = new ArrayList<Report>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(baseQuery()
                     + " WHERE r.is_archived = ? ORDER BY CASE WHEN r.archived_at IS NULL THEN 0 ELSE 1 END DESC, r.created_at DESC")) {
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
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String baseQuery() {
        return "SELECT r.*, b.barangay_name FROM " + TABLE_NAME + " r"
                + " LEFT JOIN barangay b ON r.barangay_id = b.barangay_id";
    }

    private void bind(PreparedStatement ps, Report report, boolean includeId) throws SQLException {
        ps.setString(1, report.getMessage());
        ps.setBytes(2, report.getProof());
        ps.setString(3, report.getStatus() == null ? "Under Review" : report.getStatus());
        ps.setBoolean(4, report.isRead());
        ps.setInt(5, report.getBarangayId());
        ps.setString(6, report.getType() == null ? "Report" : report.getType());
        ps.setString(7, report.getResponseMessage());
        ps.setString(8, report.getPurokAnalytics());
        if (includeId) {
            ps.setBoolean(9, report.isArchived());
            ps.setTimestamp(10, report.getArchivedAt());
            ps.setInt(11, report.getReportId());
        }
    }

    private Report map(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setReportId(rs.getInt("report_id"));
        report.setMessage(rs.getString("message"));
        report.setProof(rs.getBytes("proof"));
        report.setStatus(rs.getString("status"));
        report.setRead(rs.getBoolean("is_read"));
        report.setCreatedAt(rs.getTimestamp("created_at"));
        report.setArchived(rs.getBoolean("is_archived"));
        report.setArchivedAt(rs.getTimestamp("archived_at"));
        report.setBarangayId(rs.getInt("barangay_id"));
        report.setBarangayName(rs.getString("barangay_name"));
        report.setType(rs.getString("type"));
        report.setResponseMessage(rs.getString("response_message"));
        report.setPurokAnalytics(rs.getString("purok_analytics"));
        return report;
    }

    private void ensureSchema() {
        try (Connection conn = SQLConnection.getConnection()) {
            safeAddColumn(conn, "type", "VARCHAR(60) NULL");
            safeAddColumn(conn, "response_message", "TEXT NULL");
            safeAddColumn(conn, "purok_analytics", "TEXT NULL");
            safeAddColumn(conn, "is_archived", "TINYINT(1) NOT NULL DEFAULT 0");
            safeAddColumn(conn, "archived_at", "TIMESTAMP NULL");
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
