package main.service;

import java.util.Collections;
import java.util.List;
import main.dao.RequestDao;
import main.model.Request;
import main.store.DataChangeBus;
import main.store.DataTopics;

public class RequestService {

    private final RequestDao requestDao;

    public RequestService() {
        this(new RequestDao());
    }

    public RequestService(RequestDao requestDao) {
        this.requestDao = requestDao;
    }

    public boolean addRequest(Request request) {
        try {
            boolean success = requestDao.save(request);
            publish(success);
            return success;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateRequest(Request request) {
        try {
            boolean success = requestDao.update(request);
            publish(success);
            return success;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteRequest(int id) {
        try {
            boolean success = requestDao.delete(id);
            publish(success);
            return success;
        } catch (Exception e) {
            return false;
        }
    }

    public Request getRequestById(int id) {
        try {
            return requestDao.findById(id);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Request> getAllRequests() {
        try {
            return requestDao.findAll();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Request> getRequestsByStatus(String status) {
        try {
            return requestDao.findByStatus(status);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Request> getUnreadRequests() {
        try {
            return requestDao.findUnread();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Request> getArchivedRequests() {
        try {
            return requestDao.findArchived();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public boolean markAsRead(int id) {
        try {
            boolean success = requestDao.markAsRead(id);
            publish(success);
            return success;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateStatus(int id, String status) {
        try {
            boolean success = requestDao.updateStatus(id, status);
            publish(success);
            return success;
        } catch (Exception e) {
            return false;
        }
    }

    public int getTotalRequestCount() {
        try {
            return requestDao.getTotalCount();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getReviewedRequestCount() {
        try {
            return requestDao.getCountByStatus("Approved") + requestDao.getCountByStatus("Rejected");
        } catch (Exception e) {
            return 0;
        }
    }

    public int getUnreadRequestCount() {
        try {
            return requestDao.getUnreadCount();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getArchivedRequestCount() {
        try {
            return requestDao.getArchivedCount();
        } catch (Exception e) {
            return 0;
        }
    }

    private void publish(boolean success) {
        if (success) {
            DataChangeBus.publish(DataTopics.REQUESTS, DataTopics.ARCHIVE, DataTopics.DASHBOARD);
        }
    }
}
