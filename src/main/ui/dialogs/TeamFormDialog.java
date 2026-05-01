package main.ui.dialogs;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import main.model.Team;
import main.model.Truck;
import main.model.Personnel;
import main.service.TeamService;
import main.service.TruckService;
import main.service.PersonnelService;
import static main.style.SystemStyle.*;
import main.style.BaseFormDialog;
import main.style.SystemStyle;

public final class TeamFormDialog extends BaseFormDialog {

    private final TeamService teamService = new TeamService();
    private final TruckService truckService = new TruckService();
    private final PersonnelService personnelService = new PersonnelService();

    private final JTextField teamNameField = styleInput(new JTextField());
    private final JComboBox<Personnel> leaderCombo = styleComboBox(new JComboBox<>());
    private final JComboBox<Personnel> driverCombo = styleComboBox(new JComboBox<>());
    private final JComboBox<Truck> truckComboBox = styleComboBox(new JComboBox<>());

    private final JPanel collectorListPanel;
    private final Map<Integer, JCheckBox> collectorCheckboxes = new HashMap<>();
    private final JLabel memberCountLabel;

    private List<Personnel> personnelList = new ArrayList<>();

    private static final int MAX_MEMBERS = 6;

    private final Team existingTeam;
    private final boolean isEditMode;

    public TeamFormDialog(Frame parent, Team team) {
        super(parent, team == null ? "Create Team" : "Edit Team");
        this.existingTeam = team;
        this.isEditMode = team != null;

        this.collectorListPanel = new JPanel();
        this.memberCountLabel = new JLabel("0/" + MAX_MEMBERS);
        memberCountLabel.setFont(BUTTONBOLD.deriveFont(14f));
        memberCountLabel.setForeground(PRIMARY);

        loadData();
        populateData();
        initFormBody();
        bindEvents();
        updateMemberCount();
        updateLeaderDriverAvailability();
    }

