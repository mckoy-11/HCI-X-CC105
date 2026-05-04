package main.ui.menro.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import main.model.Personnel;
import main.model.PopupItem;
import main.model.Team;
import main.model.Truck;
import main.service.PersonnelService;
import main.service.TeamService;
import main.service.TruckService;
import static main.ui.style.SystemStyle.BGCOLOR1;
import static main.ui.style.SystemStyle.BGCOLOR2;
import static main.ui.style.SystemStyle.Card;
import static main.ui.style.SystemStyle.GradientPaint;
import static main.ui.style.SystemStyle.SIDEBAR;
import static main.ui.style.SystemStyle.SUBTITLEBOLD;
import static main.ui.style.SystemStyle.TEXTCOLOR;
import static main.ui.style.SystemStyle.WHITE;
import static main.ui.style.SystemStyle.createFieldGroup;
import static main.ui.style.SystemStyle.createFormSubtitle;
import static main.ui.style.SystemStyle.createSplitRow;
import static main.ui.style.SystemStyle.roundPanel;
import static main.ui.style.SystemStyle.styleComboBox;
import static main.ui.style.SystemStyle.styleInput;
import main.store.DataTopics;
import static main.ui.style.SystemStyle.SELECTED;
import static main.ui.style.SystemStyle.createInfoItem;
import static main.ui.style.SystemStyle.loadIcon;
import static main.ui.style.SystemStyle.separator;
import main.ui.components.CustomButton;
import static main.ui.components.CustomButton.createButton;
import main.ui.components.Header;
import main.ui.components.ReactivePanel;
import main.ui.components.ScrollablePanel;
import main.ui.components.ScrollableTable;
import main.ui.menro.dialogs.PersonnelFormDialog;
import main.ui.menro.dialogs.TruckFormDialog;
import main.ui.menro.dialogs.TeamFormDialog;
import main.ui.components.SummaryCards;

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
        contentPanel.setBorder(new EmptyBorder(0, 0, 4, 4));
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
            List<PopupItem> actions = new ArrayList<>();
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

        JPanel grid = new JPanel(new GridLayout(0, 2, 15, 15)); // 2 columns
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (Team team : teamService.getAllTeams()) {
            grid.add(createTeamCard(team));
        }

        ScrollablePanel scrollable = new ScrollablePanel(grid);
        panel.add(scrollable, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createTeamCard(Team team) {
        JPanel card = Card(20);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setPreferredSize(new Dimension(300, 100));
        card.setBackground(WHITE);
        
        // LEFT (icon + info)
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel name = new javax.swing.JLabel(safe(team.getTeamName()));
        name.setFont(SUBTITLEBOLD);

        javax.swing.JLabel location = new javax.swing.JLabel(
                safe(team.getTruckPlateNumber(), "No assigned area")
        );
        location.setForeground(TEXTCOLOR);

        center.add(name);
        center.add(Box.createVerticalStrut(5));
        center.add(location);

        // RIGHT (status + button)
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);

        JLabel status = new JLabel(safe(team.getStatus(), "Active"));
        status.setForeground(new Color(34, 197, 94));
        status.setHorizontalAlignment(SwingConstants.CENTER);

        JButton viewBtn = createButton("View Details", "arrow.png", "arrow.png", 16);
        viewBtn.setBackground(SIDEBAR);
        viewBtn.addActionListener(e -> editTeam(team));

        right.add(status, BorderLayout.NORTH);
        right.add(viewBtn, BorderLayout.SOUTH);
        
        JLabel icon = new JLabel(loadIcon("team.png", 35));
        icon.setBorder(new EmptyBorder(10, 20, 10, 20));
        card.add(icon, BorderLayout.WEST);
        card.add(center, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    private JPanel createTruckView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(createHeader("Truck Management"), BorderLayout.NORTH);

        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0); // spacing between cards

        int y = 0;
        for (Truck truck : truckService.getAllTrucks()) {
            gbc.gridy = y++;
            container.add(createTruckCard(truck), gbc);
        }

        ScrollablePanel scrollable = new ScrollablePanel(container);
        panel.add(scrollable, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createTruckCard(Truck truck) {
        JPanel card = Card(20);
        card.setLayout(new BorderLayout(15, 10));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setBackground(WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel image = new JLabel(loadIcon("truck.png", 40));
        image.setBorder(new EmptyBorder(0, 5, 0, 10));

        JLabel title = new JLabel("Truck " + safe(truck.getPlateNumber()));
        title.setFont(SUBTITLEBOLD);

        JLabel status = new JLabel(safe(truck.getStatus(), "Active"));
        status.setOpaque(true);
        status.setBackground(new Color(220, 252, 231));
        status.setForeground(new Color(22, 163, 74));
        status.setBorder(new EmptyBorder(5, 12, 5, 12));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.setOpaque(false);

        top.add(title);
        top.add(Box.createHorizontalGlue());
        top.add(status);

        JPanel info = new JPanel(new GridLayout(1, 4, 20, 0)); // FIXED: 4 columns
        info.setOpaque(false);

        info.add(createInfoItem("Capacity", safe(truck.getCapacity(), "None")));
        info.add(createInfoItem("Assigned To", safe(truck.getAssignedTeam(), "None")));
        info.add(createInfoItem("Truck Type", safe(truck.getTruckType(), "None")));
        info.add(createInfoItem("Assigned Barangay", safe(truck.getAssignedBarangay(), "None")));

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton editBtn = createButton("Edit", "edit.png", "edit.png", 16);
        editBtn.addActionListener(e -> editTruck(truck));

        JButton deleteBtn = createButton("Delete", "delete.png", "delete.png", 16);
        deleteBtn.addActionListener(e -> deleteTruck(truck));

        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        center.add(top);
        center.add(Box.createVerticalStrut(10));
        center.add(separator(1000));
        center.add(Box.createVerticalStrut(10));
        center.add(info);
        center.add(Box.createVerticalStrut(10));
        center.add(actionPanel);

        card.add(image, BorderLayout.WEST);
        card.add(center, BorderLayout.CENTER);

        return card;
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
        navBar.setPreferredSize(new Dimension(300, 35));

        for (String view : new String[]{PERSONNEL_VIEW, TEAM_VIEW, TRUCK_VIEW}) {
            CustomButton button = new CustomButton(
                    view,
                    "",
                    "",
                    20,
                    70,
                    25,
                    70,
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

    private PopupItem openPersonnelDialog(Personnel personnel) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);

        PersonnelFormDialog dialog = new PersonnelFormDialog(parent, personnel);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return null;
    }

    private PopupItem deletePersonnel(Personnel personnel) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete " + personnel.getFullName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = personnelService.deletePersonnel(personnel.getId());
            if (!success) {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete personnel",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
        return null;
    }

    private void editTeam(Team team) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        TeamFormDialog dialog = new TeamFormDialog(parent, team);
        dialog.setVisible(true);
    }

    private void deleteTeam(Team team) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete team " + team.getTeamName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = teamService.deleteTeam(team.getId());
            if (!success) {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete team",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void editTruck(Truck truck) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        TruckFormDialog dialog = new TruckFormDialog(parent, truck);
        dialog.setVisible(true);
    }

    private void deleteTruck(Truck truck) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete truck " + truck.getPlateNumber() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = truckService.deleteTruck(truck.getId());
            if (!success) {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete truck",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void openCreateDialog() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        switch (currentView) {
            case PERSONNEL_VIEW:
                PersonnelFormDialog personnelDialog = new PersonnelFormDialog(parent, null);
                personnelDialog.setVisible(true);
                break;
            case TRUCK_VIEW:
                TruckFormDialog truckDialog = new TruckFormDialog(parent, null);
                truckDialog.setVisible(true);
                break;
            case TEAM_VIEW:
                TeamFormDialog teamDialog = new TeamFormDialog(parent, null);
                teamDialog.setVisible(true);
                break;
        }
    }
}
