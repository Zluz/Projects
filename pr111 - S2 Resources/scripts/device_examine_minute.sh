#!/bin/bash

export SENSOR_TEMP_PORT=`jq '."sensor-temperature-gpio"' < /tmp/session/device_config.json`
export SENSOR_TEMP_DESC=`jq '."sensor-temperature-description"' < /tmp/session/device_config.json`

# not sure why jo complains about spaces here..
#export SENSOR_TEMP_DESC=`echo $SENSOR_TEMP_DESC | tr '"' "'"`
export SENSOR_TEMP_DESC=`echo $SENSOR_TEMP_DESC | tr ' ' '_'` 

export NOW=$(date +"%Y%m%d_%H%M%S")

export OUT="time=$NOW "

export THROTTLED=`/usr/bin/vcgencmd get_throttled`
export THROTTLED=`echo $THROTTLED | cut -d '=' -f 2`
export OUT="$OUT cpu-throttle=\"$THROTTLED\""
if [ "$THROTTLED" == "0x0" ]; then
	export OUT="$OUT _9_throttle=\"$THROTTLED\""
else
	export OUT="$OUT _3_throttle=\"$THROTTLED\""
fi

export CPU_TEMP=`/usr/bin/vcgencmd measure_temp`
export CPU_TEMP=`echo $CPU_TEMP | tr "'" '=' | cut -d '=' -f 2`
export OUT="$OUT cpu-temperature=$CPU_TEMP"


# export PID_PRANY=`ps -ef | grep java | tr '/' '\n' | grep jar | grep pr`

export PID_PR101=`ps -ef | grep java | grep pr101_ | tr -s ' ' '-' | cut -d '-' -f 2`
if [ -n "$PID_PR101" ]; then
	export TIME_PR101=`ps -p $PID_PR101 -o etimes | tail -1 | awk '{$1=$1;print}'`
	export OUT="$OUT proc-pr101-pid=$PID_PR101"
	export OUT="$OUT proc-pr101-time=$TIME_PR101"
else
	export OUT="$OUT proc-pr101-pid=<none>"
fi

export PID_PR130=`ps -ef | grep java | grep pr130_ | tr -s ' ' '-' | cut -d '-' -f 2`
if [ -n "$PID_PR130" ]; then
	export TIME_PR130=`ps -p $PID_PR130 -o etimes | tail -1 | awk '{$1=$1;print}'`
	export OUT="$OUT proc-pr130-pid=$PID_PR130"
	export OUT="$OUT proc-pr130-time=$TIME_PR130"
fi

export PID_VNC=`ps -ef | grep vncserver | grep serviced | tr -s ' ' '-' | cut -d '-' -f 2`
if [ -n "$PID_VNC" ]; then
	export TIME_VNC=`ps -p $PID_VNC -o etimes | tail -1 | awk '{$1=$1;print}'`
	export OUT="$OUT proc-vncserver-pid=$PID_VNC"
	export OUT="$OUT proc-vncserver-time=$TIME_VNC"
else
	export OUT="$OUT proc-vncserver-pid=<none>"
fi






export PID_VLC=`ps -ef | grep "/usr/bin/vlc" | grep -v grep | tr -s ' ' '-' | cut -d '-' -f 2`
if [ -n "$PID_VLC" ]; then
        export FILE_MP3=`lsof -p $PID_VLC | egrep "/Media/" | sed 's/.*\/Media\//\/Media\//'`
        # export MP3_TITLE=`ffprobe "$FILE_MP3" -loglevel error -show_entries format_tags=title -of default=noprint_wrappers=1:nokey=1`
        export MP3_TAGS=`ffprobe "$FILE_MP3" -loglevel error -show_entries format_tags=track,title,artist,album -of default=noprint_wrappers=1`
        export MP3_TAGS=`echo $MP3_TAGS | tr -s ' ' '_'`
        export LINE=`echo $MP3_TAGS | sed 's/_TAG:/\^ media-/g' | sed 's/\=/\=\^/g'`
        export LINE=`echo "$LINE^" | sed 's/TAG\:/media-/' | tr -s '^' '"'`

	export OUT="$OUT proc-vlc-pid=$PID_VLC"

        export MP3_FILE=`echo $FILE_MP3 | tr -s ' ' '_'`
        export OUT="$OUT media-file=$MP3_FILE"
        export OUT="$OUT $LINE"
else
	export OUT="$OUT proc-vlc-pid= "
fi








if [ -n "$SENSOR_TEMP_PORT" ]; then

	echo -n "Reading GPIO port $SENSOR_TEMP_PORT..."
	export DHT_OUT=`/Local/scripts/sensor_read_dht.py $SENSOR_TEMP_PORT | grep '='`
	echo "Done."
	echo "DHT_OUT = $DHT_OUT"

	export DHT_TEMP=`echo "$DHT_OUT" | grep -i Temp | cut -d '=' -f 2`
	export DHT_HUMID=`echo "$DHT_OUT" | grep -i Humid | cut -d '=' -f 2`

	OUT="$OUT sensor-temperature-value=$DHT_TEMP"
	OUT="$OUT sensor-humidity-value=$DHT_HUMID"
	OUT="$OUT sensor-temperature-gpio=$SENSOR_TEMP_PORT"
	OUT="$OUT sensor-temperature-description=$SENSOR_TEMP_DESC"
fi




echo

echo "OUT = $OUT"
export JO_OUT=`jo $OUT`
echo $JO_OUT
echo $JO_OUT | jq '.'
echo $JO_OUT | jq '.' > /tmp/session/device_report.json

