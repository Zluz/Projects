#!/bin/bash

export UPTIME_THIS=`uptime -s`
export UPTIME_LOG=`cat /tmp/reboot.log | head -1 2> /dev/null`

export NEW_LOG=0

if [[ "$UPTIME_THIS" == "$UPTIME_LOG" ]];
then
	export NEW_LOG=0
	echo "Reboot log is current."
	echo "    reboot time : $UPTIME_THIS"
else
	export NEW_LOG=1
	echo "Reboot log is NOT current."
	echo "    workstation : $UPTIME_THIS"
	echo "    log record  : $UPTIME_LOG"
	rm -f /tmp/reboot_last.log
	mv reboot.log reboot_last.log 2> /dev/null
	echo "$UPTIME_THIS" > /tmp/reboot.log
fi





# check, post system event

export LOGGED_EVENT=`cat /tmp/reboot.log | grep DEVICE_STARTED`

if [[ "$LOGGED_EVENT" == "" ]];
then
	echo "Event DEVICE_STARTED not already posted."

	export RUNDIR=/Share/Development/Export
	export LATEST=`/bin/ls -1t $RUNDIR/pr117_* | /usr/bin/head -1`

	if [[ "$LATEST" == "" ]];
	then
		echo "Unable to post system event; pr117 executable not found. Share may not be mounted."
		echo 0;
	fi

	/usr/bin/java -jar $LATEST DEVICE_STARTED > /tmp/PostEvent.out

	echo "Recording DEVICE_STARTED event.." >> /tmp/reboot.log
	echo "    `cat /tmp/PostEvent.out | grep Registering`" >> /tmp/reboot.log
	echo "    Event `cat /tmp/PostEvent.out | grep seq`" >> /tmp/reboot.log
	# echo "    device `cat /tmp/PostEvent.out | grep \"device.name\"`" >> /tmp/reboot.log


	# while we're here, run the beacon

	echo "Running beacon.sh.."

	/bin/bash /Local/scripts/beacon.sh > /tmp/beacon.log

else
	echo "Event DEVICE_STARTED already posted."
fi



