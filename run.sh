#!/usr/bin/env bash

#!/bin/sh


export JAVA_OPTS="$(java-dynamic-memory-opts 80)"
export JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=PROD"

if [ -n "$APP_CACHE_TIMEOUT" ]; then
    export JAVA_OPTS="$JAVA_OPTS -Dcache.timeout=$APP_CACHE_TIMEOUT"
fi

if [ -n "$STACK_CLEANUP_IF_ONLY_ONE_LEFT" ]; then
    export JAVA_OPTS="$JAVA_OPTS -Dstack.cleanup.onlyoneleft=$STACK_CLEANUP_IF_ONLY_ONE_LEFT"
fi

if [ -n "$APP_INCLUDE" ]; then
    export JAVA_OPTS="$JAVA_OPTS -Dapps.include=$APP_INCLUDE"
fi

if [ -n "$APP_EXCLUDE" ]; then
    export JAVA_OPTS="$JAVA_OPTS -Dapps.exclude=$APP_EXCLUDE"
fi

if [ -n "$STACK_NOTRAFFIC_TTL" ]; then
    export JAVA_OPTS="$JAVA_OPTS -Dstack.notraffic.ttl=$STACK_NOTRAFFIC_TTL"
fi


echo JAVA_OPTS: ${JAVA_OPTS}

java ${JAVA_OPTS} -jar ./root.jar
