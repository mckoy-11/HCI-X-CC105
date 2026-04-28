package main.ui;

import java.awt.*;

import main.ui.admin_sections.HomePanel;
import main.ui.admin_sections.SchedulePanel;
import main.ui.admin_sections.BarangayPanel;
import main.ui.admin_sections.UsersPanel;
import main.ui.admin_sections.SettingsPanel;

import main.ui.components.Sidebar;

import javax.swing.*;
import main.ui.admin_sections.Management;

public final class Menro extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    
    private final String[] SIDEBARLABEL = { 
        "Home", "Schedule", "Management", 
        "Barangay", "Users", "Settings" 
    };

    private final String[] SIDEBARICONS = {
        "house-white.png", 
        "calendar-white.png", 
        "user-check-white.png", 
        "pin-house-white.png",
        "user-cog-white.png",
        "settings-white.png"
    };
    
    private final String[] SIDEBARICONHOVER = {
        "house.png",
        "calendar.png",
        "user-check.png",
        "pin-house.png",
        "user-cog.png",
        "settings.png"
    };

    private final AppRouter router;

    public Menro() {
        setTitle("WASTELY - MENRO");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setMinimumSize(new Dimension(1000, 600));

        router = new AppRouter(contentPanel, cardLayout);

        // Register screens
        router.register("Home",       new HomePanel());
        router.register("Schedule",   new SchedulePanel());
        router.register("Management", new Management());
        router.register("Barangay",   new BarangayPanel());
        router.register("Users",      new UsersPanel());
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
        SwingUtilities.invokeLater(Menro::new);
    }
}
