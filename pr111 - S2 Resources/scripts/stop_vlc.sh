#!/bin/bash

if [[ "`lsof /dev/snd/* | grep vlc`" == "" ]]
then 
	echo "no vlc audio"
	exit
fi

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

