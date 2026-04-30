package main.ui.dialogs;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import static main.style.SystemStyle.*;
import static main.ui.components.CustomButton.createButton;

public final class AdminDialogSupport {

    private AdminDialogSupport() {}

    public static Frame resolveFrame(Component c) {
        Window window = SwingUtilities.getWindowAncestor(c);
        if (window instanceof Frame) return (Frame) window;

        if (window instanceof Dialog) {
            Window owner = ((Dialog) window).getOwner();
            if (owner instanceof Frame) return (Frame) owner;
        }

        return JOptionPane.getRootFrame();
    }

    public static void configureFormDialog(JDialog dialog) {
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public static JPanel createFormContentPanel() {
        JPanel content = createTransparentPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        return content;
    }

    public static JScrollPane createContentScroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    public static boolean showFormDialog(Component parent, String title, JComponent content) {

        Frame frame = resolveFrame(parent);
        JDialog dialog = new JDialog(frame, title, true);
        configureFormDialog(dialog);

        // MAIN CARD
        JPanel main = Card(24, 24, 24, 24, 2, 48, WHITE);
        main.setLayout(new BorderLayout());

        // HEADER
        JPanel header = Card(20, 20, 0, 0, 0, 0, SIDEBAR);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(SUBTITLEBOLD.deriveFont(20f));

        header.add(titleLabel, BorderLayout.WEST);

        // BODY (CENTERED)
        JPanel body = createTransparentPanel(new GridBagLayout());
        body.setBorder(new EmptyBorder(20, 24, 10, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        body.add(content, gbc);

        // FOOTER (RIGHT ALIGNED)
        JButton cancel = createSecondaryButton("Cancel");
        cancel.setPreferredSize(new Dimension(120, 42));
        cancel.addActionListener(e -> dialog.dispose());

        boolean[] saved = {false};

        JButton save = createPrimaryButton("Save");
        save.setPreferredSize(new Dimension(140, 42));
        save.addActionListener(e -> {
            saved[0] = true;
            dialog.dispose();
        });

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 24, 16, 24));
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));

        footer.add(Box.createHorizontalGlue());
        footer.add(cancel);
        footer.add(Box.createHorizontalStrut(12));
        footer.add(save);

        // ASSEMBLE
        main.add(header, BorderLayout.NORTH);
        main.add(body, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        dialog.add(main);

        int width = dialog.getPreferredSize().width;
        int height = dialog.getPreferredSize().height;

        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);

        return saved[0];
    }
    
    public static JScrollPane createTextAreaScroll(JTextArea textArea, int height) { 
        textArea.setLineWrap(true); 
        textArea.setWrapStyleWord(true); 
        JScrollPane scrollPane = new JScrollPane(textArea); 
        scrollPane.setPreferredSize(new Dimension(0, height)); 
        return scrollPane; 
    }

    public static boolean confirmAction(Component parent, String title, String message) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showFailure(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}