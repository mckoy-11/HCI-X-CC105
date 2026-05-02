package main.ui.officials_pages;

import main.model.Barangay;
import main.service.BarangayService;
import main.store.DataChangeBus;
import main.store.DataTopics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;
import static main.style.SystemStyle.*;
import main.ui.components.Header;

/**
 * BarangayDetailsPanel - Allows barangay users to view and edit their barangay details.
 */
public class BarangayDetailsPanel extends JPanel {

    private final BarangayService barangayService = new BarangayService();

    private Barangay currentBarangay;

    // Form fields
    private final JTextField barangayNameField = styleInput(new JTextField());
    private final JSpinner purokCountSpinner;
    private final JSpinner populationSpinner;
    private final JTextField collectionDayField = styleInput(new JTextField());
    private final JComboBox<String> statusComboBox = styleComboBox(new JComboBox<>(new String[]{"Active", "Inactive"}));

    private final JButton saveButton = createPrimaryButton("Save Changes");
    private final JButton deleteButton = createSecondaryButton("Delete Barangay");

    public BarangayDetailsPanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        // Initialize spinners
        SpinnerModel purokModel = new SpinnerNumberModel(1, 1, 99, 1);
        purokCountSpinner = new JSpinner(purokModel);
        styleSpinner(purokCountSpinner);

        SpinnerModel popModel = new SpinnerNumberModel(100, 1, 999999, 1);
        populationSpinner = new JSpinner(popModel);
        styleSpinner(populationSpinner);

        loadBarangayData();
        add(new Header("Barangay Details"), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        
        setupListeners();
    }
    
    private JPanel createMainContent() {
       JPanel root = GradientPaint(BGCOLOR1, BGCOLOR2, 0, false, 20);
        root.setLayout(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        root.add(createForm(), BorderLayout.CENTER);
        root.add(createActions(), BorderLayout.SOUTH);

        return root;
    }

    private void loadBarangayData() {
        try {
            // Assuming the logged-in account is associated with a barangay
            // For simplicity, load the first barangay or based on account
            // In real app, get from session or account
            java.util.List<Barangay> barangays = barangayService.getAllBarangays();
            if (!barangays.isEmpty()) {
                currentBarangay = barangays.get(0); // TODO: Get based on logged-in account
                populateFields();
            } else {
                JOptionPane.showMessageDialog(this, "No barangay data found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading barangay data: " + e.getMessage());
        }
    }

    private void populateFields() {
        if (currentBarangay != null) {
            barangayNameField.setText(currentBarangay.getBarangayName());
            purokCountSpinner.setValue(currentBarangay.getPurokCount());
            populationSpinner.setValue(currentBarangay.getPopulation());
            collectionDayField.setText(currentBarangay.getCollectionDay() != null ? currentBarangay.getCollectionDay() : "");
            statusComboBox.setSelectedItem(currentBarangay.getStatus());
        }
    }

    private JPanel createForm() {
        JPanel form = createTransparentPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(createFormSection("Barangay Information"));
        form.add(Box.createVerticalStrut(16));

        form.add(createFormField("Barangay Name", barangayNameField));
        form.add(Box.createVerticalStrut(12));
        form.add(createFormField("Number of Purok", purokCountSpinner));
        form.add(Box.createVerticalStrut(12));
        form.add(createFormField("Population", populationSpinner));
        form.add(Box.createVerticalStrut(12));
        form.add(createFormField("Collection Day", collectionDayField));
        form.add(Box.createVerticalStrut(12));
        form.add(createFormField("Status", statusComboBox));

        return form;
    }

    private JPanel createFormSection(String title) {
        JPanel section = createTransparentPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(BUTTONBOLD.deriveFont(14f));
        titleLabel.setForeground(MUTED_TEXT);

        section.add(titleLabel);
        return section;
    }

    private JPanel createFormField(String label, JComponent field) {
        JPanel row = createTransparentPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(400, 60));

        JLabel lbl = createFieldLabel(label);
        lbl.setPreferredSize(new Dimension(120, 20));

        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JPanel createActions() {
        JPanel actions = createTransparentPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        actions.add(deleteButton);
        actions.add(Box.createHorizontalStrut(12));
        actions.add(saveButton);

        return actions;
    }

    private void setupListeners() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChanges();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteBarangay();
            }
        });
    }

    private void saveChanges() {
        if (currentBarangay == null) return;

        currentBarangay.setBarangayName(barangayNameField.getText().trim());
        currentBarangay.setPurokCount((Integer) purokCountSpinner.getValue());
        currentBarangay.setPopulation((Integer) populationSpinner.getValue());
        currentBarangay.setCollectionDay(collectionDayField.getText().trim());
        currentBarangay.setStatus((String) statusComboBox.getSelectedItem());

        try {
            boolean updated = barangayService.updateBarangay(currentBarangay);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Barangay details updated successfully.");
                DataChangeBus.publish(DataTopics.BARANGAYS);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update barangay details.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating barangay: " + e.getMessage());
        }
    }

    private void deleteBarangay() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this barangay?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = barangayService.deleteBarangay(currentBarangay.getBarangayId());
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Barangay deleted successfully.");
                    DataChangeBus.publish(DataTopics.BARANGAYS);
                    // Perhaps navigate away or disable form
                    setEnabled(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete barangay.");
                }
            } catch (HeadlessException e) {
                JOptionPane.showMessageDialog(this, "Error deleting barangay: " + e.getMessage());
            }
        }
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(SUBTITLEPLAIN.deriveFont(15f));
        spinner.setForeground(textDark);
        spinner.setBackground(INPUT_BACKGROUND);
        spinner.setBorder(EMPTY_BORDER);
        spinner.setMaximumSize(new Dimension(400, FIELD_HEIGHT));
        spinner.setPreferredSize(new Dimension(400, FIELD_HEIGHT));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor defaultEditor = (JSpinner.DefaultEditor) editor;
            defaultEditor.getTextField().setEditable(true);
            defaultEditor.getTextField().setBackground(INPUT_BACKGROUND);
            defaultEditor.getTextField().setBorder(null);
        }
    }
}