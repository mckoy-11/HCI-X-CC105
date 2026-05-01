package main.ui.admin_pages;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import main.dao.AccountDao;
import main.database.SQLConnection;
import main.model.Account;
import main.model.UserSession;
import main.service.AuthService;
import static main.style.SystemStyle.*;
import main.ui.components.CustomButton;
import main.ui.components.Header;
import main.store.DataChangeBus;
import main.store.DataTopics;

public final class SettingsPanel extends JPanel {

    private static final String ACCOUNT = "Account";
    private static final String CUSTOMIZATION = "Customization";
    private static final String NOTIFICATION = "Notification";
    private static final String SECURITY = "Security";
    private static final String HELP = "Help Center";

    private static final String DARK_MODE = "darkMode";
    private static final String PUSH_NOTIF = "pushNotif";
    private static final String COLLECTION_ALERTS = "collectionAlerts";
    private static final String SCHEDULE_REMINDERS = "scheduleReminders";
    private static final String DAILY_SUMMARY = "dailySummary";
    private static final String WEEKLY_REPORT = "weeklyReport";
    private static final String TWO_FACTOR = "twoFactor";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private JButton[] navButtons;
    private int selectedIndex = 0;

    private boolean darkModeEnabled;
    private boolean pushNotificationsEnabled = true;
    private boolean collectionAlertsEnabled = true;
    private boolean scheduleRemindersEnabled = true;
    private boolean dailySummaryEnabled;
    private boolean weeklyReportEnabled = true;
    private boolean twoFactorEnabled;
    private String fontSize = "Medium";
    private String tableDensity = "Comfortable";
    private String animationMode = "Enabled";

    private AccountDao accountDao;
    private AuthService authService;

    // Design constants
    private static final int SIDEBAR_WIDTH = 220;
    private static final int CARD_RADIUS = 16;
    private static final int SECTION_SPACING = 24;
    private static final int CARD_SPACING = 16;
    private static final int ITEM_SPACING = 12;
    private static final int FORM_WIDTH = 400;

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        initServices();

