#!/bin/sh

echo "Creating native image ... "
native-image --no-fallback \
             --native-image-info \
             --verbose \
             -H:ConfigurationFileDirectories=target/config \
             -H:+ReportExceptionStackTraces \
             -H:+ReportUnsupportedElementsAtRuntime \
             -Djava.awt.headless=false \
             -J-Xmx7G \
             -jar target/JTabbedPaneExtended-1.0.0-SNAPSHOT.jar \
             JTabbedPaneExtended
             
# echo "Compressing executable ... "
upx JTabbedPaneExtended

