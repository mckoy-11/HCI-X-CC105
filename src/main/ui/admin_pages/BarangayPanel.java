package main.ui.admin_pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import main.model.Barangay;
import main.model.Complaint;
import main.model.PopupItem;
import main.model.Report;
import main.model.Request;
import main.model.Truck;
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
import static main.style.SystemStyle.createCapsuleLabel;
import static main.style.SystemStyle.createFormButton;
import static main.style.SystemStyle.createTransparentPanel;
import main.ui.components.CustomButton;
import main.ui.components.Header;
import main.ui.components.ReactivePanel;
import main.ui.components.ScrollablePanel;
import main.ui.components.ScrollableTable;
import main.ui.components.SummaryCards;
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
                "Barangay", "Purok", "Population", "Household", "Collection Day", "Status", "Action"
        );

        for (Barangay barangay : barangayService.getAllBarangays()) {
            table.addRow(
                    safe(barangay.getBarangayName()),
                    barangay.getPurokCount(),
                    barangay.getPopulation(),
                    barangay.getBarangayHousehold(),
                    safe(barangay.getCollectionDay()),
                    safe(barangay.getStatus(), "Active")
            );
        }

        panel.add(table, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createReport() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(createHeader("Truck Management"), BorderLayout.NORTH);

        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0); // spacing between cards

        int y = 0;
        for (Report report : reportService.getAllReports()) {
            gbc.gridy = y++;
            container.add(createReportCard(report), gbc);
        }

        ScrollablePanel scrollable = new ScrollablePanel(container);
        panel.add(scrollable, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createReportCard(Report report) {
        JPanel card = Card(20);
        card.setLayout(new BorderLayout(15, 10));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setBackground(WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        
        
        return card;
    }
    
    private JPanel createComplaintView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(createHeader("Complaint Management"), BorderLayout.NORTH);

        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);

        int y = 0;
        for (Complaint complaint : complaintService.getAllComplaints()) {
            gbc.gridy = y++;
            container.add(createComplaintCard(complaint), gbc);
        }

        ScrollablePanel scrollable = new ScrollablePanel(container);
        panel.add(scrollable, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createComplaintCard(Complaint complaint) {

        JPanel card = Card(20);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setBackground(WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        // =========================================================
        // TOP SECTION
        // =========================================================
        JPanel top = createTransparentPanel(new BorderLayout());

        JLabel id = new JLabel("Report no: " + complaint.getComplaintId());
        JLabel status = createCapsuleLabel(
                complaint.getStatus(),
                Color.LIGHT_GRAY,
                TEXTCOLOR
        );

        top.add(id, BorderLayout.WEST);
        top.add(status, BorderLayout.EAST);

        // =========================================================
        // IMAGE
        // =========================================================
        JLabel image = createImageLabel(complaint.getProof());
        image.setPreferredSize(new Dimension(0, 120));
        image.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        image.setHorizontalAlignment(SwingConstants.CENTER);

        // =========================================================
        // DETAILS SECTION
        // =========================================================
        JPanel details = createTransparentPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel barangay = new JLabel("Barangay: " + complaint.getBarangayName());
        JLabel location = new JLabel("Location: " + complaint.getLocation());
        JLabel date = new JLabel("Date: " + complaint.getCreatedAt());

        JTextArea description = new JTextArea(complaint.getMessage());
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        description.setOpaque(false);
        description.setBorder(null);

        details.add(barangay);
        details.add(location);
        details.add(date);
        details.add(description);

        // =========================================================
        // BUTTONS
        // =========================================================
        JPanel btns = createTransparentPanel(new GridLayout(1, 2, 10, 0));

        JButton contact = createFormButton("Contact", false);
        JButton view = createFormButton("Review", false);

        btns.add(contact);
        btns.add(view);

        // =========================================================
        // CARD ASSEMBLY
        // =========================================================
        card.add(top);
        card.add(Box.createVerticalStrut(10));
        card.add(image);
        card.add(Box.createVerticalStrut(10));
        card.add(details);
        card.add(Box.createVerticalStrut(10));
        card.add(btns);

        return card;
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

    private JPanel createComplaint() {
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
    
    private PopupItem reviewRequest(Request request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private PopupItem deleteRequest(Request request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private PopupItem reviewComplaint(Complaint complaint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private PopupItem deleteComplaint(Complaint complaint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private PopupItem reviewReport(Report report) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private PopupItem deleteReport(Report report) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private static JLabel createImageLabel(byte[] imageBytes) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage img = ImageIO.read(bis);

            ImageIcon icon = new ImageIcon(img);

            return new JLabel(icon);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JLabel("No Image");
    }
}
