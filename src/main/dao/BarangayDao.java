package main.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import main.database.SQLConnection;
import main.model.Barangay;

public class BarangayDao {

    public boolean save(Barangay barangay) throws SQLException {
        String sql = "INSERT INTO barangay(barangay_name, barangay_household, contact, collection_day, status)"
                + " VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindBarangay(ps, barangay, false);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        barangay.setBarangayId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean update(Barangay barangay) throws SQLException {
        String sql = "UPDATE barangay SET barangay_name = ?, barangay_household = ?, contact = ?,"
                + " collection_day = ?, status = ? WHERE barangay_id = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindBarangay(ps, barangay, true);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM barangay WHERE barangay_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Barangay findById(int id) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM barangay WHERE barangay_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Barangay> findAll() throws SQLException {
        List<Barangay> results = new ArrayList<Barangay>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM barangay ORDER BY barangay_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(map(rs));
            }
        }
        return results;
    }

    public Barangay findByName(String name) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM barangay WHERE UPPER(TRIM(barangay_name)) = UPPER(TRIM(?))")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Barangay> findByCollectionDay(String day) throws SQLException {
        List<Barangay> results = new ArrayList<Barangay>();
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM barangay WHERE collection_day = ? ORDER BY barangay_name")) {
            ps.setString(1, day);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }
        }
        return results;
    }

    public int getTotalSchedBarangay() throws SQLException {
        return count("SELECT COUNT(*) FROM barangay WHERE UPPER(COALESCE(status, '')) = 'SCHEDULED'");
    }

    public int getTotalCount() throws SQLException {
        return count("SELECT COUNT(*) FROM barangay");
    }

    public int getTotalHousehold() throws SQLException {
        return count("SELECT COALESCE(SUM(barangay_household), 0) FROM barangay");
    }

    private int count(String sql) throws SQLException {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void bindBarangay(PreparedStatement ps, Barangay barangay, boolean includeId) throws SQLException {
        ps.setString(1, barangay.getBarangayName());
        ps.setInt(2, barangay.getBarangayHousehold());
        ps.setString(3, barangay.getContact());
        ps.setString(4, barangay.getCollectionDay());
        ps.setString(5, barangay.getStatus() == null || barangay.getStatus().trim().isEmpty()
                ? "Active"
                : barangay.getStatus());
        if (includeId) {
            ps.setInt(6, barangay.getBarangayId());
        }
    }

    private Barangay map(ResultSet rs) throws SQLException {
        Barangay barangay = new Barangay();
        barangay.setBarangayId(rs.getInt("barangay_id"));
        barangay.setBarangayName(rs.getString("barangay_name"));
        barangay.setBarangayHousehold(rs.getInt("barangay_household"));
        barangay.setContact(rs.getString("contact"));
        barangay.setCollectionDay(rs.getString("collection_day"));
        barangay.setStatus(rs.getString("status"));
        return barangay;
    }
}
