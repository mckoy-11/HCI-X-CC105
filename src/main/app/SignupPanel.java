package main.app;

import main.dao.AccountDao;
import main.database.SQLConnection;
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

public class SignupPanel extends JPanel {

    private final JTextField nameField = new JTextField();
    private final JTextField ageField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JPasswordField confirmPassField = new JPasswordField();
    private final JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    private final JLabel statusLabel = createStatusLabel();

    private AuthService authService;
    private final Runnable onSuccess;
    private final Runnable onBack;
    private final Runnable onLogin;

    public SignupPanel() {
        this(null, null, null, null);
    }

    public SignupPanel(AuthService authService, Runnable onSuccess, Runnable onBack, Runnable onLogin) {
        this.authService = authService;
        this.onSuccess = onSuccess;
        this.onBack = onBack;
        this.onLogin = onLogin;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        AuthLayout authLayout = new AuthLayout(
                "Create a new account",
                "Set up your access in one clean pass.",
                "Create a barangay-ready account using the same spacing, typography, and form rhythm used throughout the system.",
                "Related fields stay grouped so the form feels lighter.",
                "Reusable controls keep the full app visually consistent.",
                "The entire signup flow fits comfortably in a single screen."
        );
        add(authLayout, BorderLayout.CENTER);

        JPanel form = authLayout.getFormContent();

        JButton backBtn = createLinkButton("Back to welcome");
        backBtn.addActionListener(e -> runAction(onBack));

        JButton signupBtn = createPrimaryButton("Create Account");
        signupBtn.addActionListener(e -> performSignup());

        JButton loginBtn = createLinkButton("Sign in");
        loginBtn.addActionListener(e -> runAction(onLogin));

        JPanel footer = createTransparentPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(createFormSubtitle("Already have an account?"));
        footer.add(loginBtn);

        styleInput(nameField);
        styleInput(ageField);
        styleInput(emailField);
        styleInput(passField);
        styleInput(confirmPassField);
        styleComboBox(genderCombo);
        ageField.setToolTipText("Enter age in years");
        emailField.setToolTipText("Enter your email address");

        form.add(backBtn);
        form.add(Box.createVerticalStrut(18));
        form.add(createFormTitle("Create your account"));
        form.add(Box.createVerticalStrut(12));
        form.add(createFormSubtitle("Add your details below to start using WASTELY."));
        form.add(Box.createVerticalStrut(26));
        form.add(createSplitRow(
                createFieldGroup("Full name", nameField),
                createFieldGroup("Age", ageField)
        ));
        form.add(Box.createVerticalStrut(16));
        form.add(createSplitRow(
                createFieldGroup("Email address", emailField),
                createFieldGroup("Gender", genderCombo)
        ));
        form.add(Box.createVerticalStrut(16));
        form.add(createFieldGroup("Password", passField));
        form.add(Box.createVerticalStrut(16));
        form.add(createFieldGroup("Confirm password", confirmPassField));
        form.add(Box.createVerticalStrut(24));
        form.add(signupBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(16));
        form.add(footer);
        form.add(Box.createVerticalGlue());

        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSignup();
                }
            }
        };
        nameField.addKeyListener(enterListener);
        ageField.addKeyListener(enterListener);
        emailField.addKeyListener(enterListener);
        passField.addKeyListener(enterListener);
        confirmPassField.addKeyListener(enterListener);
        registerStatusReset();
    }

    private void performSignup() {
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passField.getPassword());
        String confirmPassword = new String(confirmPassField.getPassword());
        String gender = (String) genderCombo.getSelectedItem();

        if (name.isEmpty()) {
            showError(statusLabel, "Full name is required.");
            nameField.requestFocusInWindow();
            return;
        }
        if (name.length() < 2) {
            showError(statusLabel, "Name must be at least 2 characters.");
            nameField.requestFocusInWindow();
            return;
        }
        if (email.isEmpty()) {
            showError(statusLabel, "Email is required.");
            emailField.requestFocusInWindow();
            return;
        }
        if (ageText.isEmpty()) {
            showError(statusLabel, "Age is required.");
            ageField.requestFocusInWindow();
            return;
        }
        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            showError(statusLabel, "Please enter a valid age.");
            ageField.requestFocusInWindow();
            return;
        }
        if (age <= 0) {
            showError(statusLabel, "Age must be greater than 0.");
            ageField.requestFocusInWindow();
            return;
        }
        if (!isValidEmail(email)) {
            showError(statusLabel, "Please enter a valid email address.");
            emailField.requestFocusInWindow();
            return;
        }
        if (password.isEmpty()) {
            showError(statusLabel, "Password is required.");
            passField.requestFocusInWindow();
            return;
        }
        if (password.length() < 6) {
            showError(statusLabel, "Password must be at least 6 characters.");
            passField.requestFocusInWindow();
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError(statusLabel, "Passwords do not match.");
            confirmPassField.requestFocusInWindow();
            return;
        }

        if (authService == null) {
            try {
                Connection conn = SQLConnection.getConnection();
                authService = new AuthService(new AccountDao(conn));
            } catch (SQLException e) {
                showError(statusLabel, "Database connection error.");
                return;
            }
        }

        clearStatus(statusLabel);

        String result = authService.register(name, email, password, age, gender);
        if ("SUCCESS".equals(result)) {
            runAction(onSuccess);
        } else {
            showError(statusLabel, result);
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
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

        nameField.getDocument().addDocumentListener(clearOnChange);
        ageField.getDocument().addDocumentListener(clearOnChange);
        emailField.getDocument().addDocumentListener(clearOnChange);
        passField.getDocument().addDocumentListener(clearOnChange);
        confirmPassField.getDocument().addDocumentListener(clearOnChange);
        genderCombo.addActionListener(e -> clearStatus(statusLabel));
    }

    private void runAction(Runnable action) {
        if (action != null) {
            action.run();
        }
        if (action == onLogin || action == onBack) {
            clearStatus(statusLabel);
        }
    }
}
