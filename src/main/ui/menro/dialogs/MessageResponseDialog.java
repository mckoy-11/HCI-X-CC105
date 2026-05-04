package main.ui.menro.dialogs;

import java.awt.*;
import javax.swing.*;
import main.model.Complaint;
import main.service.ComplaintService;
import static main.ui.style.SystemStyle.*;
import main.ui.style.BaseFormDialog;

/**
 * MessageResponseDialog - Form for responding to complaints/messages
 * Extends BaseFormDialog for consistent styling
 */
public final class MessageResponseDialog extends BaseFormDialog {

    private final ComplaintService complaintService = new ComplaintService();
    
    private final JTextArea complaintArea;
    private final JTextArea responseArea;
    private final JComboBox<String> statusComboBox;
    
    private final Complaint existingComplaint;
    private final boolean isEditMode;

    public MessageResponseDialog(Frame parent, Complaint complaint) {
        super(parent, complaint == null ? "New Message" : "Respond to Message");
        this.existingComplaint = complaint;
        this.isEditMode = complaint != null;
        
        // Initialize components
        this.complaintArea = new JTextArea(6, 24);
        styleTextArea(complaintArea, 6);
        complaintArea.setEditable(false);
        
        this.responseArea = new JTextArea(6, 24);
        styleTextArea(responseArea, 6);
        
        this.statusComboBox = styleComboBox(new JComboBox<>(new String[]{"Pending", "In Progress", "Resolved", "Rejected"}));
        
        // Populate data and initialize form
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
        
        // Complaint message (read-only, full-width)
        JPanel complaintPanel = new JPanel(new BorderLayout(0, LABEL_FIELD_GAP));
        complaintPanel.setOpaque(false);
        
        JLabel complaintLabel = createFieldLabel("Original Message");
        complaintPanel.add(complaintLabel, BorderLayout.NORTH);
        
        JScrollPane complaintScroll = new JScrollPane(complaintArea);
        complaintScroll.setBorder(EMPTY_BORDER);
        complaintScroll.setPreferredSize(new Dimension(FORM_WIDTH - 80, 100));
        complaintScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        complaintPanel.add(complaintScroll, BorderLayout.CENTER);
        
        addFormFieldFull(gbc, null, complaintPanel);
        gbc.gridy++;
        
        // Response message (full-width)
        JPanel responsePanel = new JPanel(new BorderLayout(0, LABEL_FIELD_GAP));
        responsePanel.setOpaque(false);
        
        JLabel responseLabel = createFieldLabel("Response Message");
        responsePanel.add(responseLabel, BorderLayout.NORTH);
        
        JScrollPane responseScroll = new JScrollPane(responseArea);
        responseScroll.setBorder(EMPTY_BORDER);
        responseScroll.setPreferredSize(new Dimension(FORM_WIDTH - 80, 100));
        responseScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        responsePanel.add(responseScroll, BorderLayout.CENTER);
        
        addFormFieldFull(gbc, null, responsePanel);
        gbc.gridy++;
        
        // Status dropdown
        addFormField(gbc, "Update Status", statusComboBox);
        
        return null;
    }

    @Override
    protected void saveForm() {
        // Validate required fields
        if (responseArea.getText().trim().isEmpty()) {
            showError("Response message is required");
            return;
        }
        
        if (statusComboBox.getSelectedItem() == null) {
            showError("Please select a status");
            return;
        }
        
        try {
            // Update complaint with response
            existingComplaint.setResponseMessage(responseArea.getText().trim());
            existingComplaint.setStatus((String) statusComboBox.getSelectedItem());
            
            boolean success = complaintService.updateComplaint(existingComplaint);
            
            if (success) {
                dispose();
            } else {
                showError("Failed to save response");
            }
        } catch (Exception e) {
            showError("Error saving response: " + e.getMessage());
        }
    }
    
    private void populateData() {
        if (existingComplaint != null) {
            complaintArea.setText(existingComplaint.getMessage());
            
            // Pre-fill response if there's an existing response
            if (existingComplaint.getResponseMessage() != null) {
                responseArea.setText(existingComplaint.getResponseMessage());
            }
            
            // Select status
            if (existingComplaint.getStatus() != null) {
                statusComboBox.setSelectedItem(existingComplaint.getStatus());
            }
        }
    }
}
