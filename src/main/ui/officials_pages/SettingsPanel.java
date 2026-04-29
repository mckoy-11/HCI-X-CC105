package main.ui.officials_pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import main.dao.AccountDao;
import main.database.SQLConnection;
import main.model.Account;
import main.model.UserSession;
import main.service.AuthService;
import static main.style.SystemStyle.Card;
import static main.style.SystemStyle.SUBTITLEBOLD;
import static main.style.SystemStyle.TEXTCOLOR;
import static main.style.SystemStyle.createFieldGroup;
import static main.style.SystemStyle.createFormSubtitle;
import static main.style.SystemStyle.roundPanel;
import static main.style.SystemStyle.styleComboBox;
import static main.style.SystemStyle.styleInput;
import main.ui.components.CustomButton;
import main.ui.components.Header;
import main.ui.components.ReactivePanel;
import main.ui.dialogs.AdminDialogSupport;

public class SettingsPanel extends ReactivePanel {

    private static final String PROFILE = "Profile";
    private static final String NOTIFICATIONS = "Notifications";
    private static final String PREFERENCES = "Preferences";

    private final java.awt.CardLayout cardLayout = new java.awt.CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private String currentView = PROFILE;
    private AccountDao accountDao;
    private AuthService authService;

    public SettingsPanel() {
        initServices();
        setLayout(new BorderLayout());
        setOpaque(false);

        add(new Header("Settings"), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private void initServices() {
        try {
            accountDao = new AccountDao(SQLConnection.getConnection());
            authService = new AuthService(accountDao);
        } catch (Exception e) {
            accountDao = null;
            authService = null;
        }
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        root.add(buildNav(), BorderLayout.NORTH);
        contentPanel.setOpaque(false);
        contentPanel.add(buildProfilePanel(), PROFILE);
        contentPanel.add(buildNotificationsPanel(), NOTIFICATIONS);
        contentPanel.add(buildPreferencesPanel(), PREFERENCES);
        cardLayout.show(contentPanel, currentView);
        root.add(contentPanel, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildNav() {
        JPanel nav = Card(16);
        nav.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 10));
        nav.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel navBar = roundPanel(50);
        navBar.setLayout(new GridLayout(1, 3, 5, 0));
        navBar.setPreferredSize(new java.awt.Dimension(360, 35));
        for (String view : new String[]{PROFILE, NOTIFICATIONS, PREFERENCES}) {
            CustomButton button = new CustomButton(
                    view, "", "", 18, 100, 25, 100, 25,
                    currentView.equals(view) ? Color.WHITE : new Color(129, 219, 122),
                    currentView.equals(view) ? Color.WHITE : TEXTCOLOR,
                    currentView.equals(view) ? TEXTCOLOR : Color.WHITE,
                    currentView.equals(view) ? TEXTCOLOR : Color.WHITE,
                    false, true
            );
            button.addActionListener(event -> {
                currentView = view;
                cardLayout.show(contentPanel, currentView);
            });
            navBar.add(button);
        }
        nav.add(navBar);
        return nav;
    }

    private JPanel buildProfilePanel() {
        Account account = currentAccount();
        JPanel panel = createSectionCard("Barangay Profile");
        panel.add(createFormSubtitle("Update your barangay profile details and account access from one place."));
        panel.add(Box.createVerticalStrut(16));

        JTextField barangayField = styleInput(new JTextField(account == null ? "" : safe(account.getBarangay())));
        JTextField nameField = styleInput(new JTextField(account == null ? "" : safe(account.getName())));
        JTextField emailField = styleInput(new JTextField(account == null ? "" : safe(account.getEmail())));

        panel.add(createFieldGroup("Barangay Name", barangayField));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createFieldGroup("Contact Name", nameField));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createFieldGroup("Email Address", emailField));
        panel.add(Box.createVerticalStrut(16));

        JButton save = new JButton("Save Profile");
        save.setBackground(new Color(40, 124, 39));
        save.setForeground(Color.WHITE);
        save.addActionListener(event -> saveProfile(account, nameField.getText().trim(), emailField.getText().trim()));
        panel.add(save);
        return wrap(panel);
    }

