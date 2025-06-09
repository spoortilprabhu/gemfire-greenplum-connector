## Demo Overview

This demo details an environment setup and demonstrates the
GemFire-Greenplum Connector ability to copy data from GPDB to GemFire.  If anything the most important file in this project is `GemfireClientApplicationTests.java`.  This is a file under the `gemfire-client` sub project, and this file will run two separate integration tests that interact with a GPDB instancce via JDBC and a Gemfire instance.  The tests will run pre and post assertions to prove out that running functions on the cluster will provide an expexted output.  

The demo runs `GPDB` and `GemFire 10.1.2` within a single `Ubuntu` VM.  
The mapping is as follows:

* `Customer` region -> `customer` table

## Prerequisites

	1.	Maven
	2.	The Gemfire-Greenplum connector jar
	3.	Gemfire 10.1.2
	4.	Greenplum 4.x


Running The Operations Programmatically
As we all know, GFSH is just a client application itself, so what it's actually doing is triggering operations that happen on the server.  In a production enviornment we're probably going to want to know how to trigger the imports and exports via a function.

Lets check out the first function `ImportFromGemfireToGPDBFunction.java` it does one important thing
```
ImportConfiguration configuration = ImportConfiguration.builder(region).build();
ImportResult importResult = GpdbService.importRegion(configuration);
int importCount =  importResult.getImportedCount();
```

	1.	So what we need to is to deploy this function onto the server so you can run it yourself!  Lets start by building the package from the root, this will build the gemfire-server package with the functions and the domain package with the Domain Objects.
```
      mvn clean install
```

	2.	You'll have everything you need deployed and now you can run the tests in Gemfire Client
   ```
   cd gemfire-client
   mvn test
   ```
	4.	This command will run the integration tests in `GemfireClientApplicationTests` that will prove the functionality of the gemfire greenplum connector.  
