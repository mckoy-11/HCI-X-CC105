package main.dao;

import main.model.NotificationSummary;
import main.database.DbSchemaHelper;
import main.database.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NotificationDao {

    public NotificationSummary fetchSummary() {
        try (Connection conn = SQLConnection.getConnection()) {

            int barangay = count(conn, "barangay");
            int schedule = count(conn, "schedule");
            int reports = count(conn, "report");

            return new NotificationSummary(barangay, schedule, reports);

        } catch (SQLException e) {
            return new NotificationSummary(0, 0, 0);
        }
    }

    private int count(Connection conn, String table) throws SQLException {
        if (!DbSchemaHelper.tableExists(conn, table)) return 0;

        String sql = "SELECT COUNT(*) FROM " + table;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
