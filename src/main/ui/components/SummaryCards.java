package main.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import static main.ui.style.SystemStyle.*;

public class SummaryCards extends JPanel {

    private String[] titles;
    private int[] values;
    private String[] details;
    private String[] iconPaths;
    private Color[] iconBgColors;
    private String[] subLabels; // Optional subtitle like "Poblacion"

    public SummaryCards(String[] titles, int[] values, String[] details, String[] iconPaths, Color[] iconBgColors) {
        this(titles, values, details, iconPaths, iconBgColors, null);
    }

    public SummaryCards(String[] titles, int[] values, String[] details, String[] iconPaths, Color[] iconBgColors, String[] subLabels) {
        this.titles = titles;
        this.values = values;
        this.details = details;
        this.iconPaths = iconPaths;
        this.iconBgColors = iconBgColors;
        this.subLabels = subLabels;
        
        setOpaque(false);
        setLayout(new GridLayout(1, titles.length, 15, 0));
        
        initializeCards();
    }

    private void initializeCards() {
        for (int i = 0; i < titles.length; i++) {
            add(createCard(i));
        }
    }

    private JPanel createCard(int index) {
        JPanel card = Card(12);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(5, 5, 10, 15));

        // Icon with rounded background
        JPanel iconContainer = createRoundedIcon(iconPaths[index], iconBgColors[index], 16);
        
        // Content with spacing
        String subLabel = (subLabels != null && index < subLabels.length) ? subLabels[index] : null;
        JPanel content = createCardContent(titles[index], values[index], details[index], subLabel);

        // Add spacing between icon and content
        JPanel wrapper = new JPanel(new BorderLayout(10, 0));
        wrapper.setOpaque(false);
        wrapper.add(iconContainer, BorderLayout.WEST);
        wrapper.add(content, BorderLayout.CENTER);

        card.add(wrapper, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRoundedIcon(String iconPath, Color bgColor, int radius) {
        return new JPanel(new BorderLayout()) {
            {
                setOpaque(false);
                setBorder(new EmptyBorder(8, 15, 8, 15));
                add(new JLabel(loadIcon(iconPath, 20)));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle background
                Shape shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius);
                g2.setColor(bgColor);
                g2.fill(shape);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    private JPanel createCardContent(String cardName, int dataValue, String detail, String subLabel) {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0, 5, 0, 0)); // Add left spacing between icon and text

        JLabel title = new JLabel(cardName);
        title.setFont(BODYBOLD);
        
        String dataText = subLabel != null ? dataValue + " " + subLabel : String.valueOf(dataValue);
        JLabel data = new JLabel(dataText);
        data.setFont(SUBTITLEBOLD);
        
        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(BODYPLAIN);

        content.add(title);
        content.add(data);
        content.add(detailLabel);

        return content;
    }

    public void updateValues(int[] newValues) {
        removeAll();
        this.values = newValues;
        initializeCards();
        revalidate();
        repaint();
    }
}