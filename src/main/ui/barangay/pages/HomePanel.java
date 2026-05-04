/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.ui.barangay.pages;

import main.ui.style.SystemStyle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import javax.swing.border.EmptyBorder;
import main.model.Announcement;
import main.model.Barangay;
import main.model.HomeCardData;
import main.model.Schedule;
import main.service.AnnouncementService;
import main.service.BarangayService;
import main.service.ScheduleService;
import static main.ui.style.SystemStyle.createTransparentPanel;
import main.ui.components.Header;
import main.ui.components.ScrollablePanel;
import main.ui.components.ScrollableTable;
import main.ui.components.SummaryCards;
import static main.ui.style.SystemStyle.SIDEBAR;

/**
 * Waste management dashboard for barangay users.
 * Uses existing SystemStyle helpers for spacing, colors, and card layout.
 */
public class HomePanel extends JPanel {

    private final JPanel statsGrid = new JPanel();
    private final JPanel dashboardGrid = new JPanel();
    private final DefaultTableModel complianceTableModel;
    private final JTable complianceTable;
    
    private HomeCardData cardData = new HomeCardData();
    private final AnnouncementService announcementService = new AnnouncementService();
    private final ScheduleService scheduleService = new ScheduleService();

    public HomePanel() {
        setLayout(new BorderLayout());
        setBackground(SystemStyle.BACKGROUND);

        JPanel pageWrapper = new JPanel(new BorderLayout(0, 10));
        pageWrapper.setOpaque(false);
        pageWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel announcementPanel = createAnnouncementSection();
        if (announcementPanel != null) {
            pageWrapper.add(announcementPanel, BorderLayout.NORTH);
        }

        pageWrapper.add(createContent(), BorderLayout.CENTER);

        complianceTableModel = createComplianceTableModel();
        complianceTable = createComplianceTable(complianceTableModel);

        add(new Header("Home"), BorderLayout.NORTH);
        add(pageWrapper, BorderLayout.CENTER);
    }
    
    private JPanel createAnnouncementSection() {
        try {
            Announcement activeAnnouncement = announcementService.getActiveAnnouncement();
            if (activeAnnouncement == null) {
                return null;
            }

            // Check if older than 7 days
            Timestamp createdAt = activeAnnouncement.getCreatedAt();

            if (createdAt != null) {
                LocalDateTime createdDateTime = createdAt.toLocalDateTime();

                // Compare with current time
                if (ChronoUnit.DAYS.between(createdDateTime, LocalDateTime.now()) > 7) {
                    // Archive it
                    announcementService.dismissAnnouncement(activeAnnouncement.getAnnouncementId());
                    return null;
                }
            }

            JPanel headerCard = SystemStyle.Card(12);
            headerCard.setLayout(new BoxLayout(headerCard, BoxLayout.X_AXIS));
            headerCard.setBorder(BorderFactory.createEmptyBorder(6, 24, 10, 24));

            JLabel messageLabel = new JLabel(activeAnnouncement.getMessage());
            messageLabel.setFont(SystemStyle.SUBTITLEBOLD.deriveFont(16f));
            messageLabel.setForeground(SystemStyle.TEXTCOLOR);

            JButton viewButton = SystemStyle.createFormButton("View", true);
            viewButton.setPreferredSize(new Dimension(110, 40));
            viewButton.addActionListener(e -> {
                try {
                    announcementService.dismissAnnouncement(activeAnnouncement.getAnnouncementId());
                    // Refresh the panel
                    revalidate();
                    repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error dismissing announcement: " + ex.getMessage());
                }
            });

            headerCard.add(messageLabel);
            headerCard.add(Box.createHorizontalGlue());
            headerCard.add(viewButton);
            return headerCard;
        } catch (Exception e) {
            // Log or handle
            return null;
        }
    }
    
