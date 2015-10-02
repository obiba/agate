#!/bin/bash

getPidFile() {
   while getopts ":p:" opt; do
     case $opt in
       p)
         echo $OPTARG
         return 0
         ;;
     esac
   done

   return 1
}

# OS specific support.
cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

if [ -z "$JAVA_OPTS" ]
then
  if [ ! -z "$JAVA_ARGS" ]
  then
    JAVA_OPTS=$JAVA_ARGS
  else
    # Set default JAVA_OPTS
    JAVA_OPTS="-Xmx2G -XX:MaxPermSize=128M"
  fi

  export JAVA_OPTS
fi

# The directory containing the agate shell script
AGATE_BIN_DIR=`dirname $0`
# resolve links - $0 may be a softlink
AGATE_DIST=$(readlink -f $AGATE_BIN_DIR/../webapp)

export AGATE_DIST

echo "JAVA_OPTS=$JAVA_OPTS"
echo "AGATE_HOME=$AGATE_HOME"
echo "AGATE_DIST=$AGATE_DIST"
echo "AGATE_LOG=$AGATE_LOG"

if [ -z "$AGATE_HOME" ]
then
  echo "AGATE_HOME not set."
  exit 2;
fi

if $cygwin; then
  # For Cygwin, ensure paths are in UNIX format before anything is touched
  [ -n "$AGATE_DIST" ] && AGATE_BIN=`cygpath --unix "$AGATE_DIST"`
  [ -n "$AGATE_HOME" ] && AGATE_HOME=`cygpath --unix "$AGATE_HOME"`

  # For Cygwin, switch paths to Windows format before running java
  export AGATE_DIST=`cygpath --absolute --windows "$AGATE_DIST"`
  export AGATE_HOME=`cygpath --absolute --windows "$AGATE_HOME"`
fi

# Java 6 supports wildcard classpath entries
# http://download.oracle.com/javase/6/docs/technotes/tools/solaris/classpath.html
CLASSPATH=$AGATE_HOME/conf:$AGATE_DIST/WEB-INF/classes:$AGATE_DIST/WEB-INF/lib/*
if $cygwin; then
  CLASSPATH=$AGATE_HOME/conf;$AGATE_DIST/WEB-INF/classes;$AGATE_DIST/WEB-INF/lib/*
fi

[ -e "$AGATE_HOME/logs" ] || mkdir "$AGATE_HOME/logs"

JAVA_DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n

# Add $JAVA_DEBUG to this line to enable remote JVM debugging (for developers)
exec java $JAVA_OPTS -cp "$CLASSPATH" -DAGATE_HOME="${AGATE_HOME}" \
  -DAGATE_DIST=${AGATE_DIST} -DAGATE_LOG=${AGATE_LOG}  org.obiba.agate.Application "$@" >$AGATE_LOG/stdout.log 2>&1 &

# On CentOS 'daemon' function does not initialize the pidfile
pidfile=$(getPidFile $@)

if [ ! -z "$pidfile" ]; then
  echo $! > $pidfile
fi
