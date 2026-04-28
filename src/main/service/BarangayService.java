package main.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import main.dao.BarangayDao;
import main.model.Barangay;
import main.store.DataChangeBus;
import main.store.DataTopics;

public class BarangayService {

    private final BarangayDao barangayDao;

    public BarangayService() {
        this(new BarangayDao());
    }

    public BarangayService(BarangayDao barangayDao) {
        this.barangayDao = barangayDao;
    }

    public boolean addBarangay(Barangay barangay) {
        try {
            boolean success = barangayDao.save(barangay);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateBarangay(Barangay barangay) {
        try {
            boolean success = barangayDao.update(barangay);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteBarangay(int id) {
        try {
            boolean success = barangayDao.delete(id);
            publish(success);
            return success;
        } catch (SQLException e) {
            return false;
        }
    }

    public Barangay getBarangayById(int id) {
        try {
            return barangayDao.findById(id);
        } catch (SQLException e) {
            return null;
        }
    }

    public List<Barangay> getAllBarangays() {
        try {
            return barangayDao.findAll();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public Barangay getBarangayByName(String name) {
        try {
            return barangayDao.findByName(name);
        } catch (SQLException e) {
            return null;
        }
    }

    public List<Barangay> getBarangaysByCollectionDay(String day) {
        try {
            return barangayDao.findByCollectionDay(day);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public int getTotalBarangayCount() {
        try {
            return barangayDao.getTotalCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getTotalHousehold() {
        try {
            return barangayDao.getTotalHousehold();
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getTotalScheduleBarangay() {
        try {
            return barangayDao.getTotalSchedBarangay();
        } catch (SQLException e) {
            return 0;
        }
    }

    private void publish(boolean success) {
        if (success) {
            DataChangeBus.publish(DataTopics.BARANGAYS, DataTopics.SCHEDULES, DataTopics.DASHBOARD);
        }
    }
}
