package main.service;

import java.util.Collections;
import java.util.List;
import main.dao.PersonnelDao;
import main.model.Personnel;
import main.store.DataChangeBus;
import main.store.DataTopics;

public class PersonnelService {

    private final PersonnelDao personnelDao;

    public PersonnelService() {
        this.personnelDao = new PersonnelDao();
    }

    public List<Personnel> getAllPersonnel() {
        return safeList(personnelDao.getAllPersonnel());
    }

    public List<Personnel> getAllUnassignedPersonnel() {
        return safeList(personnelDao.getAllUnassignedPersonnel());
    }

    public Personnel getPersonnelById(int id) {
        return personnelDao.getPersonnelById(id);
    }

    public List<Personnel> getPersonnelByRole(String role) {
        return safeList(personnelDao.getPersonnelByRole(role));
    }

    public boolean addPersonnel(Personnel personnel) {
        boolean success = personnelDao.addPersonnel(personnel);
        publish(success);
        return success;
    }

    public boolean updatePersonnel(Personnel personnel) {
        boolean success = personnelDao.updatePersonnel(personnel);
        publish(success);
        return success;
    }

    public boolean deletePersonnel(int id) {
        boolean success = personnelDao.deletePersonnel(id);
        publish(success);
        return success;
    }

    public boolean updateStatus(int id, String status) {
        boolean success = personnelDao.updateStatus(id, status);
        publish(success);
        return success;
    }

    public int getTotalPersonnelCount() {
        return personnelDao.getTotalPersonnelCount();
    }

    public int getActivePersonnelCount() {
        return personnelDao.getActivePersonnelCount();
    }

    public int getUnassignedPersonnelCount() {
        return personnelDao.getUnassignedPersonnelCount();
    }

    private List<Personnel> safeList(List<Personnel> data) {
        return data == null ? Collections.<Personnel>emptyList() : data;
    }

    private void publish(boolean success) {
        if (success) {
            DataChangeBus.publish(DataTopics.PERSONNEL, DataTopics.TEAMS, DataTopics.DASHBOARD);
        }
    }
}
