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

export mac_old=`/sbin/ifconfig | grep eth0 | awk '{print toupper($5)}' | sed 's/:/-/g'`
export mac_new=`/sbin/ifconfig eth0 | grep ether | awk '{print toupper($2)}' | sed 's/:/-/g'`
export mac=`echo $mac_old $mac_new`
echo "MAC Address: $mac"

dirSession="/Share/Sessions/$mac"

mkdir -p $dirSession

/sbin/ifconfig > $dirSession/ifconfig.out
uname -a > $dirSession/uname.out

ln -s $dirSession /tmp/session
#--- take a screenshot, upload
# these do not seem to work
#/usr/bin/scrot -z $dirSession/screenshot.png
#/usr/bin/scrot -z -u $dirSession/screenshot-focused.png
#DISPLAY=:0 /usr/bin/scrot -z /tmp/screenshot-root.png
# only the pi user works, run cron for scrot there, copy to share here
cp /tmp/screenshot.png $dirSession/screenshot.png

#rsync -uv --checksum --ignore-times $dirSession/config.txt /boot/config.txt 
#rsync -vcI $dirSession/config.txt /boot/config.txt 
rsync -vcI $dirSession/conky-device_info.txt /tmp

