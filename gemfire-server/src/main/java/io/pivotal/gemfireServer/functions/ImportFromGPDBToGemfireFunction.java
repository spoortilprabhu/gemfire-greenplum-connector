package io.pivotal.gemfireServer.functions;

import io.pivotal.gemfire.gpdb.service.ImportConfiguration;
import io.pivotal.gemfire.gpdb.service.ImportResult;
import io.pivotal.gemfire.gpdb.service.GpdbService;
import org.apache.geode.LogWriter;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.FunctionAdapter;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.RegionFunctionContext;

import java.util.Properties;

public class ImportFromGPDBToGemfireFunction extends FunctionAdapter implements Declarable {
  private static final long serialVersionUID = 1L;

  private final transient Cache cache = CacheFactory.getAnyInstance();
  private final transient LogWriter logger = cache.getDistributedSystem().getLogWriter();

  @Override
  public void execute(FunctionContext context) {
    if (!(context instanceof RegionFunctionContext)) {
      throw new FunctionException("Call this function with FunctionService.onRegion.");
    }

    try {
      Region<?, ?> region = cache.getRegion("Customer");

      // 🔑  Build configuration
      ImportConfiguration cfg = ImportConfiguration.builder(region).build();
      ImportResult result      = GpdbService.importRegion(cfg);

      String msg = "Imported rows from Greenplum: " + result.getImportedCount();
      logger.info(msg);
      context.getResultSender().lastResult(msg);
    } catch (Exception e) {
      logger.error("Import failed", e);
      context.getResultSender().lastResult(e.getMessage());
    }
  }

  @Override public String  getId()              { return getClass().getSimpleName(); }
  @Override public boolean optimizeForWrite()   { return false; }
  @Override public boolean isHA()               { return true;  }
  @Override public boolean hasResult()          { return true;  }
  @Override public void    init(Properties p)   { /* no-op */   }
}