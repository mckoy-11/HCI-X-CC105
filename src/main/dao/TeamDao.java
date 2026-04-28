package main.dao;

import main.database.DbSchemaHelper;
import main.database.SQLConnection;
import main.model.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO for Team entity handling CRUD operations and lightweight resource
 * bindings for leader, driver, truck, and collectors.
 */
public class TeamDao {

    private static final String TEAM_TABLE = "team";
    private static final String PERSONNEL_TABLE = "personnel";
    private static final String TRUCK_TABLE = "truck";
    private static final String TEAM_COLLECTORS_TABLE = "team_collectors";

    /**
     * Returns all persisted teams ordered by name.
     *
     * @return the ordered team list
     */
    public List<Team> getAllTeams() {
        List<Team> teams = new ArrayList<>();

        try (Connection conn = SQLConnection.getConnection()) {
            SchemaFlags flags = loadSchemaFlags(conn);
            try (PreparedStatement stmt = conn.prepareStatement(buildSelectSql(flags, false));
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Team team = mapResultSetToTeam(rs);
                    hydrateAssignments(conn, team, flags);
                    teams.add(team);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teams;
    }

    /**
     * Looks up one team by id.
     *
     * @param id the team id
     * @return the resolved team or {@code null}
     */
    public Team getTeamById(int id) {
        try (Connection conn = SQLConnection.getConnection()) {
            SchemaFlags flags = loadSchemaFlags(conn);
            return findTeamById(conn, id, flags);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a team and stores its related resource assignments.
     *
     * @param team the team to create
     * @return {@code true} when the insert succeeds
     */
    public boolean addTeam(Team team) {
        Connection conn = null;

        try {
            conn = SQLConnection.getConnection();
            conn.setAutoCommit(false);

            SchemaFlags flags = loadSchemaFlags(conn);
            String sql = buildInsertSql(flags);
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindTeam(stmt, team, flags, false);
                if (stmt.executeUpdate() <= 0) {
                    rollbackQuietly(conn);
                    return false;
                }

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        team.setId(keys.getInt(1));
                    }
                }
            }

            syncAssignments(conn, team, null, flags);
            conn.commit();
            return true;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Updates one team and refreshes its related resource assignments.
     *
     * @param team the team to update
     * @return {@code true} when the update succeeds
     */
    public boolean updateTeam(Team team) {
        Connection conn = null;

        try {
            conn = SQLConnection.getConnection();
            conn.setAutoCommit(false);

            SchemaFlags flags = loadSchemaFlags(conn);
            Team previous = findTeamById(conn, team.getId(), flags);
            if (previous == null) {
                rollbackQuietly(conn);
                return false;
            }

            String sql = buildUpdateSql(flags);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                bindTeam(stmt, team, flags, true);
                if (stmt.executeUpdate() <= 0) {
                    rollbackQuietly(conn);
                    return false;
                }
            }

            syncAssignments(conn, team, previous, flags);
            conn.commit();
            return true;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Deletes one team and clears any mirrored personnel or truck assignment.
     *
     * @param id the team id
     * @return {@code true} when the team is deleted
     */
    public boolean deleteTeam(int id) {
        Connection conn = null;

        try {
            conn = SQLConnection.getConnection();
            conn.setAutoCommit(false);

            SchemaFlags flags = loadSchemaFlags(conn);
            Team existing = findTeamById(conn, id, flags);
            if (existing == null) {
                rollbackQuietly(conn);
                return false;
            }

            if (flags.hasTeamCollectorsTable) {
                deleteCollectorLinks(conn, id);
            }
            if (flags.hasPersonnelTeamName && !isBlank(existing.getTeamName())) {
                clearPersonnelAssignments(conn, existing.getTeamName());
            }
            if (flags.hasTruckAssignedTeam) {
                clearTruckAssignment(conn, existing);
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM " + TEAM_TABLE + " WHERE team_id = ?")) {
                stmt.setInt(1, id);
                if (stmt.executeUpdate() <= 0) {
                    rollbackQuietly(conn);
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Returns the total number of teams.
     *
     * @return the total team count
     */
    public int getTotalTeamCount() {
        return countBySql("SELECT COUNT(*) AS count FROM " + TEAM_TABLE);
    }

    /**
     * Returns the number of active teams.
     *
     * @return the active team count
     */
    public int getActiveTeamCount() {
        return countBySql("SELECT COUNT(*) AS count FROM " + TEAM_TABLE + " WHERE status = 'Active'");
    }

    /**
     * Loads one team within an open connection.
     *
     * @param conn the active connection
     * @param id the team id
     * @param flags the resolved schema flags
     * @return the team or {@code null}
     * @throws SQLException when the query fails
     */
    private Team findTeamById(Connection conn, int id, SchemaFlags flags) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(buildSelectSql(flags, true))) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Team team = mapResultSetToTeam(rs);
                    hydrateAssignments(conn, team, flags);
                    return team;
                }
            }
        }
        return null;
    }

    /**
     * Builds the shared select statement for teams.
     *
     * @param flags the resolved schema flags
     * @param byId whether the query should target one id
     * @return the SQL statement
     */
    private String buildSelectSql(SchemaFlags flags, boolean byId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.*, p.personnel_name AS leader_name");

        if (flags.hasDriverId) {
            sql.append(", d.personnel_name AS driver_name");
        }
        if (flags.hasTruckId) {
            sql.append(", tr.plate_number AS truck_plate_number");
        }

        sql.append(" FROM ").append(TEAM_TABLE).append(" t ");
        sql.append("LEFT JOIN ").append(PERSONNEL_TABLE).append(" p ON t.leader_id = p.personnel_id ");

        if (flags.hasDriverId) {
            sql.append("LEFT JOIN ").append(PERSONNEL_TABLE)
                    .append(" d ON t.driver_id = d.personnel_id ");
        }
        if (flags.hasTruckId) {
            sql.append("LEFT JOIN ").append(TRUCK_TABLE)
                    .append(" tr ON t.truck_id = tr.truck_id ");
        }

        if (byId) {
            sql.append("WHERE t.team_id = ?");
        } else {
            sql.append("ORDER BY t.team_name ASC");
        }

        return sql.toString();
    }

    /**
     * Builds the insert statement for the current schema.
     *
     * @param flags the resolved schema flags
     * @return the insert SQL
     */
    private String buildInsertSql(SchemaFlags flags) {
        StringBuilder columns = new StringBuilder("team_name, leader_id");
        StringBuilder values = new StringBuilder("?, ?");

        if (flags.hasDriverId) {
            columns.append(", driver_id");
            values.append(", ?");
        }
        if (flags.hasTruckId) {
            columns.append(", truck_id");
            values.append(", ?");
        }

        columns.append(", status");
        values.append(", ?");

        return "INSERT INTO " + TEAM_TABLE + " (" + columns + ") VALUES (" + values + ")";
    }

    /**
     * Builds the update statement for the current schema.
     *
     * @param flags the resolved schema flags
     * @return the update SQL
     */
    private String buildUpdateSql(SchemaFlags flags) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(TEAM_TABLE).append(" SET team_name = ?, leader_id = ?");

        if (flags.hasDriverId) {
            sql.append(", driver_id = ?");
        }
        if (flags.hasTruckId) {
            sql.append(", truck_id = ?");
        }

        sql.append(", status = ? WHERE team_id = ?");
        return sql.toString();
    }

    /**
     * Binds a team into an insert or update statement.
     *
     * @param stmt the target statement
     * @param team the source team
     * @param flags the resolved schema flags
     * @param includeId whether the team id should be bound at the end
     * @throws SQLException when binding fails
     */
    private void bindTeam(PreparedStatement stmt,
                          Team team,
                          SchemaFlags flags,
                          boolean includeId) throws SQLException {
        int index = 1;
        stmt.setString(index++, team.getTeamName());
        setNullableInt(stmt, index++, team.getLeaderId());

        if (flags.hasDriverId) {
            setNullableInt(stmt, index++, team.getDriverId());
        }
        if (flags.hasTruckId) {
            setNullableInt(stmt, index++, team.getTruckId());
        }

        stmt.setString(index++, team.getStatus());

        if (includeId) {
            stmt.setInt(index, team.getId());
        }
    }

    /**
     * Enriches a team with collectors, truck, and driver details.
     *
     * @param conn the active connection
     * @param team the team to hydrate
     * @param flags the resolved schema flags
     * @throws SQLException when loading fails
     */
    private void hydrateAssignments(Connection conn, Team team, SchemaFlags flags) throws SQLException {
        loadCollectors(conn, team, flags);

        if ((team.getDriverId() <= 0 || isBlank(team.getDriverName()))
                && flags.hasPersonnelTeamName
                && !isBlank(team.getTeamName())) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT personnel_id, personnel_name FROM " + PERSONNEL_TABLE
                            + " WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?))"
                            + " AND UPPER(TRIM(role)) = 'DRIVER'"
                            + " ORDER BY personnel_name ASC LIMIT 1")) {
                stmt.setString(1, team.getTeamName());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        team.setDriverId(rs.getInt("personnel_id"));
                        team.setDriverName(rs.getString("personnel_name"));
                    }
                }
            }
        }

        if ((team.getTruckId() <= 0 || isBlank(team.getTruckPlateNumber()))
                && flags.hasTruckAssignedTeam
                && !isBlank(team.getTeamName())) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT truck_id, plate_number FROM " + TRUCK_TABLE
                            + " WHERE UPPER(TRIM(assigned_team)) = UPPER(TRIM(?))"
                            + " ORDER BY plate_number ASC LIMIT 1")) {
                stmt.setString(1, team.getTeamName());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        team.setTruckId(rs.getInt("truck_id"));
                        team.setTruckPlateNumber(rs.getString("plate_number"));
                    }
                }
            }
        }
    }

    /**
     * Loads collectors either from the collector table or from personnel
     * fallback assignments.
     *
     * @param conn the active connection
     * @param team the team being hydrated
     * @param flags the resolved schema flags
     * @throws SQLException when loading fails
     */
    private void loadCollectors(Connection conn, Team team, SchemaFlags flags) throws SQLException {
        List<Integer> collectorIds = new ArrayList<>();
        List<String> collectorNames = new ArrayList<>();

        if (flags.hasTeamCollectorsTable) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT tc.personnel_id, p.personnel_name"
                            + " FROM " + TEAM_COLLECTORS_TABLE + " tc"
                            + " LEFT JOIN " + PERSONNEL_TABLE + " p ON tc.personnel_id = p.personnel_id"
                            + " WHERE tc.team_id = ?"
                            + " ORDER BY p.personnel_name ASC")) {
                stmt.setInt(1, team.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        collectorIds.add(rs.getInt("personnel_id"));
                        collectorNames.add(rs.getString("personnel_name"));
                    }
                }
            }
        }

        if (collectorIds.isEmpty() && flags.hasPersonnelTeamName && !isBlank(team.getTeamName())) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT personnel_id, personnel_name FROM " + PERSONNEL_TABLE
                            + " WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?))"
                            + " AND UPPER(TRIM(role)) = 'COLLECTOR'"
                            + " ORDER BY personnel_name ASC")) {
                stmt.setString(1, team.getTeamName());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        collectorIds.add(rs.getInt("personnel_id"));
                        collectorNames.add(rs.getString("personnel_name"));
                    }
                }
            }
        }

        team.setCollectorIds(collectorIds);
        team.setCollectorNames(collectorNames);
    }

    /**
     * Synchronizes personnel, truck, and collector relationships after a save.
     *
     * @param conn the active connection
     * @param team the saved team
     * @param previous the previous team state for updates
     * @param flags the resolved schema flags
     * @throws SQLException when synchronization fails
     */
    private void syncAssignments(Connection conn,
                                 Team team,
                                 Team previous,
                                 SchemaFlags flags) throws SQLException {
        if (flags.hasTeamCollectorsTable) {
            deleteCollectorLinks(conn, team.getId());
            insertCollectorLinks(conn, team);
        }

        if (flags.hasPersonnelTeamName) {
            if (previous != null && !isBlank(previous.getTeamName())) {
                clearPersonnelAssignments(conn, previous.getTeamName());
            }
            assignPersonnelToTeam(conn, buildAssignedPersonnelIds(team), team.getTeamName());
        }

        if (flags.hasTruckAssignedTeam) {
            if (previous != null) {
                clearTruckAssignment(conn, previous);
            }
            assignTruckToTeam(conn, team);
        }
    }

    /**
     * Deletes collector links for one team.
     *
     * @param conn the active connection
     * @param teamId the owning team id
     * @throws SQLException when deletion fails
     */
    private void deleteCollectorLinks(Connection conn, int teamId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM " + TEAM_COLLECTORS_TABLE + " WHERE team_id = ?")) {
            stmt.setInt(1, teamId);
            stmt.executeUpdate();
        }
    }

    /**
     * Inserts collector links for the current team.
     *
     * @param conn the active connection
     * @param team the saved team
     * @throws SQLException when insertion fails
     */
    private void insertCollectorLinks(Connection conn, Team team) throws SQLException {
        if (team.getCollectorIds().isEmpty()) {
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO " + TEAM_COLLECTORS_TABLE + " (team_id, personnel_id) VALUES (?, ?)")) {
            for (Integer collectorId : team.getCollectorIds()) {
                if (collectorId == null || collectorId.intValue() <= 0) {
                    continue;
                }
                stmt.setInt(1, team.getId());
                stmt.setInt(2, collectorId.intValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /**
     * Clears mirrored personnel team assignments for one team name.
     *
     * @param conn the active connection
     * @param teamName the team name to clear
     * @throws SQLException when the update fails
     */
    private void clearPersonnelAssignments(Connection conn, String teamName) throws SQLException {
        if (isBlank(teamName)) {
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE " + PERSONNEL_TABLE
                        + " SET team_name = NULL WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?))")) {
            stmt.setString(1, teamName);
            stmt.executeUpdate();
        }
    }

    /**
     * Assigns the selected personnel ids to the provided team name.
     *
     * @param conn the active connection
     * @param personnelIds the ids to assign
     * @param teamName the destination team name
     * @throws SQLException when the update fails
     */
    private void assignPersonnelToTeam(Connection conn,
                                       Set<Integer> personnelIds,
                                       String teamName) throws SQLException {
        if (personnelIds.isEmpty() || isBlank(teamName)) {
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE " + PERSONNEL_TABLE + " SET team_name = ? WHERE personnel_id = ?")) {
            for (Integer personnelId : personnelIds) {
                if (personnelId == null || personnelId.intValue() <= 0) {
                    continue;
                }
                stmt.setString(1, teamName);
                stmt.setInt(2, personnelId.intValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /**
     * Clears any mirrored truck assignment for one team snapshot.
     *
     * @param conn the active connection
     * @param team the team snapshot to clear
     * @throws SQLException when the update fails
     */
    private void clearTruckAssignment(Connection conn, Team team) throws SQLException {
        if (team == null) {
            return;
        }

        if (team.getTruckId() > 0) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE " + TRUCK_TABLE + " SET assigned_team = NULL WHERE truck_id = ?")) {
                stmt.setInt(1, team.getTruckId());
                stmt.executeUpdate();
            }
        } else if (!isBlank(team.getTeamName())) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE " + TRUCK_TABLE
                            + " SET assigned_team = NULL WHERE UPPER(TRIM(assigned_team)) = UPPER(TRIM(?))")) {
                stmt.setString(1, team.getTeamName());
                stmt.executeUpdate();
            }
        }
    }

    /**
     * Assigns the selected truck to the current team name.
     *
     * @param conn the active connection
     * @param team the saved team
     * @throws SQLException when the update fails
     */
    private void assignTruckToTeam(Connection conn, Team team) throws SQLException {
        if (team.getTruckId() <= 0 || isBlank(team.getTeamName())) {
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE " + TRUCK_TABLE + " SET assigned_team = ? WHERE truck_id = ?")) {
            stmt.setString(1, team.getTeamName());
            stmt.setInt(2, team.getTruckId());
            stmt.executeUpdate();
        }
    }

    /**
     * Collects all assigned personnel ids for one team snapshot.
     *
     * @param team the team snapshot
     * @return the distinct personnel ids
     */
    private Set<Integer> buildAssignedPersonnelIds(Team team) {
        LinkedHashSet<Integer> ids = new LinkedHashSet<Integer>();
        if (team.getLeaderId() > 0) {
            ids.add(team.getLeaderId());
        }
        if (team.getDriverId() > 0) {
            ids.add(team.getDriverId());
        }
        ids.addAll(team.getCollectorIds());
        ids.remove(0);
        return ids;
    }

    /**
     * Maps one result-set row into a team model.
     *
     * @param rs the source result set
     * @return the mapped team
     * @throws SQLException when field access fails
     */
    private Team mapResultSetToTeam(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setId(getInt(rs, "team_id", "id"));
        team.setTeamName(getString(rs, "team_name"));
        team.setLeaderId(getInt(rs, "leader_id"));
        team.setLeaderName(getString(rs, "leader_name"));
        team.setDriverId(getInt(rs, "driver_id"));
        team.setDriverName(getString(rs, "driver_name"));
        team.setTruckId(getInt(rs, "truck_id"));
        team.setTruckPlateNumber(getString(rs, "truck_plate_number"));
        team.setStatus(getString(rs, "status"));
        return team;
    }

    /**
     * Returns the total matching row count for one count query.
     *
     * @param sql the count SQL
     * @return the resolved count
     */
    private int countBySql(String sql) {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Loads the optional schema flags and quietly bootstraps missing columns.
     *
     * @param conn the active connection
     * @return the resolved schema flags
     * @throws SQLException when schema inspection fails
     */
    private SchemaFlags loadSchemaFlags(Connection conn) throws SQLException {
        ensureOptionalSchema(conn);

        SchemaFlags flags = new SchemaFlags();
        flags.hasDriverId = DbSchemaHelper.columnExists(conn, TEAM_TABLE, "driver_id");
        flags.hasTruckId = DbSchemaHelper.columnExists(conn, TEAM_TABLE, "truck_id");
        flags.hasPersonnelTeamName = DbSchemaHelper.columnExists(conn, PERSONNEL_TABLE, "team_name");
        flags.hasTruckAssignedTeam = DbSchemaHelper.columnExists(conn, TRUCK_TABLE, "assigned_team");
        flags.hasTeamCollectorsTable = DbSchemaHelper.tableExists(conn, TEAM_COLLECTORS_TABLE);
        return flags;
    }

    /**
     * Adds optional team columns and tables when the current schema supports it.
     *
     * @param conn the active connection
     */
    private void ensureOptionalSchema(Connection conn) {
        safeAddColumn(conn, TEAM_TABLE, "driver_id", "INT NULL");
        safeAddColumn(conn, TEAM_TABLE, "truck_id", "INT NULL");
        safeAddColumn(conn, TRUCK_TABLE, "assigned_team", "VARCHAR(120) NULL");
        safeCreateCollectorsTable(conn);
    }

    /**
     * Adds one optional column when it does not exist.
     *
     * @param conn the active connection
     * @param tableName the target table
     * @param columnName the target column
     * @param definition the SQL column definition
     */
    private void safeAddColumn(Connection conn,
                               String tableName,
                               String columnName,
                               String definition) {
        try {
            if (DbSchemaHelper.tableExists(conn, tableName)
                    && !DbSchemaHelper.columnExists(conn, tableName, columnName)) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition)) {
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException ignored) {
            // The UI can still function with the base team fields if DDL is unavailable.
        }
    }

    /**
     * Creates the optional collector link table when it is missing.
     *
     * @param conn the active connection
     */
    private void safeCreateCollectorsTable(Connection conn) {
        try {
            if (!DbSchemaHelper.tableExists(conn, TEAM_COLLECTORS_TABLE)) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "CREATE TABLE " + TEAM_COLLECTORS_TABLE + " ("
                                + "team_id INT NOT NULL, "
                                + "personnel_id INT NOT NULL, "
                                + "PRIMARY KEY (team_id, personnel_id))")) {
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException ignored) {
            // Collector links fall back to personnel.team_name when table creation is unavailable.
        }
    }

    /**
     * Sets a nullable integer parameter.
     *
     * @param stmt the target statement
     * @param index the parameter index
     * @param value the integer value
     * @throws SQLException when binding fails
     */
    private void setNullableInt(PreparedStatement stmt, int index, int value) throws SQLException {
        if (value > 0) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, java.sql.Types.INTEGER);
        }
    }

    /**
     * Reads a string using candidate column names.
     *
     * @param rs the source result set
     * @param candidates candidate column names
     * @return the resolved string or {@code null}
     * @throws SQLException when field access fails
     */
    private String getString(ResultSet rs, String... candidates) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        for (String candidate : candidates) {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (candidate.equalsIgnoreCase(meta.getColumnLabel(i))
                        || candidate.equalsIgnoreCase(meta.getColumnName(i))) {
                    return rs.getString(i);
                }
            }
        }
        return null;
    }

    /**
     * Reads an integer using candidate column names.
     *
     * @param rs the source result set
     * @param candidates candidate column names
     * @return the resolved integer or {@code 0}
     * @throws SQLException when field access fails
     */
    private int getInt(ResultSet rs, String... candidates) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        for (String candidate : candidates) {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (candidate.equalsIgnoreCase(meta.getColumnLabel(i))
                        || candidate.equalsIgnoreCase(meta.getColumnName(i))) {
                    return rs.getInt(i);
                }
            }
        }
        return 0;
    }

    /**
     * Returns whether a text value is blank.
     *
     * @param value the text to test
     * @return {@code true} when the value is blank
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Rolls back an open connection without surfacing the rollback failure.
     *
     * @param conn the connection to roll back
     */
    private void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // Best effort rollback.
        }
    }

    /**
     * Closes an open connection without surfacing the close failure.
     *
     * @param conn the connection to close
     */
    private void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.close();
        } catch (SQLException ignored) {
            // Best effort close.
        }
    }

    /**
     * Small schema feature flag holder for optional team bindings.
     */
    private static final class SchemaFlags {
        private boolean hasDriverId;
        private boolean hasTruckId;
        private boolean hasPersonnelTeamName;
        private boolean hasTruckAssignedTeam;
        private boolean hasTeamCollectorsTable;
    }
}
