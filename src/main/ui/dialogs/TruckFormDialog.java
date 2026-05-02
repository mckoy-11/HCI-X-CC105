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
        
        // Initialize combo boxes - truck type and status
        this.truckTypeComboBox = styleComboBox(new JComboBox<>(new String[]{"Compactor", "Dump Truck", "Garbage Truck", "Container Carrier", "Other"}));
        this.statusComboBox = styleComboBox(new JComboBox<>(new String[]{"Active", "Inactive", "Maintenance", "Decommissioned"}));
        
        // Default status when adding = Unassigned
        if (!isEditMode) {
            statusComboBox.setSelectedItem("Active");
        }
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
            truck.setStatus("Unassigned");
            
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
    
    private void populateData() {
        if (existingTruck != null) {
            plateNumberField.setText(existingTruck.getPlateNumber());
            capacityField.setText(existingTruck.getCapacity() != null ? existingTruck.getCapacity() : "");
            
            // Select truck type
            if (existingTruck.getTruckType() != null) {
                truckTypeComboBox.setSelectedItem(existingTruck.getTruckType());
            }
            
            // Select status
            if (existingTruck.getStatus() != null) {
                statusComboBox.setSelectedItem(existingTruck.getStatus());
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
