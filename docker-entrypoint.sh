#!/bin/sh

set -e

ADDITIONAL_OPTS=""

if [ ! -z "$TIME" ]; then
    ADDITIONAL_OPTS="$ADDITIONAL_OPTS -t $TIME"
fi

LOCUST_OPTS="-f $LOCUST_FILE --master --host=$HOST --no-web --csv=dhis -c $USERS -r $HATCH_RATE $ADDITIONAL_OPTS"

cd /locust
locust ${LOCUST_OPTS}