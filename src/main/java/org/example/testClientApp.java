package org.example;

import io.pivotal.gemfire.gpdb.GpdbService;
import io.pivotal.gemfire.gpdb.ImportConfiguration;
import io.pivotal.gemfire.gpdb.ImportResult;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class MainApp {

    public static void main(String[] args) throws Exception {

        // 1. Insert data into Greenplum
        String url = "jdbc:postgresql://192.168.100.77:5432/postgres?sslmode=disable";
        String username = "gpadmin";
        String password = "deleteme123!";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String insertSQL = "INSERT INTO test_small(id, value) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertSQL);
            stmt.setString(1, "2test:keys");
            stmt.setString(2, "testValue_2");
            stmt.executeUpdate();
            System.out.println("Inserted row into Greenplum.");
        }

        // 2. Trigger import from Greenplum into GemFire
        Region<String, String> region = getRegion();
        ImportConfiguration configuration = ImportConfiguration.builder(region).build();
        ImportResult result = GpdbService.importRegion(configuration);
        System.out.println("Imported rows into GemFire: " + result.getImportedCount());

        // 3. Read the data from the region
        String key = "2test:keys";
        String value = region.get(key);
        System.out.println("Fetched from GemFire region: " + key + " -> " + value);
    }

    private static Region<String, String> getRegion() {
        ClientCache cache = new ClientCacheFactory()
                .addPoolLocator("192.168.100.24", 10334)  // Your locator IP & port
                .set("log-level", "info")
                .create();

        return cache.<String, String>createClientRegionFactory(ClientRegionShortcut.PROXY)
                .create("testRegion");  // Region name as defined in gfsh
    }
}
