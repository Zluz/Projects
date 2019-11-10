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
export latest=`ls -1t $rundir/pr101_* | head -1`
cd $rundir
/usr/bin/java -jar $latest | tee $FILE_LOG

`sleep 1;/Local/scripts/conky.sh > /dev/null 2>&1;` &

echo
# echo "Session path: `readlink /tmp/session`"
echo "Log written to:"
echo $FILE_LOG
file /tmp/session
echo ""

echo -n "Client process ended, showing desktop prompt..."
zenity --warning --no-wrap --text="Client process ended.\n\nLog written to:\n${FILE_LOG}" 2> /dev/null
echo "Done."


