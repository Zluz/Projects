#!/bin/bash

export UPTIME_THIS=`uptime -s`
export UPTIME_LOG=`cat /tmp/reboot.log | head -1 2> /dev/null`


export EVENT=$1

if [[ "$EVENT" == "" ]]; then

	# see pr105:SystemEvent.java
	echo "No event specified."
	echo "  Example events: DEVICE_STARTED, HEARTBEAT_DAY, GENERAL_MESSAGE"

	exit
fi

echo "Posting event: $EVENT"

export RUNDIR=/Share/Development/Export
export LATEST=`/bin/ls -1t $RUNDIR/pr117_* | /usr/bin/head -1`
if [[ "$LATEST" == "" ]]; then

	echo "Unable to post system event; pr117 executable not found. Share may not be mounted."

	echo "Attempting to mount.."
	mount -t cifs -o user=pi,password=rpi //192.168.1.200/Share /Share

	sleep 1

	export LATEST=`/bin/ls -1t $RUNDIR/pr117_* | /usr/bin/head -1`
	if [[ "$LATEST" == "" ]];
	then
		echo "Failed to mount Share. Exiting."
		exit 1;
	fi

	echo "    Share mounted successfully." >> /tmp/reboot.log
fi

echo "Running  $LATEST $EVENT"
/Local/scripts/timeout3.sh -t 60  /usr/bin/java -jar $LATEST $EVENT

echo "Posted $EVENT event.."

