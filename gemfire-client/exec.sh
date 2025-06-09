
#!/bin/bash

# Set the path to the JAR file
JAR_PATH="C:\Users\Administrator\Desktop\GemfireGreenplumConnectorDemo\gemfire-client\target\gemfire-client-0.0.1-SNAPSHOT.jar"


# Run the first Java class
echo "Running FirstApp..."
java -cp "$JAR_PATH" io.pivotal.gemfire.client.GpdbBulkInsert

# Run the second Java class
echo "Running SecondApp..."
java -cp "$JAR_PATH" io.pivotal.gemfire.client.GemfireClientApplicatio
