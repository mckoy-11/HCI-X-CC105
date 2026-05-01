package main.model;

public class Truck {
    
    private int id;
    private String plateNumber;
    private String truckType;
    private String capacity;
    private String assignedBarangay;
    private String status;
    private String assignedTeam;

    public Truck() {
    }

    public Truck(int id, String plateNumber, String truckType, String status) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.truckType = truckType;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getTruckType() {
        return truckType;
    }

    public void setTruckType(String truckType) {
        this.truckType = truckType;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getAssignedBarangay() {
        return assignedBarangay;
    }

    public void setAssignedBarangay(String assignedBarangay) {
        this.assignedBarangay = assignedBarangay;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

public String getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(String assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

/**
     * Returns display value for ComboBox.
     * Shows truck plate number or "Unassigned" if no truck is assigned.
     */
    @Override
    public String toString() {
        return plateNumber != null && !plateNumber.trim().isEmpty() ? plateNumber : "Unassigned";
    }
}
