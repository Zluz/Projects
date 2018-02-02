#!/bin/bash

WID=`xdotool search --name " Chromium"`
xdotool windowfocus $WID
sleep 1
# xdotool key ctrl+shift+p
xdotool key --delay 800 alt+d
# sleep 1
xdotool type --delay 10 "about:blank"
# sleep 1
xdotool key --delay 200 "Return"

