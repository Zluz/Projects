#!/bin/bash

/Local/scripts/stop_all.sh

echo "Starting VLC playback.."

WID=`xdotool search --name "VLC media player"`
xdotool windowfocus $WID
sleep 1
# xdotool key ctrl+shift+p
xdotool key s
sleep 1
xdotool key space
sleep 1
xdotool key alt+L E O

LSOF_TEST=`lsof /dev/snd/* 2>/dev/null | grep vlc | head -1`
if [[ "$LSOF_TEST" == "" ]]
then
	echo "WARNING: VLC audio did not appear to start."
else
	echo "VLC audio detected:"
	echo "    $LSOF_TEST"
fi

