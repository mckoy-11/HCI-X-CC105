package main.ui.barangay.pages;

import main.ui.style.SystemStyle;
import main.ui.components.SummaryCards;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import main.model.Report;
import main.service.ReportService;
import main.ui.components.Header;
import main.ui.components.ScrollablePanel;

import static main.ui.style.SystemStyle.SIDEBAR;

public class ReportsPanel extends JPanel {

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(SystemStyle.BACKGROUND);

        // Main content panel with padding
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BorderLayout(0, 20));
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Summary cards section (fixed height, doesn't expand)
        JPanel summarySection = createSummaryCards();
        wrapper.add(summarySection, BorderLayout.NORTH);

        // Report view section (expands to fill remaining space)
        JPanel reportView = createReportView();
        wrapper.add(reportView, BorderLayout.CENTER);

        add(new Header("Report Management"), BorderLayout.NORTH);
        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel createSummaryCards() {
        // Fixed-height container for summary cards
        JPanel cardsContainer = SystemStyle.createTransparentPanel(new GridLayout(1, 4, 12, 0));
        
        // Set preferred height to prevent expansion
        cardsContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        cardsContainer.setPreferredSize(new Dimension(0, 120));

        cardsContainer.add(new SummaryCards(
                new String[]{"Next Schedule", "Reports", "Complaints", "Request"},
                new int[]{1, 4, 1, 1},
                new String[]{"March 21, 2025", "1/4 Report", "Unread", "Unread"},
                new String[]{"calendar.png", "circle-check.png", "circle-alert.png", "message.png"},
                new Color[]{
                        new Color(59, 130, 246, 20),
                        new Color(129, 219, 122, 20),
                        new Color(232, 114, 82, 20),
                        new Color(139, 92, 246, 20),
                },
                new String[]{"", "", "", ""}
        ));

        return cardsContainer;
    }

    private JPanel createReportView() {
        JPanel container = SystemStyle.Card(12);
        container.setLayout(new BorderLayout());

        // Header section
        JLabel title = new JLabel("All Reports");
        title.setFont(SystemStyle.SUBTITLEBOLD.deriveFont(18f));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel header = SystemStyle.Card(12, 0, SIDEBAR);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        header.setPreferredSize(new Dimension(0, 50));
        header.add(title, BorderLayout.CENTER);

        container.add(header, BorderLayout.NORTH);

        // Grid for report cards using BoxLayout for proper sizing
        JPanel grid = new JPanel();
        grid.setOpaque(false);
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
        grid.setBorder(new EmptyBorder(10, 10, 10, 10));

        try {
            ReportService reportService = new ReportService();
            java.util.List<Report> reports = reportService.getAllReports();
            
            if (reports == null || reports.isEmpty()) {
                JLabel emptyLabel = new JLabel("No reports available");
                emptyLabel.setForeground(SystemStyle.MUTED_TEXT);
                grid.add(emptyLabel);
            } else {
                for (Report report : reports) {
                    JPanel card = createReportCard(report);
                    // Set max width to container width
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
                    grid.add(card);
                    grid.add(Box.createVerticalStrut(15));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading reports: " + e.getMessage());
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading reports");
            errorLabel.setForeground(Color.RED);
            grid.add(errorLabel);
        }

        // Add vertical glue to push cards to top and prevent stretching
        grid.add(Box.createVerticalGlue());

        // Wrap grid in scrollable container
        container.add(new ScrollablePanel(grid), BorderLayout.CENTER);

        return container;
    }

    private JPanel createReportCard(Report report) {
        JPanel card = SystemStyle.Card(12);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top section - Title and location
        JLabel title = new JLabel("Report #" + report.getReportId());
        title.setFont(SystemStyle.SUBTITLEBOLD.deriveFont(15f));
        title.setForeground(SystemStyle.TEXTCOLOR);

        JLabel location = new JLabel("Location: " + report.getBarangayName());
        location.setFont(SystemStyle.BODYBOLD.deriveFont(12f));
        location.setForeground(SystemStyle.MUTED_TEXT);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(location);

        card.add(top, BorderLayout.NORTH);

        // Center - Description text
        JTextArea description = new JTextArea(report.getMessage());
        description.setFont(SystemStyle.BODYPLAIN.deriveFont(12f));
        description.setForeground(SystemStyle.textDark);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setOpaque(false);
        description.setEditable(false);

        card.add(description, BorderLayout.CENTER);

        // Bottom - Status badge
        JLabel status = new JLabel(report.getStatus());
        status.setOpaque(true);
        status.setBorder(new EmptyBorder(6, 12, 6, 12));
        status.setFont(SystemStyle.BUTTONBOLD.deriveFont(12f));
        status.setForeground(SystemStyle.WHITE);

        switch (report.getStatus().toLowerCase()) {
            case "approved":
                status.setBackground(new Color(62, 177, 83));
                break;
            case "on process":
                status.setBackground(new Color(235, 180, 32));
                break;
            default:
                status.setBackground(SystemStyle.PRIMARY);
        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(status);

        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }
}