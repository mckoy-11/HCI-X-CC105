package main.ui.dialogs;

import main.model.Barangay;
import main.model.Schedule;
import main.model.Team;
import main.service.BarangayService;
import main.service.ScheduleService;
import main.service.TeamService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import static main.style.SystemStyle.*;
import static main.ui.components.CustomButton.createButton;

public class ScheduleDialog extends JDialog {

    private final ScheduleService scheduleService = new ScheduleService();
    private final BarangayService barangayService = new BarangayService();
    private final TeamService teamService = new TeamService();
    private final Schedule schedule;
    private final boolean isEdit;
    private boolean saved;

    private JComboBox<String> barangayCombo;
    private JComboBox<String> teamCombo;
    private JTextField dateField;
    private JTextField timeField;
    private JComboBox<String> statusCombo;

    public ScheduleDialog(Frame parent, Schedule schedule) {
        super(parent, schedule == null ? "Add Schedule" : "Edit Schedule", true);
        this.schedule = schedule;
        this.isEdit = schedule != null;
        initComponents();
        setSize(380, 500);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        
        AdminDialogSupport.configureFormDialog(this);

        JPanel mainPanel = Card(12);
        mainPanel.setLayout(new BorderLayout());

        JPanel header = Card(12, 0, SIDEBAR);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel(isEdit ? "Edit Schedule" : "Add Schedule");
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
        form.add(createDescription("Set the collection date, assigned team, and schedule status in one clean layout."));
        form.add(Box.createVerticalStrut(18));

        barangayCombo = createComboBox();
        loadBarangays();
        form.add(createFieldGroup("Barangay", barangayCombo));
        form.add(Box.createVerticalStrut(14));

        teamCombo = createComboBox();
        loadTeams();
        form.add(createFieldGroup("Collector Team", teamCombo));
        form.add(Box.createVerticalStrut(14));

        dateField = createField(schedule != null && schedule.getDate() != null ? schedule.getDate().toString() : "");
        timeField = createField(schedule != null && schedule.getTime() != null ? schedule.getTime().toString() : "");
        form.add(createSplitRow(
                createFieldGroup("Schedule Date (YYYY-MM-DD)", dateField),
                createFieldGroup("Schedule Time (HH:MM)", timeField)
        ));
        form.add(Box.createVerticalStrut(14));

        statusCombo = createComboBox(new String[]{"Scheduled", "In Progress", "Completed", "Missed"});
        if (schedule != null && schedule.getStatus() != null) {
            statusCombo.setSelectedItem(schedule.getStatus());
        }
        form.add(createFieldGroup("Status", statusCombo));

        content.add(AdminDialogSupport.createContentScroll(form), BorderLayout.CENTER);
        mainPanel.add(content, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        footer.setOpaque(false);

        JButton cancelBtn = createButton("Cancel", "", "", 0);
        cancelBtn.addActionListener(e -> dispose());
        footer.add(cancelBtn);

        JButton saveBtn = createButton(isEdit ? "Update" : "Save", "", "", 0);
        saveBtn.setBackground(SIDEBAR);
        saveBtn.setForeground(WHITE);
        saveBtn.addActionListener(e -> saveSchedule());
        footer.add(saveBtn);

        mainPanel.add(footer, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JTextField createField(String value) {
        return styleInput(new JTextField(value));
    }

    private JComboBox<String> createComboBox(String[] values) {
        return styleComboBox(new JComboBox<>(values));
    }

    private JComboBox<String> createComboBox() {
        return styleComboBox(new JComboBox<>());
    }

    private JLabel createDescription(String text) {
        return createFormSubtitle("<html><div style='width:" + FORM_WIDTH + "px;'>" + text + "</div></html>");
    }

    private void loadBarangays() {
        List<Barangay> barangays = barangayService.getAllBarangays();
        if (barangays != null) {
            for (Barangay barangay : barangays) {
                barangayCombo.addItem(barangay.getBarangayName());
            }
        }
        if (schedule != null && schedule.getBarangayName() != null) {
            barangayCombo.setSelectedItem(schedule.getBarangayName());
        }
    }

    private void loadTeams() {
        teamCombo.addItem("Unassigned");
        List<Team> teams = teamService.getAllTeams();
        if (teams != null) {
            for (Team team : teams) {
                teamCombo.addItem(team.getTeamName());
            }
        }
        if (schedule != null && schedule.getCollectorTeam() != null && !schedule.getCollectorTeam().trim().isEmpty()) {
            teamCombo.setSelectedItem(schedule.getCollectorTeam());
        }
    }

    private void saveSchedule() {
        String barangay = (String) barangayCombo.getSelectedItem();
        String team = (String) teamCombo.getSelectedItem();
        String dateText = dateField.getText().trim();
        String timeText = timeField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();

        if (barangay == null || barangay.trim().isEmpty()) {
            AdminDialogSupport.showFailure(this, "Please select a barangay.");
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateText);
        } catch (DateTimeParseException e) {
            AdminDialogSupport.showFailure(this, "Please enter the date as YYYY-MM-DD.");
            return;
        }

        LocalTime time;
        try {
            time = LocalTime.parse(timeText.length() == 5 ? timeText + ":00" : timeText);
        } catch (DateTimeParseException e) {
            AdminDialogSupport.showFailure(this, "Please enter the time as HH:MM.");
            return;
        }

        Schedule working = isEdit ? schedule : new Schedule();
        working.setBarangayName(barangay);
        working.setCollectorTeam("Unassigned".equalsIgnoreCase(team) ? null : team);
        working.setDate(date);
        working.setTime(time);
        working.setStatus(status);

        boolean ok = isEdit
                ? scheduleService.updateSchedule(working)
                : scheduleService.saveSchedule(working);

        if (!ok) {
            AdminDialogSupport.showFailure(
                    this,
                    isEdit ? "Failed to update the schedule." : "Failed to save the schedule."
            );
            return;
        }

        saved = true;
        dispose();
    }

    public static boolean showDialog(Frame parent, Schedule schedule) {
        ScheduleDialog dialog = new ScheduleDialog(parent, schedule);
        dialog.setVisible(true);
        return dialog.saved;
    }
}
