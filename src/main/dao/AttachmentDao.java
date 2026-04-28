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
import main.model.EntryAttachment;

public class AttachmentDao {

    private static final String TABLE_NAME = "entry_attachment";

    public AttachmentDao() {
        ensureSchema();
    }

    public void replaceForEntry(String entryType, int entryId, List<byte[]> images) throws SQLException {
        ensureSchema();
        try (Connection conn = SQLConnection.getConnection()) {
            try (PreparedStatement delete = conn.prepareStatement(
                    "DELETE FROM " + TABLE_NAME + " WHERE entry_type = ? AND entry_id = ?")) {
                delete.setString(1, entryType);
                delete.setInt(2, entryId);
                delete.executeUpdate();
            }

            if (images == null || images.isEmpty()) {
                return;
            }

            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO " + TABLE_NAME + " (entry_type, entry_id, image_blob) VALUES (?, ?, ?)")) {
                for (byte[] image : images) {
                    if (image == null || image.length == 0) {
                        continue;
                    }
                    insert.setString(1, entryType);
                    insert.setInt(2, entryId);
                    insert.setBytes(3, image);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
        }
    }

    public List<EntryAttachment> findByEntry(String entryType, int entryId) throws SQLException {
        ensureSchema();
        List<EntryAttachment> results = new ArrayList<EntryAttachment>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT attachment_id, entry_type, entry_id, image_blob FROM " + TABLE_NAME
                             + " WHERE entry_type = ? AND entry_id = ? ORDER BY attachment_id ASC")) {
            ps.setString(1, entryType);
            ps.setInt(2, entryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EntryAttachment attachment = new EntryAttachment();
                    attachment.setAttachmentId(rs.getInt("attachment_id"));
                    attachment.setEntryType(rs.getString("entry_type"));
                    attachment.setEntryId(rs.getInt("entry_id"));
                    attachment.setContent(rs.getBytes("image_blob"));
                    results.add(attachment);
                }
            }
        }
        return results;
    }

    private void ensureSchema() {
        try (Connection conn = SQLConnection.getConnection()) {
            if (!DbSchemaHelper.tableExists(conn, TABLE_NAME)) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate(
                            "CREATE TABLE " + TABLE_NAME + " ("
                                    + "attachment_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                                    + "entry_type VARCHAR(30) NOT NULL, "
                                    + "entry_id INT NOT NULL, "
                                    + "image_blob LONGBLOB NULL, "
                                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                    );
                }
            }
        } catch (SQLException ignored) {
        }
    }
}
