package main.service;

import java.util.Collections;
import java.util.List;
import main.dao.TruckDao;
import main.model.Truck;
import main.store.DataChangeBus;
import main.store.DataTopics;

public class TruckService {

    private final TruckDao truckDao;

    public TruckService() {
        this.truckDao = new TruckDao();
    }

    public List<Truck> getAllTrucks() {
        List<Truck> trucks = truckDao.getAllTrucks();
        return trucks == null ? Collections.<Truck>emptyList() : trucks;
    }

    public Truck getTruckById(int id) {
        return truckDao.getTruckById(id);
    }

    public boolean addTruck(Truck truck) {
        boolean success = truckDao.addTruck(truck);
        publish(success);
        return success;
    }

    public boolean updateTruck(Truck truck) {
        boolean success = truckDao.updateTruck(truck);
        publish(success);
        return success;
    }

    public boolean deleteTruck(int id) {
        boolean success = truckDao.deleteTruck(id);
        publish(success);
        return success;
    }

    public int getTotalTruckCount() {
        return truckDao.getTotalTruckCount();
    }

    public int getActiveTruckCount() {
        return truckDao.getActiveTruckCount();
    }

    private void publish(boolean success) {
        if (success) {
            DataChangeBus.publish(DataTopics.TRUCKS, DataTopics.TEAMS, DataTopics.DASHBOARD);
        }
    }
}
