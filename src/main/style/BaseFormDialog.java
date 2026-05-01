package main.style;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import static main.style.SystemStyle.*;

/**
 * BaseFormDialog - Modern form dialog using AuthLayout style
 * 
 * Style: White panel, 40px padding, GridBagLayout for form fields
 * Dimensions: Fixed width (480), auto height up to max (640)
 * 
 * Usage:
 * 1. Extend and call super(parent, title)
 * 2. Initialize your fields  
 * 3. Call initFormBody() to populate form
 * 4. Override saveForm()
 */
public abstract class BaseFormDialog extends JDialog {

    // Dimensions matching requirements
    public static final int FORM_WIDTH = 480;
    public static final int FORM_HEIGHT = 640;
    public static final int FORM_PADDING = 40;
    
    // Spacing
    public static final int ROW_SPACING = 14;
    public static final int COL_SPACING = 10;
    public static final int LABEL_FIELD_GAP = 6;
    
    // Button dimensions
    public static final int BUTTON_WIDTH = 100;
    public static final int BUTTON_HEIGHT = 42;

    protected JPanel formPanel;
    protected final JLabel statusLabel;
    private JPanel mainPanel;
    
    /**
     * Create rounded form panel with corner radius from SystemStyle
     */
    private JPanel createRoundedFormPanel() {
        return new JPanel(new BorderLayout()) {
            private final int radius = SystemStyle.FORM_CORNER_RADIUS;
            
            {
                setOpaque(false);
                setBackground(WHITE);
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(SystemStyle.createRoundedShape(
                        getWidth(), getHeight(), radius, radius, radius, radius));
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    public BaseFormDialog(Frame parent, String title) {
        super(parent, title, true);
        
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setModal(true);
        setResizable(false);
        
        // Root panel with dark overlay
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);
        
        // Main form panel with rounded corners (AuthLayout style)
        mainPanel = createRoundedFormPanel();
        mainPanel.setBorder(new EmptyBorder(FORM_PADDING, FORM_PADDING, FORM_PADDING, FORM_PADDING));
        
        // Header
        JPanel headerPanel = createHeader();
        
        // Empty form body - will be populated by initFormBody()
        formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        
        // Actions and status
        JPanel actionsPanel = createActions();
        statusLabel = SystemStyle.createStatusLabel();
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new EmptyBorder(ROW_SPACING, 0, 0, 0));
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        
        // Bottom section
        JPanel bottomSection = new JPanel(new BorderLayout(0, ROW_SPACING));
        bottomSection.setOpaque(false);
        bottomSection.add(statusPanel, BorderLayout.CENTER);
        bottomSection.add(actionsPanel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(bottomSection, BorderLayout.SOUTH);
        
root.add(mainPanel, BorderLayout.CENTER);
        add(root);
        
        // Defer sizing until after initFormBody() is called
        // Don't pack here - wait until form body is populated
        
        enableDragSupport(mainPanel);
    }
    
    /**
     * Initialize form body with fields - call after super() in subclass constructor
     * Creates the form panel with fields added by createFormBody()
     */
    protected void initFormBody() {
        // Get custom form body from subclass
        JPanel customBody = createFormBody();
        
        // Replace the empty form panel with custom body
        if (customBody != null && customBody != formPanel) {
            mainPanel.remove(formPanel);
            formPanel = customBody;
            mainPanel.add(formPanel, BorderLayout.CENTER);
            
            // Need to re-find the bottom section and add it properly
            Component[] comps = mainPanel.getComponents();
            JPanel bottomSection = null;
            for (Component comp : comps) {
                if (comp instanceof JPanel && comp != formPanel) {
                    bottomSection = (JPanel) comp;
                }
            }
            if (bottomSection != null) {
                mainPanel.remove(bottomSection);
                mainPanel.add(bottomSection, BorderLayout.SOUTH);
            }
        }
        
        // Now do all sizing after form body is populated
        pack();
        setMinimumSize(new Dimension(FORM_WIDTH, 200));
        setMaximumSize(new Dimension(FORM_WIDTH, FORM_HEIGHT));
        
        // Get the parent frame for positioning
        Frame parent = (Frame) getOwner();
        if (parent != null) {
            setLocationRelativeTo(parent);
        }
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Force re-render with correct size
        revalidate();
        repaint();
    }

    /**
     * Create header with title and close button
     */
    protected JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel title = new JLabel(getTitle());
        title.setFont(TITLEBOLD.deriveFont(20f));
        title.setForeground(textDark);
        
        JButton closeBtn = createCloseButton();
        
        header.add(title, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);
        
        return header;
    }

/**
     * Create form body using GridBagLayout
     * Override to add form fields using addFormField(), addFormRow(), addFormFieldFull()
     * These methods will add to the formPanel directly
     */
    protected JPanel createFormBody() {
        // Default empty - subclasses override and call addFormField/addFormRow in their override
        // Fields are added directly to formPanel via the helper methods
        return null;
    }

    /**
     * Create action buttons
     */
    protected JPanel createActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        
        JButton cancelBtn = SystemStyle.createFormButton("Cancel", false);
        cancelBtn.addActionListener(e -> dispose());
        
        JButton saveBtn = SystemStyle.createFormButton("Save", true);
        saveBtn.addActionListener(e -> saveForm());
        
        actions.add(cancelBtn);
        actions.add(Box.createHorizontalStrut(ROW_SPACING));
        actions.add(saveBtn);
        
        return actions;
    }

