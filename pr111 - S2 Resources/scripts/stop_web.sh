#!/bin/bash

if [[ "`lsof /dev/snd/* | grep chromium`" == "" ]]
then 
	echo "no web audio"
	exit
fi

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

