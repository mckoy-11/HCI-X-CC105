package main.dao;

import main.database.DbSchemaHelper;
import main.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AccountDao {

    private final Connection conn;

    public AccountDao(Connection conn) {
        this.conn = conn;
    }

    public Account findByEmail(String email) throws SQLException {
        String sql = buildAccountSelectSql("WHERE a.email_address = ?");
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();
        return rs.next() ? mapResultSet(rs) : null;
    }

    public Account findById(int id) throws SQLException {
        String sql = buildAccountSelectSql("WHERE a.account_id = ?");
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        return rs.next() ? mapResultSet(rs) : null;
    }

    public boolean save(Account a) throws SQLException {
        // Ensure the is_barangay_setup_complete column exists
        ensureBarangaySetupColumn();
        
        String sql = "INSERT INTO account(name, email_address, password, status_id, role_id, is_barangay_setup_complete) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, a.getName());
        ps.setString(2, a.getEmail());
        ps.setString(3, a.getPassword());
        ps.setInt(4, getStatusId(a.getStatus() != null ? a.getStatus() : "Active"));
        ps.setInt(5, getRoleId(a.getRole() != null ? a.getRole() : "BARANGAY"));
        ps.setBoolean(6, a.isBarangaySetupComplete());

        int rows = ps.executeUpdate();

        if (rows > 0) {
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                a.setAccountId(keys.getInt(1));
            }
            syncBarangayAdminDetails(a);
            syncBarangayAssignment(a);
            return true;
        }
        return false;
    }

    public boolean update(Account a) throws SQLException {
        String sql = "UPDATE account SET name=?, email_address=?, password=?, status_id=?, role_id=? WHERE account_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, a.getName());
        ps.setString(2, a.getEmail());
        ps.setString(3, a.getPassword());
        ps.setInt(4, getStatusId(a.getStatus()));
        ps.setInt(5, getRoleId(a.getRole()));
        ps.setInt(6, a.getAccountId());

        boolean updated = ps.executeUpdate() > 0;
        if (updated) {
            syncBarangayAdminDetails(a);
            syncBarangayAssignment(a);
        }
        return updated;
    }

    public boolean updateLastLogin(int accountId) throws SQLException {
        String sql = "UPDATE account SET last_login = NOW() WHERE account_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, accountId);
        return ps.executeUpdate() > 0;
    }

    public boolean updateStatus(int accountId, String status) throws SQLException {
        String sql = "UPDATE account SET status_id = ? WHERE account_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, getStatusId(status));
        ps.setInt(2, accountId);
        return ps.executeUpdate() > 0;
    }

    public boolean delete(int accountId) throws SQLException {
        if (DbSchemaHelper.tableExists(conn, "barangay_admin")
                && DbSchemaHelper.columnExists(conn, "barangay_admin", "account_id")) {
            try (PreparedStatement deleteBarangayAdmin = conn.prepareStatement(
                    "DELETE FROM barangay_admin WHERE account_id = ?")) {
                deleteBarangayAdmin.setInt(1, accountId);
                deleteBarangayAdmin.executeUpdate();
            }
        }

        try (PreparedStatement deleteAccount = conn.prepareStatement(
                "DELETE FROM account WHERE account_id = ?")) {
            deleteAccount.setInt(1, accountId);
            return deleteAccount.executeUpdate() > 0;
        }
    }

    public List<Account> findAll() throws SQLException {
        String sql = buildAccountSelectSql("ORDER BY a.account_id ASC");
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Account> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }

        return list;
    }

    public List<Account> findAllByRole(String role) throws SQLException {
        String sql = buildAccountSelectSql(
                "WHERE r.role_key = ? ORDER BY a.account_id ASC"
        );

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, role);

        ResultSet rs = ps.executeQuery();

        List<Account> list = new ArrayList<>();

        while (rs.next()) {
            list.add(mapResultSet(rs));
        }

        return list;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM account WHERE email_address = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    private String buildAccountSelectSql(String suffix) throws SQLException {
        boolean hasBarangayAdmin = DbSchemaHelper.tableExists(conn, "barangay_admin");
        boolean hasBarangay = DbSchemaHelper.tableExists(conn, "barangay");
        boolean hasBarangayAccountLink = hasBarangayAdmin
                && DbSchemaHelper.columnExists(conn, "barangay_admin", "account_id");

        StringBuilder sql = new StringBuilder(
            "SELECT a.account_id, a.name, a.email_address, a.password, " +
            "s.status_label as status, r.role_key as role, a.last_login, a.is_barangay_setup_complete"
        );

        if (hasBarangay && hasBarangayAccountLink) {
            sql.append(", ba.barangay_admin_id, ba.age, g.gender_label as gender, b.barangay_id, b.barangay_name ");
            sql.append("FROM account a ");
            sql.append("LEFT JOIN status_lookup s ON a.status_id = s.status_id ");
            sql.append("LEFT JOIN role_lookup r ON a.role_id = r.role_id ");
            sql.append("LEFT JOIN barangay_admin ba ON a.account_id = ba.account_id ");
            sql.append("LEFT JOIN gender_lookup g ON ba.gender_id = g.gender_id ");
            sql.append("LEFT JOIN barangay b ON ba.barangay_id = b.barangay_id ");
        } else {
            sql.append(" FROM account a ");
            sql.append("LEFT JOIN status_lookup s ON a.status_id = s.status_id ");
            sql.append("LEFT JOIN role_lookup r ON a.role_id = r.role_id ");
        }

        if (suffix != null && !suffix.trim().isEmpty()) {
            sql.append(suffix);
        }

        return sql.toString();
    }

    private void syncBarangayAssignment(Account account) throws SQLException {
        if (account == null || account.getAccountId() <= 0) {
            return;
        }
        if (account.getBarangay() == null || account.getBarangay().trim().isEmpty()) {
            return;
        }
        if (!DbSchemaHelper.tableExists(conn, "barangay_admin")
                || !DbSchemaHelper.tableExists(conn, "barangay")
                || !DbSchemaHelper.columnExists(conn, "barangay_admin", "account_id")) {
            return;
        }

        Integer barangayId = findBarangayIdByName(account.getBarangay().trim());
        if (barangayId == null) {
            return;
        }
        account.setBarangayId(barangayId);

        String checkSql = "SELECT barangay_admin_id FROM barangay_admin WHERE account_id = ?";
        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setInt(1, account.getAccountId());
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    String updateSql = "UPDATE barangay_admin SET barangay_id = ? WHERE account_id = ?";
                    try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                        update.setInt(1, barangayId);
                        update.setInt(2, account.getAccountId());
                        update.executeUpdate();
                    }
                } else {
                    String insertSql = "INSERT INTO barangay_admin (barangay_id, account_id) VALUES (?, ?)";
                    try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                        insert.setInt(1, barangayId);
                        insert.setInt(2, account.getAccountId());
                        insert.executeUpdate();
                    }
                }
            }
        }
    }

    private void syncBarangayAdminDetails(Account account) throws SQLException {
        if (account == null || account.getAccountId() <= 0) {
            return;
        }
        if (!DbSchemaHelper.tableExists(conn, "barangay_admin")
                || !DbSchemaHelper.columnExists(conn, "barangay_admin", "account_id")) {
            return;
        }

        String adminName = account.getName();
        Integer ageValue = account.getAge() > 0 ? account.getAge() : null;
        Integer genderId = getGenderId(account.getGender());

        String checkSql = "SELECT barangay_admin_id, barangay_id FROM barangay_admin WHERE account_id = ?";
        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setInt(1, account.getAccountId());
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    account.setBarangayAdminId(rs.getInt("barangay_admin_id"));
                    int currentBarangayId = rs.getInt("barangay_id");
                    String updateSql = "UPDATE barangay_admin SET barangay_admin = ?, age = ?, gender_id = ? WHERE account_id = ?";
                    try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                        update.setString(1, adminName);
                        if (ageValue != null) {
                            update.setInt(2, ageValue);
                        } else {
                            update.setNull(2, Types.INTEGER);
                        }
                        if (genderId != null) {
                            update.setInt(3, genderId);
                        } else {
                            update.setNull(3, Types.INTEGER);
                        }
                        update.setInt(4, account.getAccountId());
                        update.executeUpdate();
                    }
                    if (currentBarangayId > 0) {
                        account.setBarangayId(currentBarangayId);
                    }
                } else {
                    String insertSql = "INSERT INTO barangay_admin (barangay_id, account_id, barangay_admin, age, gender_id) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        if (account.getBarangayId() > 0) {
                            insert.setInt(1, account.getBarangayId());
                        } else {
                            insert.setNull(1, Types.INTEGER);
                        }
                        insert.setInt(2, account.getAccountId());
                        insert.setString(3, adminName);
                        if (ageValue != null) {
                            insert.setInt(4, ageValue);
                        } else {
                            insert.setNull(4, Types.INTEGER);
                        }
                        if (genderId != null) {
                            insert.setInt(5, genderId);
                        } else {
                            insert.setNull(5, Types.INTEGER);
                        }
                        insert.executeUpdate();

                        try (ResultSet keys = insert.getGeneratedKeys()) {
                            if (keys.next()) {
                                account.setBarangayAdminId(keys.getInt(1));
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean assignBarangayToAccount(int accountId, int barangayId) throws SQLException {
        if (!DbSchemaHelper.tableExists(conn, "barangay_admin")
                || !DbSchemaHelper.columnExists(conn, "barangay_admin", "account_id")) {
            return false;
        }

        String checkSql = "SELECT barangay_admin_id FROM barangay_admin WHERE account_id = ?";
        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setInt(1, accountId);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    String updateSql = "UPDATE barangay_admin SET barangay_id = ? WHERE account_id = ?";
                    try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                        update.setInt(1, barangayId);
                        update.setInt(2, accountId);
                        return update.executeUpdate() > 0;
                    }
                }
            }
        }
        return false;
    }

    private Integer findBarangayIdByName(String barangayName) throws SQLException {
        String sql = "SELECT barangay_id FROM barangay WHERE UPPER(TRIM(barangay_name)) = UPPER(TRIM(?))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barangayName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("barangay_id") : null;
            }
        }
    }

    private Account mapResultSet(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountId(rs.getInt("account_id"));
        a.setName(rs.getString("name"));
        a.setEmail(rs.getString("email_address"));
        a.setPassword(rs.getString("password"));
        a.setStatus(rs.getString("status"));
        a.setRole(rs.getString("role"));
        a.setLastLogin(rs.getTimestamp("last_login"));
        
        try {
            a.setBarangayAdminId(rs.getInt("barangay_admin_id"));
            a.setAge(rs.getInt("age"));
            a.setGender(rs.getString("gender"));
            a.setBarangayId(rs.getInt("barangay_id"));
            a.setBarangay(rs.getString("barangay_name"));
        } catch (SQLException ignored) {
            a.setBarangayAdminId(0);
            a.setAge(0);
            a.setGender(null);
            a.setBarangayId(0);
            a.setBarangay(null);
        }
        
        try {
            a.setBarangaySetupComplete(rs.getBoolean("is_barangay_setup_complete"));
        } catch (SQLException ignored) {
            a.setBarangaySetupComplete(false);
        }
        
        return a;
    }
    
    private void ensureBarangaySetupColumn() throws SQLException {
        if (!DbSchemaHelper.tableExists(conn, "account")) {
            return;
        }
        if (!DbSchemaHelper.columnExists(conn, "account", "is_barangay_setup_complete")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE account ADD COLUMN is_barangay_setup_complete BOOLEAN DEFAULT FALSE");
            }
        }
    }
    
    public boolean updateBarangaySetupComplete(int accountId, boolean complete) throws SQLException {
        ensureBarangaySetupColumn();
        String sql = "UPDATE account SET is_barangay_setup_complete = ? WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, complete);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        }
    }

    private Integer getStatusId(String statusName) throws SQLException {
        if (statusName == null || statusName.trim().isEmpty()) return 1; // Default to first status
        String sql = "SELECT status_id FROM status_lookup WHERE status_key = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statusName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("status_id") : 1;
            }
        }
    }

    private Integer getRoleId(String roleName) throws SQLException {
        if (roleName == null || roleName.trim().isEmpty()) return 1; // Default to first role
        String sql = "SELECT role_id FROM role_lookup WHERE role_key = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("role_id") : 1;
            }
        }
    }

    private Integer getGenderId(String genderName) throws SQLException {
        if (genderName == null || genderName.trim().isEmpty()) return null;
        String sql = "SELECT gender_id FROM gender_lookup WHERE gender_label = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, genderName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("gender_id") : null;
            }
        }
    }
}
