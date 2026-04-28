package main.model;

import java.sql.Timestamp;

public class PurokChecklistItem {

    private int checklistId;
    private int barangayId;
    private String barangayName;
    private String purokName;
    private boolean collected;
    private Timestamp updatedAt;

    public int getChecklistId() {
        return checklistId;
    }

    public void setChecklistId(int checklistId) {
        this.checklistId = checklistId;
    }

    public int getBarangayId() {
        return barangayId;
    }

    public void setBarangayId(int barangayId) {
        this.barangayId = barangayId;
    }

    public String getBarangayName() {
        return barangayName;
    }

    public void setBarangayName(String barangayName) {
        this.barangayName = barangayName;
    }

    public String getPurokName() {
        return purokName;
    }

    public void setPurokName(String purokName) {
        this.purokName = purokName;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
