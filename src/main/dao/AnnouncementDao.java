package main.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import main.database.DbSchemaHelper;
import main.database.SQLConnection;
import main.model.Announcement;

public class AnnouncementDao {

    private static final String TABLE_NAME = "announcement";

    public AnnouncementDao() {
        ensureSchema();
    }

    public Announcement findActive() throws SQLException {
        archiveExpired();
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM " + TABLE_NAME
                             + " WHERE is_active = 1 AND is_archived = 0"
                             + " ORDER BY created_at DESC LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? map(rs) : null;
        }
    }

    public List<Announcement> findArchived() throws SQLException {
        archiveExpired();
        ensureSchema();
        List<Announcement> results = new ArrayList<Announcement>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM " + TABLE_NAME + " WHERE is_archived = 1 ORDER BY archived_at DESC, created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(map(rs));
            }
        }
        return results;
    }

    public boolean saveAndActivate(Announcement announcement) throws SQLException {
        archiveActive();
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO " + TABLE_NAME
                             + " (title, message, is_active, is_archived, created_at, expires_at)"
                             + " VALUES (?, ?, 1, 0, CURRENT_TIMESTAMP, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY))")) {
            ps.setString(1, announcement.getTitle());
            ps.setString(2, announcement.getMessage());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean dismiss(int announcementId) throws SQLException {
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE " + TABLE_NAME
                             + " SET is_active = 0, is_archived = 1, archived_at = CURRENT_TIMESTAMP"
                             + " WHERE announcement_id = ?")) {
            ps.setInt(1, announcementId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean addAnnouncement(Announcement announcement) throws SQLException {
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO " + TABLE_NAME
                             + " (title, message, is_active, is_archived, created_at, expires_at)"
                             + " VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, announcement.getTitle());
            ps.setString(2, announcement.getMessage());
            ps.setBoolean(3, announcement.isActive());
            ps.setBoolean(4, announcement.isArchived());
            ps.setTimestamp(5, announcement.getExpiresAt());
            
            boolean result = ps.executeUpdate() > 0;
            if (result) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    announcement.setAnnouncementId(rs.getInt(1));
                }
            }
            return result;
        }
    }

    public boolean updateAnnouncement(Announcement announcement) throws SQLException {
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE " + TABLE_NAME
                             + " SET title = ?, message = ?, is_active = ?, is_archived = ?, expires_at = ?"
                             + " WHERE announcement_id = ?")) {
            ps.setString(1, announcement.getTitle());
            ps.setString(2, announcement.getMessage());
            ps.setBoolean(3, announcement.isActive());
            ps.setBoolean(4, announcement.isArchived());
            ps.setTimestamp(5, announcement.getExpiresAt());
            ps.setInt(6, announcement.getAnnouncementId());
            
            return ps.executeUpdate() > 0;
        }
    }

    public void archiveExpired() throws SQLException {
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE " + TABLE_NAME
                             + " SET is_active = 0, is_archived = 1, archived_at = CURRENT_TIMESTAMP"
                             + " WHERE is_active = 1 AND is_archived = 0 AND expires_at IS NOT NULL AND expires_at <= CURRENT_TIMESTAMP")) {
            ps.executeUpdate();
        }
    }

    private void archiveActive() throws SQLException {
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE " + TABLE_NAME
                             + " SET is_active = 0, is_archived = 1, archived_at = CURRENT_TIMESTAMP"
                             + " WHERE is_active = 1 AND is_archived = 0")) {
            ps.executeUpdate();
        }
    }

    private Announcement map(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setAnnouncementId(rs.getInt("announcement_id"));
        announcement.setTitle(rs.getString("title"));
        announcement.setMessage(rs.getString("message"));
        announcement.setActive(rs.getBoolean("is_active"));
        announcement.setArchived(rs.getBoolean("is_archived"));
        announcement.setCreatedAt(rs.getTimestamp("created_at"));
        announcement.setArchivedAt(rs.getTimestamp("archived_at"));
        announcement.setExpiresAt(rs.getTimestamp("expires_at"));
        return announcement;
    }

    private void ensureSchema() {
        try (Connection conn = SQLConnection.getConnection()) {
            if (!DbSchemaHelper.tableExists(conn, TABLE_NAME)) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate(
                            "CREATE TABLE " + TABLE_NAME + " ("
                                    + "announcement_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                                    + "title VARCHAR(255) NULL, "
                                    + "message TEXT NOT NULL, "
                                    + "is_active TINYINT(1) NOT NULL DEFAULT 1, "
                                    + "is_archived TINYINT(1) NOT NULL DEFAULT 0, "
                                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                                    + "archived_at TIMESTAMP NULL, "
                                    + "expires_at TIMESTAMP NULL)"
                    );
                }
            }
        } catch (SQLException ignored) {
        }
    }
}
