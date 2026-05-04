package main.ui.components;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import main.model.PopupItem;
import static main.ui.style.SystemStyle.*;

/**
 * ScrollableTable is a reusable Swing component that encapsulates a JTable
 * with custom styling and row management.
 *
 * It supports:
 * - Standard row insertion (addRow)
 * - Rows with action buttons (addRowWithAction)
 * - Custom rendering and selection styling
 *
 * Usage:
 * ScrollableTable table = new ScrollableTable("Name", "Age", "Action");
 * table.addRow("John", 25);
 * table.addRowWithAction("Jane", 30, popupItems);
 */
public final class ScrollableTable extends JPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private final JScrollPane scrollPane;

    public ScrollableTable(String... columnNames) {
        setLayout(new BorderLayout());
        setOpaque(false);

        final int actionColumnIndex = columnNames.length - 1;

        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the action column (last column) is editable
                return column == actionColumnIndex;
            }
        };

        table = new JTable(model);
        configureTable();
        configureHeader();
        configureCells();

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(null);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new JPanel());
        scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, new JPanel());
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JPanel());
        scrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, new JPanel());

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape clip = new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20);
        g2.clip(clip);

        g2.setColor(getBackground());
        g2.fill(clip);

        g2.dispose();
    }

    public void addRow(Object... values) {
        if (values.length != model.getColumnCount()) {
            throw new IllegalArgumentException(
                    "Expected " + model.getColumnCount() + " values but got " + values.length
            );
        }
        model.addRow(values);
    }

    public void addRowWithAction(Object... values) {
        if (values.length != model.getColumnCount()) {
            throw new IllegalArgumentException(
                    "Expected " + model.getColumnCount() + " values but got " + values.length
            );
        }
        
        // Get the last column (action column) value
        Object actionValue = values[values.length - 1];
        
        // Configure action column for this row
        int actionCol = table.getColumnCount() - 1;
        TableColumn column = table.getColumnModel().getColumn(actionCol);
        column.setCellRenderer(new ActionCellRenderer());
        column.setCellEditor(new ActionCellEditor(table, actionValue));
        
        model.addRow(values);
    }

    /**
     * Adds a row with an action button that shows edit and delete options.
     * 
     * @param onEdit Runnable to execute when Edit is clicked
     * @param onDelete Runnable to execute when Delete is clicked
     * @param values The cell values for the row (excluding action column)
     */
    public void addRowWithEditDelete(Runnable onEdit, Runnable onDelete, Object... values) {
        // Create popup items for edit and delete
        java.util.List<PopupItem> items = java.util.Arrays.asList(
            new PopupItem("Edit", "Modify this record", onEdit),
            new PopupItem("Delete", "Remove this record", onDelete)
        );
        
        // Create the values array with the popup items as the last element
        Object[] allValues = new Object[values.length + 1];
        System.arraycopy(values, 0, allValues, 0, values.length);
        allValues[values.length] = items;
        
        addRowWithAction(allValues);
    }
    
    public void clear() {
        model.setRowCount(0);
    }

    public JTable getTable() {
        return table;
    }

    private void configureTable() {
        table.setRowHeight(40);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    }

    private void configureHeader() {
        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setBackground(HEADER);
        header.setForeground(new Color(60, 60, 60));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setFont(HEADER_FONT);
                label.setOpaque(true);
                label.setBackground(HEADER);
                label.setForeground(new Color(80, 80, 80));
                label.setVerticalAlignment(SwingConstants.CENTER);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
                return label;
            }
        });
    }

    private void configureCells() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setFont(CELL_FONT);
                label.setOpaque(true);
                label.setVerticalAlignment(SwingConstants.CENTER);

                int columnIndex = table.convertColumnIndexToModel(column);
                String columnName = table.getColumnName(columnIndex);

                switch (columnName) {
                    case "Barangay":
                    case "Admin":
                    case "Name":
                    case "Address":
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        break;

                    default:
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                        break;
                }
                
                if (isSelected) {
                    label.setBackground(SELECTED_ROW);
                } else if (row % 2 == 0) {
                    label.setBackground(EVEN_ROW);
                } else {
                    label.setBackground(ODD_ROW);
                }

                label.setForeground(new Color(40, 40, 40));
                label.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private static class ActionCellRenderer implements TableCellRenderer {
        private final JButton button;

        public ActionCellRenderer() {
            button = new JButton();
            button.setIcon(loadIcon("ellipsis.png", 20));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setOpaque(true);
            button.setHorizontalAlignment(SwingConstants.CENTER);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setMargin(new Insets(0, 0, 0, 0));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            if (isSelected) {
                button.setBackground(SELECTED_ROW);
            } else if (row % 2 == 0) {
                button.setBackground(EVEN_ROW);
            } else {
                button.setBackground(ODD_ROW);
            }

            return button;
        }
    }

    private static class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

        private final JButton button;
        private final JTable parentTable;
        private Object currentValue;

        public ActionCellEditor(JTable table, Object value) {
            this.parentTable = table;
            this.currentValue = value;

            button = new JButton();
            button.setIcon(loadIcon("ellipsis.png", 20));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setOpaque(true);
            button.setHorizontalAlignment(SwingConstants.CENTER);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setMargin(new Insets(0, 0, 0, 0));

            button.addActionListener(e -> showPopupMenu());
        }

        private void showPopupMenu() {
            // Get the row from the editing context - this is the correct way
            int row = parentTable.getEditingRow();
            
            // If not in editing mode, get selected row
            if (row < 0) {
                row = parentTable.getSelectedRow();
            }
            
            if (row < 0) {
                return;
            }

            // Select the row
            parentTable.setRowSelectionInterval(row, row);

            // Get the action value from the model
            int actionCol = parentTable.getColumnCount() - 1;
            Object value = parentTable.getModel().getValueAt(row, actionCol);

            if (value instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<PopupItem> items = (java.util.List<PopupItem>) value;

                if (!items.isEmpty()) {
                    for (PopupItem item : items) {
                        item.setContext(row, parentTable);
                    }

                    // Create and show popup directly
                    JPopupMenu popup = new JPopupMenu();
                    popup.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                    popup.setPreferredSize(new Dimension(220, items.size() * 55 + 10));
                    
                    for (PopupItem item : items) {
                        JPanel itemPanel = createPopupItemPanel(item, row);
                        JMenuItem menuItem = new JMenuItem("");
                        menuItem.setLayout(new BorderLayout());
                        menuItem.add(itemPanel);
                        menuItem.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                        menuItem.setPreferredSize(new Dimension(200, 55));
                        popup.add(menuItem);
                    }
                    
                    // Show popup below button
                    popup.show(button, 0, button.getHeight());
                }
            }

            // Stop editing after popup is shown - use invokeLater to ensure popup displays first
            SwingUtilities.invokeLater(() -> {
                try {
                    fireEditingStopped();
                } catch (Exception ex) {
                    // Ignore if already stopped
                }
            });
        }
        
        private JPanel createPopupItemPanel(PopupItem item, int row) {
            JPanel panel = new JPanel(new BorderLayout(10, 2));
            panel.setOpaque(true);
            panel.setPreferredSize(new Dimension(200, 55));
            panel.setMaximumSize(new Dimension(200, 55));
            panel.setMinimumSize(new Dimension(200, 55));
            panel.setBackground(item.isDisabled() ? new Color(245, 245, 245) : Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
            
            JLabel titleLabel = new JLabel(item.getTitle());
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(item.isDisabled() ? Color.GRAY : new Color(30, 30, 30));
            
            JLabel subtitleLabel = new JLabel(item.getSubtitle() != null ? item.getSubtitle() : "");
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            subtitleLabel.setForeground(Color.GRAY);
            
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 0));
            textPanel.setOpaque(false);
            textPanel.add(titleLabel);
            textPanel.add(subtitleLabel);
            
            panel.add(textPanel, BorderLayout.CENTER);
            
            // Add click handler
            if (!item.isDisabled() && item.getAction() != null) {
                panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                panel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        item.getAction().run();
                    }
                });
            }
            
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected,
                int row, int column) {

            this.currentValue = value;

            if (isSelected) {
                button.setBackground(SELECTED_ROW);
            } else if (row % 2 == 0) {
                button.setBackground(EVEN_ROW);
            } else {
                button.setBackground(ODD_ROW);
            }

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return currentValue;
        }

        @Override
        public boolean stopCellEditing() {
            currentValue = null;
            return super.stopCellEditing();
        }
    }
}