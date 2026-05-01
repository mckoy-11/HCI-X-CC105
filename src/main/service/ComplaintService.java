package main.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import main.dao.ComplaintDao;
import main.model.Complaint;
import main.store.DataChangeBus;
import main.store.DataTopics;

public class ComplaintService {

    private final ComplaintDao complaintDao;

    public ComplaintService() {
        this(new ComplaintDao());
    }

    public ComplaintService(ComplaintDao complaintDao) {
        this.complaintDao = complaintDao;
    }

    public boolean addComplaint(Complaint complaint) {
        try {
            boolean success = complaintDao.save(complaint);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateComplaint(Complaint complaint) {
        try {
            boolean success = complaintDao.update(complaint);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteComplaint(int id) {
        try {
            boolean success = complaintDao.delete(id);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public Complaint getComplaintById(int id) {
        try {
            return complaintDao.findById(id);
        } catch (SQLException e) {
            return null;
        }
    }

    public List<Complaint> getAllComplaints() {
        try {
            return complaintDao.findAll();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<Complaint> getComplaintsByStatus(String status) {
        try {
            return complaintDao.findByStatus(status);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<Complaint> getUnreadComplaints() {
        try {
            return complaintDao.findUnread();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<Complaint> getArchivedComplaints() {
        try {
            return complaintDao.findArchived();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public boolean markAsRead(int id) {
        try {
            boolean success = complaintDao.markAsRead(id);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateStatus(int id, String status) {
        try {
            boolean success = complaintDao.updateStatus(id, status);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public int getTotalComplaintCount() {
        try {
            return complaintDao.getTotalCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getReviewedComplaintCount() {
        try {
            return complaintDao.getCountByStatus("Approved") + complaintDao.getCountByStatus("Rejected");
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getUnreadComplaintCount() {
        try {
            return complaintDao.getUnreadCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getArchivedComplaintCount() {
        try {
            return complaintDao.getArchivedCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    private void publish(boolean success) {
        if (success) {
            DataChangeBus.publish(DataTopics.COMPLAINTS, DataTopics.ARCHIVE, DataTopics.DASHBOARD);
        }
    }
}
