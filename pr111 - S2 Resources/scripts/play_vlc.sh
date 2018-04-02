#!/bin/bash

echo "#JSON  {\"caption\":\"Stopping audio\"}"

/Local/scripts/stop_all.sh

echo "Starting VLC playback.."

echo "#JSON  {\"caption\":\"Activating player\"}"

WID=`xdotool search --name "VLC media player"`

if [[ "$WID" == "" ]]
then
	echo "#JSON  {\"caption\":\"VLC not found\",\"status\":\"error\"}"
	exit 1
fi

xdotool windowfocus $WID
xdotool windowactivate $WID
sleep 1
# xdotool key ctrl+shift+p
xdotool key Escape
sleep 1
xdotool key s
sleep 1
xdotool key space
sleep 1
xdotool key alt+L E O

echo "#JSON  {\"caption\":\"Checking playback\"}"

LSOF_TEST=`lsof /dev/snd/* 2>/dev/null | grep vlc | head -1`
if [[ "$LSOF_TEST" == "" ]]
then
	echo "WARNING: VLC audio did not appear to start."
	echo "#JSON  {\"caption\":\"Audio not detected\",\"status\":\"error\"}"
else
	echo "VLC audio detected:"
	echo "    $LSOF_TEST"
	echo "#JSON  {\"caption\":\"Audio detected\",\"status\":\"done\"}"
fi

