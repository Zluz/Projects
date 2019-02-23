#!/bin/bash
#-!/bin/csh

export RAND_10=$(( RANDOM % 10 ))
echo "Sleeping for $RAND_10 second(s).."
/bin/sleep $RAND_10
echo "Posting HEARTBEAT_HOUR event.."
/Local/scripts/post_event.sh HEARTBEAT_HOUR 
echo "Done."

