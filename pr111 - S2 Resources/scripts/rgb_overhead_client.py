# -*- coding: utf-8 -*-

import time
import subprocess
import digitalio
import board
import requests
from PIL import Image, ImageDraw, ImageFont
import adafruit_rgb_display.st7789 as st7789
import io


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

font = ImageFont.truetype( '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', 24 )

# Turn on the backlight
backlight = digitalio.DigitalInOut(board.D22)
backlight.switch_to_output()
backlight.value = True
# backlight.value = False



# show startup screen
if ( True ):

        image = Image.new('RGB', (width, height))
        draw = ImageDraw.Draw( image )

        strMessage = 'Starting up..\n    Attempting to\n    connect..'
        draw.text( ( 0, 0 ), strMessage, font=font, fill="#F0F0F0" )

        cmd = "hostname -I | cut -d\' \' -f1"
        strIP = subprocess.check_output( cmd, shell=True ).decode( "utf-8" )
        draw.text( ( 0, 90 ), "IP: " + strIP, font=font, fill="#90FF90" )

        # Display image.
        disp.image( image, rotation )



# set up input buttons
btnTop    = digitalio.DigitalInOut( board.D23 )
btnBottom = digitalio.DigitalInOut( board.D24 )

strParams = ""


# loop, showing screen from server
while True:
# if ( True ):

        cmd = "hostname -I | cut -d\' \' -f1 | cut -d\'.\' -f3,4"
        strIP = subprocess.check_output( cmd, shell=True ).decode( "utf-8" )
        strSub = strIP[0]

        if ( "6" == strSub ):
                strURL = 'http://192.168.6.231:1080/overhead?' + strParams;
        if ( "7" == strSub ):
                strURL = 'http://192.168.7.230:1080/overhead?' + strParams;


        # Draw a black filled box to clear the image.
        # draw.rectangle((0, 0, width, height), outline=0, fill=0)

        try:
                response = requests.get( strURL, stream = True )

                memfile = io.BytesIO( response.content )

                image = Image.open( memfile )
                draw = ImageDraw.Draw( image )

        # except requests.exceptions.RequestException as e:
        except Exception as e:
                # print( e )

                image = Image.new('RGB', (width, height))
                draw = ImageDraw.Draw( image )

                strMessage = 'Failed to retrieve\nimage from server'
                draw.text( ( 0, 0 ), e, font=font, fill="#FF9090" )
                draw.text( ( 0, 30 ), strMessage, font=font, fill="#FF9090" )


        draw.text( ( 170, 110 ), strIP, font=font, fill="#909090" )

        strParams = "?"
        if ( not btnTop.value ):
                draw.text( ( 180, 80 ), "A", font=font, fill="#FFFFFF" )
                strParams = strParams + "a=1&"
        if ( not btnBottom.value ):
                draw.text( ( 210, 80 ), "B", font=font, fill="#FFFFFF" )
                strParams = strParams + "b=1&"
        strParams = strParams + "c=0"


        # Display image.
        disp.image( image, rotation )
        time.sleep( 0.1 )



