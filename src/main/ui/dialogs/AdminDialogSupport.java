package main.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import static main.style.SystemStyle.Card;
import static main.style.SystemStyle.BACKGROUND;
import static main.style.SystemStyle.SIDEBAR;
import static main.style.SystemStyle.SUBTITLEBOLD;
import static main.style.SystemStyle.WHITE;
import static main.style.SystemStyle.createPrimaryButton;
import static main.style.SystemStyle.createSecondaryButton;
import static main.style.SystemStyle.createTransparentPanel;
import static main.ui.components.CustomButton.createButton;

public final class AdminDialogSupport {

    private AdminDialogSupport() {
    }

    public static Frame resolveFrame(Component c) {
        Window window = SwingUtilities.getWindowAncestor(c);
        if (window instanceof Frame) {
            return (Frame) window;
        }
        if (window instanceof Dialog) {
            Window owner = ((Dialog) window).getOwner();
            if (owner instanceof Frame) {
                return (Frame) owner;
            }
        }
        return JOptionPane.getRootFrame();
    }

    public static void configureFormDialog(JDialog dialog) {
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        dialog.getRootPane().registerKeyboardAction(
                event -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public static JPanel createFormContentPanel() {
        JPanel content = createTransparentPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        return content;
    }

    public static JScrollPane createContentScroll(JComponent content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    public static JScrollPane createTextAreaScroll(JTextArea textArea, int height) {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(0, height));
        return scrollPane;
    }

    public static boolean showFormDialog(Component parent, String title, JComponent content) {
        Frame frame = resolveFrame(parent);
        JDialog dialog = new JDialog(frame, title, true);
        configureFormDialog(dialog);
        dialog.setSize(560, 700);

        JPanel main = Card(24, 24, 24, 24, 4, 48, WHITE);
        main.setLayout(new BorderLayout());
        main.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel header = Card(24, 24, 0, 0, 0, 0, SIDEBAR);
        header.setBorder(new EmptyBorder(18, 22, 18, 18));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(java.awt.Color.WHITE);
        titleLabel.setFont(SUBTITLEBOLD.deriveFont(22f));
        JButton close = createButton("", "close.png", "close.png", 14);
        close.addActionListener(event -> dialog.dispose());
        header.setLayout(new BorderLayout());
        header.add(titleLabel, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);

        JPanel body = createTransparentPanel(new BorderLayout());
        body.setBorder(new EmptyBorder(18, 18, 18, 18));
        body.add(content, BorderLayout.NORTH);

        boolean[] saved = {false};
        JButton cancel = createSecondaryButton("Cancel");
        cancel.setPreferredSize(new Dimension(140, 48));
        cancel.addActionListener(event -> dialog.dispose());

        JButton save = createPrimaryButton("Save");
        save.setPreferredSize(new Dimension(160, 48));
        save.addActionListener(event -> {
            saved[0] = true;
            dialog.dispose();
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 18, 12, 18));
        footer.add(cancel);
        footer.add(save);

        main.add(header, BorderLayout.NORTH);
        main.add(createContentScroll(body), BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        dialog.add(main);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
        return saved[0];
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
