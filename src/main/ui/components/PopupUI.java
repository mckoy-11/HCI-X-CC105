package main.ui.components;

import main.model.PopupItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PopupUI {

    public static void show(Component anchor, List<PopupItem> items) {

        JPopupMenu popup = new JPopupMenu();
        popup.setFocusable(false);

        popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(4, 0, 4, 0)
        ));

        DefaultListModel<PopupItem> model = new DefaultListModel<>();
        items.forEach(model::addElement);

        JList<PopupItem> list = new JList<>(model);
        list.setCellRenderer(new PopupRenderer());
        list.setFixedCellHeight(50);

        list.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i >= 0) list.setSelectedIndex(i);
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = list.getSelectedIndex();
                if (i >= 0) {
                    PopupItem item = model.get(i);

                    if (!item.isDisabled() && item.getAction() != null) {
                        item.getAction().run();
                        popup.setVisible(false);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(
                280,
                Math.min(items.size() * 50, 240)
        ));

        popup.add(scroll);
        
        // Simple and reliable popup display
        popup.show(anchor, 0, anchor.getHeight());
    }

    private static class PopupRenderer extends JPanel
            implements ListCellRenderer<PopupItem> {

        private final JLabel title = new JLabel();
        private final JLabel subtitle = new JLabel();

        PopupRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(8, 10, 8, 10));

            title.setFont(new Font("Segoe UI", Font.BOLD, 13));
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            subtitle.setForeground(new Color(120, 120, 120));

            add(title);
            add(subtitle);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends PopupItem> list,
                PopupItem value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            title.setText(value.getTitle());
            subtitle.setText(value.getSubtitle() != null ? value.getSubtitle() : "");

            title.setForeground(value.isDisabled()
                    ? Color.GRAY
                    : new Color(30, 30, 30));

            setBackground(isSelected && !value.isDisabled()
                    ? new Color(230, 240, 255)
                    : Color.WHITE);

            return this;
        }
    }
}