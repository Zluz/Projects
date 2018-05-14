
LINE="alias dir='ls -alF'"
grep -q -F "alias dir" /root/.bashrc || echo "$LINE" >> /root/.bashrc
grep -q -F "alias dir" /home/pi/.bashrc || echo "$LINE" >> /home/pi/.bashrc

# rsync -vcI /Local/scripts/* /Share/Resources/scripts/
rsync -vcI /Share/Resources/fonts/truetype/droid/* /usr/share/fonts/truetype/droid/


# edit swap

FILE=/etc/dphys-swapfile
CUR_SWAP=`cat $FILE | grep CONF_SWAPSIZE`
if [[ "$CUR_SWAP" == "CONF_SWAPSIZE=100" ]]
then
    echo "Updating $FILE (swap file size).."
    mv /etc/dphys-swapfile /etc/dphys-swapfile.orig
    sed '/^CONF_SWAPSIZE=/{h;s/=.*/=2048/};${x;/^$/{s//CONF_SWAPSIZE=2048/;H};x}' /etc/dphys-swapfile.orig > $FILE
fi


# edit /boot/cmdline.txt 

FILE=/boot/cmdline.txt
LINE=`cat $FILE | grep logo.nologo`
if [[ "$LINE" == "" ]]
then
    echo "Updating $FILE (startup options).."
    LINE=`cat $FILE`
    mv $FILE /boot/cmdline_orig.txt
    echo "$LINE logo.nologo" > $FILE
fi





# turn off camera led indicator

FILE=/boot/config.txt
if  grep -q -F 'disable_camera_led=1' "$FILE";  then
    echo "$FILE already modified."
else
    echo "Updating $FILE (disabling camera LED).."
    echo "" >> $FILE
    echo "# Disabling integrated camera LED indicator" >> $FILE
    echo "disable_camera_led=1" >> $FILE
    echo "" >> $FILE
fi





cp /Local/config/*.desktop /home/pi/Desktop
cp /Local/config/*.desktop /home/pi/.local/share/applications
cp /Local/config/panel /home/pi/.config/lxpanel/LXDE-pi/panels


# install additional packages
# echo "Installing additional packages.."
# apt-get install -y conky xdiskusage lsof gparted memtester ethtool
# apt-get install -y pimoroni 
# echo " "


# configure conky

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





# disable screensaver
#  see: http://www.etcwiki.org/wiki/Disable_screensaver_and_screen_blanking_Raspberry_Pi

FILE=/etc/xdg/lxsession/LXDE-pi/autostart
if  grep -q -F '@xset s noblank' "$FILE";  then
    echo "$FILE already modified."
else
    echo "@xset s noblank" >> $FILE
    echo "@xset s off" >> $FILE
    echo "@xset -dpms" >> $FILE
fi



# now setup wifi

FILE=/etc/wpa_supplicant/wpa_supplicant.conf
if  grep -q -F "PNYZ6" "$FILE";  then
    echo "$FILE already modified."
else
    echo "network={" >> $FILE
    echo "  ssid=\"PNYZ6\"" >> $FILE
    echo "  psk=30400704366a929ca5e973da58738185859fe3293135025e19b79809a57b0b51" >> $FILE
    echo "}" >> $FILE
fi

wpa_cli -i wlan0 reconfigure