    private JPanel createContent() {
        JPanel content = createTransparentPanel(new BorderLayout(0, 10));
        JPanel cards = createTransparentPanel(new GridLayout(1, 4, 0, 0));
            cards.add(
                new SummaryCards(
                    new String[]{"Next Schedule", "Total Pickups", "Report", "Responce"},
                    new int[]{
                        cardData.getTodayCollection(),
                        cardData.getCompletedCollection(),
                        cardData.getMissedCollection(),
                        cardData.getUnreadComplaints()
                    },
                    new String[]{"", "", "Needs follow up", "Awaiting response"},
                    new String[]{"calendar.png", "circle-check.png", "circle-alert.png", "message.png"},
                    new Color[]{
                        new Color(59,  130, 246, 20),
                        new Color(129, 219, 122, 20),
                        new Color(232, 114, 82,  20),
                        new Color(139, 92,  246, 20), 
                    },
                    new String[]{
                        cardData.getTodayBarangay(), 
                        cardData.getCompletedBarangay(), 
                        cardData.getMissedBarangay(), 
                        "Unread"
                    }
                )
            );
            content.add(cards, BorderLayout.NORTH);
            content.add(createPanel(), BorderLayout.CENTER);
        return content;
    }

    private JPanel createPanel() {
        dashboardGrid.setOpaque(false);
        dashboardGrid.setLayout(new GridLayout(1, 2, 15, 20));
        dashboardGrid.add(createComplianceCard());
        dashboardGrid.add(createCollectionsCard());
        return dashboardGrid;
    }

    private JPanel createComplianceCard() {
        JPanel card = SystemStyle.Card(12);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(0, 0, 8, 4));
        
        JLabel heading = new JLabel("Waste Compliance List");
        heading.setFont(SystemStyle.SUBTITLEBOLD.deriveFont(18f));
        heading.setForeground(Color.WHITE);
        
        JPanel header = SystemStyle.Card(12, 0, SIDEBAR);
        header.setBorder(new EmptyBorder(10, 0, 10, 0));
        header.setPreferredSize(new Dimension(0, 50));
        header.add(heading);
        
        ScrollableTable table = new ScrollableTable("Purok", "Status");
        
        BarangayService barangayService= new BarangayService();
        for (Barangay b : barangayService.getAllBarangays()) {
            table.addRow(
                    b.getPurokCount(),
                    b.getStatus()
            );
        }
        card.add(header, BorderLayout.NORTH);
        card.add(table, BorderLayout.CENTER);

