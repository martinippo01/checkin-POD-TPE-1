#!/bin/bash

PATH_TO_CODE_BASE=`pwd`

JAVA_OPTS="-Djava.rmi.server.codebase=file://$PATH_TO_CODE_BASE/lib/jars/grpc-com-tpe1-client-2024.1Q.jar"
#JAVA_OPTS="-Djava.rmi.server.codebase=file://$PATH_TO_CODE_BASE/lib/jars/rmi-params-client-1.0-SNAPSHOT.jar"

MAIN_CLASS="ar.edu.itba.pod.tpe1.client.CheckInClient"
#MAIN_CLASS="ar.edu.itba.pod.tpe1.client.Client

java $JAVA_OPTS -cp 'lib/jars/*'  $MAIN_CLASS $#
