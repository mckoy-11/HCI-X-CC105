package main.ui.menro.dialogs;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import main.model.Barangay;
import main.service.BarangayService;
import main.store.DataChangeBus;
import main.store.DataTopics;
import static main.ui.style.SystemStyle.*;

/**
 * BarangaySetupDialog - Modal dialog for new barangay admin onboarding
 * 
 * Displays a centered modal dialog (roadmap wizard style) guiding the user to complete barangay setup.
 * Includes: Barangay Name, Number of Purok, Total Population, Agreement Checkbox
 * 
 * UX/UI: Clean Figma-style layout, centered modal, consistent spacing,
 * equal width fields, clear section header
 */
public class BarangaySetupDialog extends JDialog {

    private final BarangayService barangayService = new BarangayService();
    private final int accountId;
    private final JLabel statusLabel = createStatusLabel();

    // Form fields
    private final JTextField barangayNameField = styleInput(new JTextField());
    private final JSpinner purokCountSpinner;
    private final JSpinner populationSpinner;
    private final JCheckBox agreementCheckBox;

    public BarangaySetupDialog(Frame parent, int accountId) {
        super(parent, "Complete Barangay Setup", true);
        this.accountId = accountId;

        // Initialize spinners with model
        SpinnerModel purokModel = new SpinnerNumberModel(1, 1, 99, 1);
        this.purokCountSpinner = new JSpinner(purokModel);
        styleSpinner(purokCountSpinner);

        SpinnerModel popModel = new SpinnerNumberModel(100, 1, 999999, 1);
        this.populationSpinner = new JSpinner(popModel);
        styleSpinner(populationSpinner);

        this.agreementCheckBox = new JCheckBox("I confirm that the information provided is accurate");
        styleCheckbox(agreementCheckBox);

        initUI();
    }

    private void initUI() {
        setUndecorated(true);
        setModal(true);
        setResizable(false);

        // Root panel with dark overlay background
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // Main form panel with rounded corners
        JPanel mainPanel = createRoundedPanel();
        mainPanel.setBorder(new EmptyBorder(32, 40, 32, 40));
        mainPanel.setBackground(WHITE);

        // Header section
        JPanel headerPanel = createHeader();

        // Form body
        JPanel formPanel = createFormBody();

        // Actions
        JPanel actionsPanel = createActions();

        // Status
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new EmptyBorder(16, 0, 0, 0));
        statusPanel.add(statusLabel, BorderLayout.NORTH);

        // Bottom section
        JPanel bottomSection = new JPanel(new BorderLayout(0, 16));
        bottomSection.setOpaque(false);
        bottomSection.add(statusPanel, BorderLayout.CENTER);
        bottomSection.add(actionsPanel, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(bottomSection, BorderLayout.SOUTH);

        root.add(mainPanel, BorderLayout.CENTER);
        add(root);

        pack();
        setMinimumSize(new Dimension(450, 0));
        setMaximumSize(new Dimension(450, 600));

        // Position relative to parent
        if (getOwner() != null) {
            setLocationRelativeTo(getOwner());
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private JPanel createRoundedPanel() {
        return new JPanel(new BorderLayout()) {
            private final int radius = 16;

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
                g2.fill(createRoundedShape(
                        getWidth(), getHeight(), radius, radius, radius, radius));
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel title = new JLabel("Complete Barangay Setup");
        title.setFont(TITLEBOLD.deriveFont(22f));
        title.setForeground(textDark);

        JLabel subtitle = new JLabel("Set up your barangay to start managing schedules");
        subtitle.setFont(SUBTITLEPLAIN.deriveFont(14f));
        subtitle.setForeground(textMuted);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createFormBody() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Section header
        JLabel sectionHeader = new JLabel("Barangay Information");
        sectionHeader.setFont(BUTTONBOLD.deriveFont(13f));
        sectionHeader.setForeground(textMuted);
        sectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sectionHeader);
        form.add(Box.createVerticalStrut(16));

        // Barangay Name
        JPanel nameField = createFieldGroup("Barangay Name", barangayNameField);
        form.add(nameField);
        form.add(Box.createVerticalStrut(14));

        // Purok and Population in a row
        JPanel rowPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        rowPanel.setOpaque(false);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowPanel.setMaximumSize(new Dimension(FORM_WIDTH, 72));

        JPanel purokPanel = createFieldGroup("Number of Purok", purokCountSpinner);
        purokPanel.setMaximumSize(new Dimension(200, 72));
        rowPanel.add(purokPanel);

        JPanel popPanel = createFieldGroup("Total Population", populationSpinner);
        popPanel.setMaximumSize(new Dimension(200, 72));
        rowPanel.add(popPanel);

        form.add(rowPanel);
        form.add(Box.createVerticalStrut(20));

        // Agreement checkbox
        JPanel agreementPanel = new JPanel();
        agreementPanel.setLayout(new BoxLayout(agreementPanel, BoxLayout.Y_AXIS));
        agreementPanel.setOpaque(false);
        agreementPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        agreementPanel.add(agreementCheckBox);
        form.add(agreementPanel);

        return form;
    }

    private JPanel createFieldGroup(String labelText, JComponent input) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(FORM_WIDTH, 72));

        JLabel label = createFieldLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(label);
        group.add(Box.createVerticalStrut(6));
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(input);

        return group;
    }

