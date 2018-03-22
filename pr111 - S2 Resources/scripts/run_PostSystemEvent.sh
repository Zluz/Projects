#!/bin/bash

/bin/sleep 1

echo "Looking for a PostSystemEvent executable.."

# nohup `sleep 1;nohup /Local/scripts/conky.sh > /dev/null 2>&1;` &

export RUNDIR=/Share/Development/Export

export LATEST=`/bin/ls -1t $RUNDIR/pr117_* | /usr/bin/head -1`

count=0

while [[ "$LATEST" == "" ]]; do
	counter=$((counter+1))

	if [[ "$counter" -gt 4 ]]; then
		echo "Failed to mount /Share, aborting."
		exit 1
	fi

	echo "PostSystemEvent not found."

	echo "Attempting to mount /Share .."
	# pwd necessary here?
	mount -t cifs -o user=pi //192.168.1.200/Share /Share

	echo "No executable found. Share probably not mounted yet."
	/bin/sleep 1

	echo "Retrying.."
	export LATEST=`/bin/ls -1t $RUNDIR/pr117_* | /usr/bin/head -1`
	echo "Latest found = $LATEST"

done

echo "Found, running $LATEST"

cd $RUNDIR
/usr/bin/java -jar $LATEST $1

echo "System event posted: $1" >> /tmp/reboot.log

