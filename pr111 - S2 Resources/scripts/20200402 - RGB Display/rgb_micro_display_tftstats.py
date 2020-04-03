# -*- coding: utf-8 -*-

import time
import subprocess
import digitalio
import board
from PIL import Image, ImageDraw, ImageFont
import adafruit_rgb_display.st7789 as st7789


# Configuration for CS and DC pins (these are FeatherWing defaults on M0/M4):
cs_pin = digitalio.DigitalInOut(board.CE0)
dc_pin = digitalio.DigitalInOut(board.D25)
reset_pin = None

# Config for display baudrate (default max is 24mhz):
BAUDRATE = 64000000

# Setup SPI bus using hardware SPI:
spi = board.SPI()

# Create the ST7789 display:
# disp = st7789.ST7789(spi, cs=cs_pin, dc=dc_pin, rst=reset_pin, baudrate=BAUDRATE,
#                      width=135, height=240, x_offset=53, y_offset=40)
disp = st7789.ST7789( spi, cs=cs_pin, dc=dc_pin, rst=reset_pin, baudrate=BAUDRATE,
                     #width=240, height=120, x_offset=0, y_offset=0 )
# works for micro    width=120, height=240, x_offset=50, y_offset=40 )
                     width=135, height=240, 
			x_offset=53, #          48 < 49..53 < 54
			y_offset=40  #   (bad) 38 < (good) 40..44 < 46 (bad) 
		)

# Create blank image for drawing.
# Make sure to create image with mode 'RGB' for full color.
height = disp.width   # we swap height/width to rotate it to landscape!
width = disp.height
image = Image.new('RGB', (width, height))
rotation = 90
# rotation = 180

# Get drawing object to draw on image.
draw = ImageDraw.Draw(image)

# Draw a black filled box to clear the image.
draw.rectangle((0, 0, width, height), outline=0, fill=(0, 0, 0))
disp.image(image, rotation)
# Draw some shapes.
# First define some constants to allow easy resizing of shapes.
padding = -2
top = padding
bottom = height-padding
# Move left to right keeping track of the current x position for drawing shapes.
x = 0


# Alternatively load a TTF font.  Make sure the .ttf font file is in the
# same directory as the python script!
# Some other nice fonts to try: http://www.dafont.com/bitmap.php
# font = ImageFont.truetype( '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', 24 )
fontL = ImageFont.truetype( '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', 24 )
fontS = ImageFont.truetype( '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', 18 )
fontT = ImageFont.truetype( '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', 14 )

# Turn on the backlight
backlight = digitalio.DigitalInOut(board.D22)
backlight.switch_to_output()
backlight.value = True
# backlight.value = False

while True:
    # Draw a black filled box to clear the image.
    draw.rectangle( (0, 0, width, height), outline=0, fill=0 )
    # draw.rectangle( (0, 0, width, height), outline=0, fill="#202040" )

    # Shell scripts for system monitoring from here:
    # https://unix.stackexchange.com/questions/119126/command-to-display-memory-usage-disk-usage-and-cpu-load
    cmd = "hostname -I | cut -d\' \' -f1"
    IP = "IP: "+subprocess.check_output(cmd, shell=True).decode("utf-8")
    cmd = "top -bn1 | grep load | awk '{printf \"CPU: %.2f %%\", $(NF-2)}'"
    CPU = subprocess.check_output(cmd, shell=True).decode("utf-8")
    # cmd = "free -m | awk 'NR==2{printf \"Mem: %s/%s MB  %.2f%%\", $3,$2,$3*100/$2 }'"
    # cmd = "free -m | awk 'NR==2{printf \"Mem: %s/%s MB\", $3,$2 }'"
    cmd = "free -m | awk 'NR==2{printf \"Mem: %.2f%%\", $3*100/$2 }'"
    MemUsage = subprocess.check_output(cmd, shell=True).decode("utf-8")
    # cmd = "df -h | awk '$NF==\"/\"{printf \"Disk: %d/%d GB  %s\", $3,$2,$5}'"
    cmd = "df -h | awk '$NF==\"/\"{printf \"Disk: %s\", $3,$2,$5}'"
    Disk = subprocess.check_output(cmd, shell=True).decode("utf-8")
    cmd = "cat /sys/class/thermal/thermal_zone0/temp |  awk \'{printf \"Temp: %.1f C\", $(NF-0) / 1000}\'" # pylint: disable=line-too-long
    Temp = subprocess.check_output(cmd, shell=True).decode("utf-8")

    cmd = "date +' %T'"
    strTime = subprocess.check_output( cmd, shell=True ).decode("utf-8")

    cmd = "iwlist wlan0 s last | egrep 'ESSID:\".+\"' | cut -d':' -f2 | tr -d '\"' | sort | uniq"
    strNetworks = subprocess.check_output( cmd, shell=True ).decode( "utf-8" )


    # Write four lines of text.
    y = top
    draw.text((x, y), IP, font=fontL, fill="#F0F0F0")
    y += fontL.getsize(IP)[1]
    y += 5

    iBY = y

    draw.text((x, y), CPU, font=fontS, fill="#FFFF00")
    y += fontS.getsize(CPU)[1]

    draw.text((x, y), Temp, font=fontS, fill="#FF60FF")
    y += fontS.getsize(Temp)[1]

    draw.text((x, y), MemUsage, font=fontS, fill="#00FF00")
    y += fontS.getsize(MemUsage)[1]

    draw.text((x, y), Disk, font=fontS, fill="#6060FF")
    y += fontS.getsize(Disk)[1]

    # draw.text((x, y), Temp, font=fontS, fill="#FF60FF")
    # y += fontS.getsize(Temp)[1]

    y += 5

    draw.text( (x, y), strTime, font=fontL, fill="#808080")

    y = iBY
    x = 134
    draw.text( (x, y), strNetworks, font=fontT, fill="#DDDDDD" )


    x = 0


    # Display image.
    disp.image(image, rotation)
    time.sleep(.1)



