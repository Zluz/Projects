#!/bin/bash
# see: https://www.raspberrypi.org/documentation/usage/camera/raspicam/raspistill.md

#DATE=$(date +"%Y-%m-%d_%H%M")

#raspistill -vf -hf -o /Share/xfer/Camera/capture_still_$DATE.jpg
#/usr/bin/raspistill -o /tmp/capture_still_$DATE.jpg


/usr/bin/raspistill -n -o /tmp/capture_still_recent.jpg
cp /tmp/capture_still_recent.jpg /tmp/session
sleep 20

/usr/bin/raspistill -n -o /tmp/capture_still_recent.jpg
cp /tmp/capture_still_recent.jpg /tmp/session
sleep 20

/usr/bin/raspistill -n -o /tmp/capture_still_recent.jpg
cp /tmp/capture_still_recent.jpg /tmp/session


