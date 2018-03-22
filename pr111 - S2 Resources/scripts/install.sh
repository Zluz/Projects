
cp /Local/config/*.desktop /home/pi/Desktop
cp /Local/config/*.desktop /home/pi/.local/share/applications
cp /Local/config/panel /home/pi/.config/lxpanel/LXDE-pi/panels

rm -f /etc/conky/conky.conf
ln -s /Local/config/conky.conf /etc/conky/conky.conf

HAS_CONKY=`cat /home/pi/.config/lxsession/LXDE-pi/autostart | grep conky`
if [[ "$HAS_CONKY" == "" ]]
then
	echo "@/Local/scripts/conky.sh" >> /home/pi/.config/lxsession/LXDE-pi/autostart
fi

# disable this for now. just check with the cron job.
rm -f /etc/network/if-up.d/STARTUP
# cp /Local/config/STARTUP /etc/network/if-up.d/
# xxecho "/bin/bash /Local/config/STARTUP > /tmp/beacon.log" > /etc/network/if-up.d/run_beacon.sh

# rm -f /etc/network/if-up.d/run_post_event.sh
# echo "/bin/bash /Local/scripts/run_PostSystemEvent.sh DEVICE_STARTED" > /etc/network/if-up.d/run_post_event.sh

# chmod a+x /etc/network/if-up.d/STARTUP

# sleep 1
lxpanelctl restart

