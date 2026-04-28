package main.ui.officials_sections;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import main.dao.AccountDao;
import main.database.SQLConnection;
import main.model.Account;
import main.model.Complaint;
import main.model.PopupItem;
import main.model.PurokChecklistItem;
import main.model.Report;
import main.model.Request;
import main.model.UserSession;
import main.service.AttachmentService;
import main.service.ComplaintService;
import main.service.PurokChecklistService;
import main.service.ReportService;
import main.service.RequestService;
import static main.style.SystemStyle.BGCOLOR1;
import static main.style.SystemStyle.BGCOLOR2;
import static main.style.SystemStyle.Card;
import static main.style.SystemStyle.GradientPaint;
import static main.style.SystemStyle.SIDEBAR;
import static main.style.SystemStyle.SUBTITLEBOLD;
import static main.style.SystemStyle.TEXTCOLOR;
import static main.style.SystemStyle.WHITE;
import static main.style.SystemStyle.createFieldGroup;
import static main.style.SystemStyle.createFormSubtitle;
import static main.style.SystemStyle.createSplitRow;
import static main.style.SystemStyle.roundPanel;
import static main.style.SystemStyle.styleComboBox;
import static main.style.SystemStyle.styleInput;
import main.store.DataTopics;
import main.ui.components.CustomButton;
import static main.ui.components.CustomButton.createButton;
import main.ui.components.Header;
import main.ui.components.ReactivePanel;
import main.ui.components.ScrollableTable;
import main.ui.components.SummaryCards;
import main.ui.dialogs.AdminDialogSupport;

public class ReportsPanel extends ReactivePanel {

    private static final String REPORT_VIEW = "Reports";
    private static final String COMPLAINT_VIEW = "Complaints";
    private static final String REQUEST_VIEW = "Requests";

    private final ReportService reportService = new ReportService();
    private final ComplaintService complaintService = new ComplaintService();
    private final RequestService requestService = new RequestService();
    private final AttachmentService attachmentService = new AttachmentService();
    private final PurokChecklistService checklistService = new PurokChecklistService();

    private final java.awt.CardLayout contentLayout = new java.awt.CardLayout();
    private final JPanel contentPanel = Card(12);
    private final JPanel summaryPanel = new JPanel(new BorderLayout());

    private String currentView = REPORT_VIEW;
    private boolean archiveMode;

