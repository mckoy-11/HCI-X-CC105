package main.ui.officials_pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import main.dao.AccountDao;
import main.database.SQLConnection;
import main.model.Account;
import main.model.Announcement;
import main.model.CollectionInfo;
import main.model.Complaint;
import main.model.PurokChecklistItem;
import main.model.Report;
import main.model.Request;
import main.model.Schedule;
import main.model.UserSession;
import main.service.AnnouncementService;
import main.service.ComplaintService;
import main.service.PurokChecklistService;
import main.service.ReportService;
import main.service.RequestService;
import main.service.ScheduleService;
import static main.style.SystemStyle.BGCOLOR1;
import static main.style.SystemStyle.BGCOLOR2;
import static main.style.SystemStyle.Card;
import static main.style.SystemStyle.GradientPaint;
import static main.style.SystemStyle.SIDEBAR;
import static main.style.SystemStyle.SUBTITLEBOLD;
import static main.style.SystemStyle.TEXTCOLOR;
import main.store.DataTopics;
import main.ui.components.Header;
import main.ui.components.ReactivePanel;
import main.ui.components.SummaryCards;
import main.ui.dialogs.AdminDialogSupport;

public class HomePanel extends ReactivePanel {

    private final ScheduleService scheduleService = new ScheduleService();
    private final ReportService reportService = new ReportService();
    private final ComplaintService complaintService = new ComplaintService();
    private final RequestService requestService = new RequestService();
    private final AnnouncementService announcementService = new AnnouncementService();
    private final PurokChecklistService checklistService = new PurokChecklistService();

