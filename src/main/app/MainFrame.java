package main.app;

import main.dao.AccountDao;
import main.database.SQLConnection;
import main.model.Account;
import main.service.AuthService;
import main.ui.BarangayFrame;
import main.ui.MenroFrame;
import main.ui.dialogs.BarangaySetupDialog;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public final class MainFrame extends JFrame {

    private static final String WELCOME = "welcome";
    private static final String LOGIN = "login";
    private static final String SIGNUP = "signup";

    private final CardLayout layout = new CardLayout();
    private final JPanel root = new JPanel(layout);
    private final AppRouter router = new AppRouter(root, layout);

    private final AuthService auth;
    private LoginPanel loginPanel;

    public MainFrame() {
        auth = initAuth();
        if (auth == null) {
            dispose();
            return;
        }

        setupFrame();
        setupRoutes();
        
        setLayout(new BorderLayout());
        add(root, BorderLayout.CENTER);
        setVisible(true);
    }

    private AuthService initAuth() {
        try {
            Connection conn = SQLConnection.getConnection();
            return new AuthService(new AccountDao(conn));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to connect to the database.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
}
    }

    private void setupFrame() {
        setTitle("WASTELY");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void setupRoutes() {
        router.register(WELCOME, new WelcomePage(
                () -> router.navigate(SIGNUP),
                () -> router.navigate(LOGIN)
        ));

        // Initialize loginPanel first
        this.loginPanel = null;
        
        // Login panel - check barangay setup after login
        this.loginPanel = new LoginPanel(
                auth,
                () -> {
                    dispose();
                    new MenroFrame();
                },
                () -> {
                    dispose();
                    
                    // Check if barangay setup is needed
                    Account acc = this.loginPanel != null ? this.loginPanel.getLastLoggedInAccount() : null;
                    final Account finalAccount = acc;
                    
                    // Open Barangay window immediately
                    final BarangayFrame barangay = new BarangayFrame();
                    
                    // If barangay setup is not complete, show setup dialog after a short delay
                    if (finalAccount != null && !finalAccount.isBarangaySetupComplete()) {
                        // Use Timer with 1000ms delay to show setup dialog
                        Timer timer = new Timer(1000, e -> {
                            SwingUtilities.invokeLater(() -> {
                                BarangaySetupDialog setupDialog = new BarangaySetupDialog(barangay, finalAccount.getAccountId());
                                setupDialog.setVisible(true);
                            });
                        });
                        timer.setRepeats(false);
                        timer.start();
                    }
                },
                () -> router.navigate(WELCOME),
                () -> router.navigate(SIGNUP)
        );
        
        router.register(LOGIN, this.loginPanel);

        router.register(SIGNUP, new SignupPanel(
                auth,
                () -> router.navigate(LOGIN),
                () -> router.navigate(WELCOME),
                () -> router.navigate(LOGIN)
        ));

        router.navigate(WELCOME);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
