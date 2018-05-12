
apt-get purge -y wolfram-engine
apt-get purge -y libreoffice*
apt-get purge -y minecraft-pi sonic-pi 

apt-get clean
apt-get autoremove 

apt-get -y upgrade
apt-get update

apt-get install -y conky xdiskusage lsof gparted memtester ethtool
apt-get install -y pimoroni 

