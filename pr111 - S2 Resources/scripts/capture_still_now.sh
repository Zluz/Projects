#!/bin/bash
# see: https://www.raspberrypi.org/documentation/usage/camera/raspicam/raspistill.md

# USB cameras no longer supported
# vid_detected=$(/bin/ls -l /dev/video* 2> /dev/null | wc -l)
vid_detected=0
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
	ls /dev/v4l/by-id/ -lh 2> /dev/null | grep video >> /tmp/session/capture_list.txt
fi


if [[ -e "/tmp/session" ]]; then
	# delete old files. see SO:13489398, SO:430106.
	# find /tmp/session/ -mtime +1 -type f -delete -name "capture_cam-t15*.jpg"

#	find   /tmp/session/ -mmin +10 -type f -delete -name "capture_cam-t15*.jpg"
#	echo "(find-delete skipped)"
	find   /tmp/session/ -mmin +10 -type f         -name "capture_cam-t15*.jpg" -exec rm {} \;
fi


echo "Time now: $(date)"
DATE=$(date +"%Y%m%d_%H%M")
# NOW=$(date +"%M")
NOW=$DATE
LAST=$NOW

NOW_VLMSG=$( stat -c %Y /var/log/messages )
LAST_VLMSG=$NOW_VLMSG

code=0
MAC=$( file -b /tmp/session | cut -d '/' -f 4 )

echo "Looping during working minute: $NOW"

while [[ "$NOW" == "$LAST" ]]
do
	echo "Capturing..."
	TIMESTAMP=$( date +%s%3N )
	export FILE_KEY="t"$TIMESTAMP
	echo "Using file key: $FILE_KEY"

	if [[ "$cam_detected" == "1" ]]
	then
		echo -n "Taking picture using RPi cam..."
		# /usr/bin/raspistill -n -o /tmp/capture_still_now.jpg --timeout 1 -ex sports 
		/usr/bin/raspistill -q 6 -n -o /tmp/capture_still_now._jpg
		code=$?
		# echo "Done. Exit code: $code"
		if [[ "$code" == "0" ]]
		then
			echo -n "Renaming..."

			rm -rf /tmp/capture_still_now.jpg
			mv /tmp/capture_still_now._jpg /tmp/capture_still_now.jpg

			if [[ -e "/tmp/session" ]]
			then
				echo -n "Copying to share..."

				cp /tmp/capture_still_now.jpg /tmp/session/capture_cam._jpg
				cp /tmp/capture_still_now.jpg /tmp/session/capture_cam-thumb._jpg

				rm -rf /tmp/session/capture_cam.jpg
				mv /tmp/session/capture_cam._jpg /tmp/session/capture_cam-$FILE_KEY.jpg

				mogrify -scale 300x -quality 50 /tmp/session/capture_cam-thumb._jpg
				rm -rf /tmp/session/capture_cam-thumb.jpg
				mv /tmp/session/capture_cam-thumb._jpg /tmp/session/capture_cam-$FILE_KEY-thumb.jpg
				
				echo "Done."
			else
				echo "WARNING: Share unavailable."
			fi

			# sleep 1
		else
			echo "WARNING. Exit code: $code"
	
			sleep 2
		fi
	fi

	if [[ "$vid_detected" != "0" ]]
	then

		vid_count=0
		while [ -e "/dev/video$vid_count" ]
		do
			# echo "video $vid_count exists"


			NOW_VLMSG=$( stat -c %Y /var/log/messages )
			if [[ "$LAST_VLMSG" != "$NOW_VLMSG" ]]
			then
				echo "WARNING: /var/log/messages updated. Last lines:"
				tail -2 /var/log/messages | sed 's/^/    /'

				echo -n "Pausing for 5s..."
				sleep 5
				echo "Done."
			fi
			LAST_VLMSG=$NOW_VLMSG


			echo -n "Taking picture from camera on /dev/video$vid_count..."

			# may have in output:
			# GD Error: gd-jpeg: JPEG library reports unrecoverable error: Unsupported marker type 0xa0

			CMD="fswebcam -r 1280x1024 --jpeg 80 --banner-colour #FF000000 --line-colour #FF000000 --shadow --timestamp %Y%m%d-%H%M  --title $MAC -d /dev/video$vid_count --skip 2 /tmp/capture_vid$vid_count._jpg --log /tmp/cap.log"

			rm -rf /tmp/cap.out
			rm -rf /tmp/cap.log
			# CAP_CMD=$(fswebcam -r 1280x1024 -d /dev/video$vid_count --no-banner /tmp/capture_vid$vid_count._jpg --log /tmp/cap.log 2>&1 > /tmp/cap.out)
			# CAP_CMD=$(fswebcam -r 1280x1024 -d /dev/video$vid_count --no-banner /tmp/capture_vid$vid_count._jpg --log /dev/null 2>&1 > /tmp/cap.out )
			CAP_CMD=$( $CMD 2>&1 > /tmp/cap.out )


			# check for errors in the log

			CAP_ERR=$(cat /tmp/cap.log | grep rror | wc -l)
			if [[ "$CAP_ERR" != "0" ]]
			then
				echo "WARNING: Possible USB error."

				/bin/rm /tmp/cap.out
				echo "Attempting to reset the USB bus.."
				sleep 10
				/Share/Resources/bin/usbreset /dev/bus/usb/001/003
				# /Share/Resources/bin/usbreset /dev/bus/usb/001/002
				sleep 1
				echo "USB bus should have been reset."

				rm /tmp/capture_vid$vid_count._jpg
			fi


			# check the image (may be blank)

			if [[ -e "/tmp/capture_vid$vid_count._jpg" ]]
			then
				BLANK_GREP=$( convert /tmp/capture_vid$vid_count._jpg -virtual-pixel edge -fuzz 1% -trim -identify info: | grep "=>" )
				# BLANK_GREP=$( convert /tmp/capture_vid$vid_count._jpg -identify -verbose info: | grep "Type: Grayscale" )
				# BLANK_CHECK=$( convert /tmp/capture_vid$vid_count._jpg -identify -verbose info: )
				# BLANK_GREP=$( echo $BLANK_CHECK | grep "Type: Grayscale" )
				if [[ "$BLANK_GREP" != "" ]]
				then
					echo "WARNING: image may be blank."
					# echo $BLANK_CHECK
					echo "Image analysis output:"
					echo "    $BLANK_GREP"
					echo "Deleting."

					rm /tmp/capture_vid$vid_count._jpg
				fi
			fi


			if [[ -e "/tmp/capture_vid$vid_count._jpg" ]]
			then
				echo -n "Copying to share..."

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

					echo "Done."
				else
					echo "WARNING: Share unavailable."
				fi
			fi

			vid_count=$((vid_count+1))
		done

	fi

	sleep 1
	LAST=$NOW
	# NOW=$(date +"%M")
	NOW=$(date +"%Y%m%d_%H%M")

done

# ( ls /tmp/session && ls /tmp/capture_still_now.jpg && cp /tmp/capture_still_now.jpg /tmp/session )

echo "Working minute changed. End of script."

