package main.ui.components;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import static main.style.SystemStyle.*;

public final class CustomButton extends JButton {

    private static final List<CustomButton> GROUP = new ArrayList<>();

    private final int baseW, baseH;
    private final int hoverW, hoverH;

    private int currentW, currentH;
    private int targetW, targetH;

    private float anim = 0f;

    private final Color bgDefault, bgHover;
    private final Color fgDefault, fgHover;

    private final String iconDefault, iconHover;
    private final int iconSize;

    private final boolean selectable;
    private final boolean rounded;

    private boolean hovered = false;
    private boolean selected = false;

    private Timer timer;

    public static JButton createButton(
            String text,
            String icon,
            String iconHover,
            int size
    ) {
        return new CustomButton(
                text, icon, iconHover, size,
                100, 35, 100, 35,
                WHITE, HOVERBTN,
                TEXTCOLOR, WHITE,
                false,
                true
        );
    }

    public CustomButton(
            String text,
            String icon,
            String iconHover,
            int iconSize,
            int width,
            int height,
            int hoverWidth,
            int hoverHeight,
            Color bgDefault,
            Color bgHover,
            Color fgDefault,
            Color fgHover,
            boolean selectable,
            boolean rounded
    ) {
        this.iconDefault = icon;
        this.iconHover = iconHover;
        this.iconSize = iconSize;

        this.baseW = width;
        this.baseH = height;
        this.hoverW = hoverWidth;
        this.hoverH = hoverHeight;

        this.currentW = width;
        this.currentH = height;
        this.targetW = width;
        this.targetH = height;

        this.bgDefault = bgDefault;
        this.bgHover = bgHover;
        this.fgDefault = fgDefault;
        this.fgHover = fgHover;

        this.selectable = selectable;
        this.rounded = rounded;

        if (selectable) GROUP.add(this);

        setText(text);
        setFont(BUTTONPLAIN);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        setForeground(fgDefault);
        setHorizontalAlignment(SwingConstants.LEFT);
        setIconTextGap(10);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        Dimension d = new Dimension(width, height);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);

        setIcon(loadIcon(icon, iconSize));

        initHover();
        initClick();
    }

    private void initHover() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectable && selected) return;
                hovered = true;
                startAnimation();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectable && selected) return;
                hovered = false;
                startAnimation();
            }
        });
    }

    private void initClick() {
        addActionListener(e -> {
            if (!selectable) return;

            GROUP.stream().filter((b) -> (b != this)).map((b) -> {
                b.selected = false;
                return b;
            }).map((b) -> {
                b.hovered = false;
                return b;
            }).forEachOrdered((b) -> {
                b.startAnimation();
            });

            setSelectedInternal(true);
        });
    }

    private void setSelectedInternal(boolean state) {
        this.selected = state;
        this.hovered = state;
        startAnimation();
    }

    public void setSelectedState(boolean state) {
        if (!selectable) return;
        setSelectedInternal(state);
    }

    private void startAnimation() {
        if (timer != null && timer.isRunning()) timer.stop();

        timer = new Timer(12, e -> {
            float target = (hovered || selected) ? 1f : 0f;

            anim += (target - anim) * 0.35f;

            if (Math.abs(anim - target) < 0.01f) {
                anim = target;
                timer.stop();
            }

            applyVisualState();
            repaint();
        });
        timer.start();
    }
    
    private void applyVisualState() {
        boolean active = hovered || selected;

        setIcon(loadIcon(active ? iconHover : iconDefault, iconSize));

        setForeground(active ? fgHover : fgDefault);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        int arc = rounded ? 18 : 0;

        Color bg = blend(bgDefault, bgHover, anim);
        Color fg = blend(fgDefault, fgHover, anim);

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        setForeground(fg);

        super.paintComponent(g);

        g2.dispose();
    }

    private Color blend(Color c1, Color c2, float t) {
        t = Math.max(0f, Math.min(1f, t));

        return new Color(
                (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * t),
                (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t),
                (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t)
        );
    }
}