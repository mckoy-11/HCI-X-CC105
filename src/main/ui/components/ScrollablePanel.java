package main.ui.components;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * ScrollablePanel with ZERO borders (no EmptyBorder either).
 * Pure scroll container wrapper.
 */
public class ScrollablePanel extends JPanel {

    private final JScrollPane scrollPane;

    /**
     * Wraps any component inside a borderless JScrollPane.
     *
     * @param content component to scroll
     */
    public ScrollablePanel(JComponent content) {
        setLayout(new BorderLayout());
        setOpaque(false);

        scrollPane = new JScrollPane(content);

        // remove all borders completely
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);

        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Access underlying scroll pane if needed.
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }
}