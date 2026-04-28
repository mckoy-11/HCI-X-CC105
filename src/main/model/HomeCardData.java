package main.model;

public class HomeCardData {
    
    private int todayCollection;
    private int completedCollection;
    private int missedCollection;
    private int unreadComplaints;
    
    private String todayBarangay;
    private String completedBarangay;
    private String missedBarangay;

    public HomeCardData() {}

    public HomeCardData(int todayCollection, int completedCollection, 
                        int missedCollection, int unreadComplaints,
                        String todayBarangay, String completedBarangay, String missedBarangay) {
        this.todayCollection = todayCollection;
        this.completedCollection = completedCollection;
        this.missedCollection = missedCollection;
        this.unreadComplaints = unreadComplaints;
        this.todayBarangay = todayBarangay;
        this.completedBarangay = completedBarangay;
        this.missedBarangay = missedBarangay;
    }

    public int getTodayCollection() { return todayCollection; }
    public void setTodayCollection(int todayCollection) { this.todayCollection = todayCollection; }

    public int getCompletedCollection() { return completedCollection; }
    public void setCompletedCollection(int completedCollection) { this.completedCollection = completedCollection; }

    public int getMissedCollection() { return missedCollection; }
    public void setMissedCollection(int missedCollection) { this.missedCollection = missedCollection; }

    public int getUnreadComplaints() { return unreadComplaints; }
    public void setUnreadComplaints(int unreadComplaints) { this.unreadComplaints = unreadComplaints; }

    public String getTodayBarangay() { return todayBarangay; }
    public void setTodayBarangay(String todayBarangay) { this.todayBarangay = todayBarangay; }

    public String getCompletedBarangay() { return completedBarangay; }
    public void setCompletedBarangay(String completedBarangay) { this.completedBarangay = completedBarangay; }

    public String getMissedBarangay() { return missedBarangay; }
    public void setMissedBarangay(String missedBarangay) { this.missedBarangay = missedBarangay; }
}