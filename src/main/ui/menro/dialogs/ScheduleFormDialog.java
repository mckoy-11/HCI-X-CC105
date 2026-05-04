package main.ui.menro.dialogs;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import main.model.Schedule;
import main.model.Team;
import main.model.Truck;
import main.model.Barangay;
import main.service.ScheduleService;
import main.service.TeamService;
import main.service.TruckService;
import main.service.BarangayService;
import static main.ui.style.SystemStyle.*;
import main.store.DataChangeBus;
import static main.store.DataTopics.SCHEDULES;
import main.ui.style.BaseFormDialog;

/**
 * ScheduleFormDialog - Form for creating/editing schedules
 * With barangay dropdown and auto-set admin/contact
 */
public final class ScheduleFormDialog extends BaseFormDialog {

    private final ScheduleService scheduleService = new ScheduleService();
    private final TeamService teamService = new TeamService();
    private final TruckService truckService = new TruckService();
    private final BarangayService barangayService = new BarangayService();
    
    private final JComboBox<Barangay> barangayComboBox;
    private final JTextField barangayAdminField = styleInput(new JTextField());
    private final JTextField contactNumberField = styleInput(new JTextField());
    private final JComboBox<Team> teamComboBox;
    private final JComboBox<Truck> truckComboBox;
    private final JTextField dateField = new JTextField();
    private final JTextField timeField = new JTextField();
    private final JComboBox<String> statusComboBox;
    
    private final Schedule existingSchedule;
    private final boolean isEditMode;

    public ScheduleFormDialog(Frame parent, Schedule schedule) {
        super(parent, schedule == null ? "Add Schedule" : "Edit Schedule");
        this.existingSchedule = schedule;
        this.isEditMode = schedule != null;
        
        // Initialize combo boxes
        this.barangayComboBox = styleComboBox(new JComboBox<>());
        this.teamComboBox = styleComboBox(new JComboBox<>());
        this.truckComboBox = styleComboBox(new JComboBox<>());
        this.statusComboBox = styleComboBox(new JComboBox<>(new String[]{"Select status", "SCHEDULED", "PENDING", "COMPLETED", "CANCELLED"}));
        
        // Style date and time fields - make them read-only
        dateField.setEditable(false);
        timeField.setEditable(false);
        dateField.setBackground(new Color(240, 240, 240));
        timeField.setBackground(new Color(240, 240, 240));
        
        // Auto-set current date and time if creating new schedule
        if (!isEditMode) {
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            dateField.setText(today.toString());
            timeField.setText(String.format("%02d:%02d", now.getHour(), now.getMinute()));
            statusComboBox.setSelectedItem("SCHEDULED");
        }
        
        // Load data
        loadBarangays();
        loadTeams();
        loadTrucks();
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
        
        // Barangay dropdown
        addFormField(gbc, "Barangay", barangayComboBox);
        gbc.gridy++;
        
        // Admin and Contact
        addFormRow(gbc, "Barangay Admin", barangayAdminField, "Contact Number", contactNumberField);
        gbc.gridy++;
        
        // Team and Truck
        addFormRow(gbc, "Collector Team", teamComboBox, "Truck", truckComboBox);
        gbc.gridy++;
        
        // Date and Time
        addFormRow(gbc, "Date (YYYY-MM-DD)", dateField, "Time (HH:MM)", timeField);
        gbc.gridy++;
        
        // Status
        addFormField(gbc, "Status", statusComboBox);
        
        return null;
    }

