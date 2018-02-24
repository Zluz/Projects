#!/bin/bash

#---initial audio check is now handled in stop_all.sh
#
# if [[ "`lsof /dev/snd/* 2>/dev/null | grep vlc`" == "" ]]
# then 
# 	echo "No VLC audio."
# 	exit
# else
# 	echo -n "Stopping VLC audio..."
# fi

echo -n "Stopping VLC audio..."

WID=`xdotool search --name "VLC media player"`
xdotool windowfocus $WID
sleep 1
# xdotool key ctrl+shift+p
xdotool key s
sleep 1
xdotool key alt+l
sleep 1
xdotool key s
sleep 1
xdotool key Escape
# xdotool key space

echo "Done."

