#!/bin/sh
java -server -Xmx4096m -XX:-UseGCOverheadLimit -XX:MaxPermSize=2048m -XX:+UseParallelGC -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -Done-jar.silent=true -jar target/MaxLikelihoodSeirModel-0.1.one-jar.jar $*

