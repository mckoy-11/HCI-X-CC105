package main.model;

public class Personnel {

    private int id;
    private String fullName;
    private String phoneNumber;
    private String role;
    private String status;
    private String barangayAssigned;
    private int age;
    private String gender;
    private String address;
    private String team;

    public Personnel() {}

    public Personnel(int id, String fullName, int age, String gender, String address, 
                     String phoneNumber, String team, String role, String status) {
        this.id = id;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.team = team;
        this.role = role;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBarangayAssigned() { return barangayAssigned; }
    public void setBarangayAssigned(String barangayAssigned) { this.barangayAssigned = barangayAssigned; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String sex) { this.gender = sex; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    /**
     * Returns display value for ComboBox.
     * Shows full name or "Unassigned" if no name is set.
     * @return 
     */
    @Override
    public String toString() {
        return fullName != null && !fullName.trim().isEmpty() ? fullName : "Unassigned";
    }
}
