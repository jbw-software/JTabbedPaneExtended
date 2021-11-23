#!/bin/sh

echo "Generate the config files ... "
java -agentlib:native-image-agent=config-output-dir=target/config \
     -jar target/JTabbedPaneExtended-1.0.0-SNAPSHOT.jar

