package io.pivotal.gemfire.client;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import io.pivotal.demo.entity.Customer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class GemfireClientApplication {

	private static ClientCache cache;

	static {
		ClientCacheFactory ccf = new ClientCacheFactory();
		ccf.set("cache-xml-file", "clientCache.xml");
		cache = ccf.create();
	}

	public static void main(String[] args) {
		runIntegrationTest();
	}

	public static void runIntegrationTest() {
		System.out.println("=== Step 1: Import from Greenplum to GemFire ===");
		executeImportFromGPDBToGemfire();

		System.out.println("=== Step 2: Fetch ALL from GemFire (Customer Region) ===");
		Region<String, Customer> region = cache.getRegion("Customer");

		if (region == null) {
			System.err.println("Region 'Customer' not found!");
			return;
		}

		for (Object key : region.keySetOnServer()) {
			Customer customer = region.get(key);
			System.out.println("Key: " + key + ", Customer: " + customer);
		}

		System.out.println("=== Step 3: Put into GemFire ===");
		Customer newCustomer = new Customer();
		newCustomer.setId("id501");
		newCustomer.setName("Imported From Java");
		newCustomer.setIncome(99999);
		region.put("id501", newCustomer);
		System.out.println("Put into GemFire: " + newCustomer);
	}

	private static void executeImportFromGemfireToGPDB() {
		Region<?, ?> region = cache.getRegion("Customer");
		ResultCollector<?, ?> rc = FunctionService.onRegion(region)
				.execute("ImportFromGemfireToGPDBFunction");
		Object result = rc.getResult();
		System.out.println("ImportFromGemfireToGPDBFunction Result: " + result);
	}

	private static void executeImportFromGPDBToGemfire() {
		Region<?, ?> region = cache.getRegion("Customer");
		ResultCollector<?, ?> rc = FunctionService.onRegion(region)
				.execute("ImportFromGPDBToGemfireFunction");
		Object result = rc.getResult();
		if (!(result instanceof List<?>)) {
			throw new IllegalStateException("Expected result to be a List");
		}
		System.out.println("ImportFromGPDBToGemfireFunction Result: " + result);
	}

	private static void insertIntoGreenplum(String id, String name, int income) {
		String sql = "INSERT INTO gp_test(id, name, income) VALUES ('" + id + "', '" + name + "', " + income + ");";
		executeSQL(sql);
	}

	private static void queryGreenplum(String id) {
		String sql = "SELECT * FROM gp_test WHERE id='" + id + "';";
		try (Connection c = DriverManager.getConnection(
				"jdbc:postgresql://192.168.100.77:5432/postgres", "gpadmin", "deleteme123!");
			 Statement stmt = c.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				System.out.println("Greenplum -> ID: " + rs.getString("id")
						+ ", Name: " + rs.getString("name")
						+ ", Income: " + rs.getInt("income"));
			}

		} catch (Exception e) {
			System.err.println("Error querying Greenplum: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void executeSQL(String sql) {
		try (Connection c = DriverManager.getConnection(
				"jdbc:postgresql://192.168.100.77:5432/postgres", "gpadmin", "deleteme123!");
			 Statement stmt = c.createStatement()) {

			stmt.executeUpdate(sql);
			System.out.println("Executed SQL: " + sql);

		} catch (Exception e) {
			System.err.println("Error executing SQL: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
