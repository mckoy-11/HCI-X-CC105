package main.ui.components;

import java.awt.*;
import javax.swing.*;

/**
 * WrapLayout - A responsive layout manager that wraps components to the next line
 * when they exceed the available width. Used for non-scrollable grids.
 * 
 * This is similar to FlowLayout but automatically wraps to create
 * a responsive grid without requiring scrollbars.
 */
public class WrapLayout extends FlowLayout {

    // Cached dimensions for layout calculations
    private int cachedWidth = 0;
    private int cachedHeight = 0;

    /**
     * Creates a WrapLayout with default settings.
     */
    public WrapLayout() {
        super(LEFT, 5, 5);
    }

    /**
     * Creates a WrapLayout with the specified hgap and vgap.
     *
     * @param hgap the horizontal gap
     * @param vgap the vertical gap
     */
    public WrapLayout(int hgap, int vgap) {
        super(LEFT, hgap, vgap);
    }

    /**
     * Creates a WrapLayout with the specified alignment, hgap and vgap.
     *
     * @param align the alignment
     * @param hgap the horizontal gap
     * @param vgap the vertical gap
     */
    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return calculateLayoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return calculateLayoutSize(target, false);
    }

    @Override
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int availableWidth = target.getWidth() - insets.left - insets.right - getHgap() * 2;
            
            if (availableWidth <= 0) {
                availableWidth = 1;
            }

            int x = insets.left + getHgap();
            int y = insets.top + getVgap();
            int rowHeight = 0;
            int maxY = y;

            int ncomponents = target.getComponentCount();
            int i = 0;

            while (i < ncomponents) {
                Component comp = target.getComponent(i);

                if (!comp.isVisible()) {
                    i++;
                    continue;
                }

                Dimension d = comp.getPreferredSize();
                int compWidth = d.width;
                int compHeight = d.height;

                // Check if we need to wrap to next row
                if (x + compWidth > insets.left + getHgap() + availableWidth && x > insets.left + getHgap()) {
                    // Wrap to next row
                    x = insets.left + getHgap();
                    y += rowHeight + getVgap();
                    maxY = y;
                    rowHeight = 0;
                }

                // Set bounds
                comp.setBounds(x, y, compWidth, compHeight);

                // Update row height
                rowHeight = Math.max(rowHeight, compHeight);

                // Move to next position
                x += compWidth + getHgap();
                maxY = Math.max(maxY, y + compHeight);

                i++;
            }

            // Store for reference
            this.cachedWidth = target.getWidth();
            this.cachedHeight = maxY + getVgap() + insets.bottom;
        }
    }

    private Dimension calculateLayoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int availableWidth = target.getWidth() - insets.left - insets.right - getHgap() * 2;

            if (availableWidth <= 0) {
                availableWidth = Integer.MAX_VALUE;
            }

            int x = insets.left + getHgap();
            int y = insets.top + getVgap();
            int rowHeight = 0;
            int maxWidth = 0;
            int maxHeight = 0;

            int ncomponents = target.getComponentCount();
            int i = 0;

            while (i < ncomponents) {
                Component comp = target.getComponent(i);

                if (!comp.isVisible()) {
                    i++;
                    continue;
                }

                Dimension d = preferred ? comp.getPreferredSize() : comp.getMinimumSize();
                
                // Use minimum size if preferred not set
                if (d == null || d.width == 0 || d.height == 0) {
                    d = comp.getMinimumSize();
                }
                if (d == null || d.width == 0 || d.height == 0) {
                    d = new Dimension(100, 50);
                }

                int compWidth = d.width;
                int compHeight = d.height;

                // Check if we need to wrap
                if (x + compWidth > availableWidth && x > insets.left + getHgap()) {
                    x = insets.left + getHgap();
                    y += rowHeight + getVgap();
                    rowHeight = 0;
                }

                x += compWidth + getHgap();
                rowHeight = Math.max(rowHeight, compHeight);
                maxWidth = Math.max(maxWidth, x);
                maxHeight = y + compHeight;

                i++;
            }

            return new Dimension(
                maxWidth + getHgap() + insets.right,
                maxHeight + getVgap() + insets.bottom
            );
        }
    }

    /**
     * Creates a panel with WrapLayout installed.
     *
     * @param hgap horizontal gap between components
     * @param vgap vertical gap between components
     * @return a JPanel with WrapLayout
     */
    public static JPanel createWrapPanel(int hgap, int vgap) {
        JPanel panel = new JPanel(new WrapLayout(FlowLayout.LEFT, hgap, vgap));
        panel.setOpaque(false);
        return panel;
    }

/**
     * Creates a panel with WrapLayout installed with default spacing.
     *
     * @return a JPanel with WrapLayout
     */
    public static JPanel createWrapPanel() {
        return createWrapPanel(10, 10);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[hgap=" + getHgap() + ",vgap=" + getVgap() + ",align=" + getAlignment() + "]";
    }
}
