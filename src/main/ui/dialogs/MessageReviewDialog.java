package main.ui.dialogs;

import main.model.Complaint;
import main.model.Report;
import main.model.Request;
import main.service.ComplaintService;
import main.service.ReportService;
import main.service.RequestService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Timestamp;

import static main.style.SystemStyle.*;
import static main.ui.components.CustomButton.createButton;

public class MessageReviewDialog extends JDialog {

    private interface SaveHandler {
        boolean save(String message, String response, String status, boolean isRead);
    }

    private final JTextArea messageArea = new JTextArea(6, 20);
    private final JTextArea responseArea = new JTextArea(4, 20);
    private final JComboBox<String> statusCombo = new JComboBox<>(
            new String[]{"Under Review", "Approved", "Rejected"}
    );
    private final JCheckBox readCheck = new JCheckBox("Mark as reviewed/read");
    private final SaveHandler saveHandler;
    private boolean saved;

    private MessageReviewDialog(Frame parent,
                                String titleText,
                                String sourceLabel,
                                String sourceValue,
                                String createdValue,
                                String message,
                                String response,
                                String status,
                                boolean isRead,
                                SaveHandler saveHandler) {
        super(parent, titleText, true);
        this.saveHandler = saveHandler;
        initComponents(titleText, sourceLabel, sourceValue, createdValue, message, response, status, isRead);
        setLocationRelativeTo(parent);
    }

