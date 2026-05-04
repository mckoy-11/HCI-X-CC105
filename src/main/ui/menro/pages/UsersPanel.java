package main.ui.menro.pages;

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

import static main.ui.style.SystemStyle.*;

import main.ui.components.*;
import static main.ui.components.CustomButton.createButton;
import main.ui.components.ReactivePanel;
import main.ui.menro.dialogs.AnnouncementFormDialog;
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
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        AnnouncementFormDialog dialog = new AnnouncementFormDialog(parent, null);
        dialog.setVisible(true);
    }

    private JPanel createMessageField(JTextArea messageArea) {
        JPanel group = createTransparentPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JLabel label = createFieldLabel("Message");
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        group.add(label);

        group.add(Box.createVerticalStrut(8));


        return group;
    }

    private void toggleUserStatus(Account account) {
        String newStatus = "Active".equalsIgnoreCase(account.getStatus()) ? "Inactive" : "Active";
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Change status of " + account.getName() + " to " + newStatus + "?",
            "Confirm Status Change",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Update status in database
                account.setStatus(newStatus);
                boolean success = accountDao.update(account);
                
                if (success) {
                    JOptionPane.showMessageDialog(
                        this,
                        "User status updated to " + newStatus,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    // Refresh UI
                    DataChangeBus.publish(DataTopics.ACCOUNTS);
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Failed to update user status",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void deleteUser(Account account) {
        // Soft delete: set status to INACTIVE instead of hard delete
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Deactivate account for " + account.getName() + "?\n(User cannot log in after this)",
            "Confirm Deactivation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Soft delete: set status to INACTIVE
                account.setStatus("Inactive");
                boolean success = accountDao.update(account);
                
                if (success) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Account deactivated successfully.\nUser cannot log in anymore.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    // Refresh UI
                    DataChangeBus.publish(DataTopics.ACCOUNTS);
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Failed to deactivate account",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
