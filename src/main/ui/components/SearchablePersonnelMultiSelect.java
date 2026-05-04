package main.ui.components;

import main.model.Personnel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static main.ui.style.SystemStyle.*;

/**
 * SearchablePersonnelMultiSelect provides a filterable multi-select list for
 * personnel records using checkboxes.
 */
public class SearchablePersonnelMultiSelect extends JPanel {

    private final List<Personnel> personnelOptions = new ArrayList<Personnel>();
    private final LinkedHashSet<Integer> selectedIds = new LinkedHashSet<Integer>();
    private final LinkedHashSet<Integer> disabledIds = new LinkedHashSet<Integer>();
    private final JTextField searchField = styleInput(new JTextField());
    private final JPanel optionList = createTransparentPanel();
    private final String searchLabel;
    private final String emptyMessage;

    /**
     * Creates a searchable multi-select component for the provided personnel.
     *
     * @param personnel the available personnel options
     */
    public SearchablePersonnelMultiSelect(List<Personnel> personnel) {
        this(
                personnel,
                "Search Collectors",
                "No collectors match the current search."
        );
    }

    /**
     * Creates a searchable multi-select component with custom labels.
     *
     * @param personnel the available personnel options
     * @param searchLabel the label shown above the search field
     * @param emptyMessage the empty state message
     */
    public SearchablePersonnelMultiSelect(List<Personnel> personnel,
                                          String searchLabel,
                                          String emptyMessage) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(FORM_WIDTH, 300));
        setPreferredSize(new Dimension(FORM_WIDTH, 300));
        this.searchLabel = searchLabel;
        this.emptyMessage = emptyMessage;

        if (personnel != null) {
            personnelOptions.addAll(personnel);
        }

        optionList.setLayout(new BoxLayout(optionList, BoxLayout.Y_AXIS));

        add(createFieldGroup(this.searchLabel, searchField), BorderLayout.NORTH);
        add(createScrollPane(), BorderLayout.CENTER);

        installFilterListener();
        refreshOptions();
    }

    /**
     * Returns the currently selected personnel records.
     *
     * @return the selected personnel in option order
     */
    public List<Personnel> getSelectedPersonnel() {
        List<Personnel> selected = new ArrayList<Personnel>();
        for (Personnel personnel : personnelOptions) {
            if (selectedIds.contains(personnel.getId())) {
                selected.add(personnel);
            }
        }
        return selected;
    }

    /**
     * Preselects the provided personnel records.
     *
     * @param personnel the personnel that should start selected
     */
    public void setSelectedPersonnel(List<Personnel> personnel) {
        selectedIds.clear();
        if (personnel != null) {
            for (Personnel item : personnel) {
                if (item != null) {
                    selectedIds.add(item.getId());
                }
            }
        }
        refreshOptions();
    }

    /**
     * Disables the provided personnel ids so they cannot be selected.
     *
     * @param ids the ids that should be disabled
     */
    public void setDisabledPersonnelIds(Set<Integer> ids) {
        disabledIds.clear();
        if (ids != null) {
            disabledIds.addAll(ids);
        }
        selectedIds.removeAll(disabledIds);
        refreshOptions();
    }

    /**
     * Creates the scroll wrapper for the checkbox list.
     *
     * @return the configured scroll pane
     */
    private JScrollPane createScrollPane() {
        JScrollPane scrollPane = new JScrollPane(optionList);
        scrollPane.setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1, true));
        scrollPane.getViewport().setBackground(INPUT_BACKGROUND);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.setOpaque(false);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    /**
     * Installs the live filter listener for the search field.
     */
    private void installFilterListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshOptions();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshOptions();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshOptions();
            }
        });
    }

    /**
     * Rebuilds the visible option list using the current search term.
     */
    private void refreshOptions() {
        optionList.removeAll();

        String query = searchField.getText() != null
                ? searchField.getText().trim().toLowerCase()
                : "";

        boolean hasMatch = false;
        for (Personnel personnel : personnelOptions) {
            if (!matchesQuery(personnel, query)) {
                continue;
            }
            hasMatch = true;
            optionList.add(createOptionRow(personnel));
            optionList.add(Box.createVerticalStrut(8));
        }

        if (!hasMatch) {
            optionList.add(createEmptyState());
        }

        optionList.revalidate();
        optionList.repaint();
    }

    /**
     * Checks whether the personnel record matches the current filter text.
     *
     * @param personnel the personnel to test
     * @param query the lowercase search query
     * @return {@code true} when the record should stay visible
     */
    private boolean matchesQuery(Personnel personnel, String query) {
        if (query.isEmpty()) {
            return true;
        }

        List<String> fields = new ArrayList<String>();
        fields.add(personnel.getFullName());
        fields.add(personnel.getRole());
        fields.add(personnel.getStatus());
        fields.add(personnel.getTeam());

        for (String field : fields) {
            if (field != null && field.toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a styled checkbox row for one personnel option.
     *
     * @param personnel the personnel option to render
     * @return the rendered option row
     */
    private JPanel createOptionRow(final Personnel personnel) {
        JPanel row = Card(14, WHITE);
        row.setLayout(new BorderLayout(10, 0));
        row.setBorder(new EmptyBorder(10, 12, 10, 12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        checkBox.setSelected(selectedIds.contains(personnel.getId()));
        checkBox.setEnabled(!disabledIds.contains(personnel.getId()));
        checkBox.addActionListener(e -> updateSelection(personnel.getId(), checkBox.isSelected()));

        JLabel nameLabel = new JLabel(personnel.getFullName());
        nameLabel.setFont(BUTTONBOLD.deriveFont(13f));
        nameLabel.setForeground(textDark);

        JLabel metaLabel = new JLabel(buildMetaText(personnel));
        metaLabel.setFont(BUTTONPLAIN.deriveFont(12f));
        metaLabel.setForeground(MUTED_TEXT);

        JPanel info = createTransparentPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(nameLabel);
        info.add(Box.createVerticalStrut(4));
        info.add(metaLabel);

        if (!checkBox.isEnabled()) {
            nameLabel.setForeground(textMuted);
            metaLabel.setForeground(textMuted);
        }

        row.add(checkBox, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        return row;
    }

    /**
     * Updates the selected id set when a checkbox changes state.
     *
     * @param personnelId the toggled personnel id
     * @param selected whether the checkbox is selected
     */
    private void updateSelection(int personnelId, boolean selected) {
        if (selected) {
            selectedIds.add(personnelId);
        } else {
            selectedIds.remove(personnelId);
        }
    }

    /**
     * Builds the metadata line shown below each personnel name.
     *
     * @param personnel the personnel option
     * @return the metadata text
     */
    private String buildMetaText(Personnel personnel) {
        List<String> parts = new ArrayList<String>();
        parts.add(personnel.getRole() != null ? personnel.getRole() : "Personnel");
        if (personnel.getStatus() != null && !personnel.getStatus().trim().isEmpty()) {
            parts.add(personnel.getStatus());
        }
        if (personnel.getTeam() != null && !personnel.getTeam().trim().isEmpty()) {
            parts.add(personnel.getTeam());
        }
        return String.join(" | ", parts);
    }

    /**
     * Creates the empty state shown when no personnel match the search.
     *
     * @return the empty state component
     */
    private JComponent createEmptyState() {
        JLabel empty = new JLabel(emptyMessage);
        empty.setFont(BUTTONPLAIN.deriveFont(12f));
        empty.setForeground(MUTED_TEXT);
        empty.setBorder(new EmptyBorder(12, 8, 12, 8));
        empty.setAlignmentX(Component.LEFT_ALIGNMENT);
        return empty;
    }
}
