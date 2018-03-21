#!/bin/bash

export UPTIME_THIS=`uptime -s`
export UPTIME_LOG=`cat /tmp/reboot.log | head -1 2> /dev/null`

if [[ "$UPTIME_THIS" == "$UPTIME_LOG" ]];
then
	echo "Reboot log is current."
	exit 0;
else
	echo "Reboot log is NOT current."
	echo "    now: $UPTIME_THIS"
	echo "    log: $UPTIME_LOG"
	# rm -f /tmp/reboot.log
	echo "$UPTIME_THIS" > /tmp/reboot.log
fi


