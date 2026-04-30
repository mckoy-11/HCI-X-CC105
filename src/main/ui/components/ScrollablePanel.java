package main.ui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ScrollablePanel extends JPanel {

    private final JScrollPane scrollPane;

    public ScrollablePanel(JComponent content) {
        setLayout(new BorderLayout());
        setOpaque(false);

        content.setOpaque(false);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(content, BorderLayout.NORTH);

        scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);

        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.getVerticalScrollBar().setOpaque(false);
        scrollPane.getHorizontalScrollBar().setOpaque(false);

        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.setFocusable(true);

        scrollPane.setBackground(new Color(0, 0, 0, 0));
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));

        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        enableKeyboardScroll();
        enableMouseWheelScroll();

        add(scrollPane, BorderLayout.CENTER);
    }

    private void enableKeyboardScroll() {
        InputMap im = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = scrollPane.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "scrollDown");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "scrollUp");

        am.put("scrollDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setValue(bar.getValue() + 20);
            }
        });

        am.put("scrollUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setValue(bar.getValue() - 20);
            }
        });
    }

    private void enableMouseWheelScroll() {
        scrollPane.addMouseWheelListener(e -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            int amount = e.getUnitsToScroll() * 10;
            bar.setValue(bar.getValue() + amount);
        });
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }
}