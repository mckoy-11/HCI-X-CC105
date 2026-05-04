package main.ui.menro.pages;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import main.model.PopupItem;
import main.model.Schedule;
import main.service.ScheduleService;
import static main.ui.style.SystemStyle.*;
import static main.ui.components.CustomButton.createButton;
import main.ui.components.Header;
import main.ui.components.ReactivePanel;
import main.ui.components.ScrollableTable;
import main.ui.menro.dialogs.ScheduleFormDialog;
import main.store.DataTopics;

public final class SchedulePanel extends ReactivePanel {

    private final ScheduleService service = new ScheduleService();

    public SchedulePanel() {
        setLayout(new BorderLayout());
        listen(DataTopics.SCHEDULES, this::refreshPanel);
        listen(DataTopics.TEAMS, this::refreshPanel);
        listen(DataTopics.BARANGAYS, this::refreshPanel);
        add(new Header("Schedule"), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }
    
    private JPanel createContent() {
        JPanel content = GradientPaint(BGCOLOR1, BGCOLOR2, 0, false, 20);
        content.setLayout(new BorderLayout());
        content.setBorder(new EmptyBorder(20, 20, 15, 15));
        
        JPanel topPanel = Card(12, 0, SIDEBAR);
        topPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        topPanel.setLayout(new BorderLayout());
        
        JLabel subTitle = new JLabel("Barangay Collection Schedule");
        subTitle.setForeground(WHITE);
        subTitle.setFont(SUBTITLEBOLD);
        topPanel.add(subTitle, BorderLayout.WEST);
        
        JButton addBtn = createButton("Add", "add.png", "add-white.png", 20);
        addBtn.addActionListener(e -> addSchedule());
        topPanel.add(addBtn, BorderLayout.EAST);
        
        JPanel wrapper = Card(12);
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(0, 0, 12, 4));
        
        wrapper.add(topPanel, BorderLayout.NORTH);
        wrapper.add(createTable(), BorderLayout.CENTER);
        content.add(wrapper, BorderLayout.CENTER);
        return content;
    }

    private ScrollableTable createTable() {
        ScrollableTable table = new ScrollableTable(
            "Barangay", "Admin", "Contact",
            "Team", "Date", "Time", "Status", "Action"
        );

        java.util.List<Schedule> schedules = service.getAllSchedules();
        
        schedules.forEach((s) -> {
            java.util.List<PopupItem> items = new java.util.ArrayList<>();
            items.add(
                new PopupItem("Edit", "Edit this schedule", () -> editSchedule(s))
            );
            items.add(
                new PopupItem("Delete", "Remove this schedule", () -> deleteSchedule(s))
            );
            
            table.addRowWithAction(
                    s.getBarangayName(),
                    s.getBarangayAdmin(),
                    s.getContactNumber(),
                    s.getCollectorTeam(),
                    s.getDate(),
                    s.getTime(),
                    s.getStatus(),
                    items
            );
        });

        return table;
    }
    
    private void addSchedule() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        ScheduleFormDialog dialog = new ScheduleFormDialog(parent, null);
        dialog.setVisible(true);
    }

    private void editSchedule(Schedule schedule) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        ScheduleFormDialog dialog = new ScheduleFormDialog(parent, schedule);
        dialog.setVisible(true);
    }

    private void deleteSchedule(Schedule schedule) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this schedule?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = service.deleteSchedule(schedule.getId());
            if (!success) {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete schedule",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void refreshPanel() {
        SwingUtilities.invokeLater(() -> {
            removeAll();
            add(new Header("Schedule"), BorderLayout.NORTH);
            add(createContent(), BorderLayout.CENTER);
            revalidate();
            repaint();
        });
    }
}
