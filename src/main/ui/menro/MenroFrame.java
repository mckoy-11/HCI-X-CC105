package main.ui.menro;

import main.app.AppRouter;
import java.awt.*;

import main.ui.menro.pages.HomePanel;
import main.ui.menro.pages.SchedulePanel;
import main.ui.menro.pages.BarangayPanel;
import main.ui.menro.pages.UsersPanel;

import main.ui.components.Sidebar;

import javax.swing.*;
import main.ui.menro.pages.Management;

public final class MenroFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    
    private final String[] SIDEBARLABEL = { 
        "Home", "Schedule", "Management", 
        "Barangay", "Users"
    };

    private final String[] SIDEBARICONS = {
        "house-white.png", 
        "calendar-white.png", 
        "user-check-white.png", 
        "pin-house-white.png",
        "user-cog-white.png",
    };
    
    private final String[] SIDEBARICONHOVER = {
        "house.png",
        "calendar.png",
        "user-check.png",
        "pin-house.png",
        "user-cog.png",
    };

    private final AppRouter router;

    public MenroFrame() {
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
        SwingUtilities.invokeLater(MenroFrame::new);
    }
}
