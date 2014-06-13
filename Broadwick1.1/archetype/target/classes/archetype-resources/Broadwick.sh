#!/bin/sh
java -server -Xmx4096m -XX:-UseGCOverheadLimit -XX:MaxPermSize=2048m -XX:+UseParallelGC -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -Done-jar.silent=true -jar target/${artifactId}-${version}.one-jar.jar $*

