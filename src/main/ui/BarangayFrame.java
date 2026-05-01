package main.ui;

import java.awt.*;

import main.ui.officials_pages.HomePanel;
import main.ui.officials_pages.ReportsPanel;
import main.ui.officials_pages.SettingsPanel;

import main.ui.components.Sidebar;
import javax.swing.*;

public final class BarangayFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    
    private final String[] SIDEBARLABEL = { 
        "Home", "Reports", "Settings" 
    };

    private final String[] SIDEBARICONS = {
        "house-white.png", 
        "file-chart-column.png",
        "settings-white.png"
    };
    
    private final String[] SIDEBARICONHOVER = {
        "house.png",
        "file-chart-column.png",
        "settings.png"
    };

    private final AppRouter router;

    public BarangayFrame() {
        setTitle("WASTELY - Barangay");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setMinimumSize(new Dimension(1000, 600));

        router = new AppRouter(contentPanel, cardLayout);

        // Register screens
        router.register("Home",       new HomePanel());
        router.register("Reports",    new ReportsPanel());
        router.register("Settings",   new SettingsPanel());

        Sidebar sidebar = new Sidebar(
            router,
            SIDEBARLABEL,
            SIDEBARICONS,
            SIDEBARICONHOVER
        );
        
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(Color.WHITE);
        background.add(sidebar, BorderLayout.WEST);
        background.add(contentPanel, BorderLayout.CENTER);
        add(background);
        
        router.navigate("Home");
        
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BarangayFrame::new);
    }
}
