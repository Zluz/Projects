#!/bin/bash

RECENT_FILES=$( /usr/bin/find /tmp -mmin -30 | grep cap | wc -l )
if [[ "$RECENT_FILES" == "0" ]]
then
	reboot now
fi

