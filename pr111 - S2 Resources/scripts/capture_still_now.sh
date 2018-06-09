#!/bin/bash
# see: https://www.raspberrypi.org/documentation/usage/camera/raspicam/raspistill.md

vid_detected=$(/bin/ls -l /dev/video* | wc -l)
cam_detected=$(vcgencmd get_camera | grep "detected=1" | wc -l)

if [[ "$vid_detected" == "0" && "$cam_detected" == "0" ]]
then
	echo "No cameras detected. Exiting."
	exit
fi

if [[ "$vid_detected" != "0" ]]
then
	echo "Attached camera(s) detected: $vid_detected."
fi

if [[ "$cam_detected" != "0" ]]
then
	echo "RPi camera detected."
fi


if [[ -e "/tmp/session" ]]
then
	echo "RPi camera: $(vcgencmd get_camera)" > /tmp/session/capture_list.txt
	echo "" >> /tmp/session/capture_list.txt
	echo "ls /dev/v4l/by-id/ -lh" >> /tmp/session/capture_list.txt
	ls /dev/v4l/by-id/ -lh | grep video >> /tmp/session/capture_list.txt
fi


echo "Time now: $(date)"
DATE=$(date +"%Y%m%d_%H%M")
NOW=$(date +"%M")
LAST=$NOW
code=0
MAC=$( file -b /tmp/session | cut -d '/' -f 4 )

while [[ "$NOW" == "$LAST" ]]
do
	echo "Capturing..."

	if [[ "$cam_detected" == "1" ]]
	then
		echo -n "Taking picture using RPi cam..."
		# /usr/bin/raspistill -n -o /tmp/capture_still_now.jpg --timeout 1 -ex sports 
		/usr/bin/raspistill -q 6 -n -o /tmp/capture_still_now._jpg
		code=$?
		echo "Done. Exit code: $code"
		if [[ "$code" == "0" ]]
		then
			rm -rf /tmp/capture_still_now.jpg
			mv /tmp/capture_still_now._jpg /tmp/capture_still_now.jpg

			if [[ -e "/tmp/session" ]]
			then
				cp /tmp/capture_still_now.jpg /tmp/session/capture_cam._jpg
				cp /tmp/capture_still_now.jpg /tmp/session/capture_cam-thumb._jpg

				rm -rf /tmp/session/capture_cam.jpg
				mv /tmp/session/capture_cam._jpg /tmp/session/capture_cam.jpg

				mogrify -scale 300x -quality 50 /tmp/session/capture_cam-thumb._jpg
				rm -rf /tmp/session/capture_cam-thumb.jpg
				mv /tmp/session/capture_cam-thumb._jpg /tmp/session/capture_cam-thumb.jpg
			fi

		else
			sleep 2
		fi
	fi

	if [[ "$vid_detected" != "0" ]]
	then

		vid_count=0
		while [ -e "/dev/video$vid_count" ]
		do
			# echo "video $vid_count exists"

			echo -n "Taking picture from camera on /dev/video$vid_count..."

			# may have in output:
			# GD Error: gd-jpeg: JPEG library reports unrecoverable error: Unsupported marker type 0xa0

			CMD="fswebcam -r 1280x1024 --jpeg 80 --banner-colour #FF000000 --line-colour #FF000000 --shadow --timestamp %Y%m%d-%H%M  --title $MAC -d /dev/video$vid_count --skip 1 /tmp/capture_vid$vid_count._jpg --log /tmp/cap.log"

			rm -rf /tmp/cap.out
			rm -rf /tmp/cap.log
			# CAP_CMD=$(fswebcam -r 1280x1024 -d /dev/video$vid_count --no-banner /tmp/capture_vid$vid_count._jpg --log /tmp/cap.log 2>&1 > /tmp/cap.out)
			# CAP_CMD=$(fswebcam -r 1280x1024 -d /dev/video$vid_count --no-banner /tmp/capture_vid$vid_count._jpg --log /dev/null 2>&1 > /tmp/cap.out )
			CAP_CMD=$( $CMD 2>&1 > /tmp/cap.out )
			# echo "CAP_CMD: $CAP_CMD"
			# CAP_ERR=$(cat /tmp/cap.log /tmp/cap.out | grep rror | wc -l)
			CAP_ERR=$(echo $CAP_CMD | grep rror | wc -l)
			if [[ "$CAP_ERR" == "0" && -e "/tmp/capture_vid$vid_count._jpg" ]]
			then
				echo "Done."
				rm -rf /tmp/capture_vid$vid_count.jpg
				mv /tmp/capture_vid$vid_count._jpg /tmp/capture_vid$vid_count.jpg

				if [[ -e "/tmp/session" ]]
				then
					cp /tmp/capture_vid$vid_count.jpg /tmp/session/capture_vid$vid_count._jpg
					cp /tmp/capture_vid$vid_count.jpg /tmp/session/capture_vid$vid_count-thumb._jpg

					rm -rf /tmp/session/capture_vid$vid_count.jpg
					mv /tmp/session/capture_vid$vid_count._jpg /tmp/session/capture_vid$vid_count.jpg

					mogrify -scale 300x -quality 50 /tmp/session/capture_vid$vid_count-thumb._jpg
					rm -rf /tmp/session/capture_vid$vid_count-thumb.jpg
					mv /tmp/session/capture_vid$vid_count-thumb._jpg /tmp/session/capture_vid$vid_count-thumb.jpg
				fi

			else
				echo "Error detected."
			fi

			vid_count=$((vid_count+1))
		done

	fi

	sleep 1
	LAST=$NOW
	NOW=$(date +"%M")

done

# ( ls /tmp/session && ls /tmp/capture_still_now.jpg && cp /tmp/capture_still_now.jpg /tmp/session )

echo "Done."

