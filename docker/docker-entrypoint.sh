#!/bin/sh

set -e

ADDITIONAL_OPTS=""

if [ "$NO_WEB" = true ]; then
    ADDITIONAL_OPTS="$ADDITIONAL_OPTS --headless --csv=reports/dhis --csv-full-history -u $USERS -r $HATCH_RATE"
    if [ ! -z "$TIME" ]; then
        ADDITIONAL_OPTS="$ADDITIONAL_OPTS -t $TIME "
    fi
fi

LOCUST_OPTS="-f ${LOCUST_FILE} --master --host=$HOST $ADDITIONAL_OPTS --html=reports/test_report.html"

cd /home/locust
locust ${LOCUST_OPTS}