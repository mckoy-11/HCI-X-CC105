package main.ui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static main.style.SystemStyle.loadIcon;

public final class SearchBar extends JPanel {

    private final JTextField field;
    private final JLabel clearBtn;
    private final JLabel icon;

    private final String placeholder;

    private boolean showClearButton = false;

    public SearchBar(int radius, String placeholder) {
        this.placeholder = placeholder;

        setLayout(new BorderLayout(10, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        field = new JTextField();
        field.setBorder(null);
        field.setOpaque(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.BLACK);

        icon = new JLabel(loadIcon("search.png", 18));

        clearBtn = new JLabel(loadIcon("close.png", 14));
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.setVisible(false);
        clearBtn.setFocusable(false);

        initListeners();

        add(icon, BorderLayout.WEST);
        add(field, BorderLayout.CENTER);
        add(clearBtn, BorderLayout.EAST);
        
        installClickOutsideToUnfocus(this);
    }

    // =========================
    // Initialization
    // =========================
    private void initListeners() {

        // typing updates
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateState(); }
            @Override public void removeUpdate(DocumentEvent e) { updateState(); }
            @Override public void changedUpdate(DocumentEvent e) { updateState(); }
        });

        // focus repaint (for placeholder rendering)
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> repaint());
            }
        });

        // clear button
        clearBtn.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                field.setText("");
                updateState();

                SwingUtilities.invokeLater(() -> {
                    field.requestFocusInWindow();
                });
            }
        });
    }

    // =========================
    // State
    // =========================
    private void updateState() {
        showClearButton = !field.getText().isEmpty();
        clearBtn.setVisible(showClearButton);
        repaint();
    }

    // =========================
    // Public API
    // =========================
    public String getText() {
        return field.getText().trim();
    }

    public void clear() {
        field.setText("");
        updateState();
    }

    public void onSearch(ActionListener action) {
        field.addActionListener(e -> {
            if (!getText().isEmpty()) {
                action.actionPerformed(e);
            }
        });
    }

    public void onType(Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { action.run(); }
            @Override public void removeUpdate(DocumentEvent e) { action.run(); }
            @Override public void changedUpdate(DocumentEvent e) { action.run(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int radius = 20;

        // shadow
        for (int i = 0; i < 6; i++) {
            g2.setColor(new Color(0, 0, 0, 15 - i * 2));
            g2.fillRoundRect(i, i, w - i * 2, h - i * 2, radius, radius);
        }
        
        // background
        g2.setColor(new Color(250, 250, 250));
        g2.fillRoundRect(0, 0, w - 4, h - 4, radius, radius);

        // placeholder (ONLY when empty)
        if (field.getText().isEmpty() && !field.hasFocus()) {
            g2.setColor(Color.GRAY);
            FontMetrics fm = g2.getFontMetrics(field.getFont());
            int x = icon.getWidth() + 20;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setFont(field.getFont());
            g2.drawString(placeholder, x, y);
        }

        g2.dispose();
        super.paintComponent(g);
    }
    
    public void installClickOutsideToUnfocus(JComponent root) {
        // Use AWTEventListener to catch mouse clicks globally
        Toolkit.getDefaultToolkit().addAWTEventListener((AWTEvent event) -> {
            if (event instanceof MouseEvent && event.getID() == MouseEvent.MOUSE_PRESSED) {
                // Only process if component is showing on screen
                if (!root.isShowing()) return;
                
                MouseEvent me = (MouseEvent) event;
                // Only process clicks that are within the root component's bounds
                Point rootLoc = root.getLocationOnScreen();
                int rootX = rootLoc.x;
                int rootY = rootLoc.y;
                int rootW = root.getWidth();
                int rootH = root.getHeight();
                
                int clickX = me.getXOnScreen();
                int clickY = me.getYOnScreen();
                
                // Check if click is outside the search bar bounds
                boolean outside = clickX < rootX || clickX > rootX + rootW 
                               || clickY < rootY || clickY > rootY + rootH;
                
                if (outside) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager()
                            .clearGlobalFocusOwner();
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }
}