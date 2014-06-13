#!/bin/sh
java -server -Xmx4096m -XX:-UseGCOverheadLimit -XX:MaxPermSize=2048m -XX:+UseParallelGC -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -Done-jar.silent=true -jar target/StochasticSIR-1.0.one-jar.jar $*

