#!/bin/bash

if [ -f "/opt/vc/bin/vcgencmd" ]; 
then 
	while :; do 
		echo "{\"temp\":\"`/opt/vc/bin/vcgencmd measure_temp`\",\"core\":\"`/opt/vc/bin/vcgencmd measure_volts core`\",\"sdram_c\":\"`/opt/vc/bin/vcgencmd measure_volts sdram_c`\",\"sdram_i\":\"`/opt/vc/bin/vcgencmd measure_volts sdram_i`\",\"sdram_p\":\"`/opt/vc/bin/vcgencmd measure_volts sdram_p`\"}"; 
		sleep 0.2; 
	done; 
fi