    public HomePanel() {
        setLayout(new BorderLayout());
        listen(DataTopics.SCHEDULES, this::refreshPanel);
        listen(DataTopics.REPORTS, this::refreshPanel);
        listen(DataTopics.COMPLAINTS, this::refreshPanel);
        listen(DataTopics.REQUESTS, this::refreshPanel);
        listen(DataTopics.CHECKLIST, this::refreshPanel);
        listen(DataTopics.ANNOUNCEMENTS, this::refreshPanel);
        listen(DataTopics.COLLECTION_INFO, this::refreshPanel);
        pollEvery(5000, this::refreshPanel);

        add(new Header("Home"), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel root = GradientPaint(BGCOLOR1, BGCOLOR2, 0, false, 20);
        root.setLayout(new BorderLayout(0, 14));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        Account account = loadCurrentAccount();
        String barangayName = account == null ? "Barangay" : safe(account.getBarangay(), "Barangay");
        int barangayId = account == null ? 0 : account.getBarangayId();

        Announcement activeAnnouncement = announcementService.getActiveAnnouncement();
        if (activeAnnouncement != null) {
            root.add(buildAnnouncementPanel(activeAnnouncement), BorderLayout.NORTH);
        }

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(buildSummarySection(barangayId, barangayName), BorderLayout.NORTH);
        center.add(buildMainSection(barangayId, barangayName), BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildAnnouncementPanel(Announcement announcement) {
        JPanel panel = Card(18, new Color(255, 252, 226));
        panel.setLayout(new BorderLayout(12, 0));
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(safe(announcement.getTitle(), "MENRO Announcement"));
        title.setFont(SUBTITLEBOLD);
        title.setForeground(TEXTCOLOR);

        JLabel message = new JLabel("<html><div style='width:780px;'>" + safe(announcement.getMessage()) + "</div></html>");
        message.setForeground(new Color(70, 80, 70));
        text.add(title);
        text.add(Box.createVerticalStrut(6));
        text.add(message);

        JButton dismiss = new JButton("Dismiss");
        dismiss.setBackground(SIDEBAR);
        dismiss.setForeground(Color.WHITE);
        dismiss.addActionListener(event -> {
            if (announcementService.dismissAnnouncement(announcement.getAnnouncementId())) {
                refreshPanel();
            }
        });

        panel.add(text, BorderLayout.CENTER);
        panel.add(dismiss, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildSummarySection(int barangayId, String barangayName) {
        List<Schedule> schedules = scheduleService.getByBarangay(barangayName);
        List<PurokChecklistItem> checklist = checklistService.getChecklist(barangayId, barangayName);
        int collectedCount = (int) checklist.stream().filter(PurokChecklistItem::isCollected).count();
        int nextCollectionDays = 0;
        String nextScheduleLabel = "No schedule";

        Schedule nextSchedule = schedules.stream()
                .filter(schedule -> schedule.getDate() != null)
                .filter(schedule -> !schedule.getDate().isBefore(LocalDate.now()))
                .sorted((left, right) -> left.getDate().compareTo(right.getDate()))
                .findFirst()
                .orElse(null);
        if (nextSchedule != null) {
            nextCollectionDays = (int) Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), nextSchedule.getDate()));
            nextScheduleLabel = nextSchedule.getDate().toString();
        }

        int reportResponses = countRespondedReports(barangayId, barangayName);
        int complaintResponses = countRespondedComplaints(barangayId, barangayName);
        int requestResponses = countRespondedRequests(barangayId, barangayName);

        return new SummaryCards(
                new String[]{
                    "Next Collection Schedule",
                    "Purok Collected",
                    "MENRO Report Responses",
                    "Complaint Responses",
                    "Request Responses"
                },
                new int[]{nextCollectionDays, collectedCount, reportResponses, complaintResponses, requestResponses},
                new String[]{
                    "Days until next collection",
                    "Checked puroks",
                    "Reports with MENRO feedback",
                    "Complaints with MENRO feedback",
                    "Requests with MENRO feedback"
                },
                new String[]{"calendar.png", "circle-check.png", "mail.png", "mail-warning.png", "mail-question-mark.png"},
                new Color[]{
                    new Color(59, 130, 246, 20),
                    new Color(129, 219, 122, 20),
                    new Color(139, 92, 246, 20),
                    new Color(232, 114, 82, 20),
                    new Color(244, 180, 0, 20)
                },
                new String[]{nextScheduleLabel, "of " + checklist.size(), "", "", ""}
        );
    }

    private JPanel buildMainSection(int barangayId, String barangayName) {
        JPanel wrapper = new JPanel(new GridLayout(1, 2, 14, 0));
        wrapper.setOpaque(false);
        wrapper.add(buildChecklistPanel(barangayId, barangayName));
        wrapper.add(buildCollectionInfoPanel(barangayName));
        return wrapper;
    }

    private JPanel buildChecklistPanel(int barangayId, String barangayName) {
        JPanel panel = Card(16);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Purok Waste Checklist");
        title.setFont(SUBTITLEBOLD);
        title.setForeground(TEXTCOLOR);
        panel.add(title, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        List<PurokChecklistItem> checklist = checklistService.getChecklist(barangayId, barangayName);
        if (checklist.isEmpty()) {
            listPanel.add(new JLabel("No purok checklist is available yet."));
        } else {
            for (PurokChecklistItem item : checklist) {
                JCheckBox checkBox = new JCheckBox(item.getPurokName());
                checkBox.setOpaque(false);
                checkBox.setSelected(item.isCollected());
                checkBox.addActionListener(event -> {
                    if (!checklistService.updateCollected(item.getChecklistId(), checkBox.isSelected())) {
                        checkBox.setSelected(!checkBox.isSelected());
                        AdminDialogSupport.showFailure(this, "Unable to update purok status.");
                    }
                });

                JLabel updatedLabel = new JLabel(item.getUpdatedAt() == null ? "Not updated yet" : "Updated " + item.getUpdatedAt().toString());
                updatedLabel.setForeground(new Color(110, 120, 110));

                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.add(checkBox, BorderLayout.WEST);
                row.add(updatedLabel, BorderLayout.EAST);
                listPanel.add(row);
                listPanel.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCollectionInfoPanel(String barangayName) {
        JPanel panel = Card(16);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Collection Information");
        title.setFont(SUBTITLEBOLD);
        title.setForeground(TEXTCOLOR);
        panel.add(title);
        panel.add(Box.createVerticalStrut(12));

        CollectionInfo info = scheduleService.getCollectionInfo(barangayName);
        if (info == null) {
            panel.add(createInfoLine("Assigned Team", "Unassigned"));
            panel.add(Box.createVerticalStrut(10));
            panel.add(createInfoLine("Truck Details", "Unavailable"));
            panel.add(Box.createVerticalStrut(10));
            panel.add(createInfoLine("ETA", "Pending"));
            panel.add(Box.createVerticalStrut(10));
            panel.add(createInfoLine("Status", "No active collection"));
            return panel;
        }

        panel.add(createInfoLine("Assigned Team", safe(info.getAssignedTeam(), "Unassigned")));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createInfoLine("Truck Details", buildTruckLabel(info)));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createInfoLine("ETA", safe(info.getEta(), "Pending")));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createInfoLine("Status", safe(info.getStatus(), "Scheduled")));
        return panel;
    }

    private JPanel createInfoLine(String labelText, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(110, 120, 110));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(SUBTITLEBOLD.deriveFont(16f));
        valueLabel.setForeground(TEXTCOLOR);
        row.add(label, BorderLayout.NORTH);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
    }

    private String buildTruckLabel(CollectionInfo info) {
        String plate = safe(info.getTruckPlateNumber(), "No truck");
        String type = safe(info.getTruckType());
        return type.isEmpty() ? plate : plate + " - " + type;
    }

    private int countRespondedReports(int barangayId, String barangayName) {
        int count = 0;
        for (Report report : reportService.getAllReports()) {
            boolean sameBarangay = report.getBarangayId() == barangayId
                    || barangayName.equalsIgnoreCase(safe(report.getBarangayName(), barangayName));
            if (sameBarangay && hasResponse(report.getResponseMessage(), report.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private int countRespondedComplaints(int barangayId, String barangayName) {
        int count = 0;
        for (Complaint complaint : complaintService.getAllComplaints()) {
            boolean sameBarangay = complaint.getBarangayId() == barangayId
                    || barangayName.equalsIgnoreCase(safe(complaint.getBarangayName(), barangayName));
            if (sameBarangay && hasResponse(complaint.getResponseMessage(), complaint.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private int countRespondedRequests(int barangayId, String barangayName) {
        int count = 0;
        for (Request request : requestService.getAllRequests()) {
            boolean sameBarangay = request.getBarangayId() == barangayId
                    || barangayName.equalsIgnoreCase(safe(request.getBarangayName(), barangayName));
            if (sameBarangay && hasResponse(request.getResponseMessage(), request.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private boolean hasResponse(String responseMessage, String status) {
        return (responseMessage != null && !responseMessage.trim().isEmpty())
                || (status != null && !"Under Review".equalsIgnoreCase(status));
    }

    private Account loadCurrentAccount() {
        if (!UserSession.isActive()) {
            return null;
        }
        try {
            return new AccountDao(SQLConnection.getConnection()).findById(UserSession.getAccountId());
        } catch (Exception e) {
            return null;
        }
    }

    private void refreshPanel() {
        SwingUtilities.invokeLater(() -> {
            removeAll();
            add(new Header("Home"), BorderLayout.NORTH);
            add(buildContent(), BorderLayout.CENTER);
            revalidate();
            repaint();
        });
    }

    private String safe(String value) {
        return safe(value, "");
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
