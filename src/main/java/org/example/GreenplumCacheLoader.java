package org.example;

import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.LoaderHelper;
import org.apache.geode.cache.CacheLoaderException;

import java.sql.*;
import java.util.Properties;

public class GreenplumCacheLoader implements CacheLoader<String, String> {

    @Override
    public String load(LoaderHelper<String, String> helper) throws CacheLoaderException {
        String key = helper.getKey();
        System.out.println("CacheLoader triggered for key: " + key);

        String dbUrl = "jdbc:postgresql://192.168.100.77:5432/postgres";
        String dbUser = "gpadmin";
        String dbPassword = "deleteme123!";
        String query = "SELECT value FROM test_caching WHERE key = ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, key);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String value = resultSet.getString("value");
                System.out.println("Loaded from Greenplum: " + key + " -> " + value);
                return value;
            } else {
                System.out.println("No data found for key: " + key);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CacheLoaderException("Error loading from Greenplum", e);
        }

        return null;
    }

    @Override
    public void close() {
        // Clean up if needed
    }
}