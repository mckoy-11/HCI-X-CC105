package main.app;

import main.dao.AccountDao;
import main.database.DbSchemaHelper;
import main.database.SQLConnection;
import main.model.Account;
import main.service.AuthService;
import main.ui.barangay.BarangayFrame;
import main.ui.menro.MenroFrame;
import main.ui.menro.dialogs.BarangaySetupDialog;

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
        System.out.println("[MainFrame] Constructor started");
        try {
            auth = initAuth();
            if (auth == null) {
                System.err.println("[MainFrame] Auth is null, disposing frame");
                dispose();
                return;
            }

            System.out.println("[MainFrame] Auth initialized, setting up frame");
            setupFrame();
            System.out.println("[MainFrame] Frame setup complete");
            
            setupRoutes();
            System.out.println("[MainFrame] Routes setup complete");
            
            setLayout(new BorderLayout());
            add(root, BorderLayout.CENTER);
            System.out.println("[MainFrame] Layout complete, making visible");
            
            setVisible(true);
            System.out.println("[MainFrame] Frame is now visible");
        } catch (Exception e) {
            System.err.println("[MainFrame] Exception in constructor:");
            e.printStackTrace();
            dispose();
            throw new RuntimeException("Failed to initialize MainFrame", e);
        }
    }

    private AuthService initAuth() {
        try {
            System.out.println("Connecting to database...");
            Connection conn = SQLConnection.getConnection();
            System.out.println("Database connection successful");
            
            // Migrate database schema if needed
            System.out.println("Starting schema migration...");
            DbSchemaHelper.migrateToNormalizedSchema(conn);
            System.out.println("Schema migration completed");
            
            System.out.println("Initializing AuthService...");
            return new AuthService(new AccountDao(conn));
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to connect to the database.\n\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Unexpected error during initialization:\n\n" + e.getMessage(),
                    "Error",
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
        try {
            System.out.println("[setupRoutes] Creating WelcomePage...");
            router.register(WELCOME, new WelcomePage(
                    () -> router.navigate(SIGNUP),
                    () -> router.navigate(LOGIN)
            ));
            System.out.println("[setupRoutes] WelcomePage registered");

            // Initialize loginPanel first
            this.loginPanel = null;
            
            System.out.println("[setupRoutes] Creating LoginPanel...");
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
            System.out.println("[setupRoutes] LoginPanel created");
            
            router.register(LOGIN, this.loginPanel);
            System.out.println("[setupRoutes] LoginPanel registered");

            System.out.println("[setupRoutes] Creating SignupPanel...");
            router.register(SIGNUP, new SignupPanel(
                    auth,
                    () -> router.navigate(LOGIN),
                    () -> router.navigate(WELCOME),
                    () -> router.navigate(LOGIN)
            ));
            System.out.println("[setupRoutes] SignupPanel registered");

            router.navigate(WELCOME);
            System.out.println("[setupRoutes] Navigated to WELCOME - setup complete");
        } catch (Exception e) {
            System.err.println("[setupRoutes] FATAL: Exception during route setup:");
            e.printStackTrace();
            throw new RuntimeException("Failed to setup routes", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== WCMS Application Starting ===");
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Creating MainFrame...");
                new MainFrame();
                System.out.println("MainFrame created successfully");
            } catch (Exception e) {
                System.err.println("FATAL ERROR: Exception in main thread:");
                e.printStackTrace();
                
                // Show error dialog
                JOptionPane.showMessageDialog(
                        null,
                        "Critical Error During Startup:\n\n" + e.getClass().getName() + ": " + e.getMessage(),
                        "Application Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}
