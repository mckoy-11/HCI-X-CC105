package main.ui;

import java.awt.CardLayout;
import javax.swing.JPanel;

/**
 * Central navigation controller.
 * Owns CardLayout and handles all screen switching.
 */
public class AppRouter {

    private final CardLayout layout;
    private final JPanel container;

    public AppRouter(JPanel container, CardLayout layout) {
        this.container = container;
        this.layout = layout;
    }

    public void register(String name, JPanel view) {
        container.add(view, name);
    }

    public void navigate(String route) {
        layout.show(container, route);
    }
}