    public ReportsPanel() {
        setLayout(new BorderLayout());
        listen(DataTopics.REPORTS, this::refreshView);
        listen(DataTopics.COMPLAINTS, this::refreshView);
        listen(DataTopics.REQUESTS, this::refreshView);
        listen(DataTopics.ARCHIVE, this::refreshView);
        listen(DataTopics.CHECKLIST, this::refreshView);
        pollEvery(5000, this::refreshView);

        add(new Header("Reports"), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel root = GradientPaint(BGCOLOR1, BGCOLOR2, 0, false, 20);
        root.setLayout(new BorderLayout(0, 14));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        summaryPanel.setOpaque(false);
        summaryPanel.add(buildSummary(), BorderLayout.CENTER);

        contentPanel.setLayout(contentLayout);
        rebuildViews();

        root.add(summaryPanel, BorderLayout.NORTH);
        root.add(contentPanel, BorderLayout.CENTER);
        return root;
    }

    private void rebuildViews() {
        contentPanel.removeAll();
        contentPanel.add(buildReportTable(), REPORT_VIEW);
        contentPanel.add(buildComplaintTable(), COMPLAINT_VIEW);
        contentPanel.add(buildRequestTable(), REQUEST_VIEW);
        contentLayout.show(contentPanel, currentView);
    }

    private SummaryCards buildSummary() {
        if (COMPLAINT_VIEW.equals(currentView)) {
            return new SummaryCards(
                    new String[]{"Total Complaints", "Responses", archiveMode ? "Archived" : "Unread"},
                    new int[]{
                        archiveMode ? complaintService.getArchivedComplaintCount() : complaintService.getTotalComplaintCount(),
                        countComplaintResponses(),
                        archiveMode ? complaintService.getArchivedComplaintCount() : complaintService.getUnreadComplaintCount()
                    },
                    new String[]{"Complaint entries", "MENRO feedback", archiveMode ? "Archived entries" : "Needs review"},
                    icons(), colors()
            );
        }
        if (REQUEST_VIEW.equals(currentView)) {
            return new SummaryCards(
                    new String[]{"Total Requests", "Responses", archiveMode ? "Archived" : "Unread"},
                    new int[]{
                        archiveMode ? requestService.getArchivedRequestCount() : requestService.getTotalRequestCount(),
                        countRequestResponses(),
                        archiveMode ? requestService.getArchivedRequestCount() : requestService.getUnreadRequestCount()
                    },
                    new String[]{"Request entries", "MENRO feedback", archiveMode ? "Archived entries" : "Needs review"},
                    icons(), colors()
            );
        }
        return new SummaryCards(
                new String[]{"Total Reports", "Responses", archiveMode ? "Archived" : "Unread"},
                new int[]{
                    archiveMode ? reportService.getArchivedReportCount() : reportService.getTotalReportCount(),
                    countReportResponses(),
                    archiveMode ? reportService.getArchivedReportCount() : reportService.getUnreadReportCount()
                },
                new String[]{"Report entries", "MENRO feedback", archiveMode ? "Archived entries" : "Needs review"},
                icons(), colors()
        );
    }

    private JPanel buildReportTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(buildHeader("Barangay Reports"), BorderLayout.NORTH);

        ScrollableTable table = new ScrollableTable("Barangay", "Analytics", "Status", "Response", "Created", "Images", "Action");
        for (Report report : getVisibleReports()) {
            List<PopupItem> actions = archiveMode ? new ArrayList<PopupItem>() : buildReportActions(report);
            table.addRowWithAction(
                    safe(report.getBarangayName(), currentBarangayName()),
                    summarize(!safe(report.getPurokAnalytics()).isEmpty() ? report.getPurokAnalytics() : report.getMessage()),
                    safe(report.getStatus(), "Under Review"),
                    summarize(safe(report.getResponseMessage(), "Awaiting MENRO response")),
                    formatTimestamp(report.getCreatedAt()),
                    String.valueOf(attachmentService.getImageContents("REPORT", report.getReportId()).size()),
                    actions
            );
        }
        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildComplaintTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(buildHeader("Complaints"), BorderLayout.NORTH);

        ScrollableTable table = new ScrollableTable("Type", "Location", "Status", "Response", "Created", "Images", "Action");
        for (Complaint complaint : getVisibleComplaints()) {
            List<PopupItem> actions = archiveMode ? new ArrayList<PopupItem>() : buildComplaintActions(complaint);
            table.addRowWithAction(
                    safe(complaint.getType(), "General"),
                    safe(complaint.getLocation(), currentBarangayName()),
                    safe(complaint.getStatus(), "Under Review"),
                    summarize(safe(complaint.getResponseMessage(), "Awaiting MENRO response")),
                    formatTimestamp(complaint.getCreatedAt()),
                    String.valueOf(attachmentService.getImageContents("COMPLAINT", complaint.getComplaintId()).size()),
                    actions
            );
        }
        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRequestTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(buildHeader("Requests"), BorderLayout.NORTH);

        ScrollableTable table = new ScrollableTable("Type", "Location", "Status", "Response", "Created", "Images", "Action");
        for (Request request : getVisibleRequests()) {
            List<PopupItem> actions = archiveMode ? new ArrayList<PopupItem>() : buildRequestActions(request);
            table.addRowWithAction(
                    safe(request.getType(), "General"),
                    safe(request.getLocation(), currentBarangayName()),
                    safe(request.getStatus(), "Under Review"),
                    summarize(safe(request.getResponseMessage(), "Awaiting MENRO response")),
                    formatTimestamp(request.getCreatedAt()),
                    String.valueOf(attachmentService.getImageContents("REQUEST", request.getRequestId()).size()),
                    actions
            );
        }
        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildHeader(String titleText) {
        JPanel header = Card(12, 0, SIDEBAR);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel(titleText + (archiveMode ? " Archive" : ""));
        title.setForeground(WHITE);
        title.setFont(SUBTITLEBOLD);

        JPanel navBar = roundPanel(50);
        navBar.setLayout(new GridLayout(1, 3, 5, 0));
        navBar.setPreferredSize(new Dimension(360, 35));
        for (String view : new String[]{REPORT_VIEW, COMPLAINT_VIEW, REQUEST_VIEW}) {
            CustomButton button = new CustomButton(
                    view, "", "", 18, 100, 25, 100, 25,
                    currentView.equals(view) ? WHITE : SIDEBAR,
                    currentView.equals(view) ? WHITE : TEXTCOLOR,
                    currentView.equals(view) ? TEXTCOLOR : WHITE,
                    currentView.equals(view) ? TEXTCOLOR : WHITE,
                    false, true
            );
            button.addActionListener(event -> switchView(view));
            navBar.add(button);
        }

        JButton archiveButton = createButton(archiveMode ? "Active View" : "Archive", "calendar.png", "calendar.png", 18);
        archiveButton.setPreferredSize(new Dimension(130, 35));
        archiveButton.addActionListener(event -> {
            archiveMode = !archiveMode;
            refreshView();
        });

        JButton addButton = createButton("Add", "add.png", "add-white.png", 20);
        addButton.setPreferredSize(new Dimension(100, 35));
        addButton.setEnabled(!archiveMode);
        addButton.addActionListener(event -> openCreateDialog());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(navBar);
        right.add(archiveButton);
        right.add(addButton);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void switchView(String view) {
        currentView = view;
        refreshView();
    }

    private void openCreateDialog() {
        if (COMPLAINT_VIEW.equals(currentView)) {
            openComplaintDialog(null);
        } else if (REQUEST_VIEW.equals(currentView)) {
            openRequestDialog(null);
        } else {
            openReportDialog(null);
        }
    }

    private List<PopupItem> buildReportActions(Report report) {
        List<PopupItem> actions = new ArrayList<PopupItem>();
        actions.add(new PopupItem("Edit", "Update this report", () -> openReportDialog(report)));
        actions.add(new PopupItem("Delete", "Remove this report", () -> deleteReport(report)));
        return actions;
    }

    private List<PopupItem> buildComplaintActions(Complaint complaint) {
        List<PopupItem> actions = new ArrayList<PopupItem>();
        actions.add(new PopupItem("Edit", "Update this complaint", () -> openComplaintDialog(complaint)));
        actions.add(new PopupItem("Delete", "Remove this complaint", () -> deleteComplaint(complaint)));
        return actions;
    }

    private List<PopupItem> buildRequestActions(Request request) {
        List<PopupItem> actions = new ArrayList<PopupItem>();
        actions.add(new PopupItem("Edit", "Update this request", () -> openRequestDialog(request)));
        actions.add(new PopupItem("Delete", "Remove this request", () -> deleteRequest(request)));
        return actions;
    }

    private void openReportDialog(Report existing) {
        Account account = currentAccount();
        String barangayName = currentBarangayName();
        String createdText = existing == null ? Timestamp.from(Instant.now()).toString() : formatTimestamp(existing.getCreatedAt());
        String analyticsText = buildPurokAnalytics(account);

        JTextField barangayField = styleInput(new JTextField(barangayName));
        barangayField.setEditable(false);
        JTextField createdField = styleInput(new JTextField(createdText));
        createdField.setEditable(false);
        JTextArea analyticsArea = new JTextArea(existing == null ? analyticsText : safe(existing.getPurokAnalytics(), analyticsText));
        analyticsArea.setLineWrap(true);
        analyticsArea.setWrapStyleWord(true);
        analyticsArea.setEditable(false);
        JTextArea descriptionArea = new JTextArea(existing == null ? "" : safe(existing.getMessage()));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        List<byte[]> selectedImages = new ArrayList<byte[]>(attachmentService.getImageContents("REPORT", existing == null ? 0 : existing.getReportId()));
        JPanel previewPanel = createPreviewPanel(selectedImages);
        JButton uploadButton = createUploadButton(selectedImages, previewPanel);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createFormSubtitle("Reports automatically include barangay name, timestamp, and current purok collection analytics."));
        form.add(Box.createVerticalStrut(16));
        form.add(createSplitRow(createFieldGroup("Barangay Name", barangayField), createFieldGroup("Created", createdField)));
        form.add(Box.createVerticalStrut(16));
        form.add(createFieldGroup("Purok Waste Analytics", new JScrollPane(analyticsArea)));
        form.add(Box.createVerticalStrut(16));
        form.add(createFieldGroup("Optional Description", new JScrollPane(descriptionArea)));
        form.add(Box.createVerticalStrut(16));
        form.add(buildUploadSection(uploadButton, previewPanel));

        if (!AdminDialogSupport.showFormDialog(this, existing == null ? "Submit Report" : "Edit Report", form)) {
            return;
        }

        Report report = existing == null ? new Report() : existing;
        report.setBarangayId(account == null ? 0 : account.getBarangayId());
        report.setBarangayName(barangayName);
        report.setType("Waste Collection Report");
        report.setPurokAnalytics(analyticsArea.getText().trim());
        report.setMessage(descriptionArea.getText().trim());
        if (!selectedImages.isEmpty()) {
            report.setProof(selectedImages.get(0));
        }
        report.setStatus(existing == null ? "Under Review" : safe(existing.getStatus(), "Under Review"));
        report.setRead(existing != null && existing.isRead());

        boolean success = existing == null ? reportService.addReport(report) : reportService.updateReport(report);
        if (!success) {
            AdminDialogSupport.showFailure(this, "Unable to save the report.");
            return;
        }
        attachmentService.replaceImages("REPORT", report.getReportId(), selectedImages);
        AdminDialogSupport.showSuccess(this, existing == null ? "Report submitted successfully." : "Report updated successfully.");
    }

    private void openComplaintDialog(Complaint existing) {
        Account account = currentAccount();
        JTextField barangayField = styleInput(new JTextField(currentBarangayName()));
        barangayField.setEditable(false);
        JTextField createdField = styleInput(new JTextField(existing == null ? Timestamp.from(Instant.now()).toString() : formatTimestamp(existing.getCreatedAt())));
        createdField.setEditable(false);
        JComboBox<String> typeCombo = styleComboBox(new JComboBox<String>(new String[]{"Missed Collection", "Improper Disposal", "Vehicle Issue", "Other"}));
        typeCombo.setSelectedItem(existing == null ? "Missed Collection" : safe(existing.getType(), "Missed Collection"));
        JTextArea descriptionArea = new JTextArea(existing == null ? "" : safe(existing.getMessage()));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        List<byte[]> selectedImages = new ArrayList<byte[]>(attachmentService.getImageContents("COMPLAINT", existing == null ? 0 : existing.getComplaintId()));
        JPanel previewPanel = createPreviewPanel(selectedImages);
        JButton uploadButton = createUploadButton(selectedImages, previewPanel);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createFormSubtitle("Complaints include your barangay automatically and support multiple image attachments."));
        form.add(Box.createVerticalStrut(16));
        form.add(createSplitRow(createFieldGroup("Barangay Name", barangayField), createFieldGroup("Created", createdField)));
        form.add(Box.createVerticalStrut(16));
        form.add(createFieldGroup("Type", typeCombo));
        form.add(Box.createVerticalStrut(16));
        form.add(createFieldGroup("Description", new JScrollPane(descriptionArea)));
        form.add(Box.createVerticalStrut(16));
        form.add(buildUploadSection(uploadButton, previewPanel));

        if (!AdminDialogSupport.showFormDialog(this, existing == null ? "Submit Complaint" : "Edit Complaint", form)) {
            return;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            AdminDialogSupport.showFailure(this, "Description is required.");
            return;
        }

        Complaint complaint = existing == null ? new Complaint() : existing;
        complaint.setBarangayId(account == null ? 0 : account.getBarangayId());
        complaint.setBarangayName(currentBarangayName());
        complaint.setLocation(currentBarangayName());
        complaint.setType((String) typeCombo.getSelectedItem());
        complaint.setMessage(descriptionArea.getText().trim());
        if (!selectedImages.isEmpty()) {
            complaint.setProof(selectedImages.get(0));
        }
        complaint.setStatus(existing == null ? "Under Review" : safe(existing.getStatus(), "Under Review"));
        complaint.setRead(existing != null && existing.isRead());

        boolean success = existing == null ? complaintService.addComplaint(complaint) : complaintService.updateComplaint(complaint);
        if (!success) {
            AdminDialogSupport.showFailure(this, "Unable to save the complaint.");
            return;
        }
        attachmentService.replaceImages("COMPLAINT", complaint.getComplaintId(), selectedImages);
        AdminDialogSupport.showSuccess(this, existing == null ? "Complaint submitted successfully." : "Complaint updated successfully.");
    }

