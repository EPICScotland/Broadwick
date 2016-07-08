#!/bin/sh

# TODO - replace the ${artifactId} and ${version} with your projects artifactId and version.

java -server -Xmx4096m -XX:-UseGCOverheadLimit -XX:+UseParallelGC -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -Done-jar.silent=true -jar target/${artifactId}-${version}.one-jar.jar $*

