import java.sql.*;

public class test_db_connection {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL driver loaded");
            
            String url = "jdbc:mysql://localhost:3306/wcms?serverTimezone=UTC";
            String user = "root";
            String password = "";
            
            System.out.println("Attempting connection to: " + url);
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✓ Database connection successful!");
            
            // Test if we can query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            if (rs.next()) {
                System.out.println("✓ Database query successful!");
            }
            
            // Check if wcms database exists
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet catalogs = dbmd.getCatalogs();
            System.out.println("\nAvailable databases:");
            while (catalogs.next()) {
                String dbName = catalogs.getString(1);
                System.out.println("  - " + dbName);
            }
            
            // Check tables in wcms
            ResultSet tables = dbmd.getTables(null, null, "%", null);
            System.out.println("\nTables in wcms:");
            while (tables.next()) {
                String tableName = tables.getString(3);
                System.out.println("  - " + tableName);
            }
            
            conn.close();
            System.out.println("\n✓ All checks passed!");
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
