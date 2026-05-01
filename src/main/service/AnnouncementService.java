package main.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import main.dao.AnnouncementDao;
import main.model.Announcement;
import main.store.DataChangeBus;
import main.store.DataTopics;

public class AnnouncementService {

    private final AnnouncementDao dao = new AnnouncementDao();

    public Announcement getActiveAnnouncement() {
        try {
            return dao.findActive();
        } catch (SQLException e) {
            return null;
        }
    }

    public List<Announcement> getArchivedAnnouncements() {
        try {
            return dao.findArchived();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public boolean publishAnnouncement(String title, String message) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setMessage(message);
        try {
            boolean success = dao.saveAndActivate(announcement);
            if (success) {
                DataChangeBus.publish(DataTopics.ANNOUNCEMENTS, DataTopics.ARCHIVE, DataTopics.DASHBOARD);
            }
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean dismissAnnouncement(int announcementId) {
        try {
            boolean success = dao.dismiss(announcementId);
            if (success) {
                DataChangeBus.publish(DataTopics.ANNOUNCEMENTS, DataTopics.ARCHIVE, DataTopics.DASHBOARD);
            }
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean addAnnouncement(Announcement announcement) {
        try {
            boolean success = dao.addAnnouncement(announcement);
            if (success) {
                DataChangeBus.publish(DataTopics.ANNOUNCEMENTS, DataTopics.DASHBOARD);
            }
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateAnnouncement(Announcement announcement) {
        try {
            boolean success = dao.updateAnnouncement(announcement);
            if (success) {
                DataChangeBus.publish(DataTopics.ANNOUNCEMENTS, DataTopics.DASHBOARD);
            }
            return success;
        } catch (SQLException e) {
            return false;
        }
    }
}
