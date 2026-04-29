package main.ui.components;

import main.ui.dialogs.SearchDialog;

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

    private SearchDialog searchDialog;

    public SearchBar(int radius, String placeholder) {
        this.placeholder = placeholder;

        setLayout(new BorderLayout(10, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        field = new JTextField();
        field.setOpaque(false);
        field.setBorder(null);
        field.setBackground(new Color(0, 0, 0, 0));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        icon = new JLabel(loadIcon("search.png", 18));
        icon.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        
        clearBtn = new JLabel(loadIcon("close.png", 14));
        clearBtn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.setVisible(false);

        initListeners();

        add(icon, BorderLayout.WEST);
        add(field, BorderLayout.CENTER);
        add(clearBtn, BorderLayout.EAST);

        installGlobalShortcut();
        installClickOutsideToUnfocus(this);
    }

    public void attachSearchDialog(SearchDialog dialog) {
        this.searchDialog = dialog;
    }

    private void initListeners() {

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        field.addActionListener(e -> {
            if (searchDialog != null) {
                searchDialog.openFromEnter(getText());
            }
        });

        clearBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                field.setText("");
                update();
                field.requestFocusInWindow();
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(248, 248, 248));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(new Color(255, 255, 255));
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

    public void onSearch(ActionListener action) {
        field.addActionListener(e -> action.actionPerformed(e));
    }

    private void installGlobalShortcut() {

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {

                    if (e.getID() != KeyEvent.KEY_PRESSED) return false;

                    // "/" key focus search
                    if (e.getKeyChar() == '/' && !field.hasFocus()) {
                        SwingUtilities.invokeLater(() -> {
                            field.requestFocusInWindow();
                            field.setText("");
                        });
                        return true;
                    }

                    // ENTER triggers search dialog update
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && field.hasFocus()) {
                        if (searchDialog != null) {
                            searchDialog.updateQuery(getText());
                        }
                    }

                    return false;
                });
    }

    private void update() {
        clearBtn.setVisible(!field.getText().isEmpty());

        if (searchDialog != null) {
            searchDialog.updateQuery(getText());
        }

        repaint();
    }

    public String getText() {
        return field.getText().trim();
    }

    public JTextField getField() {
        return field;
    }
    
    public void installClickOutsideToUnfocus(JComponent root) { 
        // Use AWTEventListener to catch mouse clicks globally 
        Toolkit.getDefaultToolkit().addAWTEventListener((AWTEvent event) -> {
            if (event instanceof MouseEvent && event.getID() == MouseEvent.MOUSE_PRESSED) { 
                
                // Only process if component is showing on screen 
                if (!root.isShowing()) return; MouseEvent me = (MouseEvent) event; 
                
                // Only process clicks that are within the root component's bounds 
                Point rootLoc = root.getLocationOnScreen(); 
                
                int rootX = rootLoc.x; 
                int rootY = rootLoc.y; 
                
                int rootW = root.getWidth(); 
                int rootH = root.getHeight(); 
                int clickX = me.getXOnScreen(); 
                int clickY = me.getYOnScreen(); 
                
                // Check if click is outside the search bar bounds 
                boolean outside = clickX < rootX || 
                                  clickX > rootX + rootW || 
                                  clickY < rootY || 
                                  clickY > rootY + rootH; 
                if (outside) {KeyboardFocusManager
                    .getCurrentKeyboardFocusManager().clearGlobalFocusOwner(); 
                } 
            } 
        }, AWTEvent.MOUSE_EVENT_MASK); 
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // background (flat)
        g2.setColor(new Color(255, 255, 255));
        g2.fillRoundRect(0, 0, w, h, 35, 35);

        // subtle border
        g2.setColor(new Color(225, 225, 225));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 35, 35);
        
        g2.dispose();
        super.paintComponent(g);
    }
}