    @Override
    protected void saveForm() {
        // Validate required fields
        if (barangayComboBox.getSelectedItem() == null) {
            showError("Barangay is required");
            return;
        }
        
        try {
            Schedule schedule = existingSchedule != null ? existingSchedule : new Schedule();
            
            // Get selected barangay
            Barangay selectedBarangay = (Barangay) barangayComboBox.getSelectedItem();
            schedule.setBarangayName(selectedBarangay.getBarangayName());
            
            // Auto-fill admin name from barangay (read from barangay_admin table)
            String admin = barangayAdminField.getText().trim();
            schedule.setBarangayAdmin(admin.isEmpty() ? "Admin" : admin);
            
            // Auto-fill contact from barangay
            String contact = contactNumberField.getText().trim();
            schedule.setContactNumber(contact.isEmpty() ? selectedBarangay.getContact() : contact);
            
            // Use today's date (read-only)
            schedule.setDate(LocalDate.parse(dateField.getText().trim()));
            schedule.setTime(LocalTime.parse(timeField.getText().trim()));
            
            // Status logic
            Object statusSelection = statusComboBox.getSelectedItem();
            LocalDate today = LocalDate.now();
            LocalDate scheduleDate = schedule.getDate();
            
            // If date is today, auto-change to PENDING
            if (scheduleDate.equals(today)) {
                schedule.setStatus("PENDING");
            } else {
                // Otherwise use selected status
                schedule.setStatus(statusSelection != null ? (String) statusSelection : "SCHEDULED");
            }
            
            if (teamComboBox.getSelectedItem() != null) {
                schedule.setCollectorTeam(((Team) teamComboBox.getSelectedItem()).getTeamName());
            }
            
            if (truckComboBox.getSelectedItem() != null) {
                Truck truck = (Truck) truckComboBox.getSelectedItem();
                schedule.setTruckPlateNumber(truck.getPlateNumber());
                schedule.setTruckType(truck.getTruckType());
            }
            
            boolean success = isEditMode 
                    ? scheduleService.updateSchedule(schedule)
                    : scheduleService.saveSchedule(schedule);
            
            if (success) {
                showSuccess("Schedule saved successfully");
                // Publish event for real-time UI updates
                SwingUtilities.invokeLater(() -> {
                    DataChangeBus.publish(SCHEDULES);
                    dispose();
                });
            } else {
                showError("Failed to save schedule");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }
    
    private void loadBarangays() {
        barangayComboBox.removeAllItems();
        List<Barangay> barangays = barangayService.getAllBarangays();
        for (Barangay b : barangays) {
            barangayComboBox.addItem(b);
        }
        
        // Add listener to auto-set admin and contact when barangay is selected
        barangayComboBox.addActionListener(e -> {
            Barangay selected = (Barangay) barangayComboBox.getSelectedItem();
            if (selected != null) {
                // Auto-fill admin and contact from barangay
                if (barangayAdminField.getText().isEmpty()) {
                    barangayAdminField.setText(selected.getContact() != null ? selected.getContact() : "");
                }
                if (contactNumberField.getText().isEmpty()) {
                    contactNumberField.setText(selected.getContact() != null ? selected.getContact() : "");
                }
            }
        });
    }
    
    private void loadTeams() {
        teamComboBox.removeAllItems();
        List<Team> teams = teamService.getAllTeams();
        for (Team team : teams) {
            teamComboBox.addItem(team);
        }
    }
    
    private void loadTrucks() {
        truckComboBox.removeAllItems();
        List<Truck> trucks = truckService.getAllTrucks();
        for (Truck truck : trucks) {
            truckComboBox.addItem(truck);
        }
    }
    
    private void populateData() {
        if (existingSchedule != null) {
            // Select barangay
            for (int i = 0; i < barangayComboBox.getItemCount(); i++) {
                Barangay b = barangayComboBox.getItemAt(i);
                if (b != null && b.getBarangayName().equals(existingSchedule.getBarangayName())) {
                    barangayComboBox.setSelectedIndex(i);
                    break;
                }
            }
            
            barangayAdminField.setText(existingSchedule.getBarangayAdmin());
            contactNumberField.setText(existingSchedule.getContactNumber());
            dateField.setText(existingSchedule.getDate() != null ? existingSchedule.getDate().toString() : "");
            timeField.setText(existingSchedule.getTime() != null ? existingSchedule.getTime().toString() : "");
            
            // Select team
            for (int i = 0; i < teamComboBox.getItemCount(); i++) {
                Team team = teamComboBox.getItemAt(i);
                if (team != null && team.getTeamName().equals(existingSchedule.getCollectorTeam())) {
                    teamComboBox.setSelectedIndex(i);
                    break;
                }
            }
            
            // Select truck
            for (int i = 0; i < truckComboBox.getItemCount(); i++) {
                Truck truck = truckComboBox.getItemAt(i);
                if (truck != null && truck.getPlateNumber().equals(existingSchedule.getTruckPlateNumber())) {
                    truckComboBox.setSelectedIndex(i);
                    break;
                }
            }
            
            // Select status (dropdown when editing)
            if (existingSchedule.getStatus() != null) {
                statusComboBox.setSelectedItem(existingSchedule.getStatus());
            }
        } else {
            // Default date/time when adding new schedule
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            dateField.setText(tomorrow.format(DateTimeFormatter.ISO_LOCAL_DATE));
            timeField.setText("08:00");
        }
    }
}
