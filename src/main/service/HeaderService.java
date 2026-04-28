package main.service;

import main.dao.NotificationDao;
import main.model.PopupItem;

import java.util.ArrayList;
import java.util.List;
import main.model.NotificationSummary;

public class HeaderService {

    private final NotificationDao notificationDao = new NotificationDao();

    public List<PopupItem> buildNotificationItems(Runnable refreshAction) {

        NotificationSummary s = notificationDao.fetchSummary();

        List<PopupItem> items = new ArrayList<>();

        items.add(new PopupItem("Recent Activity", null, null, true));

        items.add(new PopupItem(
                formatSummary(s),
                "System overview",
                null,
                true
        ));

        items.add(new PopupItem(
                "Refresh",
                "Reload system data",
                refreshAction
        ));

        return items;
    }

    private String formatSummary(NotificationSummary s) {
        return "Barangays: " + s.getBarangay()
                + " | Schedules: " + s.getSchedule()
                + " | Reports: " + s.getReports();
    }

    public List<PopupItem> buildAccountItems(Runnable editName, Runnable logout) {

        List<PopupItem> items = new ArrayList<>();

        items.add(new PopupItem(
                "Edit Display Name",
                "Update profile name",
                editName
        ));

        items.add(new PopupItem(
                "Logout",
                "Sign out of system",
                logout
        ));

        return items;
    }
}