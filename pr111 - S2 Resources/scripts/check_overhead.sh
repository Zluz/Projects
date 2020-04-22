#!/bin/bash

OH_SCRIPT_RUNNING=`ps -ef | grep rgb_overhead_client | wc -l`

if [[ "$OH_SCRIPT_RUNNING" == "2" ]]; then
        echo "Overhead script appears to be running."
elif [[ "$OH_SCRIPT_RUNNING" == "1" ]]; then
        echo "Overhead script not running. Restarting.."
        python3 /Local/scripts/rgb_overhead_client.py  >  /tmp/rgb_overhead_client.out  2>&1  &
else
	echo "Unknown check state: $OH_SCRIPT_RUNNING"
fi


