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
}
