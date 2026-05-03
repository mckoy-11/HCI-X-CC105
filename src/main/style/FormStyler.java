package main.style;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * FormStyler - Modern Figma-style form design utilities
 * Provides reusable components for clean, modern UI
 */
public final class FormStyler {

    private FormStyler() {}

    // ===================================
    // MODERN INPUT FIELDS
    // ===================================

    /**
     * Create a modern styled text field with padding and proper look
     */
    public static JTextField createModernTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(SystemStyle.BODYPLAIN);
        field.setBackground(SystemStyle.INPUT_BACKGROUND);
        field.setForeground(SystemStyle.textDark);
        field.setCaretColor(SystemStyle.PRIMARY);
        field.setBorder(createInputBorder());
        field.setPreferredSize(new Dimension(Integer.MAX_VALUE, SystemStyle.FORM_INPUT_HEIGHT));
        
        // Placeholder effect
        if (!placeholder.isEmpty()) {
            field.setText(placeholder);
            field.setForeground(SystemStyle.MUTED_TEXT);
            field.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (field.getText().equals(placeholder)) {
                        field.setText("");
                        field.setForeground(SystemStyle.textDark);
                    }
                }
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (field.getText().isEmpty()) {
                        field.setText(placeholder);
                        field.setForeground(SystemStyle.MUTED_TEXT);
                    }
                }
            });
        }
        
        return field;
    }

    /**
     * Create a modern styled text area with padding and proper look
     */
    public static JTextArea createModernTextArea(int rows, String placeholder) {
        JTextArea area = new JTextArea(rows, 20);
        area.setFont(SystemStyle.BODYPLAIN);
        area.setBackground(SystemStyle.INPUT_BACKGROUND);
        area.setForeground(SystemStyle.textDark);
        area.setCaretColor(SystemStyle.PRIMARY);
        area.setBorder(createInputBorder());
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(10, 10, 10, 10));
        
        return area;
    }

    /**
     * Create a modern styled combo box
     */
    public static <T> JComboBox<T> createModernComboBox() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(SystemStyle.BODYPLAIN);
        combo.setBackground(SystemStyle.INPUT_BACKGROUND);
        combo.setForeground(SystemStyle.textDark);
        combo.setBorder(createInputBorder());
        combo.setPreferredSize(new Dimension(Integer.MAX_VALUE, SystemStyle.FORM_INPUT_HEIGHT));
        
        return combo;
    }

    /**
     * Create a modern styled button (primary - green)
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(SystemStyle.BUTTONBOLD);
        button.setBackground(SystemStyle.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(createButtonBorder());
        button.setPreferredSize(new Dimension(120, SystemStyle.FORM_BUTTON_HEIGHT));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(40, 124, 39)); // Darker green
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(SystemStyle.PRIMARY);
            }
        });
        
        return button;
    }

    /**
     * Create a modern styled button (secondary - outline)
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(SystemStyle.BUTTONBOLD);
        button.setBackground(Color.WHITE);
        button.setForeground(SystemStyle.PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(SystemStyle.PRIMARY, 2));
        button.setPreferredSize(new Dimension(120, SystemStyle.FORM_BUTTON_HEIGHT));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(240, 245, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
        
        return button;
    }

    /**
     * Create a modern styled label
     */
    public static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SystemStyle.SUBTITLEPLAIN);
        label.setForeground(SystemStyle.textDark);
        label.setPreferredSize(new Dimension(Integer.MAX_VALUE, 20));
        
        return label;
    }

    /**
     * Create a modern styled card panel with shadow and rounded corners
     */
    public static JPanel createModernCard(Color backgroundColor) {
        JPanel card = new JPanel();
        card.setBackground(backgroundColor);
        card.setBorder(createCardBorder());
        card.setPreferredSize(new Dimension(480, Integer.MAX_VALUE));
        
        return card;
    }

    // ===================================
    // BORDERS & STYLING
    // ===================================

    private static Border createInputBorder() {
        // Light border with some padding
        Border line = BorderFactory.createLineBorder(SystemStyle.INPUT_BORDER, 1);
        Border empty = new EmptyBorder(0, 12, 0, 12);
        return BorderFactory.createCompoundBorder(line, empty);
    }

    private static Border createButtonBorder() {
        return new EmptyBorder(8, 16, 8, 16);
    }

    private static Border createCardBorder() {
        Border line = BorderFactory.createLineBorder(SystemStyle.CARD_BORDER, 1);
        return line;
    }

    /**
     * Create a modern styled scroll pane with custom scrollbar
     */
    public static JScrollPane createModernScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // Customize scrollbar
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUI(new ModernScrollBarUI());
        
        return scrollPane;
    }

    /**
     * Custom ScrollBar UI for modern look
     */
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        private static final int THUMB_SIZE = 8;
        
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = SystemStyle.PRIMARY;
            this.thumbDarkShadowColor = SystemStyle.PRIMARY;
            this.thumbHighlightColor = SystemStyle.PRIMARY;
            this.thumbLightShadowColor = SystemStyle.PRIMARY;
            this.trackColor = new Color(240, 240, 240);
            this.trackHighlightColor = new Color(240, 240, 240);
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }
    }

    // ===================================
    // FORM LAYOUTS
    // ===================================

    /**
     * Create a horizontal form row with two fields
     */
    public static JPanel createFormRow(String label1, JComponent field1, 
                                       String label2, JComponent field2) {
        JPanel row = new JPanel(new GridLayout(1, 2, SystemStyle.FORM_SPLIT_GAP, 0));
        row.setOpaque(false);
        
        JPanel col1 = new JPanel();
        col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
        col1.setOpaque(false);
        col1.add(createFormLabel(label1));
        col1.add(Box.createVerticalStrut(SystemStyle.FORM_LABEL_FIELD_GAP));
        col1.add(field1);
        
        JPanel col2 = new JPanel();
        col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
        col2.setOpaque(false);
        col2.add(createFormLabel(label2));
        col2.add(Box.createVerticalStrut(SystemStyle.FORM_LABEL_FIELD_GAP));
        col2.add(field2);
        
        row.add(col1);
        row.add(col2);
        
        return row;
    }

    /**
     * Create a form section with title and fields
     */
    public static JPanel createFormSection(String title, JComponent... components) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        
        // Section title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SystemStyle.SUBTITLEBOLD);
        titleLabel.setForeground(SystemStyle.textDark);
        section.add(titleLabel);
        section.add(Box.createVerticalStrut(SystemStyle.FORM_SECTION_SPACING));
        
        // Components
        for (int i = 0; i < components.length; i++) {
            section.add(components[i]);
            if (i < components.length - 1) {
                section.add(Box.createVerticalStrut(SystemStyle.FORM_FIELD_SPACING));
            }
        }
        
        return section;
    }

    /**
     * Apply modern styling to an existing JTextField (used for compatibility)
     */
    public static void styleTextField(JTextField field) {
        field.setFont(SystemStyle.BODYPLAIN);
        field.setBackground(SystemStyle.INPUT_BACKGROUND);
        field.setForeground(SystemStyle.textDark);
        field.setCaretColor(SystemStyle.PRIMARY);
        field.setBorder(createInputBorder());
    }

    /**
     * Apply modern styling to an existing JComboBox (used for compatibility)
     */
    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(SystemStyle.BODYPLAIN);
        combo.setBackground(SystemStyle.INPUT_BACKGROUND);
        combo.setForeground(SystemStyle.textDark);
        combo.setBorder(createInputBorder());
    }
}
