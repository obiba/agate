#!/bin/bash

exec $JAVA $JAVA_ARGS -cp "${AGATE_HOME}/conf:${AGATE_DIST}/webapp/WEB-INF/classes:${AGATE_DIST}/webapp/WEB-INF/lib/*" -DAGATE_HOME=${AGATE_HOME} -DAGATE_DIST=${AGATE_DIST} -DAGATE_LOG=${AGATE_LOG} org.obiba.agate.Application $AGATE_ARGS
