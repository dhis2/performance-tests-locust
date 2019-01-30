#!/bin/sh

set -e
ADDITIONAL_OPTS="$OPTS:-"
LOCUST_OPTS="-f $LOCUST_FILE --master --host=$HOST"

cd /locust
locust ${LOCUST_OPTS}