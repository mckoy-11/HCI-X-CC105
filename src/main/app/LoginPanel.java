package main.app;

import main.dao.AccountDao;
import main.database.SQLConnection;
import main.model.Account;
import main.model.UserSession;
import main.service.AuthService;
import main.ui.components.AuthLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.SQLException;

import static main.style.SystemStyle.*;

public class LoginPanel extends JPanel {

    private final JTextField emailField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JLabel statusLabel = createStatusLabel();

    private AuthService authService;

private final Runnable onMenro;
    private final Runnable onBarangay;
    private final Runnable onBack;
    private final Runnable onSignup;
    
    // Store last logged in account for external access
    private Account lastLoggedInAccount;

    public LoginPanel() {
        this(null, null, null, null, null);
    }

    public LoginPanel(AuthService authService,
                      Runnable onMenro,
                      Runnable onBarangay,
                      Runnable onBack,
                      Runnable onSignup) {
        this.authService = authService;
        this.onMenro = onMenro;
        this.onBarangay = onBarangay;
        this.onBack = onBack;
        this.onSignup = onSignup;

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        AuthLayout authLayout = new AuthLayout(
                "Secure access",
                "Pick up where operations left off.",
                "<html>Sign in to the workspace used for schedule <br> coordination, requests, reports, and collection visibility.</html>",
                "Shared spacing keeps the login flow readable at a glance.",
                "The layout stays centered so no extra scrolling is needed.",
                "MENRO and barangay users move through the same polished entry point."
        );
        add(authLayout, BorderLayout.CENTER);

        JPanel form = authLayout.getFormContent();

        JButton back = createLinkButton("Back to welcome");
        back.addActionListener(e -> runAction(onBack));
        
        JButton forgotPassword = createLinkButton("Forgot password?");
        forgotPassword.addActionListener(e -> performForgotPassword());
        
        JPanel forgotPanel = createTransparentPanel(new FlowLayout(FlowLayout.RIGHT));
        forgotPanel.setPreferredSize(new Dimension(0, 30));
        forgotPanel.add(forgotPassword);

        JButton login = createPrimaryButton("Sign In");
        login.addActionListener(e -> performLogin());

        JButton signupLink = createLinkButton("Create one");
        signupLink.addActionListener(e -> runAction(onSignup));

        JPanel footer = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JLabel footerText = createFormSubtitle("Need an account?");
        footer.add(footerText);
        footer.add(signupLink);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleInput(emailField);
        styleInput(passField);
        emailField.setToolTipText("Enter your email address");
        passField.setToolTipText("Enter your password");

        form.add(back);
        form.add(Box.createVerticalStrut(18));
        form.add(createFormTitle("Welcome back"));
        form.add(Box.createVerticalStrut(12));
        form.add(createFormSubtitle(
                "<html>Sign in to continue managing requests, schedules,<br> collection updates, and responses.<html>"));
        form.add(Box.createVerticalStrut(30));
        form.add(createFieldGroup("Email address", emailField));
        form.add(Box.createVerticalStrut(16));
        form.add(createFieldGroup("Password", passField));
        form.add(Box.createVerticalStrut(8));
         form.add(createFieldGroup("", forgotPanel));
        form.add(Box.createVerticalStrut(24));
        form.add(login);
        form.add(Box.createVerticalStrut(8));
        
        form.add(Box.createVerticalStrut(12));
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(16));
        form.add(footer);
        form.add(Box.createVerticalGlue());

        KeyAdapter enter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };

        emailField.addKeyListener(enter);
        passField.addKeyListener(enter);
        registerStatusReset();
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            showError(statusLabel, "Fill in both email and password.");
            return;
        }

        if (authService == null) {
            try {
                Connection conn = SQLConnection.getConnection();
                authService = new AuthService(new AccountDao(conn));
            } catch (SQLException e) {
                showError(statusLabel, "Unable to connect to the database.");
                return;
            }
        }

        Account account = authService.login(email, pass);

        if (account == null) {
            showError(statusLabel, "Invalid credentials or inactive account.");
            return;
        }

clearStatus(statusLabel);
        UserSession.startSession(
                account.getAccountId(),
                account.getEmail(),
                account.getRole(),
                account.getName()
        );
        
        // Store account for external access (e.g., barangay setup check)
        this.lastLoggedInAccount = account;

        String role = account.getRole();

        if ("MENRO".equalsIgnoreCase(role)) {
            runAction(onMenro);
        } else if ("BARANGAY".equalsIgnoreCase(role)) {
            runAction(onBarangay);
        } else {
            showError(statusLabel, "Unknown role for this account.");
        }
    }

    private void performForgotPassword() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError(statusLabel, "Enter your email address first.");
            return;
        }

        if (authService == null) {
            try {
                Connection conn = SQLConnection.getConnection();
                authService = new AuthService(new AccountDao(conn));
            } catch (SQLException e) {
                showError(statusLabel, "Unable to connect to the database.");
                return;
            }
        }

        String result = authService.resetPassword(email);

        if ("SUCCESS".equals(result)) {
            showSuccess(statusLabel, "Password reset to '123456'. Please change it after logging in.");
        } else {
            showError(statusLabel, result);
        }
    }

    private void registerStatusReset() {
        DocumentListener clearOnChange = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                clearStatus(statusLabel);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                clearStatus(statusLabel);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                clearStatus(statusLabel);
            }
        };

        emailField.getDocument().addDocumentListener(clearOnChange);
        passField.getDocument().addDocumentListener(clearOnChange);
    }

private void runAction(Runnable action) {
        if (action != null) {
            action.run();
        }
        if (action == onSignup || action == onBack) {
            clearStatus(statusLabel);
        }
    }
    
    /**
     * Get the last logged in account (for checking barangay setup status)
     * @return 
     */
    public Account getLastLoggedInAccount() {
        return lastLoggedInAccount;
    }
}