        return card;
    }

    private DefaultTableModel createComplianceTableModel() {
        return new DefaultTableModel(new Object[]{"Purok No.", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
    }

    private JTable createComplianceTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(SystemStyle.CELL_FONT);
        table.setForeground(SystemStyle.textDark);
        table.setBackground(SystemStyle.WHITE);
        table.setSelectionBackground(SystemStyle.SELECTED_ROW);
        table.setGridColor(SystemStyle.CARD_BORDER);

        table.getTableHeader().setFont(SystemStyle.BUTTONBOLD.deriveFont(12f));
        table.getTableHeader().setBackground(SystemStyle.BGCOLOR2);
        table.getTableHeader().setForeground(SystemStyle.TEXTCOLOR);
        table.getTableHeader().setReorderingAllowed(false);

        JComboBox<String> statusEditor = new JComboBox<>(new String[]{"Compliant", "Not Compliant", "Pending"});
        SystemStyle.styleComboBox(statusEditor);
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(statusEditor));

        return table;
    }

    private JPanel createCollectionsCard() {
        try {
            List<Schedule> schedules = scheduleService.getAllSchedules();
            // Filter for today's schedules or all, depending on logic. For now, all.

            JPanel card = SystemStyle.Card(12);
            card.setLayout(new BorderLayout());
            card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel heading = new JLabel("Scheduled Collections");
            heading.setFont(SystemStyle.SUBTITLEBOLD.deriveFont(18f));
            heading.setForeground(SystemStyle.TEXTCOLOR);

            card.add(heading, BorderLayout.NORTH);

            if (schedules.isEmpty()) {
                JPanel emptyState = new JPanel();
                emptyState.setOpaque(false);
                emptyState.setLayout(new GridBagLayout());

                JLabel noScheduleLabel = new JLabel("No schedule");
                noScheduleLabel.setFont(SystemStyle.SUBTITLEBOLD.deriveFont(16f));
                noScheduleLabel.setForeground(SystemStyle.MUTED_TEXT);

                JLabel contactLabel = new JLabel("Contact admin");
                contactLabel.setFont(SystemStyle.BODYPLAIN.deriveFont(14f));
                contactLabel.setForeground(SystemStyle.MUTED_TEXT);

                JPanel textPanel = new JPanel();
                textPanel.setOpaque(false);
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                textPanel.add(noScheduleLabel);
                textPanel.add(Box.createVerticalStrut(4));
                textPanel.add(contactLabel);

                emptyState.add(textPanel);
                card.add(emptyState, BorderLayout.CENTER);
            } else {
                JPanel listWrapper = new JPanel();
                listWrapper.setOpaque(false);
                listWrapper.setLayout(new BoxLayout(listWrapper, BoxLayout.Y_AXIS));
                listWrapper.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

                for (Schedule schedule : schedules) {
                    listWrapper.add(createScheduleItemCard(schedule));
                    listWrapper.add(Box.createVerticalStrut(14));
                }
                
                ScrollablePanel scrollable = new ScrollablePanel(listWrapper);
                card.add(scrollable, BorderLayout.CENTER);
            }

            return card;
        } catch (Exception e) {
            JPanel errorCard = SystemStyle.Card(12);
            errorCard.setLayout(new BorderLayout());
            errorCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel errorLabel = new JLabel("Error loading collections: " + e.getMessage());
            errorLabel.setFont(SystemStyle.BODYPLAIN);
            errorLabel.setForeground(SystemStyle.ERROR_TEXT);
            
            errorCard.add(errorLabel, BorderLayout.CENTER);
            return errorCard;
        }
    }
    
    private JPanel createScheduleItemCard(Schedule schedule) {
        JPanel card = SystemStyle.Card(18, SystemStyle.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        
        LocalDate date = schedule.getDate();
        
        String day = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        JLabel areaLabel = new JLabel((schedule.getBarangayName() != null ? schedule.getBarangayName() : "Unknown Area")+ " • " + day);
        areaLabel.setFont(SystemStyle.SUBTITLEBOLD.deriveFont(15f));
        areaLabel.setForeground(SystemStyle.TEXTCOLOR);

        JLabel locationLabel = new JLabel("Scheduled for " + (schedule.getDate() != null ? schedule.getDate().toString() : "N/A"));
        locationLabel.setFont(SystemStyle.BODYBOLD.deriveFont(12f));
        locationLabel.setForeground(SystemStyle.MUTED_TEXT);

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.add(areaLabel);
        titleGroup.add(Box.createVerticalStrut(4));
        titleGroup.add(locationLabel);

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgePanel.setOpaque(false);
        badgePanel.add(createStatusBadge(schedule.getStatus()));

        card.add(titleGroup, BorderLayout.NORTH);
        card.add(badgePanel, BorderLayout.EAST);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new GridLayout(4, 1, 4, 4));
        details.add(new JLabel("Collector: " + (schedule.getCollectorTeam() != null ? schedule.getCollectorTeam() : "N/A")));
        details.add(new JLabel("Truck: " + (schedule.getTruckPlateNumber() != null ? schedule.getTruckPlateNumber() : "N/A")));
        details.add(new JLabel("Driver: " + (schedule.getBarangayAdmin() != null ? schedule.getBarangayAdmin() : "N/A")));
        details.add(new JLabel("Time: " + (schedule.getTime() != null ? schedule.getTime().toString() : "N/A")));

        for (Component child : details.getComponents()) {
            if (child instanceof JLabel) {
                ((JLabel) child).setFont(SystemStyle.BODYPLAIN.deriveFont(12f));
                ((JLabel) child).setForeground(SystemStyle.MUTED_TEXT);
            }
        }

        card.add(details, BorderLayout.SOUTH);
        return card;
    }

    private JLabel createStatusBadge(String text) {
        JLabel badge = new JLabel(text);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        badge.setFont(SystemStyle.BUTTONBOLD.deriveFont(12f));
        badge.setForeground(SystemStyle.WHITE);

        switch (text.toLowerCase()) {
            case "completed": 
                badge.setBackground(new Color(62, 177, 83));
                break;
            case "pending": 
                badge.setBackground(new Color(235, 180, 32));
                break;
            case "delayed": 
                badge.setBackground(new Color(216, 74, 82));
                break;
            default: 
                badge.setBackground(SystemStyle.PRIMARY);
                break;
        }
        return badge;
    }

}

