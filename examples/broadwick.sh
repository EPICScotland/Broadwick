#!/bin/sh
java -server -Xmx2048m -XX:-UseGCOverheadLimit -XX:MaxPermSize=2048m -XX:+UseParallelGC -Done-jar.silent=true -jar target/BroadwickExamples-0.0.one-jar.jar $*


#mvn exec:java -Dexec.mainClass=broadwick.Broadwick -Dexec.args=$*

