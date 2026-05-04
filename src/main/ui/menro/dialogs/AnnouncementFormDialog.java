package main.ui.menro.dialogs;

import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import main.model.Announcement;
import main.model.Barangay;
import main.service.AnnouncementService;
import main.service.BarangayService;
import static main.ui.style.SystemStyle.*;
import main.store.DataChangeBus;
import static main.store.DataTopics.ANNOUNCEMENTS;
import main.ui.style.BaseFormDialog;

public final class AnnouncementFormDialog extends BaseFormDialog {

    private final AnnouncementService announcementService = new AnnouncementService();
    private final BarangayService barangayService = new BarangayService();

    private final JTextField titleField = styleInput(new JTextField());
    private final JTextArea messageArea;
    private final JSpinner expiryDatePicker = new JSpinner(new SpinnerDateModel());

    private final JCheckBox allBarangayCheckbox = new JCheckBox("All Barangays");
    private final JTextField searchBarangay = new JTextField();
    private final JPanel barangayListPanel = new JPanel();
    private final java.util.Map<Integer, JCheckBox> barangayCheckboxes = new java.util.HashMap<>();

    private final Announcement existingAnnouncement;
    private final boolean isEditMode;

    public AnnouncementFormDialog(Frame parent, Announcement announcement) {
        super(parent, announcement == null ? "Add Announcement" : "Edit Announcement");

        this.existingAnnouncement = announcement;
        this.isEditMode = announcement != null;

        this.messageArea = new JTextArea(6, 24);
        styleTextArea(messageArea, 6);

        expiryDatePicker.setEditor(new JSpinner.DateEditor(expiryDatePicker, "yyyy-MM-dd"));

        allBarangayCheckbox.setFont(BUTTONBOLD.deriveFont(13f));
        allBarangayCheckbox.setOpaque(false);

        allBarangayCheckbox.addActionListener(e -> {
            boolean s = allBarangayCheckbox.isSelected();
            for (JCheckBox cb : barangayCheckboxes.values()) {
                cb.setSelected(s);
                cb.setEnabled(!s);
            }
        });

        loadBarangays();
        populateData();
        initFormBody();
    }

    @Override
    protected JPanel createFormBody() {
        JPanel container = createTransparentPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        container.setBackground(new Color(245, 247, 250));

        JPanel root = new JPanel(new GridLayout(1, 2, 20, 0));
        root.setOpaque(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        left.add(wrap("Title", titleField));
        left.add(Box.createVerticalStrut(10));
        left.add(wrap("Message", new JScrollPane(messageArea)));
        left.add(Box.createVerticalStrut(10));
        left.add(wrap("Expiry Date", expiryDatePicker));
        left.add(Box.createVerticalStrut(10));
        left.add(allBarangayCheckbox);

        root.add(left);

        JPanel right = new JPanel(new BorderLayout(10, 10));
        right.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(searchBarangay, BorderLayout.CENTER);

        right.add(top, BorderLayout.NORTH);

        barangayListPanel.setLayout(new BoxLayout(barangayListPanel, BoxLayout.Y_AXIS));
        barangayListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(barangayListPanel);
        scroll.setBorder(null);

        right.add(scroll, BorderLayout.CENTER);

        root.add(right);

        container.add(root, BorderLayout.CENTER);

        searchBarangay.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }

            void filter() {
                String t = searchBarangay.getText().toLowerCase();
                barangayListPanel.removeAll();
                for (JCheckBox cb : barangayCheckboxes.values()) {
                    if (cb.getText().toLowerCase().contains(t)) {
                        barangayListPanel.add(cb);
                    }
                }
                barangayListPanel.revalidate();
                barangayListPanel.repaint();
            }
        });

        return container;
    }

    @Override
    protected void saveForm() {
        String title = titleField.getText().trim();
        String message = messageArea.getText().trim();
        
        // Validate required fields
        if (title.isEmpty()) {
            showError("Title is required");
            return;
        }
        if (message.isEmpty()) {
            showError("Message is required");
            return;
        }

        try {
            Announcement a = existingAnnouncement != null ? existingAnnouncement : new Announcement();

            a.setTitle(title);
            a.setMessage(message);
            a.setArchived(false);

            Date d = (Date) expiryDatePicker.getValue();
            if (d != null) {
                a.setExpiresAt(new Timestamp(d.getTime()));
            }

            if (!isEditMode) {
                a.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            }

            boolean ok = isEditMode
                    ? announcementService.updateAnnouncement(a)
                    : announcementService.addAnnouncement(a);

            if (ok) {
                showSuccess("Announcement saved successfully");
                SwingUtilities.invokeLater(() -> {
                    DataChangeBus.publish(ANNOUNCEMENTS);
                    dispose();
                });
            } else {
                showError("Failed to save announcement");
            }
        } catch (Exception e) {
            showError("Error saving announcement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBarangays() {
        List<Barangay> list = barangayService.getAllBarangays();

        for (Barangay b : list) {
            JCheckBox cb = new JCheckBox(b.getBarangayName());
            cb.setOpaque(false);
            barangayCheckboxes.put(b.getBarangayId(), cb);
            barangayListPanel.add(cb);
        }
    }

    private void populateData() {
        if (existingAnnouncement != null) {
            titleField.setText(existingAnnouncement.getTitle());
            messageArea.setText(existingAnnouncement.getMessage());

            if (existingAnnouncement.getExpiresAt() != null) {
                expiryDatePicker.setValue(new Date(existingAnnouncement.getExpiresAt().getTime()));
            }
        } else {
            expiryDatePicker.setValue(new Date());
            allBarangayCheckbox.setSelected(true);

            for (JCheckBox cb : barangayCheckboxes.values()) {
                cb.setSelected(true);
                cb.setEnabled(false);
            }
        }
    }

    private JPanel wrap(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setOpaque(false);
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }
}