    private JPanel createActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createFormButton("Cancel", false);
        cancelBtn.addActionListener(e -> handleCancel());

        JButton submitBtn = createFormButton("Submit", true);
        submitBtn.addActionListener(e -> handleSubmit());

        actions.add(cancelBtn);
        actions.add(Box.createHorizontalStrut(14));
        actions.add(submitBtn);

        return actions;
    }

    private void handleCancel() {
        dispose();
    }

    private void handleSubmit() {
        // Validate fields
        String barangayName = barangayNameField.getText().trim();
        if (barangayName.isEmpty()) {
            showError(statusLabel, "Barangay name is required");
            return;
        }

        // Check for duplicate
        if (barangayService.getBarangayByName(barangayName) != null) {
            showError(statusLabel, "Barangay already exists");
            return;
        }

        int purokCount = (Integer) purokCountSpinner.getValue();
        int population = (Integer) populationSpinner.getValue();

        if (!agreementCheckBox.isSelected()) {
            showError(statusLabel, "Please confirm the information is accurate");
            return;
        }

        try {
            // Save barangay
            Barangay barangay = new Barangay();
            barangay.setBarangayName(barangayName);
            barangay.setPurokCount(purokCount);
            barangay.setPopulation(population);
            barangay.setStatus("Active");

            boolean saved = barangayService.addBarangay(barangay);
            if (!saved) {
                showError(statusLabel, "Failed to save barangay");
                return;
            }

            // Update account setup complete flag
            try {
                main.dao.AccountDao dao = new main.dao.AccountDao(
                        main.database.SQLConnection.getConnection());
                dao.updateBarangaySetupComplete(accountId, true);
            } catch (Exception e) {
                // Log but continue - barangay was saved
                e.printStackTrace();
            }

            // Publish event
            DataChangeBus.publish(DataTopics.BARANGAYS);

            showSuccess(statusLabel, "Barangay setup completed!");
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(1000); // Show success message for 1 second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                dispose();
            });
        } catch (Exception e) {
            showError(statusLabel, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(SUBTITLEPLAIN.deriveFont(15f));
        spinner.setForeground(textDark);
spinner.setBackground(INPUT_BACKGROUND);
        spinner.setBorder(createInputBorder());
        spinner.setMaximumSize(new Dimension(FORM_WIDTH, FIELD_HEIGHT));
        spinner.setPreferredSize(new Dimension(FORM_WIDTH, FIELD_HEIGHT));
        spinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor defaultEditor = (JSpinner.DefaultEditor) editor;
            defaultEditor.getTextField().setEditable(true);
            defaultEditor.getTextField().setBackground(INPUT_BACKGROUND);
            defaultEditor.getTextField().setBorder(null);
        }
    }

    private void styleCheckbox(JCheckBox checkbox) {
        checkbox.setFont(BODYPLAIN.deriveFont(13f));
        checkbox.setForeground(textDark);
        checkbox.setBackground(null);
        checkbox.setOpaque(false);
        checkbox.setFocusPainted(false);
        checkbox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        );
    }
}
