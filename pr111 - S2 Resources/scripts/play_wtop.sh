#!/bin/bash

#---example output from lsof----------
#
# root@raspberrypi:/Local/scripts# lsof /dev/snd/* 2>/dev/null
# COMMAND     PID USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
# lxpanel     716   pi   13u   CHR  116,0      0t0 8225 /dev/snd/controlC0
# chromium- 24283   pi  289u   CHR 116,16      0t0 8226 /dev/snd/pcmC0D0p
#
#-------------------------------------

echo "#JSON  {\"caption\":\"Stopping audio\"}"

/Local/scripts/launch_web.sh
/Local/scripts/stop_all.sh

echo "#JSON  {\"caption\":\"Activting browser\"}"

WID=`xdotool search --name " Chromium"`
if [[ "$WID" == "" ]]
then
	echo "Browser window not found .. retrying .."
	sleep 2
	WID=`xdotool search --name " Chromium"`
fi

if [[ "$WID" == "" ]]
then
	echo "#JSON  {\"caption\":\"Browser not found\",\"status\":\"error\",\"status\":\"done\"}"
	exit 1
fi

echo "Chromium window: $WID"

xdotool windowfocus $WID
xdotool windowactivate $WID
sleep 0.5s
# xdotool key ctrl+shift+p
xdotool key alt+d
sleep 1
# xdotool type --delay 0 "https://www.wtop.com/listen-live/"
xdotool type --delay 0 "https://live.wtop.com/listen/?autoplay=1"
sleep 0.5s
xdotool key "Return"
xdotool windowsize $WID 50% 70%
xdotool windowmove $WID 900 40

echo "#JSON  {\"caption\":\"Activating playback\"}"
# echo "Sleeping for 15s.."
# sleep 15s

xdotool windowfocus $WID
xdotool windowactivate $WID

# xdotool mousemove 972 474 click 1
# xdotool mousemove 1829 206 click 1
# sleep 1.0s
# xdotool mousemove 936 384 click 1

echo "Sleeping for 10s.."
sleep 10s


for i in 1 2 3 4 5 6 7
do
	LSOF_TEST=`lsof /dev/snd/* 2>/dev/null | grep chromium`
	if [[ "$LSOF_TEST" == "" ]]
	then 
		echo "#JSON  {\"caption\":\"Activating playback\",\"status\":\"warning\"}"
		echo "No web audio detected yet, retrying URL.."

		xdotool windowfocus $WID
		xdotool windowactivate $WID

		xdotool key alt+d
		sleep 1
		xdotool type --delay 0 "https://live.wtop.com/listen/?autoplay=1"
		sleep 0.5s
		xdotool key "Return"

		sleep 4
		echo "#JSON  {\"caption\":\"Checking playback\",\"status\":\"warning\"}"
		sleep 1
	else
		echo "Web audio detected:"
		echo "    $LSOF_TEST"
		xdotool windowfocus $WID
		echo "#JSON  {\"caption\":\"Audio detected..1\"}"
		echo "Audio detected but waiting again (10s) to double-check.."

		sleep 10
		LSOF_TEST=`lsof /dev/snd/* 2>/dev/null | grep chromium`
		if [[ "$LSOF_TEST" == "" ]]
		then 
			echo "#JSON  {\"caption\":\"False hit, reset\",\"status\":\"warning\"}"
			echo "Web audio no longer detected. Returning to wait loop.."
		else
			echo "Web audio detected (final check):"
			echo "    $LSOF_TEST"
			echo "#JSON  {\"caption\":\"Audio detected..2\",\"status\":\"done\"}"
			xdotool windowfocus $WID
			xdotool mousemove 972 500
			exit 0
		fi
	fi
done
echo "#JSON  {\"caption\":\"Audio not detected\",\"status\":\"error\"}"
echo 1