    @Override
    protected JPanel createFormBody() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(220, 0));

        JPanel collectorHeader = new JPanel(new BorderLayout());
        collectorHeader.setOpaque(false);

        JLabel collectorTitle = createFieldLabel("Collectors (max " + (MAX_MEMBERS - 2) + ")");
        collectorHeader.add(collectorTitle, BorderLayout.NORTH);

        memberCountLabel.setForeground(PRIMARY);
        memberCountLabel.setFont(BUTTONBOLD.deriveFont(13f));
        collectorHeader.add(memberCountLabel, BorderLayout.EAST);

        collectorListPanel.setLayout(new BoxLayout(collectorListPanel, BoxLayout.Y_AXIS));
        collectorListPanel.setBackground(WHITE);

        JScrollPane collectorScroll = new JScrollPane(collectorListPanel);
        collectorScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        collectorScroll.setPreferredSize(new Dimension(200, 200));
        collectorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        leftPanel.add(collectorHeader, BorderLayout.NORTH);
        leftPanel.add(collectorScroll, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 0;

        addFormField(gbc, "Team Name", teamNameField);
        gbc.gridy++;

        addFormField(gbc, "Assigned Truck", truckComboBox);
        gbc.gridy++;

        addFormField(gbc, "Driver", driverCombo);
        gbc.gridy++;

        addFormField(gbc, "Team Leader (Supervisor)", leaderCombo);

        JPanel sidePanel = new JPanel(new BorderLayout(15, 0));
        sidePanel.setOpaque(false);
        sidePanel.add(leftPanel, BorderLayout.WEST);
        sidePanel.add(new JPanel(), BorderLayout.CENTER);

        addFormFieldFull(gbc, null, sidePanel);

        return null;
    }

    @Override
    protected void saveForm() {
        if (teamNameField.getText().trim().isEmpty()) {
            showError("Team name is required");
            return;
        }

        int selectedCount = getSelectedCollectorCount();
        if (driverCombo.getSelectedItem() != null) selectedCount++;
        if (leaderCombo.getSelectedItem() != null) selectedCount++;

        if (selectedCount > MAX_MEMBERS) {
            showError("Team cannot exceed " + MAX_MEMBERS + " members");
            return;
        }

        Team team = existingTeam != null ? existingTeam : new Team();
        team.setTeamName(teamNameField.getText().trim());
        team.setStatus("Unassigned");

        Personnel leader = (Personnel) leaderCombo.getSelectedItem();
        Personnel driver = (Personnel) driverCombo.getSelectedItem();
        Truck truck = (Truck) truckComboBox.getSelectedItem();

        team.setLeaderId(leader != null ? leader.getId() : 0);
        team.setDriverId(driver != null ? driver.getId() : 0);
        team.setTruckId(truck != null ? truck.getId() : 0);

        boolean ok = isEditMode ? teamService.updateTeam(team) : teamService.addTeam(team);

        if (ok) dispose();
        else showError("Save failed");
    }

    private void loadData() {
        truckComboBox.removeAllItems();
        for (Truck t : truckService.getAllTrucks()) {
            truckComboBox.addItem(t);
        }

        leaderCombo.removeAllItems();
        driverCombo.removeAllItems();

        personnelList.clear();

        List<Personnel> all = personnelService.getAllPersonnel();

        for (Personnel p : all) {
            String role = p.getRole();
            if ("Driver".equals(role) || "Collector".equals(role) || "Supervisor".equals(role)) {
                personnelList.add(p);
                leaderCombo.addItem(p);
                driverCombo.addItem(p);
            }
        }

        collectorListPanel.removeAll();
        collectorCheckboxes.clear();

        for (Personnel p : all) {
            if ("Collector".equals(p.getRole())) {
                JCheckBox cb = new JCheckBox(p.getFullName());
                cb.setBackground(WHITE);
                cb.setFont(BODYPLAIN.deriveFont(13f));
                collectorCheckboxes.put(p.getId(), cb);
                collectorListPanel.add(cb);
                cb.addActionListener(e -> {
                    updateMemberCount();
                    updateLeaderDriverAvailability();
                });
            }
        }

        collectorListPanel.revalidate();
        collectorListPanel.repaint();
    }

    private void populateData() {
        if (existingTeam == null) return;

        teamNameField.setText(existingTeam.getTeamName());

        for (int i = 0; i < truckComboBox.getItemCount(); i++) {
            Truck t = truckComboBox.getItemAt(i);
            if (t != null && t.getId() == existingTeam.getTruckId()) {
                truckComboBox.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < leaderCombo.getItemCount(); i++) {
            Personnel p = leaderCombo.getItemAt(i);
            if (p != null && p.getId() == existingTeam.getLeaderId()) {
                leaderCombo.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < driverCombo.getItemCount(); i++) {
            Personnel p = driverCombo.getItemAt(i);
            if (p != null && p.getId() == existingTeam.getDriverId()) {
                driverCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void bindEvents() {
        leaderCombo.addActionListener(e -> {
            updateLeaderDriverAvailability();
            updateMemberCount();
        });

        driverCombo.addActionListener(e -> {
            updateLeaderDriverAvailability();
            updateMemberCount();
        });
    }

    private int getSelectedCollectorCount() {
        int count = 0;
        for (JCheckBox cb : collectorCheckboxes.values()) {
            if (cb.isSelected()) count++;
        }
        return count;
    }

    private void updateMemberCount() {
        int count = getSelectedCollectorCount();
        if (driverCombo.getSelectedItem() != null) count++;
        if (leaderCombo.getSelectedItem() != null) count++;

        memberCountLabel.setText(count + "/" + MAX_MEMBERS);

        if (count > MAX_MEMBERS) memberCountLabel.setForeground(ERROR_TEXT);
        else memberCountLabel.setForeground(PRIMARY);
    }

    private void updateLeaderDriverAvailability() {
        Personnel leader = (Personnel) leaderCombo.getSelectedItem();
        Personnel driver = (Personnel) driverCombo.getSelectedItem();

        DefaultComboBoxModel<Personnel> leaderModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Personnel> driverModel = new DefaultComboBoxModel<>();

        Set<Personnel> blocked = new HashSet<>();
        if (leader != null) blocked.add(leader);
        if (driver != null) blocked.add(driver);

        for (JCheckBox cb : collectorCheckboxes.values()) {
            if (cb.isSelected()) {
                Personnel p = findPersonnelByName(cb.getText());
                if (p != null) blocked.add(p);
            }
        }

        for (Personnel p : personnelList) {
            if (driver == null || !p.equals(driver)) leaderModel.addElement(p);
            if (leader == null || !p.equals(leader)) driverModel.addElement(p);
        }

        leaderCombo.setModel(leaderModel);
        driverCombo.setModel(driverModel);

        if (leader != null && !leader.equals(driver)) leaderCombo.setSelectedItem(leader);
        if (driver != null && !driver.equals(leader)) driverCombo.setSelectedItem(driver);
    }

    private Personnel findPersonnelByName(String name) {
        for (Personnel p : personnelList) {
            if (p.getFullName().equals(name)) return p;
        }
        return null;
    }

    @Override
    protected JPanel createActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);

        JButton cancelBtn = SystemStyle.createFormButton("Cancel", false);
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = SystemStyle.createFormButton("Save Team", true);
        saveBtn.addActionListener(e -> saveForm());

        actions.add(cancelBtn);
        actions.add(Box.createHorizontalStrut(ROW_SPACING));
        actions.add(saveBtn);

        return actions;
    }
}