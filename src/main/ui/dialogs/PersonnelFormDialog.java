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
    private final JComboBox<Team> teamComboBox;
    private final JComboBox<String> statusComboBox;
    
    private final Personnel existingPersonnel;
    private final boolean isEditMode;

    public PersonnelFormDialog(Frame parent, Personnel personnel) {
        super(parent, personnel == null ? "Add Personnel" : "Edit Personnel");
        this.existingPersonnel = personnel;
this.isEditMode = personnel != null;
        
        // Initialize combo boxes
        this.genderComboBox = styleComboBox(new JComboBox<>(new String[]{"Male", "Female", "Other"}));
        this.roleComboBox = styleComboBox(new JComboBox<>(new String[]{"Driver", "Helper", "Collector", "Supervisor", "Admin"}));
        this.teamComboBox = styleComboBox(new JComboBox<>());
        this.statusComboBox = styleComboBox(new JComboBox<>(new String[]{"Active", "Inactive"}));
        
        // Default status when adding = Active
        if (!isEditMode) {
            statusComboBox.setSelectedItem("Active");
        }
        
        // Load teams and populate data
        loadTeams();
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
        gbc.gridy++;
        
        // Team and Status
        addFormRow(gbc, "Team", teamComboBox, "Status", statusComboBox);
        
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
            personnel.setStatus((String) statusComboBox.getSelectedItem());
            
            // Parse age
            String ageText = ageField.getText().trim();
            if (!ageText.isEmpty()) {
                personnel.setAge(Integer.parseInt(ageText));
            }
            
            // Set team
            Team selectedTeam = (Team) teamComboBox.getSelectedItem();
            personnel.setTeam(selectedTeam != null ? selectedTeam.getTeamName() : null);
            
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
    
    private void loadTeams() {
        teamComboBox.removeAllItems();
        teamComboBox.addItem(null); // For unassigned
        List<Team> teams = teamService.getAllTeams();
        for (Team team : teams) {
            teamComboBox.addItem(team);
        }
    }
    
    private void populateData() {
        if (existingPersonnel != null) {
            fullNameField.setText(existingPersonnel.getFullName());
            phoneNumberField.setText(existingPersonnel.getPhoneNumber());
            ageField.setText(String.valueOf(existingPersonnel.getAge()));
            addressField.setText(existingPersonnel.getAddress());
            
            // Select gender
            if (existingPersonnel.getGender() != null) {
                genderComboBox.setSelectedItem(existingPersonnel.getGender());
            }
            
            // Select role
            if (existingPersonnel.getRole() != null) {
                roleComboBox.setSelectedItem(existingPersonnel.getRole());
            }
            
            // Select team
            if (existingPersonnel.getTeam() != null) {
                for (int i = 0; i < teamComboBox.getItemCount(); i++) {
                    Team team = teamComboBox.getItemAt(i);
                    if (team != null && team.getTeamName().equals(existingPersonnel.getTeam())) {
                        teamComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            // Select status (dropdown when editing)
            if (existingPersonnel.getStatus() != null) {
                statusComboBox.setSelectedItem(existingPersonnel.getStatus());
            }
        }
    }
}
