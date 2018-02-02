#!/bin/bash

/Local/scripts/stop_all.sh

WID=`xdotool search --name " Chromium"`
xdotool windowfocus $WID
sleep 1
# xdotool key ctrl+shift+p
xdotool key alt+d
sleep 1
xdotool type "https://www.npr.org/?refresh=true"
sleep 1
xdotool key "Return"
xdotool windowsize $WID 50% 70%
xdotool windowmove $WID 900 40
sleep 10
xdotool mousemove 1558 160 click 1

