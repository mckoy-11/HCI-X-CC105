package main.model;

import java.sql.Timestamp;

public class Complaint {
    private int complaintId;
    private int barangayId;
    private String barangayName;
    private String type;
    private String message;
    private byte[] proof;
    private String status;
    private boolean isRead;
    private boolean archived;
    private Timestamp createdAt;
    private Timestamp archivedAt;
    private String location;
    private String responseMessage;

    public Complaint() {}

    public Complaint(String message, String status) {
        this.message = message;
        this.status = status;
        this.isRead = false;
    }

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public int getBarangayId() { return barangayId; }
    public void setBarangayId(int barangayId) { this.barangayId = barangayId; }

    public String getBarangayName() { return barangayName; }
    public void setBarangayName(String barangayName) { this.barangayName = barangayName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public byte[] getProof() { return proof; }
    public void setProof(byte[] proof) { this.proof = proof; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getArchivedAt() { return archivedAt; }
    public void setArchivedAt(Timestamp archivedAt) { this.archivedAt = archivedAt; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
}
