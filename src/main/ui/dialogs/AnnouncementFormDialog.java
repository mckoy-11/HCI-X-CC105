package main.ui.dialogs;

import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import main.model.Announcement;
import main.model.Barangay;
import main.service.AnnouncementService;
import main.service.BarangayService;
import static main.style.SystemStyle.*;
import main.store.DataChangeBus;
import static main.store.DataTopics.ANNOUNCEMENTS;
import main.style.BaseFormDialog;

/**
 * AnnouncementFormDialog - Form for creating/editing announcements
 * With barangay selection and auto-expiry
 */
public final class AnnouncementFormDialog extends BaseFormDialog {

    private final AnnouncementService announcementService = new AnnouncementService();
    private final BarangayService barangayService = new BarangayService();
    
    private final JTextField titleField = styleInput(new JTextField());
    private final JTextArea messageArea;
    private final JCheckBox activeCheckBox;
    private final JTextField expiryDateField = styleInput(new JTextField());
    
    // Barangay selection
    private final JPanel barangayListPanel;
    private final JCheckBox allBarangayCheckbox;
    private final java.util.Map<Integer, JCheckBox> barangayCheckboxes = new java.util.HashMap<>();
    
    private final Announcement existingAnnouncement;
    private final boolean isEditMode;

    public AnnouncementFormDialog(Frame parent, Announcement announcement) {
        super(parent, announcement == null ? "Add Announcement" : "Edit Announcement");
        this.existingAnnouncement = announcement;
        this.isEditMode = announcement != null;
        
        // Initialize components
        this.messageArea = new JTextArea(6, 24);
        styleTextArea(messageArea, 6);
        
        this.activeCheckBox = new JCheckBox("Active");
        activeCheckBox.setFont(BUTTONBOLD.deriveFont(14f));
        activeCheckBox.setForeground(textDark);
        activeCheckBox.setSelected(true);
        
        // Initialize barangay selection
        this.allBarangayCheckbox = new JCheckBox("Send to All Barangays");
        allBarangayCheckbox.setFont(BUTTONBOLD.deriveFont(13f));
        allBarangayCheckbox.setForeground(textDark);
        allBarangayCheckbox.addActionListener(e -> {
            boolean selected = allBarangayCheckbox.isSelected();
            for (JCheckBox cb : barangayCheckboxes.values()) {
                cb.setSelected(selected);
            }
            for (JCheckBox cb : barangayCheckboxes.values()) {
                cb.setEnabled(!selected);
            }
        });
        
        this.barangayListPanel = new JPanel();
        
        // Load barangays
        loadBarangays();
        
        // Default status when adding (Active default)
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
        
        // Title field
        addFormField(gbc, "Title", titleField);
        gbc.gridy++;
        
        // Message (full-width)
        JPanel messagePanel = new JPanel(new BorderLayout(0, LABEL_FIELD_GAP));
        messagePanel.setOpaque(false);
        
        JLabel messageLabel = createFieldLabel("Message");
        messagePanel.add(messageLabel, BorderLayout.NORTH);
        
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setBorder(EMPTY_BORDER);
        messageScroll.setPreferredSize(new Dimension(FORM_WIDTH - 80, 120));
        messageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagePanel.add(messageScroll, BorderLayout.CENTER);
        
        addFormFieldFull(gbc, null, messagePanel);
        gbc.gridy++;
        
        // Active checkbox
        JPanel activePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        activePanel.setOpaque(false);
        activePanel.add(activeCheckBox);
        
        addFormField(gbc, "Status", activePanel);
        gbc.gridy++;
        
        // Expiry date
        addFormField(gbc, "Expiry Date (YYYY-MM-DD)", expiryDateField);
        gbc.gridy++;
        
        // All barangay checkbox
        JPanel allBarangayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        allBarangayPanel.setOpaque(false);
        allBarangayPanel.add(allBarangayCheckbox);
        
        addFormField(gbc, "Target", allBarangayPanel);
        gbc.gridy++;
        
        // Barangay list (full-width)
        JPanel barangayPanel = new JPanel(new BorderLayout(0, 10));
        barangayPanel.setOpaque(false);
        
        JLabel barangayLabel = createFieldLabel("Select Specific Barangays");
        barangayPanel.add(barangayLabel, BorderLayout.NORTH);
        
        barangayListPanel.setLayout(new BoxLayout(barangayListPanel, BoxLayout.Y_AXIS));
        barangayListPanel.setBackground(WHITE);
        
        JScrollPane barangayScroll = new JScrollPane(barangayListPanel);
        barangayScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        barangayScroll.setPreferredSize(new Dimension(FORM_WIDTH - 80, 120));
        barangayScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        barangayPanel.add(barangayScroll, BorderLayout.CENTER);
        
        addFormFieldFull(gbc, null, barangayPanel);
        
        return null;
    }

