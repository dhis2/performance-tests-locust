#!/bin/sh

set -e

ADDITIONAL_OPTS=""

if [ "$NO_WEB" = true ]; then
    ADDITIONAL_OPTS="$ADDITIONAL_OPTS --no-web --csv=dhis -c $USERS -r $HATCH_RATE"
    if [ ! -z "$TIME" ]; then
        ADDITIONAL_OPTS="$ADDITIONAL_OPTS -t $TIME "
    fi
fi

LOCUST_OPTS="-f $LOCUST_FILE --master --host=$HOST $ADDITIONAL_OPTS"

cd /locust
locust ${LOCUST_OPTS}