package main.app;

import main.dao.AccountDao;
import main.database.SQLConnection;
import main.service.AuthService;
import main.ui.AppRouter;
import main.ui.Barangay;
import main.ui.Menro;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class MainFrame extends JFrame {

    private static final String WELCOME = "welcome";
    private static final String LOGIN = "login";
    private static final String SIGNUP = "signup";

    private final CardLayout layout = new CardLayout();
    private final JPanel root = new JPanel(layout);
    private final AppRouter router = new AppRouter(root, layout);

    private AuthService auth;

    public MainFrame() {
        auth = initAuth();
        if (auth == null) {
            dispose();
            return;
        }

        setupFrame();
        setupRoutes();

        setContentPane(root);
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
        setSize(1180, 760);
        setMinimumSize(new Dimension(1180, 760));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void setupRoutes() {
        router.register(WELCOME, new WelcomePage(
                () -> router.navigate(SIGNUP),
                () -> router.navigate(LOGIN)
        ));

        router.register(LOGIN, new LoginPanel(
                auth,
                () -> {
                    dispose();
                    new Menro();
                },
                () -> {
                    dispose();
                    new Barangay();
                },
                () -> router.navigate(WELCOME),
                () -> router.navigate(SIGNUP)
        ));

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
