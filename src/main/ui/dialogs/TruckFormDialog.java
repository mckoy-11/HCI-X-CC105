package main.ui.dialogs;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import main.model.Truck;
import main.model.Team;
import main.service.TruckService;
import main.service.TeamService;
import static main.style.SystemStyle.*;
import main.style.BaseFormDialog;
import main.store.DataChangeBus;
import static main.store.DataTopics.TRUCKS;
import main.style.SystemStyle;

/**
 * TruckFormDialog - Form for creating/editing trucks
 * Contains truck details with team assignment dropdown
 */
public final class TruckFormDialog extends BaseFormDialog {

    private final TruckService truckService = new TruckService();
    private final TeamService teamService = new TeamService();
    
    private final JTextField plateNumberField = styleInput(new JTextField());
    private final JTextField capacityField = styleInput(new JTextField());
    private final JComboBox<String> truckTypeComboBox;
    private final JComboBox<String> statusComboBox;
    private final JComboBox<Team> teamComboBox;
    
    private final Truck existingTruck;
    private final boolean isEditMode;
    
// Team to assign when creating (set from TeamFormDialog)
    private Team teamToAssign;

    /**
     * Constructor with 2 parameters (for Management.java calls)
     * Used when adding new truck or editing existing truck without preset team
     */
    public TruckFormDialog(Frame parent, Truck truck) {
        this(parent, truck, null);
    }
    
    /**
     * Constructor with 3 parameters (for full team assignment)
     * Used when creating truck from TeamFormDialog with preset team
     */
    public TruckFormDialog(Frame parent, Truck truck, Team teamToAssign) {
        super(parent, truck == null ? "Add Truck" : "Edit Truck");
        this.existingTruck = truck;
        this.isEditMode = truck != null;
        this.teamToAssign = teamToAssign;
        
        // Initialize combo boxes - truck type, status, and team
        this.truckTypeComboBox = styleComboBox(new JComboBox<>(new String[]{"Compactor", "Dump Truck", "Garbage Truck", "Container Carrier", "Other"}));
        this.statusComboBox = styleComboBox(new JComboBox<>(new String[]{"Active", "Inactive", "Maintenance", "Decommissioned"}));
        this.teamComboBox = styleComboBox(new JComboBox<>());
        
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
        
        // Plate Number
        addFormField(gbc, "Plate Number", plateNumberField);
        gbc.gridy++;
        
        // Truck Type and Capacity
        addFormRow(gbc, "Truck Type", truckTypeComboBox, "Capacity (kg)", capacityField);
        gbc.gridy++;
        
        // Status
        addFormField(gbc, "Status", statusComboBox);
        gbc.gridy++;
        
        // Assigned Team (dropdown for team selection)
        addFormField(gbc, "Assigned Team", teamComboBox);
        
        return null;
    }

    @Override
    protected void saveForm() {
        // Validate required fields
        if (plateNumberField.getText().trim().isEmpty()) {
            showError("Plate number is required");
            return;
        }
        
        try {
            Truck truck = existingTruck != null ? existingTruck : new Truck();
            truck.setPlateNumber(plateNumberField.getText().trim().toUpperCase());
            truck.setTruckType((String) truckTypeComboBox.getSelectedItem());
            truck.setCapacity(capacityField.getText().trim());
            truck.setStatus((String) statusComboBox.getSelectedItem());
            
            // Set team - from dropdown selection or teamToAssign parameter
            Team selectedTeam = (Team) teamComboBox.getSelectedItem();
            if (teamToAssign != null) {
                // Override: use team from constructor (when creating from team dialog)
                truck.setAssignedTeam(teamToAssign.getTeamName());
            } else if (selectedTeam != null) {
                // Use selected team from dropdown
                truck.setAssignedTeam(selectedTeam.getTeamName());
            } else {
                // No team selected - unassign
                truck.setAssignedTeam(null);
            }
            
            boolean success;
            if (isEditMode) {
                success = truckService.updateTruck(truck);
            } else {
                success = truckService.addTruck(truck);
            }
            
            if (success) {
                // Publish event for real-time UI updates
                SwingUtilities.invokeLater(() -> DataChangeBus.publish(TRUCKS));
                dispose();
            } else {
                showError("Failed to save truck");
            }
        } catch (HeadlessException e) {
            showError("Error saving truck: " + e.getMessage());
        } catch (Exception e) {
            showError("Error saving truck: " + e.getMessage());
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
        if (existingTruck != null) {
            plateNumberField.setText(existingTruck.getPlateNumber());
            capacityField.setText(existingTruck.getCapacity());
            
            // Select truck type
            if (existingTruck.getTruckType() != null) {
                truckTypeComboBox.setSelectedItem(existingTruck.getTruckType());
            }
            
            // Select assigned team
            if (existingTruck.getAssignedTeam() != null) {
                for (int i = 0; i < teamComboBox.getItemCount(); i++) {
                    Team team = teamComboBox.getItemAt(i);
                    if (team != null && team.getTeamName().equals(existingTruck.getAssignedTeam())) {
                        teamComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            // Select status
            if (existingTruck.getStatus() != null) {
                statusComboBox.setSelectedItem(existingTruck.getStatus());
            }
        } else {
            // When adding new truck with teamToAssign, select that team
            if (teamToAssign != null) {
                for (int i = 0; i < teamComboBox.getItemCount(); i++) {
                    Team team = teamComboBox.getItemAt(i);
                    if (team != null && team.getTeamName().equals(teamToAssign.getTeamName())) {
                        teamComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    protected JPanel createActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        
        JButton cancelBtn = SystemStyle.createFormButton("Cancel", false);
        cancelBtn.addActionListener(e -> dispose());
        
        JButton saveBtn = SystemStyle.createFormButton("Save Truck", true);
        saveBtn.addActionListener(e -> saveForm());
        
        actions.add(cancelBtn);
        actions.add(Box.createHorizontalStrut(ROW_SPACING));
        actions.add(saveBtn);
        
        return actions;
    }
}
