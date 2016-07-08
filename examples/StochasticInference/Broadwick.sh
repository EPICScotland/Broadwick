#!/bin/sh
java -server -Xmx4096m -XX:-UseGCOverheadLimit -XX:+UseParallelGC -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -Done-jar.silent=true -jar target/StochasticInference-0.1.one-jar.jar $*

