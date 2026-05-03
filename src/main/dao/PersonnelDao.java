package main.dao;

import main.database.SQLConnection;
import main.model.Personnel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Personnel entity handling CRUD operations.
 * Fields excluded: username, email, barangay_assigned
 */
public class PersonnelDao {

    public List<Personnel> getAllPersonnel() {
        List<Personnel> personnelList = new ArrayList<>();
        String sql = "SELECT p.*, gl.gender_label AS gender, rl.role_label AS role, sl.status_label AS status " +
                     "FROM personnel p " +
                     "LEFT JOIN gender_lookup gl ON p.gender_id = gl.gender_id " +
                     "LEFT JOIN role_lookup rl ON p.role_id = rl.role_id " +
                     "LEFT JOIN status_lookup sl ON p.status_id = sl.status_id " +
                     "ORDER BY p.personnel_name ASC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                personnelList.add(mapResultSetToPersonnel(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return personnelList;
    }

    public List<Personnel> getAllUnassignedPersonnel() {
        List<Personnel> personnelList = new ArrayList<>();

        String sql = "SELECT p.*, gl.gender_label AS gender, rl.role_label AS role, sl.status_label AS status " +
                     "FROM personnel p " +
                     "LEFT JOIN gender_lookup gl ON p.gender_id = gl.gender_id " +
                     "LEFT JOIN role_lookup rl ON p.role_id = rl.role_id " +
                     "LEFT JOIN status_lookup sl ON p.status_id = sl.status_id " +
                     "WHERE (p.team_name IS NULL OR TRIM(p.team_name) = '') " +
                     "ORDER BY p.personnel_name ASC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                personnelList.add(mapResultSetToPersonnel(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return personnelList;
    }

    public Personnel getPersonnelById(int id) {
        String sql = "SELECT p.*, gl.gender_label AS gender, rl.role_label AS role, sl.status_label AS status " +
                     "FROM personnel p " +
                     "LEFT JOIN gender_lookup gl ON p.gender_id = gl.gender_id " +
                     "LEFT JOIN role_lookup rl ON p.role_id = rl.role_id " +
                     "LEFT JOIN status_lookup sl ON p.status_id = sl.status_id " +
                     "WHERE p.personnel_id = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPersonnel(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Personnel> getPersonnelByRole(String role) {
        List<Personnel> personnelList = new ArrayList<>();
        String sql = "SELECT p.*, gl.gender_label AS gender, rl.role_label AS role, sl.status_label AS status " +
                     "FROM personnel p " +
                     "LEFT JOIN gender_lookup gl ON p.gender_id = gl.gender_id " +
                     "LEFT JOIN role_lookup rl ON p.role_id = rl.role_id " +
                     "LEFT JOIN status_lookup sl ON p.status_id = sl.status_id " +
                     "WHERE rl.role_label = ? OR rl.role_key = UPPER(?) " +
                     "ORDER BY p.personnel_name ASC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            stmt.setString(2, role);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    personnelList.add(mapResultSetToPersonnel(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return personnelList;
    }

    public boolean addPersonnel(Personnel personnel) {
        String sql = "INSERT INTO personnel (personnel_name, age, gender_id, address, contact_number, team_id, role_id, status_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, personnel.getFullName());
            stmt.setInt(2,    personnel.getAge());
            stmt.setObject(3, getGenderId(personnel.getGender()));
            stmt.setString(4, personnel.getAddress());
            stmt.setString(5, personnel.getPhoneNumber());
            stmt.setObject(6, null); // team_id - will be set later
            stmt.setInt(7, getRoleId(personnel.getRole()));
            stmt.setInt(8, getStatusId(personnel.getStatus()));

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updatePersonnel(Personnel personnel) {
        String sql = "UPDATE personnel SET personnel_name = ?, age = ?, gender_id = ?, address = ?, contact_number = ?, team_id = ?, role_id = ?, status_id = ? "
                   + "WHERE personnel_id = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, personnel.getFullName());
            stmt.setInt(2,    personnel.getAge());
            stmt.setObject(3, getGenderId(personnel.getGender()));
            stmt.setString(4, personnel.getAddress());
            stmt.setString(5, personnel.getPhoneNumber());
            stmt.setObject(6, null); // team_id - will be set later
            stmt.setInt(7, getRoleId(personnel.getRole()));
            stmt.setInt(8, getStatusId(personnel.getStatus()));
            stmt.setInt(9, personnel.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deletePersonnel(int id) {
        String sql = "DELETE FROM personnel WHERE personnel_id = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE personnel SET status_id = ? WHERE personnel_id = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, getStatusId(status));
            stmt.setInt(2, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int getTotalPersonnelCount() {
        String sql = "SELECT COUNT(*) AS count FROM personnel";

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

    public int getActivePersonnelCount() {
        String sql = "SELECT COUNT(*) AS count FROM personnel p " +
                     "JOIN status_lookup sl ON p.status_id = sl.status_id " +
                     "WHERE sl.status_key = 'ACTIVE'";

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

    public int getUnassignedPersonnelCount() {
        String sql = "SELECT COUNT(*) AS count FROM personnel p " +
                     "JOIN status_lookup sl ON p.status_id = sl.status_id " +
                     "WHERE sl.status_key = 'UNASSIGNED'";

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

    private Personnel mapResultSetToPersonnel(ResultSet rs) throws SQLException {
        Personnel personnel = new Personnel();
        personnel.setId(getInt(rs, "personnel_id"));
        personnel.setFullName(getString(rs, "personnel_name"));
        personnel.setAge(getInt(rs, "age"));
        personnel.setGender(getString(rs, "gender"));
        personnel.setAddress(getString(rs, "address"));
        personnel.setPhoneNumber(getString(rs, "contact_number"));
        personnel.setTeam(getString(rs,"team_name"));
        personnel.setRole(getString(rs, "role"));
        personnel.setStatus(getString(rs, "status"));
        return personnel;
    }

    private String getString(ResultSet rs, String... candidates) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        for (String candidate : candidates) {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (candidate.equalsIgnoreCase(meta.getColumnLabel(i))
                        || candidate.equalsIgnoreCase(meta.getColumnName(i))) {
                    return rs.getString(i);
                }
            }
        }
        return null;
    }

    private int getInt(ResultSet rs, String... candidates) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        for (String candidate : candidates) {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (candidate.equalsIgnoreCase(meta.getColumnLabel(i))
                        || candidate.equalsIgnoreCase(meta.getColumnName(i))) {
                    return rs.getInt(i);
                }
            }
        }
        return 0;
    }

    private Integer getStatusId(String statusName) throws SQLException {
        if (statusName == null || statusName.trim().isEmpty()) return 32; // Default to UNASSIGNED for personnel
        String sql = "SELECT status_id FROM status_lookup WHERE status_key = ? AND status_domain_id = 10"; // Personnel domain
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statusName.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("status_id") : 32; // Default to UNASSIGNED
            }
        }
    }

    private Integer getRoleId(String roleName) throws SQLException {
        if (roleName == null || roleName.trim().isEmpty()) return 3; // Default to PERSONNEL
        String sql = "SELECT role_id FROM role_lookup WHERE role_key = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("role_id") : 3; // Default to PERSONNEL
            }
        }
    }

    private Integer getGenderId(String genderName) throws SQLException {
        if (genderName == null || genderName.trim().isEmpty()) return null;
        String sql = "SELECT gender_id FROM gender_lookup WHERE gender_key = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, genderName.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("gender_id") : null;
            }
        }
    }
}
