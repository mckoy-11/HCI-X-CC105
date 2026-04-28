package main.model;

public class NotificationSummary {

    private final int barangay;
    private final int schedule;
    private final int reports;

    public NotificationSummary(int barangay, int schedule, int reports) {
        this.barangay = barangay;
        this.schedule = schedule;
        this.reports = reports;
    }

    public int getBarangay() {
        return barangay;
    }

    public int getSchedule() {
        return schedule;
    }

    public int getReports() {
        return reports;
    }
}