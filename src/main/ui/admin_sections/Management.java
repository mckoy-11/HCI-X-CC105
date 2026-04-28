package main.ui.admin_sections;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import main.model.Personnel;
import main.model.PopupItem;
import main.model.Team;
import main.model.Truck;
import main.service.PersonnelService;
import main.service.TeamService;
import main.service.TruckService;
import static main.style.SystemStyle.BGCOLOR1;
import static main.style.SystemStyle.BGCOLOR2;
import static main.style.SystemStyle.Card;
import static main.style.SystemStyle.GradientPaint;
import static main.style.SystemStyle.SIDEBAR;
import static main.style.SystemStyle.SUBTITLEBOLD;
import static main.style.SystemStyle.TEXTCOLOR;
import static main.style.SystemStyle.WHITE;
import static main.style.SystemStyle.createFieldGroup;
import static main.style.SystemStyle.createFormSubtitle;
import static main.style.SystemStyle.createSplitRow;
import static main.style.SystemStyle.roundPanel;
import static main.style.SystemStyle.styleComboBox;
import static main.style.SystemStyle.styleInput;
import main.store.DataTopics;
import static main.style.SystemStyle.SELECTED;
import main.ui.components.CustomButton;
import static main.ui.components.CustomButton.createButton;
import main.ui.components.Header;
import main.ui.components.ReactivePanel;
import main.ui.components.ScrollableTable;
import main.ui.components.SummaryCards;
import main.ui.dialogs.AdminDialogSupport;

public class Management extends ReactivePanel {

    private static final String PERSONNEL_VIEW = "Personnel";
    private static final String TEAM_VIEW = "Team";
    private static final String TRUCK_VIEW = "Truck";

    private final PersonnelService personnelService = new PersonnelService();
    private final TeamService teamService = new TeamService();
    private final TruckService truckService = new TruckService();

    private final java.awt.CardLayout contentLayout = new java.awt.CardLayout();
    private final JPanel contentPanel = Card(12);
    private final JPanel summaryPanel = new JPanel(new BorderLayout());

    private String currentView = PERSONNEL_VIEW;

