#!/bin/bash

# Check if the Java version is 21
java_version_output=$($JAVA -version 2>&1)
java_version=$(echo "$java_version_output" | grep version | awk '{ print $3 }' | tr -d '"')
if [[ "${java_version:0:3}" != "1.8" ]]
then
  echo "Java 1.8 is required, aborting"
	exit 1
fi

$JAVA $JAVA_ARGS -cp "${AGATE_HOME}/conf:${AGATE_DIST}/webapp/WEB-INF/classes:${AGATE_DIST}/webapp/WEB-INF/lib/*" -DAGATE_HOME=${AGATE_HOME} -DAGATE_DIST=${AGATE_DIST} -DAGATE_LOG=${AGATE_LOG} org.obiba.agate.Application $AGATE_ARGS
