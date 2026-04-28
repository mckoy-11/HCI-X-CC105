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
        String sql = "SELECT * FROM personnel ORDER BY personnel_name ASC";

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
        String sql = "SELECT * FROM personnel WHERE status = 'Unassigned' ORDER BY personnel_name ASC";

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
        String sql = "SELECT * FROM personnel WHERE personnel_id = ?";

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
        String sql = "SELECT * FROM personnel WHERE role = ? ORDER BY personnel_name ASC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);

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
        String sql = "INSERT INTO personnel (personnel_name, age, gender, address, contact_number, team_name, role, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, personnel.getFullName());
            stmt.setInt(2,    personnel.getAge());
            stmt.setString(3, personnel.getGender());
            stmt.setString(4, personnel.getAddress());
            stmt.setString(5, personnel.getPhoneNumber());
            stmt.setString(6, personnel.getTeam());
            stmt.setString(7, personnel.getRole());
            stmt.setString(8, personnel.getStatus());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updatePersonnel(Personnel personnel) {
        String sql = "UPDATE personnel SET personnel_name = ?, age = ?, gender = ?, address = ?, contact_number = ?, team_name = ?, role = ?, status = ? "
                   + "WHERE personnel_id = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, personnel.getFullName());
            stmt.setInt(2,    personnel.getAge());
            stmt.setString(3, personnel.getGender());
            stmt.setString(4, personnel.getAddress());
            stmt.setString(5, personnel.getPhoneNumber());
            stmt.setString(6, personnel.getTeam());
            stmt.setString(7, personnel.getRole());
            stmt.setString(8, personnel.getStatus());
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
        String sql = "UPDATE personnel SET status = ? WHERE personnel_id = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
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
        String sql = "SELECT COUNT(*) AS count FROM personnel WHERE status = 'Active'";

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
        String sql = "SELECT COUNT(*) AS count FROM personnel WHERE status = 'Unassigned'";

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
}
