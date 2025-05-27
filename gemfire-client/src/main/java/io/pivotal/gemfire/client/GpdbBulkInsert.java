package io.pivotal.gemfire.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class GpdbBulkInsert {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://192.168.100.77:5432/postgres?sslmode=disable";  // ✅ Replace with your DB name
        String user = "gpadmin";                                      // ✅ Replace with your DB user
        String password = "deleteme123!";                              // ✅ Replace with your DB password

        Connection conn = DriverManager.getConnection(url, user, password);
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO gp_test (id, name, income) VALUES (?, ?, ?)");

        for (int i = 1; i <= 500; i++) {
            stmt.setString(1, "CUST" + i);
            stmt.setString(2, "Customer " + i);
            stmt.setInt(3, 10000 + i);
            stmt.addBatch();
        }

        stmt.executeBatch();
        stmt.close();
        conn.close();

        System.out.println("Inserted 500 rows into gp_test");
    }
}
