#!/bin/sh
java -server -Xmx4096m -XX:-UseGCOverheadLimit -XX:+UseParallelGC -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -Done-jar.silent=true -jar target/GillespieSir-1.2.one-jar.jar $*

