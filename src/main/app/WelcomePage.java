package main.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import main.ui.components.AuthLayout;
import static main.style.SystemStyle.BGCOLOR2;
import static main.style.SystemStyle.BODYPLAIN;
import static main.style.SystemStyle.BODYBOLD;
import static main.style.SystemStyle.Card;
import static main.style.SystemStyle.createCapsuleLabel;
import static main.style.SystemStyle.createFormSubtitle;
import static main.style.SystemStyle.createFormTitle;
import static main.style.SystemStyle.createPrimaryButton;
import static main.style.SystemStyle.createSecondaryButton;
import static main.style.SystemStyle.createTransparentPanel;

public class WelcomePage extends JPanel {

    private final Runnable onGetStarted;
    private final Runnable onLogin;

    public WelcomePage() {
        this(null, null);
    }

    public WelcomePage(Runnable onGetStarted, Runnable onLogin) {
        this.onGetStarted = onGetStarted;
        this.onLogin = onLogin;

        setLayout(new BorderLayout());

        AuthLayout layout = new AuthLayout(
                "Smart waste operations",
                "A calmer way to manage barangay collection work.",
                "Track reports, monitor schedules, and move between MENRO and barangay workflows in one polished workspace.",
                "Clean dashboards keep collection details and community updates easy to spot.",
                "Consistent forms reduce clutter so actions feel fast and predictable.",
                "Shared visual language keeps the whole system feeling connected."
        );
        add(layout, BorderLayout.CENTER);

        buildActions(layout.getFormContent());
    }

    private void buildActions(JPanel form) {
        JButton getStartedButton = createPrimaryButton("Create Account");
        getStartedButton.addActionListener(e -> runAction(onGetStarted));

        JButton loginButton = createSecondaryButton("Log In");
        loginButton.addActionListener(e -> runAction(onLogin));

        JPanel quickRow = createTransparentPanel(new GridLayout(1, 2, 5, 0));
        quickRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        quickRow.add(createStat("24/7", "Collection visibility"));
        quickRow.add(createStat("Live", "Request tracking"));

        JLabel note = createFormSubtitle("<html>Choose how you want to enter the system. <br> Everything stays on one screen for a cleaner start.<html>");
        note.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        form.add(createFormTitle("Welcome back"));
        form.add(Box.createVerticalStrut(12));
        form.add(note);
        form.add(Box.createVerticalStrut(26));
        form.add(quickRow);
        form.add(Box.createVerticalStrut(30));
        form.add(getStartedButton);
        form.add(Box.createVerticalStrut(14));
        form.add(loginButton);
        form.add(Box.createVerticalGlue());
    }

    private JPanel createStat(String value, String label) {
        JPanel panel = Card(12, BGCOLOR2);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        panel.setAlignmentY(Component.TOP_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(BODYBOLD.deriveFont(18f));
        valueLabel.setForeground(main.style.SystemStyle.textDark);

        JLabel labelText = new JLabel(label);
        labelText.setFont(BODYPLAIN.deriveFont(12f));
        labelText.setForeground(main.style.SystemStyle.MUTED_TEXT);

        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(labelText);
        return panel;
    }

    private void runAction(Runnable action) {
        if (action != null) {
            action.run();
        }
    }
}
