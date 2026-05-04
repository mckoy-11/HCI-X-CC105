package main.ui.menro.dialogs;

import main.ui.style.SystemStyle;

import javax.swing.*;
import java.awt.*;

public class ConfirmDialog extends JDialog {

    private boolean confirmed = false;

    public ConfirmDialog(Window parent, String title, String message) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);

        // 🔹 Root container (background dim)
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(0, 0, 0, 80)); // soft overlay

        // 🔹 Card (Figma-style container)
        JPanel card = SystemStyle.Card(
                SystemStyle.FORM_CORNER_RADIUS,
                SystemStyle.cardBackground
        );
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(
                SystemStyle.FORM_PADDING,
                SystemStyle.FORM_PADDING,
                SystemStyle.FORM_PADDING,
                SystemStyle.FORM_PADDING
        ));
        card.setMaximumSize(new Dimension(360, Integer.MAX_VALUE));

        // 🔹 Title
        JLabel titleLabel = SystemStyle.createFormTitle(title);

        // 🔹 Message
        JLabel messageLabel = SystemStyle.createFormSubtitle(
                "<html style='width:260px'>" + message + "</html>"
        );

        // 🔹 Buttons
        JButton cancelBtn = SystemStyle.createFormButton("Cancel", false);
        JButton confirmBtn = SystemStyle.createFormButton("Confirm", true);

        cancelBtn.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        confirmBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        JPanel actions = SystemStyle.createFormActions(cancelBtn, confirmBtn);

        // 🔹 Layout assembly
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(messageLabel);
        card.add(actions);

        root.add(card);

        setContentPane(root);
        pack();
        setSize(380, getHeight());
        setLocationRelativeTo(parent);
    }

    public static boolean show(Component parent, String title, String message) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        ConfirmDialog dialog = new ConfirmDialog(window, title, message);
        dialog.setVisible(true);
        return dialog.confirmed;
    }
}