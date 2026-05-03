package main.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import main.app.MainFrame;
import main.dao.AccountDao;
import main.database.SQLConnection;
import main.model.UserSession;
import main.service.AuthService;
import main.service.HeaderService;
import static main.style.SystemStyle.PlainBtn;
import static main.style.SystemStyle.TEXTCOLOR;
import static main.style.SystemStyle.WHITE;
import main.ui.dialogs.SearchDialog;

public class Header extends JPanel {

    private final HeaderService headerService = new HeaderService();
    private SearchDialog searchDialog;
    
    public Header(String title) {
        setBackground(WHITE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 60));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel headerTitle = new JLabel(title);
        headerTitle.setForeground(TEXTCOLOR);
        headerTitle.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        headerTitle.setVerticalAlignment(SwingConstants.CENTER);
        add(headerTitle, BorderLayout.WEST);

        SearchBar searchBar = new SearchBar(25, "Search...");
        searchBar.setPreferredSize(new Dimension(350, 40));

        // create ONE dialog instance
        searchDialog = new SearchDialog(searchBar, null, null);

        // attach dialog to search bar
        searchBar.attachSearchDialog(searchDialog);

        installSearch(searchBar);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        centerPanel.setOpaque(false);
        centerPanel.add(searchBar);
        add(centerPanel, BorderLayout.CENTER);

        JButton notificationsButton = PlainBtn(null, "bell.png", 22);
        notificationsButton.addActionListener(event -> showNotifications(notificationsButton));

        JButton accountButton = PlainBtn(null, "account.png", 22);
        accountButton.addActionListener(event -> showAccountMenu(accountButton));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        rightPanel.setOpaque(false);
        rightPanel.add(notificationsButton);
        rightPanel.add(accountButton);
        add(rightPanel, BorderLayout.EAST);
    }

    private void installSearch(SearchBar searchBar) {
        Timer timer = new Timer(250, event -> {
            String query = searchBar.getText().trim();

            if (searchDialog != null) {
                searchDialog.updateQuery(query);
            }
        });

        timer.setRepeats(false);

        searchBar.onSearch(event -> {
            timer.stop();
            searchDialog.updateQuery(searchBar.getText());
        });

        searchBar.onType(timer::restart);
    }
    
    private void showNotifications(Component anchor) {
        PopupUI.show(anchor, headerService.buildNotificationItems(() -> showNotifications(anchor)));
    }

    private void showAccountMenu(Component anchor) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem sessionItem = new JMenuItem(UserSession.isActive()
                ? UserSession.getDisplayName() + " (" + UserSession.getRole() + ")"
                : "No active session");
        sessionItem.setEnabled(false);
        menu.add(sessionItem);

        JMenuItem changePasswordItem = new JMenuItem("Change Password");
        changePasswordItem.addActionListener(event -> changePassword());
        menu.add(changePasswordItem);

        JMenuItem changeEmailItem = new JMenuItem("Change Email");
        changeEmailItem.addActionListener(event -> changeEmail());
        menu.add(changeEmailItem);

        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(event -> logout());
        menu.add(logout);

        menu.show(anchor, 0, anchor.getHeight());
    }

    private void changePassword() {
        if (!UserSession.isActive()) {
            JOptionPane.showMessageDialog(null, "No active session.");
            return;
        }

        JPasswordField currentField = new JPasswordField();
        JPasswordField newField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();

        Object[] message = {
                "Current password:", currentField,
                "New password:", newField,
                "Confirm new password:", confirmField
        };

        int option = JOptionPane.showConfirmDialog(
                null,
                message,
                "Change Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String current = new String(currentField.getPassword());
        String newPass = new String(newField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All password fields are required.");
            return;
        }

        if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(null, "New passwords do not match.");
            return;
        }

        try (java.sql.Connection conn = SQLConnection.getConnection()) {
            AuthService auth = new AuthService(new AccountDao(conn));
            String result = auth.changePassword(UserSession.getAccountId(), current, newPass);
            if ("SUCCESS".equals(result)) {
                JOptionPane.showMessageDialog(null, "Password updated successfully.");
            } else {
                JOptionPane.showMessageDialog(null, result);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to change password: " + e.getMessage());
        }
    }

    private void changeEmail() {
        if (!UserSession.isActive()) {
            JOptionPane.showMessageDialog(null, "No active session.");
            return;
        }

        String newEmail = JOptionPane.showInputDialog(null, "Enter new email address:", UserSession.getEmail());
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return;
        }

        try (java.sql.Connection conn = SQLConnection.getConnection()) {
            AuthService auth = new AuthService(new AccountDao(conn));
            String result = auth.changeEmail(UserSession.getAccountId(), newEmail.trim());
            if ("SUCCESS".equals(result)) {
                UserSession.startSession(
                        UserSession.getAccountId(),
                        newEmail.trim(),
                        UserSession.getRole(),
                        UserSession.getDisplayName()
                );
                JOptionPane.showMessageDialog(null, "Email updated successfully.");
            } else {
                JOptionPane.showMessageDialog(null, result);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to change email: " + e.getMessage());
        }
    }

    private void editDisplayName() {
        String currentName = UserSession.isActive() ? UserSession.getDisplayName() : "";
        String newName = JOptionPane.showInputDialog(null, "Enter display name:", currentName);
        if (newName != null && !newName.trim().isEmpty()) {
            UserSession.setDisplayName(newName.trim());
            JOptionPane.showMessageDialog(null, "Display name updated.");
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Logout and return to welcome screen?",
                "Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        UserSession.logout();
        Window top = SwingUtilities.getWindowAncestor(this);
        if (top instanceof JFrame) {
            ((JFrame) top).dispose();
        }
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
