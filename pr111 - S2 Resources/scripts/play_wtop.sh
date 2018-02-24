#!/bin/bash

/Local/scripts/stop_all.sh

WID=`xdotool search --name " Chromium"`
xdotool windowfocus $WID
sleep 1
# xdotool key ctrl+shift+p
xdotool key alt+d
sleep 1
xdotool type "https://www.wtop.com/listen-live/"
sleep 1
xdotool key "Return"
xdotool windowsize $WID 50% 70%
xdotool windowmove $WID 900 40

sleep 2
for i in 1 2 3 4 5
do
	if [[ "`lsof /dev/snd/* | grep chromium`" == "" ]]
	then 
		xdotool mousemove 972 474 click 1
		sleep 4
	else
		xdotool mousemove 972 500
		exit
	fi
done

