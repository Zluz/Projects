

# see:
# http://wiringpi.com/the-gpio-utility/

# gpio mode 0 out
# gpio write 0 1

# gpio -g mode 17 out
# gpio -g write 17 1


# sleep 1

# see:
# http://www.raspberry-projects.com/pi/command-line/io-pins-command-line/io-pin-control-from-the-command-line

echo "17" > /sys/class/gpio/export
echo "out" > /sys/class/gpio/gpio17/direction

echo "1" > /sys/class/gpio/gpio17/value
sleep 1
# echo "0" > /sys/class/gpio/gpio17/value

cat /sys/class/gpio/gpio17/value

sleep 1
echo "17" > /sys/class/gpio/unexport

