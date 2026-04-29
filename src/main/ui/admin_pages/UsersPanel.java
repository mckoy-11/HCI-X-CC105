package main.ui.admin_pages;

import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.dao.AccountDao;
import main.database.SQLConnection;
import main.model.Account;
import main.model.PopupItem;
import main.service.AnnouncementService;
import main.ui.dialogs.AdminDialogSupport;

import static main.style.SystemStyle.*;

import main.ui.components.*;
import static main.ui.components.CustomButton.createButton;
import main.ui.components.ReactivePanel;
import main.store.DataChangeBus;
import main.store.DataTopics;

/**
 * UsersPanel - BARANGAY account management UI
 * Filters:
 * - All
 * - Active
 * - Inactive
 */
public class UsersPanel extends ReactivePanel {

    private static final SimpleDateFormat LAST_LOGIN_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private AccountDao accountDao;
    private final AnnouncementService announcementService = new AnnouncementService();

    private CardLayout contentLayout;
    private JPanel contentPanel;
    private JPanel summaryPanel;

    // UI filter only applies to STATUS
    private String userFilter = "All";

    public UsersPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        listen(DataTopics.ACCOUNTS, this::refreshUI);
        
        init();

        add(new Header("Users Management"), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }
    
    private void init() {
        try {
            accountDao = new AccountDao(SQLConnection.getConnection());
        } catch (SQLException e){}
        
    }
        

