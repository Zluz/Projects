!/bin/bash

#---example output from lsof----------
#
# root@raspberrypi:/Local/scripts# lsof /dev/snd/* 2>/dev/null
# COMMAND     PID USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
# lxpanel     716   pi   13u   CHR  116,0      0t0 8225 /dev/snd/controlC0
# chromium- 24283   pi  289u   CHR 116,16      0t0 8226 /dev/snd/pcmC0D0p
#
#-------------------------------------

if [[ "1" == "1" ]]
then
	echo ">>> ok"
else
	echo ">>> problems"
	exit 1
fi


PID_FILE="/tmp/.play_wtop.pid"
if [ -f $PID_FILE ]; then
	echo "Script appears to already be running (1). Exiting."
	exit
fi


PIDS=`pidof -x play_wtop.sh`
RAND=`openssl rand -hex 8`

# KEY_HERE="$PIDS $RAND"
# KEY_HERE=`echo $RAND | xargs`
KEY_HERE=$RAND
echo "$KEY_HERE" > $PID_FILE

echo "KEY_HERE = [$KEY_HERE]"

sleep 1

if [[ "1" == "1" ]]; then
	echo "ok"
else
	echo "problems"
fi


KEY_CHECK=`cat $PID_FILE | xargs`
# if [[ "$KEY_HERE" == "$KEY_CHECK" ]]
# if [[ $KEY_CHECK == *"$KEY_HERE"* ]]; then
if [[ $KEY_CHECK =~ "$KEY_HERE" ]]; then
	sleep 1
	echo "Single execution confirmed.  [$KEY_HERE]  Proceeding."
else
	echo "Script appears to already be running (2): [$KEY_CHECK]  Exiting."
	echo "  [$KEY_HERE] != [$KEY_CHECK]"
	exit
fi


echo "#JSON  {\"caption\":\"Stopping audio\"}"

/usr/bin/killall chromium-browser

# /Local/scripts/launch_web.sh
/Local/scripts/stop_all.sh

echo "#JSON  {\"caption\":\"Activting browser\"}"

/usr/lib/chromium-browser/chromium-browser &
sleep 6s

echo "#JSON  {\"caption\":\"Activating playback\"}"
/usr/lib/chromium-browser/chromium-browser --kiosk "https://live.wtop.com/listen/?autoplay=1" &

echo "Sleeping for 15s.."
sleep 15s

for i in 1 2 3 4 5 6 7
do
	LSOF_TEST=`lsof /dev/snd/* 2>/dev/null | grep chromium`
	if [[ "$LSOF_TEST" == "" ]]
	then 
		echo "#JSON  {\"caption\":\"Activating playback\",\"status\":\"warning\"}"
		echo "No web audio detected yet, retrying URL.."

		/usr/lib/chromium-browser/chromium-browser --kiosk "https://live.wtop.com/listen/?autoplay=1" &

		sleep 20 
		echo "#JSON  {\"caption\":\"Checking playback\",\"status\":\"warning\"}"
		sleep 1
	else
		echo "Web audio detected:"
		echo "    $LSOF_TEST"
		echo "#JSON  {\"caption\":\"Audio detected..1\"}"
		echo "Audio detected but waiting again (20s) to double-check.."

		sleep 20
		LSOF_TEST=`lsof /dev/snd/* 2>/dev/null | grep chromium`
		if [[ "$LSOF_TEST" == "" ]]
		then 
			echo "#JSON  {\"caption\":\"False hit, reset\",\"status\":\"warning\"}"
			echo "Web audio no longer detected. Returning to wait loop.."
		else
			echo "Web audio detected (final check):"
			echo "    $LSOF_TEST"
			echo "#JSON  {\"caption\":\"Audio detected..2\",\"status\":\"done\"}"

			rm $PID_FILE
			exit 0
		fi
	fi
done
echo "#JSON  {\"caption\":\"Audio not detected\",\"status\":\"error\"}"
echo 1

rm $PID_FILE