    /**
     * Implement save logic
     */
    protected abstract void saveForm();

    /**
     * Add single field to form
     */
    protected void addFormField(GridBagConstraints gbc, String labelText, JComponent input) {
        if (labelText == null) {
            gbc.gridx = 0;
            formPanel.add(input, gbc);
            return;
        }
        
        JPanel fieldPanel = new JPanel(new BorderLayout(0, LABEL_FIELD_GAP));
        fieldPanel.setOpaque(false);
        
        JLabel label = SystemStyle.createFieldLabel(labelText);
        
        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(input, BorderLayout.CENTER);
        
        gbc.gridx = 0;
        formPanel.add(fieldPanel, gbc);
    }

    /**
     * Add full-width field
     */
    protected void addFormFieldFull(GridBagConstraints gbc, String labelText, JComponent input) {
        JPanel fieldPanel = new JPanel(new BorderLayout(0, LABEL_FIELD_GAP));
        fieldPanel.setOpaque(false);
        
        JLabel label = SystemStyle.createFieldLabel(labelText);
        
        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(input, BorderLayout.CENTER);
        
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        formPanel.add(fieldPanel, gbc);
        gbc.gridwidth = 1;
    }

    /**
     * Add two-column row
     */
    protected void addFormRow(GridBagConstraints gbc, String label1, JComponent input1, 
                            String label2, JComponent input2) {
        JPanel leftPanel = new JPanel(new BorderLayout(0, LABEL_FIELD_GAP));
        leftPanel.setOpaque(false);
        leftPanel.add(SystemStyle.createFieldLabel(label1), BorderLayout.NORTH);
        leftPanel.add(input1, BorderLayout.CENTER);
        
        JPanel rightPanel = new JPanel(new BorderLayout(0, LABEL_FIELD_GAP));
        rightPanel.setOpaque(false);
        rightPanel.add(SystemStyle.createFieldLabel(label2), BorderLayout.NORTH);
        rightPanel.add(input2, BorderLayout.CENTER);
        
        JPanel rowPanel = new JPanel(new GridLayout(1, 2, COL_SPACING, 0));
        rowPanel.setOpaque(false);
        rowPanel.add(leftPanel);
        rowPanel.add(rightPanel);
        
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        formPanel.add(rowPanel, gbc);
        gbc.gridwidth = 1;
    }

    /**
     * Add spacing row
     */
    protected void addSpacing(GridBagConstraints gbc) {
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(0, ROW_SPACING));
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        formPanel.add(spacer, gbc);
        gbc.gridwidth = 1;
    }

    /**
     * Show error
     */
    protected void showError(String message) {
        SystemStyle.showError(statusLabel, message);
    }

    /**
     * Show success
     */
    protected void showSuccess(String message) {
        SystemStyle.showSuccess(statusLabel, message);
    }

    /**
     * Clear status
     */
    protected void clearStatus() {
        SystemStyle.clearStatus(statusLabel);
    }

    /**
     * Create close button
     */
    protected JButton createCloseButton() {
        JButton btn = new JButton("\u00D7");
        btn.setFont(new Font("Arial", Font.BOLD, 22));
        btn.setForeground(textDark);
        btn.setBackground(null);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> dispose());
        return btn;
    }

    /**
     * Add drag support
     */
    private void enableDragSupport(JPanel panel) {
        final Point offset = new Point();
        
        MouseAdapter adapter = new MouseAdapter() {
            private Point start;
            
            @Override
            public void mousePressed(MouseEvent e) {
                start = e.getPoint();
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (start != null && getOwner() != null) {
                    Point p = getOwner().getLocation();
                    getOwner().setLocation(
                            p.x + e.getPoint().x - start.x,
                            p.y + e.getPoint().y - start.y
                    );
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                start = null;
            }
        };
        
        panel.addMouseListener(adapter);
        panel.addMouseMotionListener(adapter);
    }
}
