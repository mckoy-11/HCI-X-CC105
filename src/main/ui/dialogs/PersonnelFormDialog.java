package main.ui.dialogs;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import main.model.Personnel;
import main.model.Team;
import main.service.PersonnelService;
import main.service.TeamService;
import static main.style.SystemStyle.*;
import main.style.BaseFormDialog;
import main.store.DataChangeBus;
import static main.store.DataTopics.PERSONNEL;

/**
 * PersonnelFormDialog - Form for creating/editing personnel
 * With auto-set status dropdown when editing
 */
public final class PersonnelFormDialog extends BaseFormDialog {

    private final PersonnelService personnelService = new PersonnelService();
    private final TeamService teamService = new TeamService();
    
    private final JTextField fullNameField = styleInput(new JTextField());
    private final JTextField phoneNumberField = styleInput(new JTextField());
    private final JTextField ageField = styleInput(new JTextField());
    private final JTextField addressField = styleInput(new JTextField());
    private final JComboBox<String> genderComboBox;
    private final JComboBox<String> roleComboBox;
    
    private final Personnel existingPersonnel;
    private final boolean isEditMode;

    public PersonnelFormDialog(Frame parent, Personnel personnel) {
        super(parent, personnel == null ? "Add Personnel" : "Edit Personnel");
        this.existingPersonnel = personnel;
this.isEditMode = personnel != null;
        
        // Initialize combo boxes
        this.genderComboBox = styleComboBox(new JComboBox<>(new String[]{"Male", "Female", "Other"}));
        this.roleComboBox = styleComboBox(new JComboBox<>(new String[]{"Driver", "Helper", "Collector", "Supervisor", "Admin"}));
        
        // Populate data
        populateData();
        
        // Initialize form body after fields are set up
        initFormBody();
    }

    @Override
    protected JPanel createFormBody() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 0;
        
        // Full Name
        addFormField(gbc, "Full Name", fullNameField);
        gbc.gridy++;
        
        // Phone and Age
        addFormRow(gbc, "Phone Number", phoneNumberField, "Age", ageField);
        gbc.gridy++;
        
        // Gender and Role
        addFormRow(gbc, "Gender", genderComboBox, "Role", roleComboBox);
        gbc.gridy++;
        
        // Address (full-width)
        addFormFieldFull(gbc, "Address", addressField);
        
        return null;
    }

    @Override
    protected void saveForm() {
        // Validate required fields
        if (fullNameField.getText().trim().isEmpty()) {
            showError("Full name is required");
            return;
        }
        
        try {
            Personnel personnel = existingPersonnel != null ? existingPersonnel : new Personnel();
            personnel.setFullName(fullNameField.getText().trim());
            personnel.setPhoneNumber(phoneNumberField.getText().trim());
            personnel.setAddress(addressField.getText().trim());
            personnel.setGender((String) genderComboBox.getSelectedItem());
            personnel.setRole((String) roleComboBox.getSelectedItem());
            personnel.setStatus("Unassigned");
            
            // Parse age
            String ageText = ageField.getText().trim();
            if (!ageText.isEmpty()) {
                personnel.setAge(Integer.parseInt(ageText));
            }
            
            boolean success;
            if (isEditMode) {
                success = personnelService.updatePersonnel(personnel);
            } else {
                success = personnelService.addPersonnel(personnel);
            }
            
            if (success) {
                // Publish event for real-time UI updates
                SwingUtilities.invokeLater(() -> DataChangeBus.publish(PERSONNEL));
                dispose();
            } else {
                showError("Failed to save personnel");
            }
        } catch (NumberFormatException e) {
            showError("Invalid age format. Please enter a number");
        } catch (Exception e) {
            showError("Error saving personnel: " + e.getMessage());
        }
    }
    
    private void populateData() {
        if (existingPersonnel == null) {
            return;
        }

        fullNameField.setText(safe(existingPersonnel.getFullName()));
        phoneNumberField.setText(safe(existingPersonnel.getPhoneNumber()));
        addressField.setText(safe(existingPersonnel.getAddress()));

        // Age (avoid showing 0 if not set)
        ageField.setText(existingPersonnel.getAge() > 0 
                ? String.valueOf(existingPersonnel.getAge()) 
                : "");

        // ComboBoxes
        selectComboItem(genderComboBox, existingPersonnel.getGender());
        selectComboItem(roleComboBox, existingPersonnel.getRole());
    }

    /* ---------- Helper Methods ---------- */

    private String safe(String value) {
        return value != null ? value : "";
    }

    private <T> void selectComboItem(JComboBox<T> comboBox, T value) {
        if (value == null) return;

        ComboBoxModel<T> model = comboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            T item = model.getElementAt(i);
            if (item != null && item.equals(value)) {
                comboBox.setSelectedItem(item);
                return;
            }
        }
    }
}
