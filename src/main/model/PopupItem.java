package main.model;

import javax.swing.JTable;

public class PopupItem {

    private final String title;
    private final String subtitle;
    private final Runnable action;
    private final boolean disabled;
    private int row;
    private JTable table;

    public PopupItem(String title, String subtitle, Runnable action, boolean disabled) {
        this.title = title;
        this.subtitle = subtitle;
        this.action = action;
        this.disabled = disabled;
    }

    public PopupItem(String title, String subtitle, Runnable action) {
        this(title, subtitle, action, false);
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Runnable getAction() {
        return action;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setContext(int row, JTable table) {
        this.row = row;
        this.table = table;
    }

    public int getRow() {
        return row;
    }

    public JTable getTable() {
        return table;
    }
}