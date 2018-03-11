#!/bin/bash

nohup `sleep 1;nohup /Local/scripts/conky.sh > /dev/null 2>&1;` &

export rundir=/Share/Development/Export
export latest=`ls -1t $rundir/pr101_* | head -1`
cd $rundir
/usr/bin/java -jar $latest

echo -n "Client process ended, showing desktop prompt..."
zenity --warning --text="Client process ended." 2> /dev/null
echo "Done."


