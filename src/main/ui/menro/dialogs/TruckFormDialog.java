package main.ui.menro.dialogs;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import main.model.Truck;
import main.service.TruckService;
import static main.ui.style.SystemStyle.*;
import main.ui.style.BaseFormDialog;
import main.store.DataChangeBus;
import static main.store.DataTopics.TRUCKS;
import main.ui.style.SystemStyle;

/**
 * TruckFormDialog - Form for creating/editing trucks
 * Contains truck details with team assignment dropdown
 */
public final class TruckFormDialog extends BaseFormDialog {

    private final TruckService truckService = new TruckService();
    
    private final JTextField plateNumberField = styleInput(new JTextField());
    private final JTextField capacityField = styleInput(new JTextField());
    private final JComboBox<String> truckTypeComboBox;
    
    private final Truck existingTruck;
    private final boolean isEditMode;

    /**
     * Constructor with 2 parameters (for Management.java calls)
     * Used when adding new truck or editing existing truck
     */
    public TruckFormDialog(Frame parent, Truck truck) {
        super(parent, truck == null ? "Add Truck" : "Edit Truck");
        this.existingTruck = truck;
        this.isEditMode = truck != null;
        
        // Initialize combo boxes - truck type only
        this.truckTypeComboBox = styleComboBox(new JComboBox<>(new String[]{"Compactor", "Dump Truck", "Garbage Truck", "Container Carrier", "Other"}));
        
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
        
        return null;
    }

    @Override
    protected void saveForm() {
        // Validate required fields
        String plateNumber = plateNumberField.getText().trim();
        if (plateNumber.isEmpty()) {
            showError("Plate number is required");
            return;
        }
        
        try {
            Truck truck = existingTruck != null ? existingTruck : new Truck();
            truck.setPlateNumber(plateNumber.toUpperCase());
            
            Object typeSelection = truckTypeComboBox.getSelectedItem();
            if (typeSelection != null) {
                truck.setTruckType((String) typeSelection);
            }
            
            truck.setCapacity(capacityField.getText().trim());

            if (isEditMode) {
                truck.setStatus(existingTruck.getStatus());
            } else {
                truck.setStatus("Unassigned");
            }
            
            boolean success = isEditMode 
                    ? truckService.updateTruck(truck)
                    : truckService.addTruck(truck);
            
            if (success) {
                showSuccess("Truck saved successfully");
                // Publish event for real-time UI updates
                SwingUtilities.invokeLater(() -> {
                    DataChangeBus.publish(TRUCKS);
                    dispose();
                });
            } else {
                showError("Failed to save truck");
            }
        } catch (Exception e) {
            showError("Error saving truck: " + e.getMessage());
            e.printStackTrace();
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
            
        }
    }
    
}