    private void openRequestDialog(Request existing) {
        Account account = currentAccount();
        JTextField barangayField = styleInput(new JTextField(currentBarangayName()));
        barangayField.setEditable(false);
        JTextField createdField = styleInput(new JTextField(existing == null ? Timestamp.from(Instant.now()).toString() : formatTimestamp(existing.getCreatedAt())));
        createdField.setEditable(false);
        JComboBox<String> typeCombo = styleComboBox(new JComboBox<String>(new String[]{"Equipment Support", "Schedule Change", "Additional Pickup", "Other"}));
        typeCombo.setSelectedItem(existing == null ? "Equipment Support" : safe(existing.getType(), "Equipment Support"));
        JTextArea descriptionArea = new JTextArea(existing == null ? "" : safe(existing.getMessage()));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        List<byte[]> selectedImages = new ArrayList<byte[]>(attachmentService.getImageContents("REQUEST", existing == null ? 0 : existing.getRequestId()));
        JPanel previewPanel = createPreviewPanel(selectedImages);
        JButton uploadButton = createUploadButton(selectedImages, previewPanel);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createFormSubtitle("Requests include your barangay automatically and can include multiple supporting images."));
        form.add(Box.createVerticalStrut(16));
        form.add(createSplitRow(createFieldGroup("Barangay Name", barangayField), createFieldGroup("Created", createdField)));
        form.add(Box.createVerticalStrut(16));
        form.add(createFieldGroup("Type", typeCombo));
        form.add(Box.createVerticalStrut(16));
        form.add(createFieldGroup("Description", new JScrollPane(descriptionArea)));
        form.add(Box.createVerticalStrut(16));
        form.add(buildUploadSection(uploadButton, previewPanel));

