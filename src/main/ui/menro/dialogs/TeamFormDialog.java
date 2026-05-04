package main.ui.menro.dialogs;

import main.model.Team;
import main.model.Truck;
import main.model.Personnel;
import main.service.TeamService;
import main.service.TruckService;
import main.service.PersonnelService;
import main.ui.style.BaseFormDialog;
import main.store.DataChangeBus;
import main.store.DataTopics;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static main.ui.style.SystemStyle.*;

public class TeamFormDialog extends BaseFormDialog {

    private final TeamService teamService = new TeamService();
    private final TruckService truckService = new TruckService();
    private final PersonnelService personnelService = new PersonnelService();

    private final JTextField teamNameField = styleInput(new JTextField());
    private final JComboBox<Personnel> leaderCombo = styleComboBox(new JComboBox<>());
    private final JComboBox<Personnel> driverCombo = styleComboBox(new JComboBox<>());
    private final JComboBox<Truck> truckCombo = styleComboBox(new JComboBox<>());

    private final JPanel collectorPanel = new JPanel();
    private final Map<Integer, JCheckBox> collectorMap = new LinkedHashMap<>();
    private final JLabel countLabel = new JLabel();

    private final List<Personnel> personnelList = new ArrayList<>();

    private static final int MAX_COLLECTORS = 6;

    private final Team existing;
    private final boolean editMode;

    public TeamFormDialog(Frame parent, Team team) {
        super(parent, team == null ? "Create Team" : "Edit Team");

        this.existing = team;
        this.editMode = team != null;

        loadData();
        buildCollectors();
        populate();

        initFormBody();
        bindEvents();
        updateCount();
    }

    @Override
    protected JPanel createFormBody() {
        JPanel root = new JPanel(new BorderLayout(20, 0));
        root.setOpaque(false);

        root.add(buildCollectorSection(), BorderLayout.WEST);
        root.add(buildFormSection(), BorderLayout.CENTER);

        return root;
    }

