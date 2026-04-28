package main.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Schedule {

    private int id;
    private String barangayName;
    private String barangayAdmin;
    private String contactNumber;
    private String collectorTeam;
    private String truckPlateNumber;
    private String truckType;
    private String eta;
    private LocalDate date;
    private LocalTime time;
    private String status;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBarangayName() { return barangayName; }
    public void setBarangayName(String barangayName) { this.barangayName = barangayName; }

    public String getBarangayAdmin() { return barangayAdmin; }
    public void setBarangayAdmin(String barangayAdmin) { this.barangayAdmin = barangayAdmin; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getCollectorTeam() { return collectorTeam; }
    public void setCollectorTeam(String collectorTeam) { this.collectorTeam = collectorTeam; }

    public String getTruckPlateNumber() { return truckPlateNumber; }
    public void setTruckPlateNumber(String truckPlateNumber) { this.truckPlateNumber = truckPlateNumber; }

    public String getTruckType() { return truckType; }
    public void setTruckType(String truckType) { this.truckType = truckType; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
