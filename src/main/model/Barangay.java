package main.model;

public class Barangay {
    private int barangayId;
    private String barangayName;
    private int barangayHousehold;
    private int purokCount;
    private int population;
    private String contact;
    private String collectionDay;
    private String status;

    public Barangay() {}

    public Barangay(String barangayName, String collectionDay) {
        this.barangayName = barangayName;
        this.collectionDay = collectionDay;
    }

    public int getBarangayId() { return barangayId; }
    public void setBarangayId(int barangayId) { this.barangayId = barangayId; }

    public String getBarangayName() { return barangayName; }
    public void setBarangayName(String barangayName) { this.barangayName = barangayName; }

    public int getBarangayHousehold() { return barangayHousehold; }
    public void setBarangayHousehold(int barangayHousehold) { this.barangayHousehold = barangayHousehold; }

    public int getPurokCount() { return purokCount; }
    public void setPurokCount(int purokCount) { this.purokCount = purokCount; }

    public int getPopulation() { return population; }
    public void setPopulation(int population) { this.population = population; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getCollectionDay() { return collectionDay; }
    public void setCollectionDay(String collectionDay) { this.collectionDay = collectionDay; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