        if (!AdminDialogSupport.showFormDialog(this, existing == null ? "Submit Request" : "Edit Request", form)) {
            return;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            AdminDialogSupport.showFailure(this, "Description is required.");
            return;
        }

        Request request = existing == null ? new Request() : existing;
        request.setBarangayId(account == null ? 0 : account.getBarangayId());
        request.setBarangayName(currentBarangayName());
        request.setLocation(currentBarangayName());
        request.setType((String) typeCombo.getSelectedItem());
        request.setMessage(descriptionArea.getText().trim());
        if (!selectedImages.isEmpty()) {
            request.setProof(selectedImages.get(0));
        }
        request.setStatus(existing == null ? "Under Review" : safe(existing.getStatus(), "Under Review"));
        request.setRead(existing != null && existing.isRead());

        boolean success = existing == null ? requestService.addRequest(request) : requestService.updateRequest(request);
        if (!success) {
            AdminDialogSupport.showFailure(this, "Unable to save the request.");
            return;
        }
        attachmentService.replaceImages("REQUEST", request.getRequestId(), selectedImages);
        AdminDialogSupport.showSuccess(this, existing == null ? "Request submitted successfully." : "Request updated successfully.");
    }

    private JPanel buildUploadSection(JButton uploadButton, JPanel previewPanel) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(uploadButton);
        wrapper.add(Box.createVerticalStrut(10));
        wrapper.add(previewPanel);
        return wrapper;
    }

    private JButton createUploadButton(List<byte[]> selectedImages, JPanel previewPanel) {
        JButton button = createButton("Upload Images", "add.png", "add-white.png", 18);
        button.setPreferredSize(new Dimension(180, 35));
        button.addActionListener(event -> chooseImages(selectedImages, previewPanel));
        return button;
    }

    private JPanel createPreviewPanel(List<byte[]> images) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setOpaque(false);
        refreshPreviewPanel(panel, images);
        return panel;
    }

    private void refreshPreviewPanel(JPanel panel, List<byte[]> images) {
        panel.removeAll();
        if (images.isEmpty()) {
            panel.add(new JLabel("No images selected"));
        } else {
            for (byte[] image : images) {
                ImageIcon icon = new ImageIcon(image);
                ImageIcon scaled = new ImageIcon(icon.getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH));
                panel.add(new JLabel(scaled));
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private void chooseImages(List<byte[]> selectedImages, JPanel previewPanel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        selectedImages.clear();
        try {
            for (File file : chooser.getSelectedFiles()) {
                selectedImages.add(Files.readAllBytes(file.toPath()));
            }
            refreshPreviewPanel(previewPanel, selectedImages);
        } catch (Exception e) {
            AdminDialogSupport.showFailure(this, "Unable to load selected images.");
        }
    }

    private void deleteReport(Report report) {
        if (!AdminDialogSupport.confirmAction(this, "Delete Report", "Delete this report?")) {
            return;
        }
        if (reportService.deleteReport(report.getReportId())) {
            AdminDialogSupport.showSuccess(this, "Report deleted successfully.");
        } else {
            AdminDialogSupport.showFailure(this, "Failed to delete the report.");
        }
    }

    private void deleteComplaint(Complaint complaint) {
        if (!AdminDialogSupport.confirmAction(this, "Delete Complaint", "Delete this complaint?")) {
            return;
        }
        if (complaintService.deleteComplaint(complaint.getComplaintId())) {
            AdminDialogSupport.showSuccess(this, "Complaint deleted successfully.");
        } else {
            AdminDialogSupport.showFailure(this, "Failed to delete the complaint.");
        }
    }

    private void deleteRequest(Request request) {
        if (!AdminDialogSupport.confirmAction(this, "Delete Request", "Delete this request?")) {
            return;
        }
        if (requestService.deleteRequest(request.getRequestId())) {
            AdminDialogSupport.showSuccess(this, "Request deleted successfully.");
        } else {
            AdminDialogSupport.showFailure(this, "Failed to delete the request.");
        }
    }

    private List<Report> getVisibleReports() {
        String barangay = currentBarangayName();
        int barangayId = currentBarangayId();
        List<Report> source = archiveMode ? reportService.getArchivedReports() : reportService.getAllReports();
        List<Report> filtered = new ArrayList<Report>();
        for (Report report : source) {
            if (report.getBarangayId() == barangayId || barangay.equalsIgnoreCase(safe(report.getBarangayName(), barangay))) {
                filtered.add(report);
            }
        }
        return filtered;
    }

    private List<Complaint> getVisibleComplaints() {
        String barangay = currentBarangayName();
        int barangayId = currentBarangayId();
        List<Complaint> source = archiveMode ? complaintService.getArchivedComplaints() : complaintService.getAllComplaints();
        List<Complaint> filtered = new ArrayList<Complaint>();
        for (Complaint complaint : source) {
            if (complaint.getBarangayId() == barangayId || barangay.equalsIgnoreCase(safe(complaint.getBarangayName(), barangay))) {
                filtered.add(complaint);
            }
        }
        return filtered;
    }

    private List<Request> getVisibleRequests() {
        String barangay = currentBarangayName();
        int barangayId = currentBarangayId();
        List<Request> source = archiveMode ? requestService.getArchivedRequests() : requestService.getAllRequests();
        List<Request> filtered = new ArrayList<Request>();
        for (Request request : source) {
            if (request.getBarangayId() == barangayId || barangay.equalsIgnoreCase(safe(request.getBarangayName(), barangay))) {
                filtered.add(request);
            }
        }
        return filtered;
    }

    private String buildPurokAnalytics(Account account) {
        if (account == null) {
            return "No barangay checklist available.";
        }
        List<PurokChecklistItem> checklist = checklistService.getChecklist(account.getBarangayId(), currentBarangayName());
        int collected = 0;
        for (PurokChecklistItem item : checklist) {
            if (item.isCollected()) {
                collected++;
            }
        }
        return "Collected " + collected + " of " + checklist.size() + " puroks for " + currentBarangayName() + ".";
    }

    private int countReportResponses() {
        int count = 0;
        for (Report report : getVisibleReports()) {
            if (hasResponse(report.getResponseMessage(), report.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private int countComplaintResponses() {
        int count = 0;
        for (Complaint complaint : getVisibleComplaints()) {
            if (hasResponse(complaint.getResponseMessage(), complaint.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private int countRequestResponses() {
        int count = 0;
        for (Request request : getVisibleRequests()) {
            if (hasResponse(request.getResponseMessage(), request.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private boolean hasResponse(String responseMessage, String status) {
        return (responseMessage != null && !responseMessage.trim().isEmpty())
                || (status != null && !"Under Review".equalsIgnoreCase(status));
    }

    private int currentBarangayId() {
        Account account = currentAccount();
        return account == null ? 0 : account.getBarangayId();
    }

    private String currentBarangayName() {
        Account account = currentAccount();
        return account == null ? "Barangay" : safe(account.getBarangay(), "Barangay");
    }

    private Account currentAccount() {
        if (!UserSession.isActive()) {
            return null;
        }
        try {
            return new AccountDao(SQLConnection.getConnection()).findById(UserSession.getAccountId());
        } catch (Exception e) {
            return null;
        }
    }

    private void refreshView() {
        SwingUtilities.invokeLater(() -> {
            summaryPanel.removeAll();
            summaryPanel.add(buildSummary(), BorderLayout.CENTER);
            summaryPanel.revalidate();
            summaryPanel.repaint();
            rebuildViews();
            contentPanel.revalidate();
            contentPanel.repaint();
        });
    }

    private String formatTimestamp(Timestamp timestamp) {
        return timestamp == null ? "N/A" : timestamp.toString();
    }

    private String summarize(String value) {
        String safeValue = safe(value, "");
        return safeValue.length() > 42 ? safeValue.substring(0, 39) + "..." : safeValue;
    }

    private String safe(String value) {
        return safe(value, "");
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String[] icons() {
        return new String[]{"calendar.png", "circle-check.png", "circle-alert.png"};
    }

    private Color[] colors() {
        return new Color[]{
            new Color(139, 92, 246, 20),
            new Color(59, 130, 246, 20),
            new Color(232, 114, 82, 20)
        };
    }
}
