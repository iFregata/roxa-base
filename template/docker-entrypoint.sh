#!/bin/bash
set -e

setup() {
    DEFAULT_VERTX_OPTS="-Dfile.encoding=UTF-8 -Dlog4j.configurationFile=$ARTIFACT_CFG_LOCATION/log4j2.xml -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"
    DEFAULT_JAVA_OPTS="-XX:+UseG1GC"
    #
    # Set up some easily accessible MIN/MAX params for JVM mem usage
    #
    if [ "x${JAVA_MIN_MEM}" != "x" ]; then
        DEFAULT_JAVA_OPTS="${DEFAULT_JAVA_OPTS} -Xms${JAVA_MIN_MEM}"
    fi
    if [ "x${JAVA_MAX_MEM}" != "x" ]; then
        DEFAULT_JAVA_OPTS="${DEFAULT_JAVA_OPTS} -Xmx${JAVA_MAX_MEM}"
    fi
    
    if [ "x${JAVA_OPTS}" = "x" ]; then
        JAVA_OPTS="${DEFAULT_JAVA_OPTS}"
    fi
    export JAVA_OPTS
    
    if [ "x${EXTRA_JAVA_OPTS}" != "x" ]; then
      JAVA_OPTS="${JAVA_OPTS} ${EXTRA_JAVA_OPTS}"
    fi

    if [ "x${VERTX_OPTS}" = "x" ]; then
        VERTX_OPTS="${DEFAULT_VERTX_OPTS}"
    fi
    export VERTX_OPTS
    
    if [ "x${EXTRA_VERTX_OPTS}" != "x" ]; then
        VERTX_OPTS="${VERTX_OPTS} ${EXTRA_VERTX_OPTS}"
    fi
}

if [ "$1" = 'run' ]; then
  setup
  shift
  exec java $JAVA_OPTS $VERTX_OPTS -jar $ARTIFACT_FILE "$@"
fi

exec "$@"