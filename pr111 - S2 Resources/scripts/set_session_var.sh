#!/bin/bash

dirParent="/Share/Sessions"
if [ -d "$dirParent" ]
then
	echo "dir found"
else
	mount -a

	if [ -d "$dirParent" ]
	then
		echo "dir still not found after mount -a"
	else
		echo "Directory ($dirParent) not found, even after 'mount -a'. Aborting."
		exit # no good, quit
	fi
fi

echo "Directory found."

#export ifconfig=`/sbin/ifconfig`
#echo "ifconfig:"
#echo "$ifconfig"

export mac=`/sbin/ifconfig | grep eth0 | awk '{print toupper($5)}' | sed 's/:/-/g'`
echo "MAC Address: $mac"

# dirSession="/Share/Sessions/$mac"
export dirSession="/Share/Sessions/$mac"

mkdir -p $dirSession

