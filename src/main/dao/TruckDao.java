package main.dao;

import main.database.DbSchemaHelper;
import main.database.SQLConnection;
import main.model.Truck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Truck entity handling CRUD operations.
 */
public class TruckDao {

    private static final String TRUCK_TABLE = "truck";

    public List<Truck> getAllTrucks() {
        List<Truck> trucks = new ArrayList<Truck>();
        String sql = "SELECT * FROM " + TRUCK_TABLE + " ORDER BY plate_number ASC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                trucks.add(mapResultSetToTruck(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return trucks;
    }

    public Truck getTruckById(int id) {
        String sql = "SELECT * FROM " + TRUCK_TABLE + " WHERE truck_id = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTruck(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean addTruck(Truck truck) {
        try (Connection conn = SQLConnection.getConnection()) {
            SchemaFlags flags = resolveSchemaFlags(conn);
            StringBuilder columns = new StringBuilder("plate_number, truck_type, status");
            StringBuilder values = new StringBuilder("?, ?, ?");

            if (flags.capacityColumn != null) {
                columns.append(", ").append(flags.capacityColumn);
                values.append(", ?");
            }
            if (flags.assignedBarangayColumn != null) {
                columns.append(", ").append(flags.assignedBarangayColumn);
                values.append(", ?");
            }

            String sql = "INSERT INTO " + TRUCK_TABLE + " (" + columns + ") VALUES (" + values + ")";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int index = 1;
                stmt.setString(index++, truck.getPlateNumber());
                stmt.setString(index++, truck.getTruckType());
                stmt.setString(index++, truck.getStatus());

                if (flags.capacityColumn != null) {
                    stmt.setString(index++, truck.getCapacity());
                }
                if (flags.assignedBarangayColumn != null) {
                    stmt.setString(index++, truck.getAssignedBarangay());
                }

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateTruck(Truck truck) {
        try (Connection conn = SQLConnection.getConnection()) {
            SchemaFlags flags = resolveSchemaFlags(conn);
            StringBuilder sql = new StringBuilder(
                    "UPDATE " + TRUCK_TABLE + " SET plate_number = ?, truck_type = ?, status = ?"
            );

            if (flags.capacityColumn != null) {
                sql.append(", ").append(flags.capacityColumn).append(" = ?");
            }
            if (flags.assignedBarangayColumn != null) {
                sql.append(", ").append(flags.assignedBarangayColumn).append(" = ?");
            }
            sql.append(" WHERE truck_id = ?");

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int index = 1;
                stmt.setString(index++, truck.getPlateNumber());
                stmt.setString(index++, truck.getTruckType());
                stmt.setString(index++, truck.getStatus());

                if (flags.capacityColumn != null) {
                    stmt.setString(index++, truck.getCapacity());
                }
                if (flags.assignedBarangayColumn != null) {
                    stmt.setString(index++, truck.getAssignedBarangay());
                }
                stmt.setInt(index, truck.getId());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteTruck(int id) {
        String sql = "DELETE FROM " + TRUCK_TABLE + " WHERE truck_id = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int getTotalTruckCount() {
        String sql = "SELECT COUNT(*) as count FROM " + TRUCK_TABLE;

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

    public int getActiveTruckCount() {
        String sql = "SELECT COUNT(*) as count FROM " + TRUCK_TABLE + " WHERE status = 'Active'";

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

    private Truck mapResultSetToTruck(ResultSet rs) throws SQLException {
        Truck truck = new Truck();
        truck.setId(rs.getInt("truck_id"));
        truck.setPlateNumber(rs.getString("plate_number"));
        truck.setTruckType(rs.getString("truck_type"));
        truck.setCapacity(getString(rs, "capacity", "truck_capacity"));
        truck.setAssignedBarangay(getString(rs, "assigned_barangay", "barangay_assigned", "barangay_name"));
        truck.setStatus(rs.getString("status"));
        truck.setAssignedTeam(getString(rs, "assigned_team"));
        return truck;
    }

    private SchemaFlags resolveSchemaFlags(Connection conn) throws SQLException {
        SchemaFlags flags = new SchemaFlags();
        flags.capacityColumn = DbSchemaHelper.getFirstColumn(conn, TRUCK_TABLE, "capacity", "truck_capacity");
        flags.assignedBarangayColumn = DbSchemaHelper.getFirstColumn(
                conn,
                TRUCK_TABLE,
                "assigned_barangay",
                "barangay_assigned",
                "barangay_name"
        );
        return flags;
    }

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

    private static final class SchemaFlags {
        private String capacityColumn;
        private String assignedBarangayColumn;
    }
}
