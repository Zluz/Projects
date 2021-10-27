#!/bin/bash

#---initial audio check handled in stop_all.sh
#
# if [[ "`lsof /dev/snd/* 2>/dev/null | grep chromium`" == "" ]]
# then 
# 	echo "No web audio."
# 	exit
# else
# 	echo -n "Stopping web audio..."
# fi

echo -n "Stopping web audio..."

WID=`xdotool search --name " Chromium"`
xdotool windowfocus $WID
sleep 1
# xdotool key ctrl+shift+p
# xdotool key --delay 800 alt+d
xdotool key alt+d
# sleep 1
# xdotool type --delay 10 "about:blank"
xdotool type "about:blank"
# sleep 1
xdotool key --delay 200 "Return"

echo "Done."

