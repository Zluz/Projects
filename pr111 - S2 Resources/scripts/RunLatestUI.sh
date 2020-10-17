#!/bin/bash

export NOW=$(date +"%Y%m%d_%H%M")
export FILE_LOG="/tmp/session/SWT_UI__${NOW}.log"

echo "Writing log to:"
echo $FILE_LOG
file /tmp/session
echo ""

echo "Writing log: ${FILE_LOG}"

`sleep 1;/Local/scripts/conky.sh > /dev/null 2>&1;` &

export rundir=/Share/Development/Export

if [[ -e $rundir ]]
then
	echo "Share is mounted, Export directory found."
else
	echo -n "Share does not appear to be available. Waiting (up to 60 seconds) .. "
	for i in 1 2 3 4 5 6
	do
		if [[ -e $rundir ]]
		then
			// share is mounted .. just iterate through loop ..
		else
			echo -n "$i.. "
			sleep 10
		fi
	done
fi

export latest=`ls -1t $rundir/pr101_* | head -1`

echo "Running: $latest"
echo ""

cd $rundir
/usr/bin/java -jar $latest | tee $FILE_LOG
export exit_code=$?

`sleep 1;/Local/scripts/conky.sh > /dev/null 2>&1;` &

echo
# echo "Session path: `readlink /tmp/session`"
echo "Log written to:"
echo $FILE_LOG
file /tmp/session
echo ""

echo -n "Client process ended, exit code $exit_code.  Script about to end..."
zenity --warning --no-wrap --text="Client process ended\nExit code  $exit_code\n\nLog written to:\n${FILE_LOG}" 2> /dev/null
echo "Done."