    private JPanel buildNotificationsPanel() {
        JPanel panel = createSectionCard("Notification Preferences");
        panel.add(createFormSubtitle("Keep notification preferences aligned with the MENRO settings structure."));
        panel.add(Box.createVerticalStrut(16));

        JComboBox<String> announcementCombo = styleComboBox(new JComboBox<String>(new String[]{"Enabled", "Muted"}));
        JComboBox<String> responseCombo = styleComboBox(new JComboBox<String>(new String[]{"Instant", "Daily Summary"}));
        JComboBox<String> archiveCombo = styleComboBox(new JComboBox<String>(new String[]{"Show Active Only", "Show Active + Archive Alerts"}));

        panel.add(createFieldGroup("Announcement Alerts", announcementCombo));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createFieldGroup("Response Updates", responseCombo));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createFieldGroup("Archive Notifications", archiveCombo));
        panel.add(Box.createVerticalStrut(16));

        JButton save = new JButton("Save Preferences");
        save.setBackground(new Color(40, 124, 39));
        save.setForeground(Color.WHITE);
        save.addActionListener(event -> AdminDialogSupport.showSuccess(this, "Notification preferences saved."));
        panel.add(save);
        return wrap(panel);
    }

    private JPanel buildPreferencesPanel() {
        JPanel panel = createSectionCard("System Preferences");
        panel.add(createFormSubtitle("Use the same structure as MENRO for security and interface controls."));
        panel.add(Box.createVerticalStrut(16));

        JComboBox<String> themeCombo = styleComboBox(new JComboBox<String>(new String[]{"Light", "Soft Green"}));
        JComboBox<String> dashboardCombo = styleComboBox(new JComboBox<String>(new String[]{"Compact", "Comfortable"}));
        JPasswordField currentField = styleInput(new JPasswordField());
        JPasswordField newField = styleInput(new JPasswordField());

        panel.add(createFieldGroup("Theme", themeCombo));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createFieldGroup("Dashboard Density", dashboardCombo));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createFieldGroup("Current Password", currentField));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createFieldGroup("New Password", newField));
        panel.add(Box.createVerticalStrut(16));

        JButton save = new JButton("Update Password");
        save.setBackground(new Color(40, 124, 39));
        save.setForeground(Color.WHITE);
        save.addActionListener(event -> updatePassword(currentField, newField));
        panel.add(save);
        return wrap(panel);
    }

    private JPanel createSectionCard(String titleText) {
        JPanel panel = Card(16);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel(titleText);
        title.setFont(SUBTITLEBOLD);
        title.setForeground(TEXTCOLOR);
        panel.add(title);
        panel.add(Box.createVerticalStrut(12));
        return panel;
    }

    private JPanel wrap(JPanel card) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.NORTH);
        return wrapper;
    }

    private void saveProfile(Account account, String name, String email) {
        if (account == null || accountDao == null) {
            AdminDialogSupport.showFailure(this, "Account details are unavailable.");
            return;
        }
        if (name.isEmpty() || email.isEmpty()) {
            AdminDialogSupport.showFailure(this, "Name and email are required.");
            return;
        }
        account.setName(name);
        account.setEmail(email);
        try {
            if (accountDao.update(account)) {
                UserSession.startSession(account.getAccountId(), email, account.getRole(), name);
                AdminDialogSupport.showSuccess(this, "Profile updated successfully.");
            } else {
                AdminDialogSupport.showFailure(this, "Unable to update the profile.");
            }
        } catch (Exception e) {
            AdminDialogSupport.showFailure(this, "Unable to update the profile.");
        }
    }

    private void updatePassword(JPasswordField currentField, JPasswordField newField) {
        if (!UserSession.isActive() || authService == null) {
            AdminDialogSupport.showFailure(this, "Account details are unavailable.");
            return;
        }
        String result = authService.changePassword(
                UserSession.getAccountId(),
                new String(currentField.getPassword()),
                new String(newField.getPassword())
        );
        if ("SUCCESS".equals(result)) {
            AdminDialogSupport.showSuccess(this, "Password updated successfully.");
            currentField.setText("");
            newField.setText("");
        } else {
            AdminDialogSupport.showFailure(this, result);
        }
    }

    private Account currentAccount() {
        if (!UserSession.isActive() || accountDao == null) {
            return null;
        }
        try {
            return accountDao.findById(UserSession.getAccountId());
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
