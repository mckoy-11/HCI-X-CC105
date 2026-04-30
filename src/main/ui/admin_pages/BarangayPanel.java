package main.ui.admin_pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import main.model.Barangay;
import main.model.Complaint;
import main.model.PopupItem;
import main.model.Report;
import main.model.Request;
import main.service.BarangayService;
import main.service.ComplaintService;
import main.service.ReportService;
import main.service.RequestService;
import static main.style.SystemStyle.BGCOLOR1;
import static main.style.SystemStyle.BGCOLOR2;
import static main.style.SystemStyle.Card;
import static main.style.SystemStyle.GradientPaint;
import static main.style.SystemStyle.SIDEBAR;
import static main.style.SystemStyle.SUBTITLEBOLD;
import static main.style.SystemStyle.roundPanel;
import main.store.DataTopics;
import static main.style.SystemStyle.SELECTED;
import static main.style.SystemStyle.TEXTCOLOR;
import static main.style.SystemStyle.WHITE;
import main.ui.components.CustomButton;
import main.ui.components.Header;
import main.ui.components.ReactivePanel;
import main.ui.components.ScrollableTable;
import main.ui.components.SummaryCards;
import main.ui.dialogs.AdminDialogSupport;
import main.ui.dialogs.MessageReviewDialog;

public class BarangayPanel extends ReactivePanel {

    private static final String BARANGAY_VIEW = "Barangay";
    private static final String REPORT_VIEW = "Report";
    private static final String COMPLAINT_VIEW = "Complaint";
    private static final String REQUEST_VIEW = "Request";

    private final BarangayService barangayService = new BarangayService();
    private final ReportService reportService = new ReportService();
    private final ComplaintService complaintService = new ComplaintService();
    private final RequestService requestService = new RequestService();

    private final java.awt.CardLayout contentLayout = new java.awt.CardLayout();
    private final JPanel contentPanel = Card(12);
    private final JPanel summaryPanel = new JPanel(new BorderLayout());

    private String currentView = BARANGAY_VIEW;

    public BarangayPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        listen(DataTopics.BARANGAYS, this::refreshCurrentView);
        listen(DataTopics.REPORTS, this::refreshCurrentView);
        listen(DataTopics.COMPLAINTS, this::refreshCurrentView);
        listen(DataTopics.REQUESTS, this::refreshCurrentView);

