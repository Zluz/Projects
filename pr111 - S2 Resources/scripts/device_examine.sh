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

export CPU_TEMP=`/usr/bin/vcgencmd measure_temp`
export CPU_TEMP=`echo $CPU_TEMP | tr "'" '=' | cut -d '=' -f 2`
export OUT="$OUT cpu-temperature=$CPU_TEMP"

export OS_NAME=`/bin/uname -a | cut -d '#' -f 1 | awk '{$1=$1;print}' | tr ' ' '_'`
export OUT="$OUT os-name=$OS_NAME"

export OS_UPTIME=`/usr/bin/uptime -p | tr ' ' '_'`
export OUT="$OUT os-uptime=$OS_UPTIME"

export PID_PR101=`ps -ef | grep java | grep pr101_ | tr -s ' ' '-' | cut -d '-' -f 2`
if [ -n "$PID_PR101" ]; then
	export TIME_PR101=`ps -p $PID_PR101 -o etimes | tail -1 | awk '{$1=$1;print}'`
	export OUT="$OUT proc-pr101-pid=$PID_PR101"
	export OUT="$OUT proc-pr101-time=$TIME_PR101"
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
fi

export CPU_HARDWARE=`cat /proc/cpuinfo | grep -i Hardware | cut -d ':' -f 2 | awk '{$1=$1;print}' | tr ' ' '_'`
export OUT="$OUT cpu-hardware=$CPU_HARDWARE"
export CPU_CORES=`cat /proc/cpuinfo | grep "processor" | wc -l`
export OUT="$OUT cpu-cores=$CPU_CORES"
export CPU_MODEL=`cat /proc/cpuinfo | grep -i "model name" | head -1 | cut -d ':' -f 2 | awk '{$1=$1;print}' | tr ' ' '_'`
export OUT="$OUT cpu-model=${CPU_MODEL}"
export CPU_MIPS=`cat /proc/cpuinfo | grep -i "mips" | head -1 | cut -d ':' -f 2 | awk '{$1=$1;print}' | tr ' ' '_'`
export OUT="$OUT cpu-MIPS=$CPU_MIPS"


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