    private JPanel buildCollectorSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(220, 400));

        JLabel title = createFieldLabel("Collectors (max " + MAX_COLLECTORS + ")");
        countLabel.setFont(BUTTONBOLD);
        countLabel.setForeground(PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);

        collectorPanel.setLayout(new BoxLayout(collectorPanel, BoxLayout.Y_AXIS));
        collectorPanel.setBackground(WHITE);

        JScrollPane scroll = new JScrollPane(collectorPanel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildFormSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 12, 0);

        addField(panel, g, "Team Name", teamNameField);
        g.gridy++;

        addField(panel, g, "Truck", truckCombo);
        g.gridy++;

        addField(panel, g, "Driver", driverCombo);
        g.gridy++;

        addField(panel, g, "Leader", leaderCombo);

        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints g, String label, JComponent input) {
        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setOpaque(false);

        wrap.add(createFieldLabel(label), BorderLayout.NORTH);
        wrap.add(input, BorderLayout.CENTER);

        panel.add(wrap, g);
    }

    private void loadData() {
        loadTrucks();
        loadPersonnel();
    }

    private void loadTrucks() {
        truckCombo.removeAllItems();

        List<Truck> trucks = truckService.getAllUnassignedTrucks();

        if (editMode && existing.getTruckId() > 0) {
            Truck t = truckService.getTruckById(existing.getTruckId());
            if (t != null && trucks.stream().noneMatch(x -> x.getId() == t.getId())) {
                trucks.add(0, t);
            }
        }

        for (Truck t : trucks) {
            truckCombo.addItem(t);
        }
    }

    private void loadPersonnel() {
        leaderCombo.removeAllItems();
        driverCombo.removeAllItems();

        personnelList.clear();

        List<Personnel> list = personnelService.getAllUnassignedPersonnel();

        if (editMode) {
            includeExisting(list);
        }

        for (Personnel p : list) {
            personnelList.add(p);

            if (isDriverOrLeader(p)) {
                leaderCombo.addItem(p);
                driverCombo.addItem(p);
            }
        }
    }

    private void buildCollectors() {
        collectorPanel.removeAll();
        collectorMap.clear();

        for (Personnel p : personnelList) {
            if (!"Collector".equalsIgnoreCase(p.getRole())) continue;

            JCheckBox cb = new JCheckBox(p.getFullName());
            cb.setBackground(WHITE);
            cb.setFont(BODYPLAIN);

            collectorMap.put(p.getId(), cb);
            collectorPanel.add(cb);

            cb.addActionListener(e -> {
                enforceLimit(cb);
                updateCount();
                refreshAvailability();
            });
        }
    }

    private void populate() {
        if (!editMode) return;

        teamNameField.setText(existing.getTeamName());

        selectCombo(truckCombo, existing.getTruckId());
        selectCombo(leaderCombo, existing.getLeaderId());
        selectCombo(driverCombo, existing.getDriverId());

        for (Integer id : existing.getCollectorIds()) {
            JCheckBox cb = collectorMap.get(id);
            if (cb != null) cb.setSelected(true);
        }
    }

    private void bindEvents() {
        leaderCombo.addActionListener(e -> refreshAvailability());
        driverCombo.addActionListener(e -> refreshAvailability());
    }

    private void refreshAvailability() {
        List<Integer> selectedCollectors = getSelectedCollectorIds();

        Personnel leader = (Personnel) leaderCombo.getSelectedItem();
        Personnel driver = (Personnel) driverCombo.getSelectedItem();

        for (Map.Entry<Integer, JCheckBox> entry : collectorMap.entrySet()) {
            int id = entry.getKey();
            JCheckBox cb = entry.getValue();

            boolean locked =
                    (leader != null && leader.getId() == id) ||
                    (driver != null && driver.getId() == id);

            cb.setEnabled(!locked);

            if (locked) cb.setSelected(false);
        }
    }

    private void enforceLimit(JCheckBox changed) {
        if (getSelectedCollectorIds().size() <= MAX_COLLECTORS) return;

        changed.setSelected(false);
        showError("Max collectors is " + MAX_COLLECTORS);
    }

    private void updateCount() {
        countLabel.setText(getSelectedCollectorIds().size() + "/" + MAX_COLLECTORS);
    }

    private List<Integer> getSelectedCollectorIds() {
        List<Integer> ids = new ArrayList<>();

        for (Map.Entry<Integer, JCheckBox> e : collectorMap.entrySet()) {
            if (e.getValue().isSelected()) {
                ids.add(e.getKey());
            }
        }

        return ids;
    }

    private List<String> getSelectedCollectorNames() {
        List<String> names = new ArrayList<>();

        for (Integer id : getSelectedCollectorIds()) {
            Personnel p = findById(id);
            if (p != null) names.add(p.getFullName());
        }

        return names;
    }

    private void includeExisting(List<Personnel> list) {
        addIfMissing(list, existing.getLeaderId());
        addIfMissing(list, existing.getDriverId());

        for (Integer id : existing.getCollectorIds()) {
            addIfMissing(list, id);
        }
    }

    private void addIfMissing(List<Personnel> list, int id) {
        if (id <= 0) return;

        boolean exists = list.stream().anyMatch(p -> p.getId() == id);
        if (!exists) {
            Personnel p = personnelService.getPersonnelById(id);
            if (p != null) list.add(p);
        }
    }

    private Personnel findById(int id) {
        return personnelList.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private boolean isDriverOrLeader(Personnel p) {
        String r = p.getRole();
        return "Driver".equalsIgnoreCase(r) || "Supervisor".equalsIgnoreCase(r);
    }

    private <T> void selectCombo(JComboBox<T> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object o = combo.getItemAt(i);
            if (o instanceof Personnel && ((Personnel) o).getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
            if (o instanceof Truck && ((Truck) o).getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    @Override
    protected void saveForm() {
        if (teamNameField.getText().trim().isEmpty()) {
            showError("Team name is required");
            return;
        }

        try {
            Team t = editMode ? existing : new Team();

            t.setTeamName(teamNameField.getText().trim());
            t.setLeaderId(getId(leaderCombo));
            t.setDriverId(getId(driverCombo));
            t.setTruckId(getId(truckCombo));

            t.setCollectorIds(getSelectedCollectorIds());
            t.setCollectorNames(getSelectedCollectorNames());

            t.setStatus("Active");

            boolean ok = editMode
                    ? teamService.updateTeam(t)
                    : teamService.addTeam(t);

            if (ok) {
                showSuccess("Team saved successfully");
                SwingUtilities.invokeLater(() -> {
                    DataChangeBus.publish(DataTopics.TEAMS);
                    dispose();
                });
            } else {
                showError("Failed to save team");
            }
        } catch (Exception e) {
            showError("Error saving team: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getId(JComboBox<?> combo) {
        Object o = combo.getSelectedItem();
        if (o instanceof Personnel) return ((Personnel) o).getId();
        if (o instanceof Truck) return ((Truck) o).getId();
        return 0;
    }

    @Override
    protected JPanel createActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);

        JButton cancel = createFormButton("Cancel", false);
        cancel.addActionListener(e -> dispose());

        JButton save = createFormButton("Save", true);
        save.addActionListener(e -> saveForm());

        panel.add(cancel);
        panel.add(save);

        return panel;
    }
}