        add(new Header("Barangay"), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    private JPanel createMainContent() {
        JPanel content = GradientPaint(BGCOLOR1, BGCOLOR2, 0, false, 20);
        content.setLayout(new BorderLayout(0, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        summaryPanel.setOpaque(false);
        summaryPanel.add(createSummary(currentView), BorderLayout.CENTER);

        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 4));
        contentPanel.setLayout(contentLayout);
        rebuildViews();

        content.add(summaryPanel, BorderLayout.NORTH);
        content.add(contentPanel, BorderLayout.CENTER);
        return content;
    }

    private void rebuildViews() {
        contentPanel.removeAll();
        contentPanel.add(createBarangayView(), BARANGAY_VIEW);
        contentPanel.add(createReportView(), REPORT_VIEW);
        contentPanel.add(createComplaintView(), COMPLAINT_VIEW);
        contentPanel.add(createRequestView(), REQUEST_VIEW);
        contentLayout.show(contentPanel, currentView);
    }

    private JPanel createBarangayView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(createHeader("Barangay Management"), BorderLayout.NORTH);

        ScrollableTable table = new ScrollableTable(
                "Barangay", "Household", "Collection Day", "Contact", "Status", "Action"
        );

        for (Barangay barangay : barangayService.getAllBarangays()) {
            table.addRow(
                    safe(barangay.getBarangayName()),
                    barangay.getBarangayHousehold(),
                    safe(barangay.getCollectionDay()),
                    safe(barangay.getContact()),
                    safe(barangay.getStatus(), "Active")
            );
        }

        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(createHeader("Report Management"), BorderLayout.NORTH);

        ScrollableTable table = new ScrollableTable(
                "Barangay", "Message", "Status", "Created", "Read", "Action"
        );

        for (Report report : reportService.getAllReports()) {
            List<PopupItem> actions = new ArrayList<>();
            actions.add(new PopupItem("Review", "Open report review", () -> reviewReport(report)));
            actions.add(new PopupItem("Delete", "Remove this report", () -> deleteReport(report)));

            table.addRowWithAction(
                    safe(report.getBarangayName(), "Unassigned"),
                    summarize(report.getMessage()),
                    safe(report.getStatus(), "Under Review"),
                    report.getCreatedAt() == null ? "N/A" : report.getCreatedAt().toString(),
                    report.isRead() ? "Yes" : "No",
                    actions
            );
        }

        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createComplaintView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(createHeader("Complaint Management"), BorderLayout.NORTH);

        ScrollableTable table = new ScrollableTable(
                "Location", "Message", "Status", "Created", "Read", "Action"
        );

        for (Complaint complaint : complaintService.getAllComplaints()) {
            List<PopupItem> actions = new ArrayList<>();
            actions.add(new PopupItem("Review", "Open complaint review", () -> reviewComplaint(complaint)));
            actions.add(new PopupItem("Delete", "Remove this complaint", () -> deleteComplaint(complaint)));

            table.addRowWithAction(
                    safe(complaint.getLocation(), "N/A"),
                    summarize(complaint.getMessage()),
                    safe(complaint.getStatus(), "Under Review"),
                    complaint.getCreatedAt() == null ? "N/A" : complaint.getCreatedAt().toString(),
                    complaint.isRead() ? "Yes" : "No",
                    actions
            );
        }

        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRequestView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(createHeader("Request Management"), BorderLayout.NORTH);

        ScrollableTable table = new ScrollableTable(
                "Location", "Message", "Status", "Created", "Read", "Action"
        );

        for (Request request : requestService.getAllRequests()) {
            List<PopupItem> actions = new ArrayList<>();
            actions.add(new PopupItem("Review", "Open request review", () -> reviewRequest(request)));
            actions.add(new PopupItem("Delete", "Remove this request", () -> deleteRequest(request)));

            table.addRowWithAction(
                    safe(request.getLocation(), "N/A"),
                    summarize(request.getMessage()),
                    safe(request.getStatus(), "Under Review"),
                    request.getCreatedAt() == null ? "N/A" : request.getCreatedAt().toString(),
                    request.isRead() ? "Yes" : "No",
                    actions
            );
        }

        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHeader(String titleText) {
        JPanel header = Card(12, 0, SIDEBAR);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel(titleText);
        title.setForeground(WHITE);
        title.setFont(SUBTITLEBOLD);

        JPanel navBar = roundPanel(50);
        navBar.setLayout(new GridLayout(1, 4, 5, 0));
        navBar.setPreferredSize(new Dimension(560, 35));

        for (String view : new String[]{BARANGAY_VIEW, REPORT_VIEW, COMPLAINT_VIEW, REQUEST_VIEW}) {
            CustomButton button = new CustomButton(
                    view,
                    "",
                    "",
                    20,
                    80,
                    25,
                    80,
                    25,
                    currentView.equals(view) ? SELECTED : WHITE,
                    currentView.equals(view) ? SELECTED : WHITE,
                    currentView.equals(view) ? WHITE : TEXTCOLOR,
                    currentView.equals(view) ? WHITE : TEXTCOLOR,
                    false,
                    true
            );
            button.addActionListener(event -> switchView(view));
            navBar.add(button);
        }

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(navBar);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private SummaryCards createSummary(String type) {
        if (REPORT_VIEW.equals(type)) {
            return new SummaryCards(
                    new String[]{"Total Reports", "Reviewed", "Unread"},
                    new int[]{
                        reportService.getTotalReportCount(),
                        reportService.getReviewedReportCount(),
                        reportService.getUnreadReportCount()
                    },
                    new String[]{"All reports", "Approved or rejected", "Needs review"},
                    icons(),
                    colors()
            );
        }

        if (COMPLAINT_VIEW.equals(type)) {
            return new SummaryCards(
                    new String[]{"Total Complaints", "Reviewed", "Unread"},
                    new int[]{
                        complaintService.getTotalComplaintCount(),
                        complaintService.getReviewedComplaintCount(),
                        complaintService.getUnreadComplaintCount()
                    },
                    new String[]{"All complaints", "Approved or rejected", "Needs review"},
                    icons(),
                    colors()
            );
        }

        if (REQUEST_VIEW.equals(type)) {
            return new SummaryCards(
                    new String[]{"Total Requests", "Reviewed", "Unread"},
                    new int[]{
                        requestService.getTotalRequestCount(),
                        requestService.getReviewedRequestCount(),
                        requestService.getUnreadRequestCount()
                    },
                    new String[]{"All requests", "Approved or rejected", "Needs review"},
                    icons(),
                    colors()
            );
        }

        return new SummaryCards(
                new String[]{"Scheduled Collections", "Total Barangays", "Households"},
                new int[]{
                    barangayService.getTotalScheduleBarangay(),
                    barangayService.getTotalBarangayCount(),
                    barangayService.getTotalHousehold()
                },
                new String[]{"Scheduled areas", "Registered barangays", "Service coverage"},
                icons(),
                colors()
        );
    }

    private void switchView(String view) {
        currentView = view;
        refreshCurrentView();
    }

    private void reviewReport(Report report) {
        java.awt.Frame frame = AdminDialogSupport.resolveFrame(this);
        if (MessageReviewDialog.showReportDialog(frame, report, reportService)) {
            AdminDialogSupport.showSuccess(this, "Report updated successfully.");
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

    private void reviewComplaint(Complaint complaint) {
        java.awt.Frame frame = AdminDialogSupport.resolveFrame(this);
        if (MessageReviewDialog.showComplaintDialog(frame, complaint, complaintService)) {
            AdminDialogSupport.showSuccess(this, "Complaint updated successfully.");
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

    private void reviewRequest(Request request) {
        java.awt.Frame frame = AdminDialogSupport.resolveFrame(this);
        if (MessageReviewDialog.showRequestDialog(frame, request, requestService)) {
            AdminDialogSupport.showSuccess(this, "Request updated successfully.");
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

    private void refreshCurrentView() {
        SwingUtilities.invokeLater(() -> {
            summaryPanel.removeAll();
            summaryPanel.add(createSummary(currentView), BorderLayout.CENTER);
            summaryPanel.revalidate();
            summaryPanel.repaint();

            rebuildViews();
            contentPanel.revalidate();
            contentPanel.repaint();
        });
    }

    private String summarize(String value) {
        String safeValue = safe(value, "");
        return safeValue.length() > 36 ? safeValue.substring(0, 33) + "..." : safeValue;
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
