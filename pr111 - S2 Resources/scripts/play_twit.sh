#!/bin/bash

/Local/scripts/stop_all.sh

WID=`xdotool search --name " Chromium"`
xdotool windowfocus $WID
sleep 1
# xdotool key ctrl+shift+p
xdotool key alt+d
sleep 1
xdotool type "https://www.twitch.tv/twit"
sleep 1
xdotool key "Return"
xdotool windowsize $WID 50% 70%
xdotool windowmove $WID 900 40

