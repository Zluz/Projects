#!/bin/bash

export SP="_SP_"

export SENSOR_TEMP_PORT=`jq '."sensor-temperature-gpio"' < /tmp/session/device_config.json`
export SENSOR_TEMP_DESC=`jq '."sensor-temperature-description"' < /tmp/session/device_config.json`

# not sure why jo complains about spaces here..
#export SENSOR_TEMP_DESC=`echo $SENSOR_TEMP_DESC | tr '"' "'"`
export SENSOR_TEMP_DESC=`echo $SENSOR_TEMP_DESC | tr ' ' '$SP'` 

export NOW=$(date +"%Y%m%d_%H%M%S")
export TIME_ISO=$(date "+%Y-%m-%dT%H:%M:%S")

export OUT="time-label=$NOW "
export OUT="$OUT time-iso=$TIME_ISO"

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
export OUT="$OUT soc-temperature=$CPU_TEMP"

export CPU_VOLTS=`/usr/bin/vcgencmd measure_volts core`
export CPU_VOLTS=`echo $CPU_VOLTS | tr "V" '=' | cut -d '=' -f 2`
export OUT="$OUT core-volts=$CPU_VOLTS"


export OS_NAME=`/bin/uname -a | cut -d '#' -f 1 | awk '{$1=$1;print}' | tr ' ' '$SP'`
export OUT="$OUT os-name=$OS_NAME"

export OS_UPTIME=`/usr/bin/uptime -p | tr ' ' '$SP'`
export OUT="$OUT os-uptime=$OS_UPTIME"

export VIDEO_NAME=`/usr/bin/tvservice -n 2> /dev/null | cut -d '=' -f 2 | tr ' ' '$SP'` 
if [ "$VIDEO_NAME" == "null" ]; then
	: # probably no camera
elif [ "$VIDEO_NAME" == "" ]; then
	: # probably no camera
else
	export OUT="$OUT video_display_name=$VIDEO_NAME"
	export VIDEO_STATUS=`/usr/bin/tvservice -s | tr ' ' '$SP'`
	export OUT="$OUT video_status=$VIDEO_STATUS"
fi

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

export CPU_HARDWARE=`cat /proc/cpuinfo | grep -i Hardware | cut -d ':' -f 2 | awk '{$1=$1;print}' | tr ' ' '$SP'`
export OUT="$OUT cpu-hardware=$CPU_HARDWARE"
export CPU_CORES=`cat /proc/cpuinfo | grep "processor" | wc -l`
export OUT="$OUT cpu-cores=$CPU_CORES"
export CPU_MODEL=`cat /proc/cpuinfo | grep -i "model name" | head -1 | cut -d ':' -f 2 | awk '{$1=$1;print}' | tr ' ' '$SP'`
export OUT="$OUT cpu-model=${CPU_MODEL}"
export CPU_MIPS=`cat /proc/cpuinfo | grep -i "mips" | head -1 | cut -d ':' -f 2 | awk '{$1=$1;print}' | tr ' ' '$SP'`
export OUT="$OUT cpu-MIPS=$CPU_MIPS"

export NET_MAC=`/sbin/ifconfig eth0 2> /dev/null | grep ether | awk '{print toupper($2)}' | sed 's/:/_/g'`
if [ "$NET_MAC" == "" ]; then
        export NET_MAC=`/sbin/ifconfig wlan0 | grep ether | awk '{print toupper($2)}' | sed 's/:/_/g'`
fi

export OUT="$OUT network-mac=$NET_MAC"
export NET_IP=`ip a | grep 192.168 | sort --key=1.40 | head -1 | awk '{print $2;}' | cut -d '/' -f 1`
export OUT="$OUT network-ip=$NET_IP"


export FILE_CONFIG=/tmp/session/device_config.json
export CONFIG_DESC=`jq '.description' $FILE_CONFIG | tr -s ' ' '$SP'`
if [ "$CONFIG_DESC" == "" ]; then
	: # missing config
elif [ "$CONFIG_DESC" == "null" ]; then
	: # missing/invlid config
else
	OUT="$OUT config-description=$CONFIG_DESC"
fi



# if [ -n $SENSOR_TEMP_PORT ]; then
if [ "$SENSOR_TEMP_PORT" == "" ]; then
	: # no sensor
elif [ "$SENSOR_TEMP_PORT" == "null" ]; then
	: # no sensor
else
	echo -n "Reading GPIO port $SENSOR_TEMP_PORT..."
	export DHT_OUT=`/Local/scripts/sensor_read_dht.py $SENSOR_TEMP_PORT | grep '='`
	echo "Done."
	# echo "DHT_OUT = $DHT_OUT"

	export DHT_TEMP=`echo "$DHT_OUT" | grep -i Temp | cut -d '=' -f 2`
	export DHT_HUMID=`echo "$DHT_OUT" | grep -i Humid | cut -d '=' -f 2`

	OUT="$OUT sensor-temperature-value=$DHT_TEMP"
	OUT="$OUT sensor-humidity-value=$DHT_HUMID"
	OUT="$OUT sensor-temperature-gpio=$SENSOR_TEMP_PORT"
	OUT="$OUT sensor-temperature-description=$SENSOR_TEMP_DESC"
fi

echo



export FILE_JSON=/tmp/session/device_report.json

export JO_OUT=`jo $OUT`
echo $JO_OUT | jq '.' | tr -s '$SP' ' '  > $FILE_JSON


echo Final JSON saved to $FILE_JSON
cat $FILE_JSON

echo
echo POSTing to the 'status-node' index...
curl -X POST 'http://192.168.6.20:9200/status-node/_doc/' -H 'Content-Type: application/json' -d @$FILE_JSON > /dev/null

echo

