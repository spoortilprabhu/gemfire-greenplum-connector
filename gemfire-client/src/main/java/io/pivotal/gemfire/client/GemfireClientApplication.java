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
	public static void main(String[] args) {

		// Create GemFire client cache using clientCache.xml
		ClientCache cache = new ClientCacheFactory()
				.set("cache-xml-file", "clientCache.xml")
				.create();

		// region name
		String regionName = "/Customer";
		Region<Object, Object> region = cache.getRegion(regionName);
		try {

			// Export data from GemFire region to Greenplum
			ExportConfiguration exportConfig = ExportConfiguration.builder(region).build();
			ExportResult exportResult = executeExport(exportConfig);
			System.out.println("Exported rows count: " + safeGetCount(exportResult, "exportedCount"));

			// Import data from Greenplum into GemFire region
			ImportConfiguration importConfig = ImportConfiguration.builder(region).build();
			ImportResult importResult = executeImport(importConfig);
			System.out.println("Imported rows count: " + safeGetCount(importResult, "importedCount"));

		} catch (OperationException e) {

			System.err.println("Operation failed: " + e.getMessage());
			e.printStackTrace();

		} finally {
			cache.close();
		}
	}

	/**

	 * Execute the export operation by invoking the ExportOperationFunction directly.

	 */

	private static ExportResult executeExport(ExportConfiguration exportConfig) throws OperationException {

		try {
			Execution execution = getExecutionForRegion(exportConfig.getRegionPath());
			@SuppressWarnings("unchecked")
			ResultCollector<Object, List<Object>> results = (ResultCollector<Object, List<Object>>) execution
					.withArgs(OperationFunctionArguments.export(exportConfig))
					.execute(ExportOperationFunction.ID);
			return ResultCollectorReducers.extractExportResult(results);

		} catch (FunctionException e) {
			throw buildOperationException(e);
		}
	}
	/**
	 * Execute the import operation by invoking the ImportOperationFunction directly.
	 */
	private static ImportResult executeImport(ImportConfiguration importConfig) throws OperationException {
		try {
			Execution execution = getExecutionForRegion(importConfig.getRegionPath());
			@SuppressWarnings("unchecked")
			ResultCollector<Object, List<Object>> results = (ResultCollector<Object, List<Object>>) execution
					.withArgs(OperationFunctionArguments.import_(importConfig))
					.execute(ImportOperationFunction.ID);
			return ResultCollectorReducers.extractImportResult(results);
		} catch (FunctionException e) {
			throw buildOperationException(e);
		}
	}

	/**
	 * Helper method to get Execution object to invoke function on a single server member.
	 * If running in client mode, targets the region with a filter of a singleton to ensure single execution.
	 */

	private static Execution getExecutionForRegion(String regionPath) {

		// Use internal GemFireCacheImpl to detect client or server mode
		if (GemFireCacheImpl.getInstance().isClient()) {
			Region region = GemFireCacheImpl.getInstance().getRegion(regionPath);
			Set<Integer> filter = Collections.singleton(0); // target a single member to avoid duplicates
			return FunctionService.onRegion(region).withFilter(filter);
		} else {
			return FunctionService.onMember(GemFireCacheImpl.getInstance().getDistributedSystem().getDistributedMember());
		}
	}

	/**
	 * Wrap FunctionException as OperationException for uniform error handling.
	 */

	private static OperationException buildOperationException(FunctionException e) {
		Throwable cause = e.getCause();
		if (cause == null) {
			return new OperationException(e);
		}

		if (cause instanceof OperationException) {
			return (OperationException) cause;
		}
		return new OperationException(cause);
	}



	private static Integer safeGetCount(Object result, String fieldName) {
		try {
			java.lang.reflect.Field field = result.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (Integer) field.get(result);
		} catch (Exception e) {
			return null;
		}
	}



	// Inner helper class to build arguments OperationFunction.arguments(factory, config)

	private static class OperationFunctionArguments {
		static Object export(ExportConfiguration exportConfig) {
			return new Object[] { new ExportOperationFactory(), exportConfig };
		}



		static Object import_(ImportConfiguration importConfig) {
			return new Object[] { new ImportOperationFactory(), importConfig };
		}
	}
}