    @Override
    protected void saveForm() {
        // Validate required fields
        if (titleField.getText().trim().isEmpty()) {
            showError("Title is required");
            return;
        }
        
        if (messageArea.getText().trim().isEmpty()) {
            showError("Message is required");
            return;
        }
        
        // Get selected barangays
        List<String> selectedBarangays = new java.util.ArrayList<>();
        if (allBarangayCheckbox.isSelected()) {
            selectedBarangays.add("ALL");
        } else {
            for (java.util.Map.Entry<Integer, JCheckBox> entry : barangayCheckboxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    // Get barangay name from the checkbox text or from stored names
                    selectedBarangays.add(entry.getValue().getText());
                }
            }
        }
        
        // If no specific barangays selected, and not "all", send to none or default all
        String targetBarangays = selectedBarangays.isEmpty() ? "ALL" : String.join(",", selectedBarangays);
        
        try {
            Announcement announcement = existingAnnouncement != null ? existingAnnouncement : new Announcement();
            announcement.setTitle(titleField.getText().trim());
            announcement.setMessage(messageArea.getText().trim());
            announcement.setActive(activeCheckBox.isSelected());
            announcement.setArchived(false);
            
            // Parse expiry date or set default (1 week from now)
            String expiryText = expiryDateField.getText().trim();
            if (!expiryText.isEmpty()) {
                announcement.setExpiresAt(Timestamp.valueOf(expiryText + " 23:59:59"));
            } else {
                // Default: 1 week from now
                LocalDateTime oneWeekLater = LocalDateTime.now().plusWeeks(1);
                announcement.setExpiresAt(Timestamp.valueOf(oneWeekLater));
            }
            
            boolean success;
            if (isEditMode) {
                success = announcementService.updateAnnouncement(announcement);
            } else {
                success = announcementService.addAnnouncement(announcement);
            }
            
            if (success) {
                // Publish event for real-time UI updates
                SwingUtilities.invokeLater(() -> DataChangeBus.publish(ANNOUNCEMENTS));
                dispose();
            } else {
                showError("Failed to save announcement");
            }
        } catch (Exception e) {
            showError("Invalid date format. Use YYYY-MM-DD");
        }
    }
    
    private void loadBarangays() {
        List<Barangay> barangays = barangayService.getAllBarangays();
        
        for (Barangay b : barangays) {
            JCheckBox checkBox = new JCheckBox(b.getBarangayName());
            checkBox.setBackground(WHITE);
            checkBox.setFont(BODYPLAIN.deriveFont(13f));
            checkBox.setToolTipText("Households: " + b.getBarangayHousehold());
            
            barangayCheckboxes.put(b.getBarangayId(), checkBox);
            barangayListPanel.add(checkBox);
        }
        
        barangayListPanel.revalidate();
        barangayListPanel.repaint();
    }
    
    private void populateData() {
        if (existingAnnouncement != null) {
            titleField.setText(existingAnnouncement.getTitle());
            messageArea.setText(existingAnnouncement.getMessage());
            activeCheckBox.setSelected(existingAnnouncement.isActive());
            
            if (existingAnnouncement.getExpiresAt() != null) {
                expiryDateField.setText(existingAnnouncement.getExpiresAt().toString().substring(0, 10));
            }
        } else {
            // Default expiry: 1 week from now when adding
            LocalDateTime oneWeekLater = LocalDateTime.now().plusWeeks(1);
            expiryDateField.setText(oneWeekLater.format(DateTimeFormatter.ISO_LOCAL_DATE));
            
            // Default: all barangays selected
            allBarangayCheckbox.setSelected(true);
            for (JCheckBox cb : barangayCheckboxes.values()) {
                cb.setEnabled(false);
            }
        }
    }
}
