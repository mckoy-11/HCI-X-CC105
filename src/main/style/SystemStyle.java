package main.style;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import main.ui.components.CustomButton;

public final class SystemStyle {

    public static final Font TITLEBOLD = new Font("Inter", Font.BOLD, 26);
    public static final Font TITLEPLAIN = new Font("Inter", Font.PLAIN, 16);
    public static final Font SUBTITLEBOLD = new Font("Outfit", Font.BOLD, 20);
    public static final Font SUBTITLEPLAIN = new Font("Inter", Font.PLAIN, 16);
    public static final Font BODYBOLD = new Font("Outfit", Font.BOLD, 12);
    public static final Font BODYPLAIN = new Font("Inter", Font.PLAIN, 10);
    public static final Font BUTTONBOLD = new Font("Inter", Font.BOLD, 12);
    public static final Font BUTTONPLAIN = new Font("Inter", Font.PLAIN, 12);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font CELL_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    public static Color backgroundColor = new Color(246, 247, 251);
    public static Color cardBackground = Color.WHITE;
    public static Color PRIMARY = new Color(80, 99, 255);
    public static Color secondaryGreen = new Color(224, 229, 255);
    public static Color textDark = new Color(24, 28, 42);
    public static Color textMuted = new Color(111, 118, 144);
    public static Color shadowColor = new Color(20, 24, 44, 16);

    public static final Color SELECTED_ROW = new Color(220, 235, 255);
    public static final Color ODD_ROW = Color.WHITE;
    public static final Color EVEN_ROW = Color.WHITE;
    public static final Color BACKGROUND = Color.decode("#F6F7FB");
    public static final Color BGCOLOR1 = Color.decode("#D6F5CC");
    public static final Color BGCOLOR2 = Color.decode("#F0F7EE");
    public static final Color HEADER = Color.decode("#EEF2FF");
    public static final Color SIDEBAR = Color.decode("#81DB7A");
    public static final Color TEXTCOLOR = Color.decode("#287C27");
    public static final Color WHITE = Color.WHITE;
    public static final Color HOVERBTN = Color.decode("#81DB7A");
    public static final Color SELECTED = Color.decode("#287C27");
    public static final Color ACTIVE = Color.decode("#287C27");
    public static final Color CARD_BORDER = new Color(233, 236, 245);
    public static final Color INPUT_BORDER = new Color(225, 229, 240);
    public static final Color INPUT_FOCUS = new Color(226, 232, 255);
    public static final Color INPUT_BACKGROUND = new Color(244, 246, 251);
    public static final Color INPUT_BACKGROUND_FOCUS = new Color(238, 242, 255);
    public static final Color MUTED_TEXT = new Color(109, 116, 139);
    public static final Color INFO_GRADIENT_TOP = new Color(248, 249, 255);
    public static final Color INFO_GRADIENT_BOTTOM = new Color(236, 240, 255);
    public static final Color ERROR_TEXT = new Color(197, 62, 92);
    public static final Color SUCCESS_TEXT = new Color(46, 150, 97);

    public static final int FORM_WIDTH = 400;
    public static final int FIELD_HEIGHT = 56;
    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();

    private SystemStyle() {
    }

