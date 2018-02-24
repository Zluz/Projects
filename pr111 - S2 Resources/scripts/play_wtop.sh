#!/bin/bash

#---example output from lsof----------
#
# root@raspberrypi:/Local/scripts# lsof /dev/snd/* 2>/dev/null
# COMMAND     PID USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
# lxpanel     716   pi   13u   CHR  116,0      0t0 8225 /dev/snd/controlC0
# chromium- 24283   pi  289u   CHR 116,16      0t0 8226 /dev/snd/pcmC0D0p
#
#-------------------------------------


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
	LSOF_TEST=`lsof /dev/snd/* 2>/dev/null | grep chromium`
	if [[ "$LSOF_TEST" == "" ]]
	then 
		echo "No web audio detected yet, sending mouse click.."
		xdotool mousemove 972 474 click 1
		sleep 4
	else
		echo "Web audio detected:"
		echo "    $LSOF_TEST"
		xdotool mousemove 972 500
		exit
	fi
done