    private void initComponents(String titleText,
                                String sourceLabel,
                                String sourceValue,
                                String createdValue,
                                String message,
                                String response,
                                String status,
                                boolean isRead) {
        AdminDialogSupport.configureFormDialog(this);

        JPanel mainPanel = Card(12);
        mainPanel.setLayout(new BorderLayout());

        JPanel header = Card(12, 0, SIDEBAR);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel(titleText);
        title.setFont(SUBTITLEBOLD);
        title.setForeground(WHITE);
        header.add(title, BorderLayout.WEST);

        JButton closeBtn = createButton("", "close.png", "close.png", 16);
        closeBtn.setBorder(null);
        closeBtn.addActionListener(e -> dispose());
        header.add(closeBtn, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel content = GradientPaint(BGCOLOR1, BGCOLOR2, 0, false, 0);
        content.setLayout(new BorderLayout());

        JPanel form = AdminDialogSupport.createFormContentPanel();
        form.add(createDescription("Review the submission details, update the message if needed, and confirm the final status."));
        form.add(Box.createVerticalStrut(18));
        form.add(createFieldGroup(sourceLabel.replace(":", ""), createReadOnlyField(sourceValue)));
        form.add(Box.createVerticalStrut(14));
        form.add(createFieldGroup("Created", createReadOnlyField(createdValue)));
        form.add(Box.createVerticalStrut(14));
        form.add(createFieldLabel("Message"));
        form.add(Box.createVerticalStrut(8));

        messageArea.setText(message != null ? message : "");
        JScrollPane scrollPane = AdminDialogSupport.createTextAreaScroll(messageArea, 280);
        form.add(scrollPane);

        form.add(Box.createVerticalStrut(14));
        form.add(createFieldLabel("Response"));
        form.add(Box.createVerticalStrut(8));
        responseArea.setText(response != null ? response : "");
        JScrollPane responseScrollPane = AdminDialogSupport.createTextAreaScroll(responseArea, 150);
        form.add(responseScrollPane);

        form.add(Box.createVerticalStrut(14));
        styleComboBox(statusCombo);
        statusCombo.setSelectedItem(status != null ? status : "Under Review");
        form.add(createFieldGroup("Status", statusCombo));

        readCheck.setOpaque(false);
        readCheck.setSelected(isRead);
        readCheck.setFont(SUBTITLEPLAIN.deriveFont(14f));
        readCheck.setForeground(textDark);
        readCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Box.createVerticalStrut(14));
        form.add(readCheck);

        content.add(AdminDialogSupport.createContentScroll(form), BorderLayout.CENTER);
        mainPanel.add(content, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        footer.setOpaque(false);

        JButton cancelBtn = createButton("Cancel", "", "", 0);
        cancelBtn.addActionListener(e -> dispose());
        footer.add(cancelBtn);

        JButton saveBtn = createButton("Save", "", "", 0);
        saveBtn.setBackground(SIDEBAR);
        saveBtn.setForeground(WHITE);
        saveBtn.addActionListener(e -> saveAndClose());
        footer.add(saveBtn);

        mainPanel.add(footer, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JTextField createReadOnlyField(String value) {
        JTextField field = styleInput(new JTextField(value != null && !value.trim().isEmpty() ? value : "N/A"));
        field.setEditable(false);
        field.setFocusable(false);
        return field;
    }

    private JLabel createDescription(String text) {
        return createFormSubtitle("<html><div style='width:" + FORM_WIDTH + "px;'>" + text + "</div></html>");
    }

    private void saveAndClose() {
        String message = messageArea.getText().trim();
        if (message.isEmpty()) {
            AdminDialogSupport.showFailure(this, "Message cannot be empty.");
            return;
        }

        boolean ok = saveHandler.save(
                message,
                responseArea.getText().trim(),
                (String) statusCombo.getSelectedItem(),
                readCheck.isSelected()
        );

        if (!ok) {
            return;
        }

        saved = true;
        dispose();
    }

    public static boolean showReportDialog(Frame parent, Report report, ReportService service) {
        MessageReviewDialog dialog = new MessageReviewDialog(
                parent,
                "Review Report",
                "Barangay:",
                report.getBarangayName(),
                formatTimestamp(report.getCreatedAt()),
                report.getMessage(),
                report.getResponseMessage(),
                report.getStatus(),
                report.isRead(),
                (message, response, status, isRead) -> {
                    report.setMessage(message);
                    report.setResponseMessage(response);
                    report.setStatus(status);
                    report.setRead(isRead);
                    boolean ok = service.updateReport(report);
                    if (!ok) {
                        AdminDialogSupport.showFailure(parent, "Failed to update the report.");
                    }
                    return ok;
                }
        );
        dialog.setVisible(true);
        return dialog.saved;
    }

    public static boolean showComplaintDialog(Frame parent, Complaint complaint, ComplaintService service) {
        MessageReviewDialog dialog = new MessageReviewDialog(
                parent,
                "Review Complaint",
                "Location:",
                complaint.getLocation(),
                formatTimestamp(complaint.getCreatedAt()),
                complaint.getMessage(),
                complaint.getResponseMessage(),
                complaint.getStatus(),
                complaint.isRead(),
                (message, response, status, isRead) -> {
                    complaint.setMessage(message);
                    complaint.setResponseMessage(response);
                    complaint.setStatus(status);
                    complaint.setRead(isRead);
                    boolean ok = service.updateComplaint(complaint);
                    if (!ok) {
                        AdminDialogSupport.showFailure(parent, "Failed to update the complaint.");
                    }
                    return ok;
                }
        );
        dialog.setVisible(true);
        return dialog.saved;
    }

    public static boolean showRequestDialog(Frame parent, Request request, RequestService service) {
        MessageReviewDialog dialog = new MessageReviewDialog(
                parent,
                "Review Request",
                "Location:",
                request.getLocation(),
                formatTimestamp(request.getCreatedAt()),
                request.getMessage(),
                request.getResponseMessage(),
                request.getStatus(),
                request.isRead(),
                (message, response, status, isRead) -> {
                    request.setMessage(message);
                    request.setResponseMessage(response);
                    request.setStatus(status);
                    request.setRead(isRead);
                    boolean ok = service.updateRequest(request);
                    if (!ok) {
                        AdminDialogSupport.showFailure(parent, "Failed to update the request.");
                    }
                    return ok;
                }
        );
        dialog.setVisible(true);
        return dialog.saved;
    }

    private static String formatTimestamp(Timestamp timestamp) {
        return timestamp != null ? timestamp.toString() : "N/A";
    }
}
