package main.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import main.database.SQLConnection;
import main.model.CollectionInfo;
import main.model.Schedule;

public class ScheduleDao {

    public List<Schedule> findAll() {
        List<Schedule> results = new ArrayList<Schedule>();
        String sql = "SELECT s.schedule_id, s.barangay_id, s.team_id, s.schedule_date, s.schedule_time, s.status,"
                + " b.barangay_name, b.contact, ba.barangay_admin, t.team_name,"
                + " tr.plate_number AS truck_plate_number, tr.truck_type, tr.assigned_team"
                + " FROM schedule s"
                + " LEFT JOIN barangay b ON s.barangay_id = b.barangay_id"
                + " LEFT JOIN barangay_admin ba ON b.barangay_id = ba.barangay_id"
                + " LEFT JOIN team t ON s.team_id = t.team_id"
                + " LEFT JOIN truck tr ON (t.truck_id = tr.truck_id OR UPPER(TRIM(tr.assigned_team)) = UPPER(TRIM(t.team_name)))"
                + " ORDER BY s.schedule_date ASC, s.schedule_time ASC, b.barangay_name ASC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                results.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public boolean saveOrUpdate(Schedule schedule) {
        if (schedule == null || isBlank(schedule.getBarangayName())) {
            return false;
        }

        try (Connection conn = SQLConnection.getConnection()) {
            Integer barangayId = findBarangayId(conn, schedule.getBarangayName());
            if (barangayId == null) {
                return false;
            }

            Integer teamId = findTeamId(conn, schedule.getCollectorTeam());
            Integer existingId = schedule.getId() > 0 ? schedule.getId() : findScheduleIdByBarangay(conn, barangayId);

            if (existingId != null && existingId.intValue() > 0) {
                String sql = "UPDATE schedule SET team_id = ?, schedule_date = ?, schedule_time = ?, status = ?"
                        + " WHERE schedule_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    bind(stmt, schedule, teamId, false);
                    stmt.setInt(5, existingId.intValue());
                    return stmt.executeUpdate() > 0;
                }
            }

            String sql = "INSERT INTO schedule (barangay_id, team_id, schedule_date, schedule_time, status)"
                    + " VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, barangayId.intValue());
                bind(stmt, schedule, teamId, true);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteById(int id) {
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM schedule WHERE schedule_id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CollectionInfo findCurrentCollectionInfo(String barangayName) {
        String sql = "SELECT t.team_name, tr.plate_number AS truck_plate_number, tr.truck_type,"
                + " s.schedule_time, s.status"
                + " FROM schedule s"
                + " LEFT JOIN barangay b ON s.barangay_id = b.barangay_id"
                + " LEFT JOIN team t ON s.team_id = t.team_id"
                + " LEFT JOIN truck tr ON (t.truck_id = tr.truck_id OR UPPER(TRIM(tr.assigned_team)) = UPPER(TRIM(t.team_name)))"
                + " WHERE UPPER(TRIM(b.barangay_name)) = UPPER(TRIM(?))"
                + " ORDER BY s.schedule_date ASC, s.schedule_time ASC LIMIT 1";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barangayName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CollectionInfo info = new CollectionInfo();
                    info.setAssignedTeam(rs.getString("team_name"));
                    info.setTruckPlateNumber(rs.getString("truck_plate_number"));
                    info.setTruckType(rs.getString("truck_type"));
                    Time etaTime = rs.getTime("schedule_time");
                    info.setEta(etaTime == null ? null : etaTime.toString());
                    info.setStatus(rs.getString("status"));
                    return info;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void bind(PreparedStatement stmt, Schedule schedule, Integer teamId, boolean skipBarangay) throws SQLException {
        int index = skipBarangay ? 2 : 1;
        setNullableTeamId(stmt, index++, teamId);
        stmt.setDate(index++, schedule.getDate() == null ? null : Date.valueOf(schedule.getDate()));
        stmt.setTime(index++, schedule.getTime() == null ? null : Time.valueOf(schedule.getTime()));
        stmt.setString(index, isBlank(schedule.getStatus()) ? "Scheduled" : schedule.getStatus());
    }

    private Schedule map(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule();
        schedule.setId(rs.getInt("schedule_id"));
        schedule.setBarangayName(rs.getString("barangay_name"));
        schedule.setBarangayAdmin(rs.getString("barangay_admin"));
        schedule.setContactNumber(rs.getString("contact"));
        schedule.setCollectorTeam(rs.getString("team_name"));
        schedule.setTruckPlateNumber(rs.getString("truck_plate_number"));
        schedule.setTruckType(rs.getString("truck_type"));
        Date date = rs.getDate("schedule_date");
        if (date != null) {
            schedule.setDate(date.toLocalDate());
        }
        Time time = rs.getTime("schedule_time");
        if (time != null) {
            schedule.setTime(time.toLocalTime());
            schedule.setEta(time.toString());
        }
        schedule.setStatus(rs.getString("status"));
        return schedule;
    }

    private Integer findBarangayId(Connection conn, String barangayName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT barangay_id FROM barangay WHERE UPPER(TRIM(barangay_name)) = UPPER(TRIM(?))")) {
            stmt.setString(1, barangayName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Integer.valueOf(rs.getInt("barangay_id")) : null;
            }
        }
    }

    private Integer findTeamId(Connection conn, String teamName) throws SQLException {
        if (isBlank(teamName)) {
            return null;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT team_id FROM team WHERE UPPER(TRIM(team_name)) = UPPER(TRIM(?))")) {
            stmt.setString(1, teamName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Integer.valueOf(rs.getInt("team_id")) : null;
            }
        }
    }

    private Integer findScheduleIdByBarangay(Connection conn, int barangayId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT schedule_id FROM schedule WHERE barangay_id = ?")) {
            stmt.setInt(1, barangayId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Integer.valueOf(rs.getInt("schedule_id")) : null;
            }
        }
    }

    private void setNullableTeamId(PreparedStatement stmt, int index, Integer teamId) throws SQLException {
        if (teamId != null && teamId.intValue() > 0) {
            stmt.setInt(index, teamId.intValue());
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
