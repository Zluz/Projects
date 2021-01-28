#!/bin/bash

WID=`xdotool search --onlyvisible --name "s116.+VNC"`

if [[ "$WID" == "" ]]
then
	echo "VNC session not open"
else
	echo "Bringing VNC session to front.."
	xdotool windowraise $WID
fi

