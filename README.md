
# Greenplum to GemFire Region Import Example

This example demonstrates how to integrate VMware GemFire 10.1.2 with a PostgreSQL-compatible Greenplum database and import table data into a GemFire region using the Greenplum Connector(`GpdbService`).

Greenplum Table Sample

Before starting GemFire, ensure your Greenplum table contains data:

postgres=# SELECT * FROM test_small;
     id     |    value
------------+-------------
 2test:keys | testValue_2


GemFire Setup

1. Start the Locator:
   gfsh> start locator --name=locator1 --bind-address=<IP>  --port=10334
   
2. Configure PDX Serialization:
   gfsh> configure pdx --read-serialized=true --auto-serializable-classes=io.pivotal.gemfire.demo.entity.*

3. Start the Server:
   gfsh> start server --name=server1 --server-bind-address=<IP> --server-port=40404 --J=-Dgemfire.prometheus.metrics.emission=Default --J=-Dgemfire.prometheus.metrics.port=8002

4. Create JDBC JNDI Binding:
   gfsh> create jndi-binding --name=datasource --type=SIMPLE --jdbc-driver-class="org.postgresql.Driver" --username="<username>" --password="<password>" --connection-url="jdbc:postgresql://<Greenplum-IP>:5432/postgres?sslmode=disable"

5. Enable gpfdist Protocol:
   gfsh> configure gpfdist-protocol --port=8000

6. Create a Region:
   gfsh> create region --name=/testRegion --type=PARTITION
 
7. Map Region to Greenplum Table:
   gfsh> create gpdb-mapping --region=/testRegion --data-source=datasource --pdx-name="testRegion" --table=test_small --id=id,value

## Objective

The goal is to import data from Greenplum into a GemFire region using the Greenplum Connector API:

### Java Code Example
ImportConfiguration configuration = ImportConfiguration.builder("/testRegion").build();
ImportResult importResult = GpdbService.importRegion(configuration);

// Get the total number of Greenplum rows imported into the GemFire region.
int importCount = importResult.getImportedCount();
System.out.println("Imported rows: " + importCount);

To hook into the import lifecycle, implement the `OperationEventListener` interface to receive callbacks at key points during the import.

## Result

Once GpdbService.importRegion() is invoked, the data from the `test_small` Greenplum table will be imported into the `/testRegion` GemFire region.
