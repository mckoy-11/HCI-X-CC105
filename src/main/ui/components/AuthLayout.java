package main.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import static main.style.SystemStyle.BACKGROUND;
import static main.style.SystemStyle.BGCOLOR1;
import static main.style.SystemStyle.BGCOLOR2;
import static main.style.SystemStyle.BODYPLAIN;
import static main.style.SystemStyle.FORM_WIDTH;
import static main.style.SystemStyle.INFO_GRADIENT_BOTTOM;
import static main.style.SystemStyle.INFO_GRADIENT_TOP;
import static main.style.SystemStyle.MUTED_TEXT;
import static main.style.SystemStyle.PRIMARY;
import static main.style.SystemStyle.TITLEBOLD;
import static main.style.SystemStyle.WHITE;
import static main.style.SystemStyle.createAuthCard;
import static main.style.SystemStyle.createTransparentPanel;

public class AuthLayout extends JPanel {

    private final JPanel formContent = createTransparentPanel();

    public AuthLayout(String tagLine, String headline, String description, String... highlights) {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        JPanel viewport = createTransparentPanel(new GridBagLayout());
        viewport.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        viewport.add(buildShell(tagLine, headline, description, highlights));

        add(viewport, BorderLayout.CENTER);
    }

    public JPanel getFormContent() {
        return formContent;
    }

    private JPanel buildShell(String tagLine, String headline, String description, String[] highlights) {
        JPanel shell = createTransparentPanel(new GridLayout(1, 2, 0, 0));
        shell.setPreferredSize(new Dimension(1040, 620));
        shell.setMaximumSize(new Dimension(1040, 620));

        shell.add(buildInfoPanel(tagLine, headline, description, highlights));
        shell.add(buildFormPanel());
        return shell;
    }

    private JPanel buildInfoPanel(String tagLine, String headline, String description, String[] highlights) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, INFO_GRADIENT_TOP, getWidth(), getHeight(), INFO_GRADIENT_BOTTOM);
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new java.awt.Color(255, 255, 255, 115));
                g2.fillOval(getWidth() - 240, -70, 260, 260);
                g2.setColor(new java.awt.Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 24));
                g2.fillOval(-120, getHeight() - 180, 260, 260);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(52, 52, 52, 52));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel(tagLine.toUpperCase());
        eyebrow.setFont(BODYPLAIN.deriveFont(12f));
        eyebrow.setForeground(PRIMARY);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brand = new JLabel("WASTELY");
        brand.setFont(TITLEBOLD.deriveFont(18f));
        brand.setForeground(new java.awt.Color(37, 43, 70));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(toHtml(headline, 360));
        title.setFont(TITLEBOLD.deriveFont(40f));
        title.setForeground(new java.awt.Color(27, 33, 56));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detail = new JLabel(toHtml(description, 340));
        detail.setFont(BODYPLAIN.deriveFont(14f));
        detail.setForeground(MUTED_TEXT);
        detail.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(brand);
        panel.add(Box.createVerticalStrut(56));
        panel.add(eyebrow);
        panel.add(Box.createVerticalStrut(18));
        panel.add(title);
        panel.add(Box.createVerticalStrut(16));
        panel.add(detail);
        panel.add(Box.createVerticalStrut(30));

        if (highlights != null) {
            for (String highlight : highlights) {
                panel.add(buildHighlight(highlight));
                panel.add(Box.createVerticalStrut(14));
            }
        }

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = createTransparentPanel(new GridBagLayout());
        wrapper.setBackground(WHITE);
        wrapper.setOpaque(true);

        JPanel card = createAuthCard();
        card.setPreferredSize(new Dimension(440, 520));

        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContent.setMaximumSize(new Dimension(FORM_WIDTH, Integer.MAX_VALUE));

        card.add(formContent);
        wrapper.add(card, centeredConstraints());
        return wrapper;
    }

    private GridBagConstraints centeredConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        return gbc;
    }

    private JPanel buildHighlight(String text) {
        JPanel row = createTransparentPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel marker = new JPanel();
        marker.setOpaque(true);
        marker.setBackground(BGCOLOR1);
        marker.setMaximumSize(new Dimension(10, 10));
        marker.setPreferredSize(new Dimension(10, 10));
        marker.setMinimumSize(new Dimension(10, 10));
        marker.setBorder(BorderFactory.createLineBorder(BGCOLOR2, 2, true));

        JLabel label = new JLabel(toHtml(text, 300));
        label.setFont(BODYPLAIN.deriveFont(14f));
        label.setForeground(new java.awt.Color(59, 67, 94));
        label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        row.add(marker);
        row.add(label);
        return row;
    }

    private String toHtml(String text, int width) {
        return "<html><div style='width:" + width + "px;'>" + text + "</div></html>";
    }
}
