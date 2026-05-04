package main.ui.menro.dialogs;

import main.database.DbSchemaHelper;
import main.database.SQLConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SearchDialog {

    private final JComponent anchor;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    private final JPopupMenu popup;
    private final JList<SearchResult> list;
    private final DefaultListModel<SearchResult> model;

    private final List<SearchResult> results = new ArrayList<>();
    private String query = "";

    // =========================
    // Constructor
    // =========================
    public SearchDialog(JComponent anchor,
                        JPanel cardPanel,
                        CardLayout cardLayout) {

        this.anchor = anchor;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;

        this.model = new DefaultListModel<>();
        this.list = new JList<>(model);

        configureList();
        this.popup = createPopup();

        popup.setFocusable(false);
    }

    // =========================
    // PUBLIC API
    // =========================

    public void updateQuery(String newQuery) {
        query = newQuery == null ? "" : newQuery.trim();

        model.clear();
        results.clear();

        if (query.isEmpty()) {
            hide();
            return;
        }

        loadScreenResults();
        loadDatabaseResults();

        if (results.isEmpty()) {
            model.addElement(new SearchResult("Info", "No results", "Try another keyword", null));
        } else {
            for (SearchResult r : results) {
                model.addElement(r);
            }
        }

        show();
    }

    public void openFromEnter(String text) {
        updateQuery(text);

        if (!model.isEmpty()) {
            list.setSelectedIndex(0);
            openSelected();
        }
    }

    public void hide() {
        popup.setVisible(false);
    }

    // =========================
    // POPUP
    // =========================
    private void show() {
        if (!anchor.isShowing()) return;

        if (!popup.isVisible()) {
            popup.show(anchor, 0, anchor.getHeight());
        }

        if (!model.isEmpty()) {
            list.setSelectedIndex(0);
        }
    }

    private JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(420, 260));

        menu.add(scroll);
        return menu;
    }

    // =========================
    // LIST CONFIG
    // =========================
    private void configureList() {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(56);
        list.setCellRenderer(new Renderer());

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelected();
                }
            }
        });

        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openSelected();
                }
            }
        });
    }

    // =========================
    // NAVIGATION
    // =========================
    private void openSelected() {
        int i = list.getSelectedIndex();
        if (i < 0 || i >= model.size()) return;

        SearchResult r = model.get(i);

        if (r.panelKey != null && cardLayout != null) {
            cardLayout.show(cardPanel, r.panelKey);
        }

        hide();
    }

    // =========================
    // DATA LOADING
    // =========================
    private void loadScreenResults() {
        List<String> screens = Arrays.asList(
                "Home", "Schedule", "Management",
                "Barangay", "Users", "Settings"
        );

        String q = query.toLowerCase();

        for (String s : screens) {
            if (s.toLowerCase().contains(q)) {
                results.add(new SearchResult("Screen", s, "Open " + s, s));
            }
        }
    }

    private void loadDatabaseResults() {
        String like = "%" + query + "%";

        try (Connection conn = SQLConnection.getConnection()) {

            if (DbSchemaHelper.tableExists(conn, "barangay")) {
                searchBarangay(conn, like);
            }

            if (DbSchemaHelper.tableExists(conn, "team")) {
                searchTeam(conn, like);
            }

        } catch (SQLException ignored) {}
    }

    private void searchBarangay(Connection conn, String like) throws SQLException {
        String col = DbSchemaHelper.getFirstColumn(conn, "barangay",
                "Barangay_Name", "Barangay");

        if (col == null) return;

        String sql = "SELECT " + col + " FROM barangay WHERE " + col + " LIKE ? LIMIT 10";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new SearchResult(
                            "Barangay",
                            rs.getString(1),
                            "Barangay record",
                            "Barangay Management"
                    ));
                }
            }
        }
    }

    private void searchTeam(Connection conn, String like) throws SQLException {
        String col = DbSchemaHelper.getFirstColumn(conn, "team",
                "Team_Name", "Name");

        if (col == null) return;

        String sql = "SELECT " + col + " FROM team WHERE " + col + " LIKE ? LIMIT 10";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new SearchResult(
                            "Team",
                            rs.getString(1),
                            "Team record",
                            "Team Management"
                    ));
                }
            }
        }
    }

    // =========================
    // MODEL
    // =========================
    private static class SearchResult {
        final String type;
        final String title;
        final String details;
        final String panelKey;

        SearchResult(String type, String title, String details, String panelKey) {
            this.type = type;
            this.title = title;
            this.details = details;
            this.panelKey = panelKey;
        }
    }

    // =========================
    // RENDERER
    // =========================
    private class Renderer extends JPanel implements ListCellRenderer<SearchResult> {

        JLabel type = new JLabel();
        JLabel title = new JLabel();
        JLabel details = new JLabel();

        Renderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(8, 10, 8, 10));

            type.setFont(new Font("Segoe UI", Font.BOLD, 11));
            title.setFont(new Font("Segoe UI", Font.BOLD, 13));
            details.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            add(type);
            add(title);
            add(details);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends SearchResult> list,
                SearchResult value,
                int index,
                boolean selected,
                boolean focus) {

            type.setText(value.type.toUpperCase());
            title.setText(value.title);
            details.setText(value.details);

            setBackground(selected ? new Color(230, 240, 255) : Color.WHITE);

            return this;
        }
    }
}