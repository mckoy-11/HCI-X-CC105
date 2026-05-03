package main.ui.admin_pages;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import main.model.HomeCardData;
import main.model.Schedule;
import main.service.HomeService;
import main.service.ScheduleService;
import static main.style.SystemStyle.*;
import main.ui.components.Header;
import main.ui.components.ReactivePanel;
import main.ui.components.ScrollableTable;
import main.ui.components.SummaryCards;
import main.store.DataTopics;

public final class HomePanel extends ReactivePanel {

    private HomeCardData cardData;
    private final ScheduleService scheduleService = new ScheduleService();

    public HomePanel() {
        setLayout(new BorderLayout());
        listen(DataTopics.SCHEDULES, this::refreshPanel);
        listen(DataTopics.COMPLAINTS, this::refreshPanel);
        listen(DataTopics.BARANGAYS, this::refreshPanel);

        HomeService homeService = new HomeService();
        cardData = homeService.getHomeCardData();

        add(new Header("Home"), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }
    
    private JPanel createContent() {
        JPanel content = GradientPaint(BGCOLOR1, BGCOLOR2, 0, false, 20);
        content.setLayout(new BorderLayout(10, 10));
        
        LocalDate today = LocalDate.now();
        String date = today.toString();
        
        JPanel cards = new JPanel();
        cards.setLayout(new BoxLayout(cards, BoxLayout.X_AXIS));
        cards.setOpaque(false);
        cards.add(
            new SummaryCards(
                new String[]{"Today's Collection", "Completed Collection", "Missed Collection", "Complaints"},
                new int[]{
                    cardData.getTodayCollection(),
                    cardData.getCompletedCollection(),
                    cardData.getMissedCollection(),
                    cardData.getUnreadComplaints()
                },
                new String[]{date, "Completion Rate", "Needs follow up", "Awaiting response"},
                new String[]{"calendar.png", "circle-check.png", "circle-alert.png", "message.png"},
                new Color[]{
                    new Color(59,  130, 246, 20),
                    new Color(129, 219, 122, 20),
                    new Color(232, 114, 82,  20),
                    new Color(139, 92,  246, 20), 
                },
                new String[]{"Brgy", "Brgy", "Brgy", "Unread"}
            )
        );
        
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setOpaque(false);
        
        wrapper.add(createDailySchedBoard(), BorderLayout.NORTH);
        wrapper.add(createTablePanel(), BorderLayout.CENTER);
        content.add(cards, BorderLayout.NORTH);
        content.add(wrapper, BorderLayout.CENTER);
        return content;
    }
    
    // daily card
    private JPanel createDailySchedBoard() {
        JPanel panel = new JPanel(new GridLayout(1, 7, 10, 0));
        panel.setOpaque(false);

        String[] days = {
            "Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"
        };

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        for (String day : days) {
            panel.add(createDayCard(day, today, weekStart, weekEnd));
        }

        return panel;
    }
    
    private JPanel createDayCard(String dayLabel,
        LocalDate today, LocalDate weekStart, LocalDate weekEnd) {

        JPanel card = Card(12);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(148, 200));
        card.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel header = new JLabel(dayLabel, SwingConstants.CENTER);
        header.setFont(SUBTITLEPLAIN);

        JPanel content = createDayScheduleInfo(card, dayLabel, today, weekStart, weekEnd);

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }
    
    private JPanel createTablePanel() {
        JPanel wrapper = Card(12);
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(0, 0, 12, 4));
        
        JPanel topPanel = Card(12, 0, SIDEBAR);
        topPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        topPanel.setLayout(new BorderLayout());
        
        JLabel subTitle = new JLabel("Daily Collection Schedule");
        subTitle.setForeground(WHITE);
        subTitle.setFont(SUBTITLEBOLD);
        topPanel.add(subTitle, BorderLayout.WEST);

        ScrollableTable table = createTable();
        
        wrapper.add(topPanel, BorderLayout.NORTH);
        wrapper.add(table, BorderLayout.CENTER);
        return wrapper;
    }
    
    private JPanel createDayScheduleInfo(JPanel card, String dayLabel,
            LocalDate today, LocalDate weekStart, LocalDate weekEnd) {

        JPanel panel = Card(0, 12, null);
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        ScheduleService service = new ScheduleService();
        java.util.List<String> barangays =
                service.getBarangaysScheduledForDay(weekStart, weekEnd, dayLabel);

        String todayName = today.getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);

        boolean isToday = dayLabel.equalsIgnoreCase(todayName);

        if (isToday) {
            card.setBackground(SIDEBAR);
        } else {
            card.setBackground(Color.WHITE);
        }

        // Only show barangays if it's TODAY
        if (isToday && barangays != null && !barangays.isEmpty()) {
            barangays.forEach((b) -> {
                panel.add(createWhiteLabel(b));
            });
        } else if (!isToday && barangays != null && !barangays.isEmpty()) {
            // For other days, show in green
            barangays.forEach((b) -> {
                panel.add(createGreenLabel(b));
            });
        } else {
            panel.add(createCenteredLabel("No schedule"));
        }

        return panel;
    }
        
    private JLabel createCenteredLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JLabel createGreenLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(TEXTCOLOR);
        return label;
    }

    private JLabel createWhiteLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(Color.WHITE);
        return label;
    }
    
    private ScrollableTable createTable() {
        ScrollableTable table = new ScrollableTable(
            "Barangay", "Team", "Date", "Time", "Status"
        );

        java.util.List<Schedule> schedules = scheduleService.getAllSchedules();
        
        // FILTER: Show ONLY today's schedules
        LocalDate today = LocalDate.now();
        schedules.stream()
            .filter(s -> s.getDate() != null && s.getDate().equals(today))
            .forEach(s -> {
                // Determine status based on report submission
                // PENDING if no report submitted, COMPLETED if report submitted
                String displayStatus = s.getStatus();
                if (displayStatus == null || displayStatus.isEmpty()) {
                    displayStatus = "PENDING";
                }
                
                table.addRow(
                    s.getBarangayName(),
                    s.getCollectorTeam(),
                    s.getDate(),
                    s.getTime(),
                    displayStatus
                );
            });

        return table;
    }

    private void refreshPanel() {
        SwingUtilities.invokeLater(() -> {
            removeAll();
            HomeService homeService = new HomeService();
            cardData = homeService.getHomeCardData();
            add(new Header("Home"), BorderLayout.NORTH);
            add(createContent(), BorderLayout.CENTER);
            revalidate();
            repaint();
        });
    }
}
