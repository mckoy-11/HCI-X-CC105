package main.ui.admin_pages;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.model.*;
import main.service.*;
import main.store.DataTopics;
import main.ui.components.*;

import static main.style.SystemStyle.*;
import static main.ui.components.CustomButton.createButton;
import main.ui.dialogs.PersonnelFormDialog;
import main.ui.dialogs.TeamFormDialog;
import main.ui.dialogs.TruckFormDialog;

public class BarangayPanel extends ReactivePanel {

    private static final String BARANGAY_VIEW = "Barangay";
    private static final String REPORT_VIEW = "Report";
    private static final String COMPLAINT_VIEW = "Complaint";
    private static final String REQUEST_VIEW = "Request";

    private final BarangayService barangayService = new BarangayService();
    private final ReportService reportService = new ReportService();
    private final ComplaintService complaintService = new ComplaintService();
    private final RequestService requestService = new RequestService();

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = Card(12);
    private final JPanel summaryPanel = new JPanel(new BorderLayout());

    private String currentView = BARANGAY_VIEW;

    public BarangayPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        listen(DataTopics.BARANGAYS, this::refreshUI);
        listen(DataTopics.REPORTS, this::refreshUI);
        listen(DataTopics.COMPLAINTS, this::refreshUI);
        listen(DataTopics.REQUESTS, this::refreshUI);