    public static ImageIcon loadIcon(String path, int size) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        ImageIcon icon = new ImageIcon("src/main/resources/icons/" + path);
        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            return null;
        }

        Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public static JSeparator separator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(240, 1));
        separator.setForeground(Color.LIGHT_GRAY);
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        return separator;
    }

    public static JButton PlainBtn(String text, String icon, int size) {
        JButton button = new JButton(text);
        button.setIcon(loadIcon(icon, size));
        button.setBackground(null);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        return button;
    }

    public static JPanel createTransparentPanel() {
        return createTransparentPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    }

    public static JPanel createTransparentPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel createAuthCard() {
        JPanel card = Card(30, 30, 30, 30, 2, 48, WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));
        card.setMaximumSize(new Dimension(460, Integer.MAX_VALUE));
        return card;
    }

    public static JLabel createCapsuleLabel(String text, Color background, Color foreground) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setFont(BUTTONBOLD.deriveFont(11f));
        label.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static JLabel createFormTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLEBOLD.deriveFont(32f));
        label.setForeground(textDark);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static JLabel createFormSubtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBTITLEPLAIN.deriveFont(14f));
        label.setForeground(MUTED_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BUTTONBOLD.deriveFont(12f));
        label.setForeground(textMuted);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static <T extends JTextComponent> T styleInput(T field) {
        field.setFont(SUBTITLEPLAIN.deriveFont(15f));
        field.setForeground(textDark);
        field.setCaretColor(PRIMARY);
        field.setBackground(INPUT_BACKGROUND);
        field.setBorder(createInputBorder());
        field.setMaximumSize(new Dimension(FORM_WIDTH, FIELD_HEIGHT));
        field.setPreferredSize(new Dimension(FORM_WIDTH, FIELD_HEIGHT));
        field.setMinimumSize(new Dimension(140, FIELD_HEIGHT));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        installTextFocusState(field);
        return field;
    }

    public static <T> JComboBox<T> styleComboBox(JComboBox<T> comboBox) {
        comboBox.setFont(SUBTITLEPLAIN.deriveFont(15f));
        comboBox.setForeground(textDark);
        comboBox.setBackground(INPUT_BACKGROUND);
        comboBox.setBorder(createInputBorder());
        comboBox.setMaximumSize(new Dimension(FORM_WIDTH, FIELD_HEIGHT));
        comboBox.setPreferredSize(new Dimension(FORM_WIDTH, FIELD_HEIGHT));
        comboBox.setMinimumSize(new Dimension(140, FIELD_HEIGHT));
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboBox.setFocusable(true);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return label;
            }
        });
        comboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent event) {
                comboBox.setBackground(INPUT_BACKGROUND_FOCUS);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent event) {
                comboBox.setBackground(INPUT_BACKGROUND);
            }
        });
        return comboBox;
    }

    public static JPanel createFieldGroup(String labelText, JComponent input) {
        JPanel group = createTransparentPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(FORM_WIDTH, 88));
        group.add(createFieldLabel(labelText));
        group.add(Box.createVerticalStrut(6));
        group.add(input);
        return group;
    }

    public static JPanel createSplitRow(JComponent left, JComponent right) {
        JPanel row = createTransparentPanel(new GridLayout(1, 2, 14, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(FORM_WIDTH, 88));
        row.add(left);
        row.add(right);
        return row;
    }

    public static JButton createPrimaryButton(String text) {
        CustomButton button = new CustomButton(
                text, null, null, 0,
                FORM_WIDTH, 50, FORM_WIDTH, 50,
                PRIMARY, HOVERBTN,
                WHITE, WHITE,
                false, true
        );
        configureActionButton(button);
        return button;
    }

    public static JButton createSecondaryButton(String text) {
        CustomButton button = new CustomButton(
                text, null, null, 0,
                FORM_WIDTH, 50, FORM_WIDTH, 50,
                new Color(240, 243, 252), new Color(232, 236, 250),
                textDark, PRIMARY,
                false, true
        );
        configureActionButton(button);
        return button;
    }

    public static JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTONBOLD.deriveFont(13f));
        button.setForeground(PRIMARY);
        button.setBorder(EMPTY_BORDER);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }

    public static JLabel createStatusLabel() {
        JLabel label = new JLabel();
        label.setFont(BUTTONPLAIN.deriveFont(13f));
        label.setForeground(ERROR_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setVisible(false);
        return label;
    }

    public static void showError(JLabel label, String message) {
        showStatus(label, message, ERROR_TEXT);
    }

    public static void showSuccess(JLabel label, String message) {
        showStatus(label, message, SUCCESS_TEXT);
    }

    public static void clearStatus(JLabel label) {
        label.setText("");
        label.setVisible(false);
    }

    public static JPanel Card() {
        return Card(25, 25, 25, 25, 2, 100, WHITE);
    }

    public static JPanel Card(int radius) {
        return Card(radius, radius, radius, radius, 2, 20, WHITE);
    }

    public static JPanel Card(int radius, Color bg) {
        return Card(radius, radius, radius, radius, 2, 20, bg);
    }

    public static JPanel Card(int top, int bottom, Color bg) {
        return Card(top, top, bottom, bottom, 0, 0, bg);
    }

    public static JPanel Card(
            int topLeft,
            int topRight,
            int bottomRight,
            int bottomLeft,
            int shadowSize,
            int shadowOpacity,
            Color background
    ) {
        return new RoundedCardPanel(topLeft, topRight, bottomRight, bottomLeft, shadowSize, shadowOpacity, background);
    }

    public static JPanel roundPanel(int radius) {
        return GradientPaint(WHITE, WHITE, radius, false, 0);
    }

    public static JPanel GradientPaint(Color start, Color end, int radius, boolean shadow, int margin) {
        return new GradientPanel(start, end, radius, shadow, margin);
    }

    public static void addField(JPanel panel, GridBagConstraints constraints, int row, String label, JComponent input) {
        constraints.gridx = 0;
        constraints.gridy = row;

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(TEXTCOLOR);
        fieldLabel.setFont(BODYPLAIN);

        wrapper.add(fieldLabel, BorderLayout.NORTH);
        wrapper.add(input, BorderLayout.CENTER);
        panel.add(wrapper, constraints);
    }

    public static final class RoundedPanel {

        private RoundedPanel() {
        }

        public static void paintRounded(
                Graphics graphics,
                JComponent component,
                int topLeft,
                int topRight,
                int bottomRight,
                int bottomLeft,
                int shadowSize,
                float shadowOpacity,
                Color shadowColor,
                boolean shadowRight,
                boolean shadowBottom,
                boolean useBlur
        ) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = component.getWidth();
            int height = component.getHeight();
            int offsetX = shadowRight ? shadowSize : 0;
            int offsetY = shadowBottom ? shadowSize : 0;
            int contentWidth = width - offsetX;
            int contentHeight = height - offsetY;

            if (shadowSize > 0) {
                paintShadow(g2, contentWidth, contentHeight, topLeft, topRight, bottomRight, bottomLeft,
                        shadowSize, shadowOpacity, shadowColor, shadowRight, shadowBottom, useBlur);
            }

            g2.setColor(component.getBackground());
            g2.fill(createRoundedShape(contentWidth, contentHeight, topLeft, topRight, bottomRight, bottomLeft));
            g2.dispose();
        }

        public static Insets createShadowInsets(int shadowSize, boolean right, boolean bottom) {
            return new Insets(0, 0, bottom ? shadowSize : 0, right ? shadowSize : 0);
        }
    }

    public static abstract class BaseDialog extends javax.swing.JWindow {

        protected BufferedImage createBlurredBackground(Frame parent) {
            try {
                if (parent == null) {
                    return createFallbackBackground();
                }
                Rectangle bounds = parent.getBounds();
                BufferedImage capture = new Robot().createScreenCapture(bounds);
                return blurImage(capture, 1);
            } catch (AWTException e) {
                return createFallbackBackground();
            }
        }

        public class BackgroundPanel extends JPanel {
            private final Image image;

            public BackgroundPanel(Image image) {
                this.image = image;
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                graphics.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }

    private static void showStatus(JLabel label, String message, Color color) {
        if (message == null || message.trim().isEmpty()) {
            clearStatus(label);
            return;
        }
        label.setForeground(color);
        label.setText(message);
        label.setVisible(true);
    }

    private static void configureActionButton(AbstractButton button) {
        button.setFont(BUTTONBOLD.deriveFont(15f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(FORM_WIDTH, 50));
        button.setPreferredSize(new Dimension(FORM_WIDTH, 50));
        button.setMinimumSize(new Dimension(FORM_WIDTH, 50));
    }

    private static void installTextFocusState(final JTextComponent field) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent event) {
                field.setBackground(INPUT_BACKGROUND_FOCUS);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent event) {
                field.setBackground(INPUT_BACKGROUND);
            }
        });
    }

    private static Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        );
    }

    private static void paintShadow(
            Graphics2D g2,
            int width,
            int height,
            int topLeft,
            int topRight,
            int bottomRight,
            int bottomLeft,
            int shadowSize,
            float shadowOpacity,
            Color color,
            boolean shadowRight,
            boolean shadowBottom,
            boolean useBlur
    ) {
        if (useBlur) {
            BufferedImage shadow = createShadowImage(width, height, topLeft, topRight, bottomRight, bottomLeft,
                    shadowSize, color, shadowOpacity);
            int drawX = shadowRight ? 0 : shadowSize;
            int drawY = shadowBottom ? 0 : shadowSize;
            g2.drawImage(shadow, drawX, drawY, null);
            return;
        }

        for (int i = shadowSize; i > 0; i--) {
            float alpha = shadowOpacity * (i / (float) shadowSize);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(255 * alpha)));
            Shape shape = createRoundedShape(width, height, topLeft, topRight, bottomRight, bottomLeft);
            int dx = shadowRight ? i : -i;
            int dy = shadowBottom ? i : -i;
            g2.translate(dx, dy);
            g2.fill(shape);
            g2.translate(-dx, -dy);
        }
    }

    private static BufferedImage createShadowImage(
            int width,
            int height,
            int topLeft,
            int topRight,
            int bottomRight,
            int bottomLeft,
            int shadowSize,
            Color color,
            float opacity
    ) {
        BufferedImage image = new BufferedImage(width + shadowSize * 2, height + shadowSize * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(255 * opacity)));
        g2.translate(shadowSize, shadowSize);
        g2.fill(createRoundedShape(width, height, topLeft, topRight, bottomRight, bottomLeft));
        g2.dispose();
        return blurImage(image, shadowSize);
    }

    private static BufferedImage blurImage(BufferedImage image, int radius) {
        if (radius <= 0) {
            return image;
        }

        int size = radius * 2 + 1;
        float[] data = new float[size * size];
        float value = 1f / data.length;
        for (int i = 0; i < data.length; i++) {
            data[i] = value;
        }

        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp blur = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return blur.filter(image, null);
    }

    private static Shape createRoundedShape(int width, int height, int topLeft, int topRight, int bottomRight, int bottomLeft) {
        Path2D path = new Path2D.Double();
        path.moveTo(topLeft, 0);
        path.lineTo(width - topRight, 0);
        path.quadTo(width, 0, width, topRight);
        path.lineTo(width, height - bottomRight);
        path.quadTo(width, height, width - bottomRight, height);
        path.lineTo(bottomLeft, height);
        path.quadTo(0, height, 0, height - bottomLeft);
        path.lineTo(0, topLeft);
        path.quadTo(0, 0, topLeft, 0);
        path.closePath();
        return path;
    }

    private static BufferedImage createFallbackBackground() {
        BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(new Color(40, 40, 40));
        g2.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2.dispose();
        return image;
    }

    private static final class RoundedCardPanel extends JPanel {
        private final int topLeft;
        private final int topRight;
        private final int bottomRight;
        private final int bottomLeft;
        private final int shadowSize;
        private final int shadowOpacity;
        private BufferedImage shadowCache;
        private int cachedWidth = -1;
        private int cachedHeight = -1;

        private RoundedCardPanel(
                int topLeft,
                int topRight,
                int bottomRight,
                int bottomLeft,
                int shadowSize,
                int shadowOpacity,
                Color background
        ) {
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomRight = bottomRight;
            this.bottomLeft = bottomLeft;
            this.shadowSize = shadowSize;
            this.shadowOpacity = shadowOpacity;
            setOpaque(false);
            setBackground(background);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            ensureShadowCache(width, height);

            if (shadowCache != null) {
                g2.drawImage(shadowCache, shadowSize, shadowSize, null);
            }

            g2.setColor(getBackground());
            g2.fill(createRoundedShape(
                    width - shadowSize * 2,
                    height - shadowSize * 2,
                    topLeft,
                    topRight,
                    bottomRight,
                    bottomLeft
            ));
            g2.dispose();
            super.paintComponent(graphics);
        }

        private void ensureShadowCache(int width, int height) {
            if (shadowSize <= 0) {
                shadowCache = null;
                return;
            }
            if (shadowCache != null && cachedWidth == width && cachedHeight == height) {
                return;
            }
            cachedWidth = width;
            cachedHeight = height;
            shadowCache = createShadowImage(
                    width - shadowSize * 2,
                    height - shadowSize * 2,
                    topLeft,
                    topRight,
                    bottomRight,
                    bottomLeft,
                    shadowSize,
                    Color.BLACK,
                    Math.min(1f, shadowOpacity / 255f)
            );
        }
    }

    private static final class GradientPanel extends JPanel {
        private final Color start;
        private final Color end;
        private final int radius;
        private final boolean shadow;

        private GradientPanel(Color start, Color end, int radius, boolean shadow, int margin) {
            this.start = start;
            this.end = end;
            this.radius = radius;
            this.shadow = shadow;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (shadow) {
                for (int i = 0; i < 8; i++) {
                    int alpha = Math.max(5, 20 - i * 2);
                    g2.setColor(new Color(0, 0, 0, alpha));
                    g2.fillRoundRect(i, i, width - i * 2, height - i * 2, radius, radius);
                }
            }

            GradientPaint gradient = new GradientPaint(0, 0, start, width, 0, end);
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, width - (shadow ? 6 : 0), height - (shadow ? 6 : 0), radius, radius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
