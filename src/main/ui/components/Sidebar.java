package main.ui.components;

import java.awt.*;
import java.awt.event.*;

import static main.ui.style.SystemStyle.*;

import javax.swing.*;
import main.app.MainFrame;
import main.app.WelcomePage;
import main.model.UserSession;
import main.ui.style.SystemStyle.RoundedPanel;

import main.app.AppRouter;

public class Sidebar extends JPanel implements ActionListener {

    private final AppRouter router;

    private JButton[] sidebarBtn;
    private JButton activeButton;

    public Sidebar(
            AppRouter router,
            String[] labels,
            String[] icons,
            String[] iconHover
    ) {
        this.router = router;

        setBackground(SIDEBAR);
        setPreferredSize(new Dimension(250, 100));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(null);

        add(createSystemName());
        add(separator(240));
        add(Box.createVerticalStrut(5));

        createMenu(labels, icons, iconHover);
    }

    private JLabel createSystemName() {
        JLabel name = new JLabel("WASTELY");
        
        name.setFont(new Font("Segoe UI", Font.BOLD, 30));
        name.setForeground(WHITE);
        name.setIconTextGap(10);
        name.setIcon(loadIcon("", 16));
        
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setHorizontalAlignment(SwingConstants.CENTER);
        
        name.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return name;
    }

    private void createMenu(String[] labels, String[] icons, String[] iconHover) {

        sidebarBtn = new JButton[labels.length];

        for (int i = 0; i < labels.length; i++) {
            CustomButton btn = createSidebarButton(labels[i], icons[i], iconHover[i]);

            btn.setActionCommand(labels[i]);
            btn.addActionListener(this);

            sidebarBtn[i] = btn;
            
            add(btn);
            add(Box.createVerticalStrut(5));
        }
        
        CustomButton logout = createSidebarButton("Logout", "settings-white.png", "settings.png");
        logout.addActionListener(event -> logout());
        add(Box.createVerticalGlue());
        add(logout);
        add(Box.createVerticalStrut(5));
        
        

        if (sidebarBtn.length > 0) {
            activeButton = sidebarBtn[0];
            activeButton.setBackground(ACTIVE);

            // initial route
            router.navigate(activeButton.getActionCommand());
        }
    }

    private CustomButton createSidebarButton(String text, String iconPath, String iconHover) {

        CustomButton btn = new CustomButton(
                text, 
                iconPath, iconHover, 20, 
                230, 45, 
                215, 45, 
                SIDEBAR, WHITE,
                WHITE, TEXTCOLOR, 
                true, true
        );

        btn.setForeground(WHITE);
        btn.setBackground(SIDEBAR);
        
        btn.setFocusable(true);
        btn.addActionListener(e -> {
            btn.requestFocusInWindow();
        });
        btn.setBorder(BorderFactory.createEmptyBorder(0, 35, 0, 15));

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(10);

        return btn;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton clicked = (JButton) e.getSource();

        for (JButton btn : sidebarBtn) {
            btn.setBackground(SIDEBAR);
        }

        clicked.setBackground(ACTIVE);
        activeButton = clicked;

        String route = clicked.getActionCommand();

        router.navigate(route);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        RoundedPanel.paintRounded(
                g,
                this,
                0, 20, 20, 0,
                2,
                0.2f,
                Color.BLACK,
                true,    // shadow right
                false,   // shadow bottom
                true     // blur enabled
        );
    }
}