        add(new Header("Barangay Management"), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    // =========================================================
    // MAIN
    // =========================================================

    private JPanel createMainContent() {
        JPanel root = GradientPaint(BGCOLOR1, BGCOLOR2, 0, false, 20);
        root.setLayout(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        summaryPanel.setOpaque(false);
        summaryPanel.add(createSummary(currentView), BorderLayout.CENTER);

        contentPanel.setLayout(contentLayout);
        rebuildViews();

        root.add(summaryPanel, BorderLayout.NORTH);
        root.add(contentPanel, BorderLayout.CENTER);

        return root;
    }

    private void rebuildViews() {
        contentPanel.removeAll();
        contentPanel.add(createBarangayView(), BARANGAY_VIEW);
        contentPanel.add(createReportView(), REPORT_VIEW);
        contentPanel.add(createComplaintView(), COMPLAINT_VIEW);
        contentPanel.add(createRequestView(), REQUEST_VIEW);
        contentLayout.show(contentPanel, currentView);
    }

    // =========================================================
    // VIEWS
    // =========================================================

    private JPanel createBarangayView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        panel.add(createHeader("Barangay Management"), BorderLayout.NORTH);

        ScrollableTable table = new ScrollableTable(
                "Barangay", "Purok", "Population", "Households", "Collection Day", "Status"
        );

        for (Barangay b : barangayService.getAllBarangays()) {
            table.addRow(
                    safe(b.getBarangayName()),
                    b.getPurokCount(),
                    b.getPopulation(),
                    b.getBarangayHousehold(),
                    safe(b.getCollectionDay()),
                    safe(b.getStatus(), "Active")
            );
        }

        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        panel.add(createHeader("Report Management"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 15, 15)); // same as Team
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (Report r : reportService.getAllReports()) {
            grid.add(createReportCard(r));
        }

        ScrollablePanel scroll = new ScrollablePanel(grid);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createComplaintView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        panel.add(createHeader("Complaint Management"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 15, 15));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (Complaint c : complaintService.getAllComplaints()) {
            grid.add(createComplaintCard(c));
        }

        ScrollablePanel scroll = new ScrollablePanel(grid);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createRequestView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        panel.add(createHeader("Request Management"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 15, 15));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (Request r : requestService.getAllRequests()) {
            grid.add(createRequestCard(r));
        }

        ScrollablePanel scroll = new ScrollablePanel(grid);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // CARD LIST
    // =========================================================

    private <T> JPanel buildCardList(List<T> list, java.util.function.Function<T, JPanel> mapper) {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        for (T item : list) {
            gbc.gridy = y++;
            container.add(mapper.apply(item), gbc);
        }

        return new ScrollablePanel(container);
    }

    // =========================================================
    // CARDS (CLEAN MANAGEMENT STYLE)
    // =========================================================

    private JPanel createBaseCard(String title, String status) {
        JPanel card = Card(20);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setBackground(WHITE);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(SUBTITLEBOLD);

        JLabel statusLbl = new JLabel(safe(status, "Pending"));
        statusLbl.setOpaque(true);
        statusLbl.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusLbl.setBackground(new Color(220, 252, 231));
        statusLbl.setForeground(new Color(22, 163, 74));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titleLbl, BorderLayout.WEST);
        top.add(statusLbl, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);

        return card;
    }

    private JPanel createReportCard(Report r) {
        JPanel card = createBaseCard("Report #" + r.getReportId(), r.getStatus());

        JPanel body = vertical();
        body.add(new JLabel("Barangay: " + safe(r.getBarangayName())));
        body.add(new JLabel("Date: " + r.getCreatedAt()));
        body.add(new JLabel("Message: " + safe(r.getMessage())));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createComplaintCard(Complaint c) {
        JPanel card = createBaseCard("Complaint #" + c.getComplaintId(), c.getStatus());

        JPanel body = vertical();
        body.add(new JLabel("Location: " + safe(c.getLocation())));
        body.add(new JLabel("Message: " + safe(c.getMessage())));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRequestCard(Request r) {
        JPanel card = createBaseCard("Request #" + r.getRequestId(), r.getStatus());

        JPanel body = vertical();
        body.add(new JLabel("Type: " + safe(r.getType())));
        body.add(new JLabel("Location: " + safe(r.getLocation())));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // =========================================================
    // HEADER (MATCH MANAGEMENT)
    // =========================================================

    private JPanel createHeader(String titleText) {
        JPanel header = Card(12, 0, SIDEBAR);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel(titleText);
        title.setForeground(WHITE);
        title.setFont(SUBTITLEBOLD);

        JPanel navBar = roundPanel(50);
        navBar.setLayout(new GridLayout(1, 3, 5, 0));
        navBar.setPreferredSize(new Dimension(500, 35));

        for (String view : new String[]{BARANGAY_VIEW, REPORT_VIEW, COMPLAINT_VIEW, REQUEST_VIEW}) {
            CustomButton btn = new CustomButton(
                    view, "", "", 20, 70, 25, 70, 25,
                    currentView.equals(view) ? SELECTED : WHITE,
                    currentView.equals(view) ? SELECTED : WHITE,
                    currentView.equals(view) ? WHITE : TEXTCOLOR,
                    currentView.equals(view) ? WHITE : TEXTCOLOR,
                    false, true
            );
            btn.addActionListener(e -> switchView(view));
            navBar.add(btn);
        }

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(navBar);
        
        /*JButton addButton = createButton("Add", "add.png", "add-white.png", 20);
        addButton.setPreferredSize(new Dimension(100, 35));
        addButton.addActionListener(event -> openCreateDialog());
        right.add(addButton);*/

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private SummaryCards createSummary(String view) {
        if (REPORT_VIEW.equals(view)) {
            List<Report> list = reportService.getAllReports();
            return new SummaryCards(
                    new String[]{"Total Reports", "Resolved", "Pending"},
                    new int[]{
                            list.size(),
                            (int) list.stream().filter(r -> "Resolved".equalsIgnoreCase(r.getStatus())).count(),
                            (int) list.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count()
                    },
                    new String[]{"All reports", "Completed", "Needs action"},
                    icons(), colors()
            );
        }

        if (COMPLAINT_VIEW.equals(view)) {
            List<Complaint> list = complaintService.getAllComplaints();
            return new SummaryCards(
                    new String[]{"Total Complaints", "Resolved", "Pending"},
                    new int[]{
                            list.size(),
                            (int) list.stream().filter(c -> "Resolved".equalsIgnoreCase(c.getStatus())).count(),
                            (int) list.stream().filter(c -> "Pending".equalsIgnoreCase(c.getStatus())).count()
                    },
                    new String[]{"All complaints", "Handled", "Waiting"},
                    icons(), colors()
            );
        }

        if (REQUEST_VIEW.equals(view)) {
            List<Request> list = requestService.getAllRequests();
            return new SummaryCards(
                    new String[]{"Total Requests", "Approved", "Pending"},
                    new int[]{
                            list.size(),
                            (int) list.stream().filter(r -> "Approved".equalsIgnoreCase(r.getStatus())).count(),
                            (int) list.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count()
                    },
                    new String[]{"All requests", "Processed", "Waiting"},
                    icons(), colors()
            );
        }

        List<Barangay> list = barangayService.getAllBarangays();
        return new SummaryCards(
                new String[]{"Total Barangays", "Active", "Inactive"},
                new int[]{
                        list.size(),
                        (int) list.stream().filter(b -> "Active".equalsIgnoreCase(b.getStatus())).count(),
                        (int) list.stream().filter(b -> "Inactive".equalsIgnoreCase(b.getStatus())).count()
                },
                new String[]{"Coverage", "Operational", "Inactive"},
                BrgyIcons(), colors()
        );
    }

    private void switchView(String view) {
        currentView = view;
        refreshUI();
    }

    private void refreshUI() {
        SwingUtilities.invokeLater(() -> {
            summaryPanel.removeAll();
            summaryPanel.add(createSummary(currentView), BorderLayout.CENTER);

            rebuildViews();
            revalidate();
            repaint();
        });
    }

    private JPanel vertical() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        return p;
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private String safe(String v, String def) {
        return (v == null || v.isEmpty()) ? def : v;
    }

    private String[] BrgyIcons() {
        return new String[]{"calendar.png", "circle-check.png", "circle-alert.png"};
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
    
    /*private void openCreateDialog() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        switch (currentView) {
            case BARANGAY_VIEW:
                BarangayFormDialog brgydialog = new BarangayDialogForm(parent, null);
                brgydialog.setVisible(true);
                break;
            case REPORT_VIEW:
                ReportFormDialog reportdialog = new ReportFormDialog(parent, null);
                reportdialog.setVisible(true);
                break;
            case COMPLAINT_VIEW:
                ComaplaintFormDialog complaintDialog = new ComaplaintFormDialog(parent, null);
                complaintDialog.setVisible(true);
                break;
            case REQUEST_VIEW:
                RequestFormDialog requestDialog = new RequestFormDialog(parent, null);
                requestDialog.setVisible(true);
                break;
        }
    }*/
}