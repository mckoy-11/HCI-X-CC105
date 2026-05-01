package main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection team together with its bound personnel and truck.
 */
public class Team {

    private int id;
    private String teamName;
    private int leaderId;
    private String status;
    private String leaderName;
    private int truckId;
    private String truckPlateNumber;
    private int driverId;
    private String driverName;
    private List<Integer> collectorIds = new ArrayList<>();
    private List<String> collectorNames = new ArrayList<>();

    /**
     * Creates an empty team instance.
     */
    public Team() {
    }

    /**
     * Creates a team with the base persisted fields.
     *
     * @param id the team identifier
     * @param teamName the team display name
     * @param leaderId the selected leader id
     * @param status the team status
     */
    public Team(int id, String teamName, int leaderId, String status) {
        this.id = id;
        this.teamName = teamName;
        this.leaderId = leaderId;
        this.status = status;
    }

    /**
     * Returns the team identifier.
     *
     * @return the team id
     */
    public int getId() {
        return id;
    }

    /**
     * Updates the team identifier.
     *
     * @param id the team id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the team name.
     *
     * @return the team name
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Updates the team name.
     *
     * @param teamName the team name
     */
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    /**
     * Returns the leader identifier.
     *
     * @return the leader id
     */
    public int getLeaderId() {
        return leaderId;
    }

    /**
     * Updates the leader identifier.
     *
     * @param leaderId the leader id
     */
    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    /**
     * Returns the team status.
     *
     * @return the status value
     */
    public String getStatus() {
        return status;
    }

    /**
     * Updates the team status.
     *
     * @param status the status value
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the leader display name.
     *
     * @return the leader name
     */
    public String getLeaderName() {
        return leaderName;
    }

    /**
     * Updates the leader display name.
     *
     * @param leaderName the leader name
     */
    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    /**
     * Returns the assigned truck id.
     *
     * @return the truck id
     */
    public int getTruckId() {
        return truckId;
    }

    /**
     * Updates the assigned truck id.
     *
     * @param truckId the truck id
     */
    public void setTruckId(int truckId) {
        this.truckId = truckId;
    }

    /**
     * Returns the assigned truck plate number.
     *
     * @return the truck plate number
     */
    public String getTruckPlateNumber() {
        return truckPlateNumber;
    }

    /**
     * Updates the assigned truck plate number.
     *
     * @param truckPlateNumber the truck plate number
     */
    public void setTruckPlateNumber(String truckPlateNumber) {
        this.truckPlateNumber = truckPlateNumber;
    }

    /**
     * Returns the assigned driver id.
     *
     * @return the driver id
     */
    public int getDriverId() {
        return driverId;
    }

    /**
     * Updates the assigned driver id.
     *
     * @param driverId the driver id
     */
    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    /**
     * Returns the assigned driver name.
     *
     * @return the driver name
     */
    public String getDriverName() {
        return driverName;
    }

    /**
     * Updates the assigned driver name.
     *
     * @param driverName the driver name
     */
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    /**
     * Returns the selected collector ids.
     *
     * @return an ordered list of collector ids
     */
    public List<Integer> getCollectorIds() {
        return new ArrayList<>(collectorIds);
    }

    /**
     * Updates the selected collector ids.
     *
     * @param collectorIds the collector ids to store
     */
    public void setCollectorIds(List<Integer> collectorIds) {
        this.collectorIds = collectorIds == null
                ? new ArrayList<>()
                : new ArrayList<>(collectorIds);
    }

    /**
     * Returns the selected collector names.
     *
     * @return an ordered list of collector names
     */
    public List<String> getCollectorNames() {
        return new ArrayList<>(collectorNames);
    }

    /**
     * Updates the selected collector names.
     *
     * @param collectorNames the collector names to store
     */
public void setCollectorNames(List<String> collectorNames) {
        this.collectorNames = collectorNames == null
                ? new ArrayList<>()
                : new ArrayList<>(collectorNames);
    }

/**
     * Returns display value for ComboBox.
     * Shows team name or "Unassigned" if no team is assigned.
     */
    @Override
    public String toString() {
        return teamName != null && !teamName.trim().isEmpty() ? teamName : "Unassigned";
    }
}
