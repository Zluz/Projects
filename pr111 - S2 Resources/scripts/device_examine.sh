
export SENSOR_TEMP_PORT=`jq '."sensor-temperature-gpio"' < /tmp/session/device_config.json`
export SENSOR_TEMP_DESC=`jq '."sensor-temperature-description"' < /tmp/session/device_config.json`

export OUT="name=device_examine "

export THROTTLED=`/usr/bin/vcgencmd get_throttled`
export THROTTLED=`echo $THROTTLED | cut -d '=' -f 2`
export OUT="$OUT cpu-throttle=\"$THROTTLED\""

export CPU_TEMP=`/usr/bin/vcgencmd measure_temp`
export CPU_TEMP=`echo $CPU_TEMP | tr "'" '=' | cut -d '=' -f 2`
export OUT="$OUT cpu-temp=$CPU_TEMP"


if [ ! SENSOR_TEMP_PORT == "" ]; then
	# see test_DHT.py
	OUT="$OUT sensor-temperature-value=WIP"
	OUT="$OUT sensor-temperature-gpio=$SENSOR_TEMP_PORT"
fi

echo

echo "OUT = $OUT"
export JO_OUT=`jo $OUT`
echo $JO_OUT
echo $JO_OUT | jq '.'
echo $JO_OUT | jq '.' > /tmp/session/device_report.json

