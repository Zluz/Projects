#!/bin/bash
# see: https://www.raspberrypi.org/documentation/usage/camera/raspicam/raspistill.md

cam_detected=$(vcgencmd get_camera | grep "detected=1" | wc -l)
if [[ "$cam_detected" == "0" ]]
then
	echo "No camera detected. Exiting."
	exit
else
	echo "Camera detected."
fi

echo "Time now: $(date)"
#DATE=$(date +"%Y-%m-%d_%H%M")
NOW=$(date +"%M")
LAST=$NOW
code=0

while [[ "$NOW" == "$LAST" ]]
do
#	echo "Capturing..."
	/usr/bin/raspistill -n -o /tmp/capture_still_now.jpg --timeout 1 -ex sports 
	code=$?
	echo "Exit code: $code"

	if [[ "$code" != "0" ]]
	then
		sleep 2
	fi
	sleep 1
	LAST=$NOW
	NOW=$(date +"%M")
done

( ls /tmp/session && ls /tmp/capture_still_now.jpg && cp /tmp/capture_still_now.jpg /tmp/session )

echo "Done."

