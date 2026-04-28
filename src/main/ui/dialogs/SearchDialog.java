package main.ui.dialogs;

import main.database.DbSchemaHelper;
import main.database.SQLConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SearchDialog {

    private static SearchDialog activeDialog;

    private final Component anchor;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    private final JPopupMenu popup;
    private final JList<SearchResult> resultList;
    private final DefaultListModel<SearchResult> listModel;

    private final List<SearchResult> results = new ArrayList<>();

    private String currentQuery = "";

    // =========================
    // Constructor
    // =========================
    private SearchDialog(Component anchor, String query,
                         JPanel cardPanel, CardLayout cardLayout) {

        this.anchor = anchor;
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;

        this.listModel = new DefaultListModel<>();
        this.resultList = new JList<>(listModel);

        configureList();
        this.popup = createPopup();
        bindKeyboardFromInput(); // key fix (no focus stealing)

        updateQuery(query);
        showPopup();
    }

    // =========================
    // UI Configuration
    // =========================
    private void configureList() {
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setCellRenderer(new SearchRenderer());
        resultList.setFixedCellHeight(56);

        // Mouse select + double click
        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelected();
            }
        });

        // Hover selection
        resultList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = resultList.locationToIndex(e.getPoint());
                if (index >= 0) resultList.setSelectedIndex(index);
            }
        });
    }

    private JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();
        menu.setFocusable(false);
        menu.setBackground(Color.WHITE);

        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(4, 0, 4, 0)
        ));

        JScrollPane scroll = new JScrollPane(resultList);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(
                Math.max(360, anchor.getWidth()), 260
        ));

        menu.add(scroll);

        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) { activeDialog = null; }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { activeDialog = null; }
        });

        return menu;
    }

    private void showPopup() {
        if (anchor == null || !anchor.isShowing()) return;

        popup.show(anchor, 0, anchor.getHeight());

        if (!listModel.isEmpty()) {
            resultList.setSelectedIndex(0);
        }

        activeDialog = this;
    }

    // =========================
    // Keyboard Binding (FIX)
    // =========================
    private void bindKeyboardFromInput() {
        if (!(anchor instanceof JComponent)) return;

        JComponent input = (JComponent) anchor;

        InputMap im = input.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = input.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "navDown");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "navUp");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "navEnter");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "navEsc");

        am.put("navDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = resultList.getSelectedIndex();
                if (i < listModel.size() - 1) resultList.setSelectedIndex(i + 1);
            }
        });

        am.put("navUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = resultList.getSelectedIndex();
                if (i > 0) resultList.setSelectedIndex(i - 1);
            }
        });

        am.put("navEnter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSelected();
            }
        });

        am.put("navEsc", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeActiveDialog();
            }
        });
    }

    // =========================
    // Query Handling
    // =========================
    private void updateQuery(String query) {
        currentQuery = query == null ? "" : query.trim();

        listModel.clear();
        results.clear();

        if (currentQuery.isEmpty()) return;

        loadScreenResults();
        loadDatabaseResults();

        if (results.isEmpty()) {
            listModel.addElement(new SearchResult("Info", "No results", "Try another keyword", null));
        } else {
            results.forEach(listModel::addElement);
        }

        if (popup.isVisible()) {
            showPopup();
        }
    }

    // =========================
    // Data Sources
    // =========================
    private void loadScreenResults() {
        List<String> screens = Arrays.asList(
                "Home",
                "Schedule",
                "Management",
                "Barangay",
                "Users",
                "Settings"
        );

        String q = currentQuery.toLowerCase();

        screens.stream().filter((s) -> (s.toLowerCase().contains(q))).forEachOrdered((s) -> {
            results.add(new SearchResult(
                    "Screen", s, "Open " + s, s
            ));
        });
    }

    private void loadDatabaseResults() {
        String like = "%" + currentQuery + "%";

        try (Connection conn = SQLConnection.getConnection()) {

            if (DbSchemaHelper.tableExists(conn, "barangay")) {
                searchBarangay(conn, like);
            }

            if (DbSchemaHelper.tableExists(conn, "team")) {
                searchTeams(conn, like);
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

    private void searchTeams(Connection conn, String like) throws SQLException {
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
    // Actions
    // =========================
    private void openSelected() {
        int index = resultList.getSelectedIndex();
        if (index < 0 || index >= results.size()) return;

        SearchResult r = results.get(index);

        if (r.panelKey != null && cardLayout != null && cardPanel != null) {
            cardLayout.show(cardPanel, r.panelKey);
        } else {
            JOptionPane.showMessageDialog(anchor, r.details);
        }

        closeActiveDialog();
    }

    public static void showSearchDialog(Component parent, String query,
                                        JPanel cardPanel, CardLayout layout) {

        if (query == null || query.trim().isEmpty()) {
            closeActiveDialog();
            return;
        }

        if (activeDialog != null) {
            activeDialog.updateQuery(query);
            return;
        }

        new SearchDialog(parent, query, cardPanel, layout);
    }

    public static void closeActiveDialog() {
        if (activeDialog != null) {
            activeDialog.popup.setVisible(false);
            activeDialog = null;
        }
    }

    // =========================
    // Model
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
    // Renderer
    // =========================
    private class SearchRenderer extends JPanel implements ListCellRenderer<SearchResult> {

        private final JLabel type = new JLabel();
        private final JLabel title = new JLabel();
        private final JLabel details = new JLabel();

        SearchRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setOpaque(true);

            type.setFont(new Font("Segoe UI", Font.BOLD, 11));
            type.setForeground(new Color(100, 100, 100));

            title.setFont(new Font("Segoe UI", Font.BOLD, 13));

            details.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            details.setForeground(new Color(120, 120, 120));

            add(type);
            add(title);
            add(details);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends SearchResult> list,
                SearchResult value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            if (value == null) return this;

            type.setText(value.type.toUpperCase());
            title.setText(highlight(value.title));
            details.setText(highlight(value.details));

            setBackground(isSelected
                    ? new Color(230, 240, 255)
                    : Color.WHITE);

            return this;
        }
    }

    // =========================
    // Highlight
    // =========================
    private String highlight(String text) {
        if (text == null || currentQuery.isEmpty()) return text;

        String lower = text.toLowerCase();
        String q = currentQuery.toLowerCase();

        int i = lower.indexOf(q);
        if (i < 0) return text;

        return "<html>" +
                text.substring(0, i) +
                "<b>" + text.substring(i, i + q.length()) + "</b>" +
                text.substring(i + q.length()) +
                "</html>";
    }
}