    public Management() {
        setLayout(new BorderLayout());
        setOpaque(false);

        listen(DataTopics.PERSONNEL, this::refreshUI);
        listen(DataTopics.TEAMS, this::refreshUI);
        listen(DataTopics.TRUCKS, this::refreshUI);

        add(new Header("Management"), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    private JPanel createMainContent() {
        JPanel root = GradientPaint(BGCOLOR1, BGCOLOR2, 0, false, 20);
        root.setLayout(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        summaryPanel.setOpaque(false);
        summaryPanel.add(createSummary(currentView), BorderLayout.CENTER);

        contentPanel.setLayout(contentLayout);
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 4));
        rebuildContentViews();

        root.add(summaryPanel, BorderLayout.NORTH);
        root.add(contentPanel, BorderLayout.CENTER);
        return root;
    }

    private void rebuildContentViews() {
        contentPanel.removeAll();
        contentPanel.add(createPersonnelView(), PERSONNEL_VIEW);
        contentPanel.add(createTeamView(), TEAM_VIEW);
        contentPanel.add(createTruckView(), TRUCK_VIEW);
        contentLayout.show(contentPanel, currentView);
    }

    private JPanel createPersonnelView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(createHeader("Personnel Management"), BorderLayout.NORTH);

        ScrollableTable table = new ScrollableTable(
                "Name", "Age", "Sex", "Address", "Phone", "Team", "Role", "Status", "Action"
        );

        for (Personnel personnel : personnelService.getAllPersonnel()) {
            List<PopupItem> actions = new ArrayList<PopupItem>();
            actions.add(new PopupItem("Edit", "Update personnel details", () -> openPersonnelDialog(personnel)));
            actions.add(new PopupItem("Delete", "Remove this personnel", () -> deletePersonnel(personnel)));
            table.addRowWithAction(
                    safe(personnel.getFullName()),
                    personnel.getAge(),
                    safe(personnel.getGender()),
                    safe(personnel.getAddress()),
                    safe(personnel.getPhoneNumber()),
                    safe(personnel.getTeam(), "Unassigned"),
                    safe(personnel.getRole()),
                    safe(personnel.getStatus()),
                    actions
            );
        }

        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTeamView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(createHeader("Team Management"), BorderLayout.NORTH);

        ScrollableTable table = new ScrollableTable(
                "Team", "Leader", "Driver", "Collectors", "Truck", "Status", "Action"
        );

        for (Team team : teamService.getAllTeams()) {
            List<PopupItem> actions = new ArrayList<PopupItem>();
            actions.add(new PopupItem("Edit", "Update team assignments", () -> openTeamDialog(team)));
            actions.add(new PopupItem("Delete", "Remove this team", () -> deleteTeam(team)));
            table.addRowWithAction(
                    safe(team.getTeamName()),
                    safe(team.getLeaderName(), "Unassigned"),
                    safe(team.getDriverName(), "Unassigned"),
                    String.valueOf(team.getCollectorNames().size()),
                    safe(team.getTruckPlateNumber(), "No truck"),
                    safe(team.getStatus(), "Active"),
                    actions
            );
        }

        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTruckView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(createHeader("Truck Management"), BorderLayout.NORTH);

        ScrollableTable table = new ScrollableTable(
                "Plate", "Type", "Capacity", "Assigned Team", "Status", "Action"
        );

        for (Truck truck : truckService.getAllTrucks()) {
            List<PopupItem> actions = new ArrayList<PopupItem>();
            actions.add(new PopupItem("Edit", "Update truck details", () -> openTruckDialog(truck)));
            actions.add(new PopupItem("Delete", "Remove this truck", () -> deleteTruck(truck)));
            table.addRowWithAction(
                    safe(truck.getPlateNumber()),
                    safe(truck.getTruckType()),
                    safe(truck.getCapacity()),
                    safe(truck.getAssignedTeam(), "Unassigned"),
                    safe(truck.getStatus(), "Active"),
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

        javax.swing.JLabel title = new javax.swing.JLabel(titleText);
        title.setForeground(WHITE);
        title.setFont(SUBTITLEBOLD);

        JPanel navBar = roundPanel(50);
        navBar.setLayout(new GridLayout(1, 3, 5, 0));
        navBar.setPreferredSize(new Dimension(380, 35));

        for (String view : new String[]{PERSONNEL_VIEW, TEAM_VIEW, TRUCK_VIEW}) {
            CustomButton button = new CustomButton(
                    view,
                    "",
                    "",
                    20,
                    100,
                    25,
                    100,
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

        javax.swing.JButton addButton = createButton("Add", "add.png", "add-white.png", 20);
        addButton.setPreferredSize(new Dimension(100, 35));
        addButton.addActionListener(event -> openCreateDialog());
        right.add(addButton);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private SummaryCards createSummary(String view) {
        if (TEAM_VIEW.equals(view)) {
            List<Team> teams = teamService.getAllTeams();
            return new SummaryCards(
                    new String[]{"Total Teams", "Active Teams", "Inactive Teams"},
                    new int[]{
                        teams.size(),
                        (int) teams.stream().filter(team -> "Active".equalsIgnoreCase(team.getStatus())).count(),
                        (int) teams.stream().filter(team -> "Inactive".equalsIgnoreCase(team.getStatus())).count()
                    },
                    new String[]{"All teams", "Ready for dispatch", "Needs attention"},
                    icons(),
                    colors()
            );
        }

        if (TRUCK_VIEW.equals(view)) {
            List<Truck> trucks = truckService.getAllTrucks();
            return new SummaryCards(
                    new String[]{"Total Trucks", "Active Trucks", "Maintenance"},
                    new int[]{
                        trucks.size(),
                        (int) trucks.stream().filter(truck -> "Active".equalsIgnoreCase(truck.getStatus())).count(),
                        (int) trucks.stream().filter(truck -> "Maintenance".equalsIgnoreCase(truck.getStatus())).count()
                    },
                    new String[]{"Fleet total", "Available", "Unavailable"},
                    icons(),
                    colors()
            );
        }

        return new SummaryCards(
                new String[]{"Total Personnel", "Active Personnel", "Unassigned"},
                new int[]{
                    personnelService.getTotalPersonnelCount(),
                    personnelService.getActivePersonnelCount(),
                    personnelService.getUnassignedPersonnelCount()
                },
                new String[]{"Team members", "Ready to assign", "Awaiting assignment"},
                icons(),
                colors()
        );
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

    private void switchView(String view) {
        currentView = view;
        refreshUI();
    }

    private void openCreateDialog() {
        if (PERSONNEL_VIEW.equals(currentView)) {
            openPersonnelDialog(null);
        } else if (TEAM_VIEW.equals(currentView)) {
            openTeamDialog(null);
        } else {
            openTruckDialog(null);
        }
    }

    private void openPersonnelDialog(Personnel existing) {
        JTextField nameField = styleInput(new JTextField(existing == null ? "" : safe(existing.getFullName())));
        JTextField ageField = styleInput(new JTextField(existing == null || existing.getAge() <= 0 ? "" : String.valueOf(existing.getAge())));
        JTextField addressField = styleInput(new JTextField(existing == null ? "" : safe(existing.getAddress())));
        JTextField phoneField = styleInput(new JTextField(existing == null ? "" : safe(existing.getPhoneNumber())));
        JComboBox<String> genderCombo = styleComboBox(new JComboBox<String>(new String[]{"Male", "Female", "Other"}));
        JComboBox<String> roleCombo = styleComboBox(new JComboBox<String>(new String[]{"Collector", "Driver", "Supervisor", "Unassigned"}));
        JComboBox<String> statusCombo = styleComboBox(new JComboBox<String>(new String[]{"Active", "Inactive", "Unassigned"}));

        if (existing != null) {
            genderCombo.setSelectedItem(safe(existing.getGender(), "Male"));
            roleCombo.setSelectedItem(safe(existing.getRole(), "Collector"));
            statusCombo.setSelectedItem(safe(existing.getStatus(), "Active"));
        }

        JPanel form = buildFormPanel(
                createFieldGroup("Full Name", nameField),
                createSplitRow(
                        createFieldGroup("Age", ageField),
                        createFieldGroup("Gender", genderCombo)
                ),
                createFieldGroup("Address", addressField),
                createFieldGroup("Phone", phoneField),
                createSplitRow(
                        createFieldGroup("Role", roleCombo),
                        createFieldGroup("Status", statusCombo)
                )
        );

        if (!AdminDialogSupport.showFormDialog(this, existing == null ? "Add Personnel" : "Edit Personnel", form)) {
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException e) {
            AdminDialogSupport.showFailure(this, "Age must be a valid number.");
            return;
        }

        Personnel personnel = existing == null ? new Personnel() : existing;
        personnel.setFullName(nameField.getText().trim());
        personnel.setAge(age);
        personnel.setGender((String) genderCombo.getSelectedItem());
        personnel.setAddress(addressField.getText().trim());
        personnel.setPhoneNumber(phoneField.getText().trim());
        personnel.setRole((String) roleCombo.getSelectedItem());
        personnel.setStatus((String) statusCombo.getSelectedItem());
        if (existing == null && personnel.getTeam() == null) {
            personnel.setTeam(null);
        }

        boolean success = existing == null
                ? personnelService.addPersonnel(personnel)
                : personnelService.updatePersonnel(personnel);

        if (!success) {
            AdminDialogSupport.showFailure(this, "Unable to save personnel details.");
            return;
        }

        AdminDialogSupport.showSuccess(this, existing == null ? "Personnel added successfully." : "Personnel updated successfully.");
    }

    private void openTeamDialog(Team existing) {
        JTextField nameField = styleInput(new JTextField(existing == null ? "" : safe(existing.getTeamName())));
        JComboBox<SelectionItem> leaderCombo = createPersonnelCombo("Select leader", personnelService.getAllPersonnel());
        JComboBox<SelectionItem> driverCombo = createPersonnelCombo("Select driver", personnelService.getAllPersonnel());
        JComboBox<SelectionItem> truckCombo = createTruckCombo("Select truck", truckService.getAllTrucks());
        JComboBox<String> statusCombo = styleComboBox(new JComboBox<String>(new String[]{"Active", "Inactive"}));

        List<Personnel> collectorOptions = personnelService.getAllPersonnel().stream()
                .filter(personnel -> personnel.getId() > 0)
                .collect(Collectors.toList());
        JList<SelectionItem> collectorsList = createCollectorList(collectorOptions);

        if (existing != null) {
            selectById(leaderCombo, existing.getLeaderId());
            selectById(driverCombo, existing.getDriverId());
            selectById(truckCombo, existing.getTruckId());
            statusCombo.setSelectedItem(safe(existing.getStatus(), "Active"));
            selectCollectors(collectorsList, existing.getCollectorIds());
        }

        JPanel form = buildFormPanel(
                createFieldGroup("Team Name", nameField),
                createSplitRow(
                        createFieldGroup("Leader", leaderCombo),
                        createFieldGroup("Driver", driverCombo)
                ),
                createSplitRow(
                        createFieldGroup("Truck", truckCombo),
                        createFieldGroup("Status", statusCombo)
                ),
                collectorField(collectorsList)
        );

        if (!AdminDialogSupport.showFormDialog(this, existing == null ? "Add Team" : "Edit Team", form)) {
            return;
        }

        Team team = existing == null ? new Team() : existing;
        team.setTeamName(nameField.getText().trim());
        team.setLeaderId(selectedId(leaderCombo));
        team.setLeaderName(selectedLabel(leaderCombo));
        team.setDriverId(selectedId(driverCombo));
        team.setDriverName(selectedLabel(driverCombo));
        team.setTruckId(selectedId(truckCombo));
        team.setTruckPlateNumber(selectedLabel(truckCombo));
        team.setStatus((String) statusCombo.getSelectedItem());
        team.setCollectorIds(selectedIds(collectorsList));
        team.setCollectorNames(selectedNames(collectorsList));

        if (team.getTeamName().trim().isEmpty()) {
            AdminDialogSupport.showFailure(this, "Team name is required.");
            return;
        }

        boolean success = existing == null ? teamService.addTeam(team) : teamService.updateTeam(team);
        if (!success) {
            AdminDialogSupport.showFailure(this, "Unable to save the team.");
            return;
        }

        AdminDialogSupport.showSuccess(this, existing == null ? "Team added successfully." : "Team updated successfully.");
    }

    private void openTruckDialog(Truck existing) {
        JTextField plateField = styleInput(new JTextField(existing == null ? "" : safe(existing.getPlateNumber())));
        JTextField typeField = styleInput(new JTextField(existing == null ? "" : safe(existing.getTruckType())));
        JTextField capacityField = styleInput(new JTextField(existing == null ? "" : safe(existing.getCapacity())));
        JComboBox<String> statusCombo = styleComboBox(new JComboBox<String>(new String[]{"Active", "Maintenance", "Inactive"}));
        statusCombo.setSelectedItem(existing == null ? "Active" : safe(existing.getStatus(), "Active"));

        JPanel form = buildFormPanel(
                createFieldGroup("Plate Number", plateField),
                createSplitRow(
                        createFieldGroup("Truck Type", typeField),
                        createFieldGroup("Capacity", capacityField)
                ),
                createFieldGroup("Status", statusCombo)
        );

        if (!AdminDialogSupport.showFormDialog(this, existing == null ? "Add Truck" : "Edit Truck", form)) {
            return;
        }

        Truck truck = existing == null ? new Truck() : existing;
        truck.setPlateNumber(plateField.getText().trim());
        truck.setTruckType(typeField.getText().trim());
        truck.setCapacity(capacityField.getText().trim());
        truck.setStatus((String) statusCombo.getSelectedItem());

        if (truck.getPlateNumber().trim().isEmpty()) {
            AdminDialogSupport.showFailure(this, "Plate number is required.");
            return;
        }

        boolean success = existing == null ? truckService.addTruck(truck) : truckService.updateTruck(truck);
        if (!success) {
            AdminDialogSupport.showFailure(this, "Unable to save the truck.");
            return;
        }

        AdminDialogSupport.showSuccess(this, existing == null ? "Truck added successfully." : "Truck updated successfully.");
    }

    private void deletePersonnel(Personnel personnel) {
        if (!AdminDialogSupport.confirmAction(this, "Delete Personnel", "Delete " + safe(personnel.getFullName()) + "?")) {
            return;
        }
        if (personnelService.deletePersonnel(personnel.getId())) {
            AdminDialogSupport.showSuccess(this, "Personnel deleted successfully.");
        } else {
            AdminDialogSupport.showFailure(this, "Failed to delete personnel.");
        }
    }

    private void deleteTeam(Team team) {
        if (!AdminDialogSupport.confirmAction(this, "Delete Team", "Delete " + safe(team.getTeamName()) + "?")) {
            return;
        }
        if (teamService.deleteTeam(team.getId())) {
            AdminDialogSupport.showSuccess(this, "Team deleted successfully.");
        } else {
            AdminDialogSupport.showFailure(this, "Failed to delete team.");
        }
    }

    private void deleteTruck(Truck truck) {
        if (!AdminDialogSupport.confirmAction(this, "Delete Truck", "Delete " + safe(truck.getPlateNumber()) + "?")) {
            return;
        }
        if (truckService.deleteTruck(truck.getId())) {
            AdminDialogSupport.showSuccess(this, "Truck deleted successfully.");
        } else {
            AdminDialogSupport.showFailure(this, "Failed to delete truck.");
        }
    }

    private JPanel buildFormPanel(JComponent... sections) {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createFormSubtitle("Keep records consistent and reusable across the management workspace."));
        form.add(Box.createVerticalStrut(18));
        for (int i = 0; i < sections.length; i++) {
            form.add(sections[i]);
            if (i < sections.length - 1) {
                form.add(Box.createVerticalStrut(16));
            }
        }
        return form;
    }

    private JPanel collectorField(JList<SelectionItem> collectorsList) {
        JScrollPane scrollPane = new JScrollPane(collectorsList);
        scrollPane.setPreferredSize(new Dimension(0, 180));
        return createFieldGroup("Collectors", scrollPane);
    }

    private JComboBox<SelectionItem> createPersonnelCombo(String placeholder, List<Personnel> personnel) {
        List<SelectionItem> items = new ArrayList<SelectionItem>();
        items.add(new SelectionItem(0, placeholder));
        for (Personnel person : personnel) {
            items.add(new SelectionItem(person.getId(), person.getFullName()));
        }
        JComboBox<SelectionItem> comboBox = new JComboBox<SelectionItem>(items.toArray(new SelectionItem[0]));
        styleComboBox(comboBox);
        return comboBox;
    }

    private JComboBox<SelectionItem> createTruckCombo(String placeholder, List<Truck> trucks) {
        List<SelectionItem> items = new ArrayList<SelectionItem>();
        items.add(new SelectionItem(0, placeholder));
        for (Truck truck : trucks) {
            items.add(new SelectionItem(truck.getId(), truck.getPlateNumber()));
        }
        JComboBox<SelectionItem> comboBox = new JComboBox<SelectionItem>(items.toArray(new SelectionItem[0]));
        styleComboBox(comboBox);
        return comboBox;
    }

    private JList<SelectionItem> createCollectorList(List<Personnel> personnel) {
        List<SelectionItem> items = new ArrayList<SelectionItem>();
        for (Personnel person : personnel) {
            items.add(new SelectionItem(person.getId(), person.getFullName()));
        }

        JList<SelectionItem> list = new JList<SelectionItem>(items.toArray(new SelectionItem[0]));
        list.setVisibleRowCount(8);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> source, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(source, value, index, isSelected, cellHasFocus);
                if (value instanceof SelectionItem) {
                    setText(((SelectionItem) value).label);
                }
                return component;
            }
        });
        return list;
    }

    private void selectById(JComboBox<SelectionItem> comboBox, int id) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            SelectionItem item = comboBox.getItemAt(i);
            if (item.id == id) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectCollectors(JList<SelectionItem> list, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < list.getModel().getSize(); i++) {
            SelectionItem item = list.getModel().getElementAt(i);
            if (ids.contains(Integer.valueOf(item.id))) {
                indexes.add(Integer.valueOf(i));
            }
        }
        int[] selected = indexes.stream().mapToInt(Integer::intValue).toArray();
        list.setSelectedIndices(selected);
    }

    private int selectedId(JComboBox<SelectionItem> comboBox) {
        SelectionItem item = (SelectionItem) comboBox.getSelectedItem();
        return item == null ? 0 : item.id;
    }

    private String selectedLabel(JComboBox<SelectionItem> comboBox) {
        SelectionItem item = (SelectionItem) comboBox.getSelectedItem();
        return item == null || item.id == 0 ? null : item.label;
    }

    private List<Integer> selectedIds(JList<SelectionItem> list) {
        List<Integer> ids = new ArrayList<Integer>();
        for (SelectionItem item : list.getSelectedValuesList()) {
            ids.add(Integer.valueOf(item.id));
        }
        return ids;
    }

    private List<String> selectedNames(JList<SelectionItem> list) {
        return list.getSelectedValuesList().stream()
                .map(item -> item.label)
                .collect(Collectors.toList());
    }

    private String safe(String value) {
        return safe(value, "");
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private void refreshUI() {
        SwingUtilities.invokeLater(() -> {
            summaryPanel.removeAll();
            summaryPanel.add(createSummary(currentView), BorderLayout.CENTER);
            summaryPanel.revalidate();
            summaryPanel.repaint();

            rebuildContentViews();
            contentPanel.revalidate();
            contentPanel.repaint();
        });
    }

    private static final class SelectionItem {
        private final int id;
        private final String label;

        private SelectionItem(int id, String label) {
            this.id = id;
            this.label = label == null ? "" : label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
