package main.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import main.dao.ReportDao;
import main.model.Report;
import main.store.DataChangeBus;
import main.store.DataTopics;

public class ReportService {

    private final ReportDao reportDao;

    public ReportService() {
        this(new ReportDao());
    }

    public ReportService(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public boolean addReport(Report report) {
        try {
            boolean success = reportDao.save(report);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateReport(Report report) {
        try {
            boolean success = reportDao.update(report);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteReport(int id) {
        try {
            boolean success = reportDao.delete(id);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public Report getReportById(int id) {
        try {
            return reportDao.findById(id);
        } catch (SQLException e) {
            return null;
        }
    }

    public List<Report> getAllReports() {
        try {
            return reportDao.findAll();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<Report> getReportsByStatus(String status) {
        try {
            return reportDao.findByStatus(status);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<Report> getUnreadReports() {
        try {
            return reportDao.findUnread();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<Report> getArchivedReports() {
        try {
            return reportDao.findArchived();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public boolean markAsRead(int id) {
        try {
            boolean success = reportDao.markAsRead(id);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateStatus(int id, String status) {
        try {
            boolean success = reportDao.updateStatus(id, status);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public int getTotalReportCount() {
        try {
            return reportDao.getTotalCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getReviewedReportCount() {
        try {
            return reportDao.getCountByStatus("Approved") + reportDao.getCountByStatus("Rejected");
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getUnreadReportCount() {
        try {
            return reportDao.getUnreadCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getArchivedReportCount() {
        try {
            return reportDao.getArchivedCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    private void publish(boolean success) {
        if (success) {
            DataChangeBus.publish(DataTopics.REPORTS, DataTopics.ARCHIVE, DataTopics.DASHBOARD);
        }
    }
}
