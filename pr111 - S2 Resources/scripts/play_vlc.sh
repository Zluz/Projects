#!/bin/bash

/Local/scripts/stop_all.sh

WID=`xdotool search --name "VLC media player"`
xdotool windowfocus $WID
sleep 1
# xdotool key ctrl+shift+p
xdotool key s
sleep 1
xdotool key space
sleep 1
xdotool key alt+L E O

