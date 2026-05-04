package main.ui.barangay;

import main.app.AppRouter;
import java.awt.*;

import main.ui.barangay.pages.HomePanel;
import main.ui.barangay.pages.ReportsPanel;
import main.ui.barangay.pages.ComplaintPanel;
import main.ui.barangay.pages.RequestPanel;
import main.ui.barangay.pages.BarangayDetailsPanel;

import main.ui.components.Sidebar;
import javax.swing.*;

public final class BarangayFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    
    private final String[] SIDEBARLABEL = { 
        "Home", "Barangay", "Reports", "Complaint", "Request"
    };

    private final String[] SIDEBARICONS = {
        "house-white.png", 
        "pin-house-white.png",
        "file-chart-column.png",
        "file-chart-column.png",
        "file-chart-column.png",
    };
    
    private final String[] SIDEBARICONHOVER = {
        "house.png",
        "pin-house.png",
        "file-chart-column.png",
        "file-chart-column.png",
        "file-chart-column.png",
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
        router.register("Barangay",   new BarangayDetailsPanel());
        router.register("Reports",    new ReportsPanel());
        router.register("Complaint",  new ComplaintPanel());
        router.register("Request",    new RequestPanel());

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
