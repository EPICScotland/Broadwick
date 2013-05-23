#!/bin/sh

java -server -Xmx2048m -XX:-UseGCOverheadLimit -XX:MaxPermSize=2048m -XX:+UseParallelGC -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044 -Done-jar.silent=true -jar target/BroadwickExamples-0.0.one-jar.jar $*


#/usr/share/maven/bin/mvnDebug exec:java -Dexec.mainClass="broadwick.Broadwick" -Dexec.args="$*"


