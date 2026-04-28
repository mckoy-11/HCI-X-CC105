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
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import main.app.MainFrame;
import main.model.UserSession;
import main.service.HeaderService;
import static main.style.SystemStyle.PlainBtn;
import static main.style.SystemStyle.TEXTCOLOR;
import static main.style.SystemStyle.WHITE;

public class Header extends JPanel {

    private final HeaderService headerService = new HeaderService();

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
        Timer timer = new Timer(250, event -> performSearch(searchBar));
        timer.setRepeats(false);

        searchBar.onSearch(event -> {
            timer.stop();
            performSearch(searchBar);
        });
        searchBar.onType(timer::restart);
    }

    private void performSearch(SearchBar searchBar) {
        String query = searchBar.getText().trim();
        if (query.isEmpty() || "Search...".equalsIgnoreCase(query)) {
            return;
        }

        Component root = SwingUtilities.getRoot(this);
        JOptionPane.showMessageDialog(
                root,
                "Search is available as a quick command placeholder for now.\nQuery: " + query,
                "Search",
                JOptionPane.INFORMATION_MESSAGE
        );
        searchBar.clear();
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

        JMenuItem editName = new JMenuItem("Edit display name");
        editName.addActionListener(event -> editDisplayName());
        menu.add(editName);

        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(event -> logout());
        menu.add(logout);

        menu.show(anchor, 0, anchor.getHeight());
    }

    private void editDisplayName() {
        String currentName = UserSession.isActive() ? UserSession.getDisplayName() : "";
        String newName = JOptionPane.showInputDialog(this, "Enter display name:", currentName);
        if (newName != null && !newName.trim().isEmpty()) {
            UserSession.setDisplayName(newName.trim());
            JOptionPane.showMessageDialog(this, "Display name updated.");
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
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
