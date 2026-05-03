package main.service;

import java.sql.SQLException;
import main.dao.AccountDao;
import main.model.Account;
import main.store.DataChangeBus;
import main.store.DataTopics;

/**
 * Handles authentication and account operations.
 */
public class AuthService {

    private final AccountDao dao;

    public AuthService(AccountDao dao) {
        this.dao = dao;
        initializeAdminAccount();
    }

    /**
     * Initialize the built-in MENRO Admin account if it doesn't exist
     */
    private void initializeAdminAccount() {
        try {
            // Check if admin account already exists
            Account existingAdmin = dao.findByEmail("admin@municipal.gov");
            if (existingAdmin != null) {
                return; // Admin account already exists
            }

            // Create the admin account
            Account adminAccount = new Account("Menro Admin", "admin@municipal.gov", "admin123");
            adminAccount.setRole("MENRO"); // This will be converted to role_id 1
            adminAccount.setStatus("ACTIVE"); // This will be converted to status_id 1
            adminAccount.setBarangaySetupComplete(true); // Admin doesn't need barangay setup

            boolean success = dao.save(adminAccount);
            if (success) {
                System.out.println("Built-in MENRO Admin account created successfully");
            } else {
                System.err.println("Failed to create built-in MENRO Admin account");
            }
        } catch (SQLException e) {
            System.err.println("Error initializing admin account: " + e.getMessage());
        }
    }

    /**
     * Authenticate user.
     * @param email
     * @param password
     * @return Account if valid, null otherwise
     */
    public Account login(String email, String password) {

        try {
            Account a = dao.findByEmail(email);

            if (a == null) return null;

            if (!a.getPassword().equals(password)) return null;

            if (!"Active".equalsIgnoreCase(a.getStatus())) return null;

            dao.updateLastLogin(a.getAccountId());

            return a;

        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Register new account
     * @param name
     * @param email
     * @param password
     * @return 
     */
    public String register(String name, String email, String password) {
        return register(name, email, password, 0, null, null);
    }

    public String register(String name, String email, String password, String barangay) {
        return register(name, email, password, 0, null, barangay);
    }

    public String register(String name, String email, String password, int age, String gender) {
        return register(name, email, password, age, gender, null);
    }

    public String register(String name, String email, String password, int age, String gender, String barangay) {

        try {
            if (dao.emailExists(email)) {
                return "Email already exists";
            }

            if (password.length() < 6) {
                return "Password must be at least 6 characters";
            }

            Account a = new Account(name, email, password);
            a.setAge(age);
            a.setGender(gender);
            a.setBarangay(barangay);
            boolean ok = dao.save(a);
            if (ok) {
                DataChangeBus.publish(DataTopics.ACCOUNTS, DataTopics.BARANGAYS, DataTopics.DASHBOARD);
            }

            return ok ? "SUCCESS" : "Failed to register";

        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Reset password (Forgot Password)
     * @param email
     * @return 
     */
    public String resetPassword(String email) {

        try {
            Account acc = dao.findByEmail(email);

            if (acc == null) return "Account not found";

            acc.setPassword("123456"); // default reset
            dao.update(acc);
            DataChangeBus.publish(DataTopics.ACCOUNTS);

            return "SUCCESS";

        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Change password (Settings)
     * @param accountId
     * @param current
     * @param newPass
     * @return 
     */
    public String changePassword(int accountId, String current, String newPass) {

        try {
            Account acc = dao.findById(accountId);

            if (acc == null) return "Account not found";

            if (!acc.getPassword().equals(current)) {
                return "Current password is incorrect";
            }

            if (newPass.length() < 6) {
                return "Password must be at least 6 characters";
            }

            acc.setPassword(newPass);
            dao.update(acc);
            DataChangeBus.publish(DataTopics.ACCOUNTS);

            return "SUCCESS";

        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    public Account getAccountByEmail(String email) {
        try {
            return dao.findByEmail(email);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Change account email address
     * @param accountId
     * @param newEmail
     * @return success message or error
     */
    public String changeEmail(int accountId, String newEmail) {
        try {
            Account acc = dao.findById(accountId);
            if (acc == null) return "Account not found";

            // Check if new email is already taken
            if (dao.emailExists(newEmail) && !acc.getEmail().equals(newEmail)) {
                return "Email address is already in use";
            }

            acc.setEmail(newEmail);
            dao.update(acc);
            DataChangeBus.publish(DataTopics.ACCOUNTS);

            return "SUCCESS";

        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }
}