        add(new Header("Settings"), BorderLayout.NORTH);
        add(createMainContainer(), BorderLayout.CENTER);
    }

    private void initServices() {
        try {
            Connection conn = SQLConnection.getConnection();
            accountDao = new AccountDao(conn);
            authService = new AuthService(accountDao);
        } catch (SQLException e) {
            accountDao = null;
            authService = null;
        }
    }

    private JPanel createMainContainer() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setOpaque(false);
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createSidebar(), createContentArea());
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerLocation(SIDEBAR_WIDTH);
        splitPane.setDividerSize(0);
        splitPane.setContinuousLayout(true);

        mainContainer.add(splitPane, BorderLayout.CENTER);
        return mainContainer;
    }

    private JPanel createSidebar() {
        JPanel sidebar = Card(CARD_RADIUS);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setMaximumSize(new Dimension(SIDEBAR_WIDTH, Integer.MAX_VALUE));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(SUBTITLEBOLD.deriveFont(18f));
        titleLabel.setForeground(TEXTCOLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        sidebar.add(titleLabel);

        String[] sections = {ACCOUNT, CUSTOMIZATION, NOTIFICATION, SECURITY, HELP};
        navButtons = new JButton[sections.length];

        for (int i = 0; i < sections.length; i++) {
            JButton btn = createNavButton(sections[i], i);
            navButtons[i] = btn;
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());
        updateButtonStates();
        return sidebar;
    }

    private JButton createNavButton(String section, int index) {
        CustomButton btn = new CustomButton(
                section,
                "icon.png", "icon.png", 14,
                SIDEBAR_WIDTH, 48, SIDEBAR_WIDTH - 5, 48,
                WHITE, HOVERBTN,
                TEXTCOLOR, WHITE,
                true, true
        );

        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(BODYBOLD.deriveFont(14f));
        btn.setBorder(new EmptyBorder(0, 16, 0, 16));
        btn.setFocusPainted(false);

        btn.addActionListener(e -> switchSection(section, index));
        return btn;
    }

    private void switchSection(String section, int index) {
        selectedIndex = index;
        cardLayout.show(contentPanel, section);
        updateButtonStates();
    }

    private void updateButtonStates() {
        for (int i = 0; i < navButtons.length; i++) {
            boolean isSelected = i == selectedIndex;
            navButtons[i].setBackground(isSelected ? ACTIVE : SIDEBAR);
            navButtons[i].setForeground(isSelected ? WHITE : TEXTCOLOR);
        }
        repaint();
    }

    private JScrollPane createContentArea() {
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 20, 0, 0));

        contentPanel.add(createAccountPanel(), ACCOUNT);
        contentPanel.add(createCustomizationPanel(), CUSTOMIZATION);
        contentPanel.add(createNotificationPanel(), NOTIFICATION);
        contentPanel.add(createSecurityPanel(), SECURITY);
        contentPanel.add(createHelpPanel(), HELP);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        return scroll;
    }

    private JPanel createAccountPanel() {
        return createSectionPanel(
                createSectionTitle("Account Settings"),
                createActionCard("Profile Information", "Edit Profile", "Change Display Name"),
                createActionCard("Account Management", "Change Email", "Change Password", "Delete Account")
        );
    }

    private JPanel createCustomizationPanel() {
        return createSectionPanel(
                createSectionTitle("Customization"),
                createToggleCard("Dark Mode", darkModeEnabled, DARK_MODE),
                createOptionCard("Display", "Font Size", "Table Density", "Animations")
        );
    }

    private JPanel createNotificationPanel() {
        return createSectionPanel(
                createSectionTitle("Notifications"),
                createToggleCard("Push Notifications", pushNotificationsEnabled, PUSH_NOTIF),
                createToggleCard("Collection Alerts", collectionAlertsEnabled, COLLECTION_ALERTS),
                createToggleCard("Schedule Reminders", scheduleRemindersEnabled, SCHEDULE_REMINDERS),
                createToggleCard("Daily Summary", dailySummaryEnabled, DAILY_SUMMARY),
                createToggleCard("Weekly Report", weeklyReportEnabled, WEEKLY_REPORT)
        );
    }

    private JPanel createSecurityPanel() {
        return createSectionPanel(
                createSectionTitle("Security"),
                createToggleCard("Two-Factor Authentication", twoFactorEnabled, TWO_FACTOR),
                createActionCard("Login Security", "Change Password", "Active Sessions")
        );
    }

    private JPanel createHelpPanel() {
        return createSectionPanel(
                createSectionTitle("Help Center"),
                createActionCard("Support", "Contact Support", "Report Bug", "Feature Request")
        );
    }

    private JPanel createSectionPanel(Component... components) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (Component component : components) {
            panel.add(component);
            if (component != components[0]) {
                panel.add(Box.createVerticalStrut(CARD_SPACING));
            }
        }
        return panel;
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBTITLEBOLD.deriveFont(20f));
        label.setForeground(TEXTCOLOR);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(0, 0, SECTION_SPACING, 0));
        return label;
    }

    private JPanel createActionCard(String title, String... actions) {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = createCardTitle(title);
        card.add(titleLabel);

        for (String action : actions) {
            JButton button = createActionButton(action);
            button.addActionListener(e -> handleAction(action));
            card.add(button);
            card.add(Box.createVerticalStrut(ITEM_SPACING));
        }

        return card;
    }

    private JPanel createToggleCard(String text, boolean state, String id) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel(text);
        label.setFont(BODYBOLD.deriveFont(15f));
        label.setForeground(TEXTCOLOR);

        JToggleButton toggle = createStyledToggle(state);
        toggle.addItemListener(e -> handleToggle(id, e));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 0, 0, 16));
        content.add(label, BorderLayout.CENTER);

        card.add(content, BorderLayout.WEST);
        card.add(toggle, BorderLayout.EAST);
        return card;
    }

    private JPanel createOptionCard(String title, String... options) {
        return createActionCard(title, options);
    }

    private JPanel createCard() {
        JPanel card = Card(CARD_RADIUS, Color.WHITE);
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        return card;
    }

    private JLabel createCardTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(BODYBOLD.deriveFont(16f));
        label.setForeground(TEXTCOLOR.darker());
        label.setBorder(new EmptyBorder(0, 0, 16, 0));
        return label;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BODYPLAIN.deriveFont(14f));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(12, 0, 12, 0));
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setForeground(TEXTCOLOR);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(ACTIVE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(TEXTCOLOR);
            }
        });
        
        return button;
    }

    private JToggleButton createStyledToggle(boolean selected) {
        JToggleButton toggle = new JToggleButton();
        toggle.setSelected(selected);
        toggle.setPreferredSize(new Dimension(50, 28));
        toggle.setFocusPainted(false);
        toggle.setContentAreaFilled(false);
        toggle.setBorder(BorderFactory.createEmptyBorder());
        toggle.setRolloverEnabled(false);
        return toggle;
    }

    private void handleAction(String action) {
        switch (action) {
            case "Edit Profile":
            case "Change Display Name":
                handleDisplayNameChange();
                break;
            case "Change Email":
                handleEmailChange();
                break;
            case "Change Password":
                handlePasswordChange();
                break;
            case "Delete Account":
                handleAccountDeactivation();
                break;
            case "Font Size":
                fontSize = chooseOption("Font Size", "Choose font size:", new String[]{"Small", "Medium", "Large"}, fontSize);
                break;
            case "Table Density":
                tableDensity = chooseOption("Table Density", "Choose table density:", new String[]{"Compact", "Comfortable", "Spacious"}, tableDensity);
                break;
            case "Animations":
                animationMode = chooseOption("Animations", "Choose animation mode:", new String[]{"Enabled", "Reduced", "Disabled"}, animationMode);
                break;
            case "Active Sessions":
                showSessionDialog();
                break;
            case "Contact Support":
            case "Report Bug":
            case "Feature Request":
                handleSupportRequest(action);
                break;
            default:
                
                break;
        }
    }

    private void handleToggle(String id, ItemEvent e) {
        boolean enabled = e.getStateChange() == ItemEvent.SELECTED;

        switch (id) {
            case DARK_MODE:
                darkModeEnabled = enabled;
                break;
            case PUSH_NOTIF:
                pushNotificationsEnabled = enabled;
                break;
            case COLLECTION_ALERTS:
                collectionAlertsEnabled = enabled;
                break;
            case SCHEDULE_REMINDERS:
                scheduleRemindersEnabled = enabled;
                break;
            case DAILY_SUMMARY:
                dailySummaryEnabled = enabled;
                break;
            case WEEKLY_REPORT:
                weeklyReportEnabled = enabled;
                break;
            case TWO_FACTOR:
                twoFactorEnabled = enabled;
                break;
        }
    }

    private void handleDisplayNameChange() {
       
    }

    private void handleEmailChange() {
       
    }

    private void handlePasswordChange() {
       
    }

    private void handleAccountDeactivation() {
        
    }

    private String chooseOption(String title, String label, String[] options, String currentValue) {
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setSelectedItem(currentValue);

        if (!showFormDialog(title, panelWithField(label, comboBox))) {
            return currentValue;
        }

        String selected = (String) comboBox.getSelectedItem();
        return selected;
    }

    private void showSessionDialog() {
        String message = UserSession.isActive() 
            ? "Active account: " + UserSession.getEmail() + "\nRole: " + UserSession.getRole()
            : "No active session found.";
        JOptionPane.showMessageDialog(this, message, "Active Sessions", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleSupportRequest(String title) {
        JTextField subjectField = new JTextField(title);
                JTextArea messageArea = new JTextArea(6, 24);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(panelWithField("Subject:", subjectField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Message:"));
        panel.add(Box.createVerticalStrut(6));
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setPreferredSize(new Dimension(FORM_WIDTH, 220));
        panel.add(messageScroll);

        if (!showFormDialog(title, panel)) return;

    }

    private Account getCurrentAccount() {
        
            return null;
        
    }

    private boolean showFormDialog(String title, JPanel panel) {
        return false;
    }

    private JPanel panelWithField(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(BODYBOLD.deriveFont(13f));
        panel.add(labelComp, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel labeledField(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(BODYBOLD.deriveFont(13f));
        panel.add(labelComp, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
}
