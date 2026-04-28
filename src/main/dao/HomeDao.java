package main.dao;

import main.database.SQLConnection;
import main.model.HomeCardData;

import java.sql.*;
import java.time.LocalDate;

public class HomeDao {

    public HomeCardData getHomeCardData() {
        HomeCardData data = new HomeCardData();
        
        LocalDate today = LocalDate.now();
        
        // Get today's collection count and barangay
        data.setTodayCollection(getTodayCollectionCount(today));
        data.setTodayBarangay(getTodayCollectionBarangay(today));
        
        // Get completed collection count and barangay
        data.setCompletedCollection(getCompletedCollectionCount());
        data.setCompletedBarangay(getCompletedCollectionBarangay());
        
        // Get missed collection count and barangay
        data.setMissedCollection(getMissedCollectionCount(today));
        data.setMissedBarangay(getMissedCollectionBarangay(today));
        
        // Get unread complaints count
        data.setUnreadComplaints(getUnreadComplaintsCount());
        
        return data;
    }
    
    private int getTodayCollectionCount(LocalDate today) {
        String sql = "SELECT COUNT(*) AS count FROM schedule WHERE schedule_date = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(today));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private String getTodayCollectionBarangay(LocalDate today) {
        String sql = "SELECT b.barangay_name FROM schedule s " +
                     "JOIN barangay b ON s.barangay_id = b.barangay_id " +
                     "WHERE s.schedule_date = ? LIMIT 1";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(today));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("barangay_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Brgy";
    }
    
    private int getCompletedCollectionCount() {
        String sql = "SELECT COUNT(*) AS count FROM schedule WHERE status = 'Completed'";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private String getCompletedCollectionBarangay() {
        String sql = "SELECT b.barangay_name FROM schedule s " +
                     "JOIN barangay b ON s.barangay_id = b.barangay_id " +
                     "WHERE s.status = 'Completed' LIMIT 1";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("barangay_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Brgy";
    }
    
    private int getMissedCollectionCount(LocalDate today) {
        String sql = "SELECT COUNT(*) AS count FROM schedule " +
                     "WHERE schedule_date < ? AND (status != 'Completed' OR status IS NULL)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(today));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private String getMissedCollectionBarangay(LocalDate today) {
        String sql = "SELECT b.barangay_name FROM schedule s " +
                     "JOIN barangay b ON s.barangay_id = b.barangay_id " +
                     "WHERE s.schedule_date < ? AND (s.status != 'Completed' OR s.status IS NULL) LIMIT 1";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(today));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("barangay_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Brgy";
    }
    
    private int getUnreadComplaintsCount() {
        // Check if complaints table exists, if not return 0
        String checkTable = "SELECT COUNT(*) FROM information_schema.tables " +
                           "WHERE table_schema = 'wcms' AND table_name = 'complaint'";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkTable);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                return 0;
            }
        } catch (SQLException e) {
            return 0;
        }
        
        String sql = "SELECT COUNT(*) AS count FROM complaint WHERE is_read = 0 OR is_read IS NULL";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}