    private JPanel createMainContent() {

        JPanel root = GradientPaint(BGCOLOR1, BGCOLOR2, 0, false, 20);
        root.setLayout(new BorderLayout(0, 10));

        summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setOpaque(false);
        summaryPanel.add(createSummary());

        contentLayout = new CardLayout();
        contentPanel = Card(12);
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 4));
        contentPanel.setLayout(contentLayout);

        contentPanel.add(createUserView(), "Users");

        root.add(summaryPanel, BorderLayout.NORTH);
        root.add(contentPanel, BorderLayout.CENTER);

        return root;
    }

    private JPanel createHeader(JLabel title) {

        JPanel header = Card(12, 0, SIDEBAR);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        title.setForeground(Color.WHITE);
        title.setFont(SUBTITLEBOLD);

        JPanel navBar = roundPanel(50);
        navBar.setLayout(new GridLayout(1, 3, 5, 0));
        navBar.setPreferredSize(new Dimension(400, 35));

        String[] filters = {"All", "Active", "Inactive"};

        for (String filter : filters) {

            CustomButton btn = new CustomButton(
                    filter,
                    filter.toLowerCase() + ".png",
                    filter.toLowerCase() + "-white.png",
                    20, 100, 25, 100, 25,
                    Color.WHITE, TEXTCOLOR, TEXTCOLOR, Color.WHITE,
                    false, true
            );

            btn.addActionListener(e -> {
                userFilter = filter;
                refreshUI();
            });

            btn.setContentAreaFilled(false);
            navBar.add(btn);
        }

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton announce = createButton("Send Announcement", "send.png", "send.png", 20);
        announce.setPreferredSize(new Dimension(180, 35));
        announce.addActionListener(e -> openAnnouncementDialog());

        right.add(navBar);
        right.add(announce);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private SummaryCards createSummary() {

        java.util.List<Account> accounts = getAllAccounts();

        int total = accounts.size();

        int active = (int) accounts.stream()
                .filter(a -> "Active".equalsIgnoreCase(a.getStatus()))
                .count();

        int inactive = (int) accounts.stream()
                .filter(a -> "Inactive".equalsIgnoreCase(a.getStatus()))
                .count();

        return new SummaryCards(
                new String[]{"All Users", "Active Users", "Inactive Users"},
                new int[]{total, active, inactive},
                new String[]{"All", "Active", "Inactive"},
                icons(),
                colors()
        );
    }
    
    
    private String[] icons() {
        return new String[]{"calendar.png", "circle-check.png", "circle-alert.png"};
    }

    private Color[] colors() {
        return new Color[]{
                new Color(139, 92, 246, 20),
                new Color(59, 130, 246, 20),
                new Color(232, 114, 82, 20)
        };
    }

    private java.util.List<Account> getAllAccounts() {
        try {
            if (accountDao != null) {
                return accountDao.findAllByRole("BARANGAY");
            }
        } catch (SQLException ignored) {}
        return new ArrayList<>();
    }

    private JPanel createUserView() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("Users Management");

        panel.add(createHeader(title), BorderLayout.NORTH);
        panel.add(createUserTable(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createUserTable() {

        ScrollableTable table = new ScrollableTable(
                "Name", "Email", "Barangay", "Last Login", "Status", "Action"
        );

        java.util.List<Account> accounts = getAllAccounts();

        accounts.forEach((u) -> {
            boolean matchStatus =
                    userFilter.equals("All") ||
                    u.getStatus().equalsIgnoreCase(userFilter);
            if (!(!matchStatus)) {
                java.util.List<PopupItem> actions = new ArrayList<>();
                actions.add(new PopupItem("Toggle Status", "Switch active state", () -> toggleUserStatus(u)));
                actions.add(new PopupItem("Delete", "Remove this account", () -> deleteUser(u)));
                
                table.addRowWithAction(
                        u.getName(),
                        u.getEmail(),
                        u.getBarangay() != null ? u.getBarangay() : "Unassigned",
                        u.getLastLogin() != null ? LAST_LOGIN_FORMAT.format(u.getLastLogin()) : "Never",
                        u.getStatus(),
                        actions
                );
            }
        });

        return table;
    }

    private void refreshUI() {
        SwingUtilities.invokeLater(() -> {
            summaryPanel.removeAll();
            summaryPanel.add(createSummary());
            summaryPanel.revalidate();
            summaryPanel.repaint();

            contentPanel.removeAll();
            contentPanel.add(createUserView(), "Users");
            contentPanel.revalidate();
            contentPanel.repaint();
        });
    }

    private void openAnnouncementDialog() {
        JTextField titleField = styleInput(new JTextField());
        JTextArea messageArea = new JTextArea(8, 24);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createFormSubtitle("Active announcements appear at the top of the Barangay dashboard and archive automatically after one week or when replaced."));
        form.add(Box.createVerticalStrut(16));
        form.add(createFieldGroup("Title", titleField));
        form.add(Box.createVerticalStrut(16));
        form.add(createFieldLabel("Message"));
        form.add(Box.createVerticalStrut(8));
        form.add(new JScrollPane(messageArea));

        if (!AdminDialogSupport.showFormDialog(this, "Send Announcement", form)) {
            return;
        }

        String title = titleField.getText().trim();
        String message = messageArea.getText().trim();
        if (message.isEmpty()) {
            AdminDialogSupport.showFailure(this, "Announcement message is required.");
            return;
        }

        if (announcementService.publishAnnouncement(title, message)) {
            AdminDialogSupport.showSuccess(this, "Announcement sent successfully.");
        } else {
            AdminDialogSupport.showFailure(this, "Unable to send the announcement.");
        }
    }

    private void toggleUserStatus(Account account) {
        String nextStatus = "Active".equalsIgnoreCase(account.getStatus()) ? "Inactive" : "Active";
        boolean confirmed = AdminDialogSupport.confirmAction(
                this,
                "Change Status",
                "Change " + account.getName() + " to " + nextStatus + "?"
        );
        if (!confirmed || accountDao == null) {
            return;
        }

        try {
            if (accountDao.updateStatus(account.getAccountId(), nextStatus)) {
                DataChangeBus.publish(DataTopics.ACCOUNTS, DataTopics.DASHBOARD);
                AdminDialogSupport.showSuccess(this, "User status updated successfully.");
                refreshUI();
            } else {
                AdminDialogSupport.showFailure(this, "Failed to update user status.");
            }
        } catch (SQLException e) {
            AdminDialogSupport.showFailure(this, "Failed to update user status.");
        }
    }

    private void deleteUser(Account account) {
        boolean confirmed = AdminDialogSupport.confirmAction(
                this,
                "Delete User",
                "Delete " + account.getName() + " permanently?"
        );
        if (!confirmed || accountDao == null) {
            return;
        }

        try {
            if (accountDao.delete(account.getAccountId())) {
                DataChangeBus.publish(DataTopics.ACCOUNTS, DataTopics.DASHBOARD);
                AdminDialogSupport.showSuccess(this, "User deleted successfully.");
                refreshUI();
            } else {
                AdminDialogSupport.showFailure(this, "Failed to delete user.");
            }
        } catch (SQLException e) {
            AdminDialogSupport.showFailure(this, "Failed to delete user.");
        }
    }
}
