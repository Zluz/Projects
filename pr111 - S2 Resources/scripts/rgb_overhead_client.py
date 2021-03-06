# -*- coding: utf-8 -*-

import time
import subprocess
import digitalio
import board
import requests
from PIL import Image, ImageDraw, ImageFont
import adafruit_rgb_display.st7789 as st7789
import io


print()
print( "Starting overhead display script.." )


print( "Initializing I/O.." )

# Configuration for CS and DC pins (these are FeatherWing defaults on M0/M4):
cs_pin = digitalio.DigitalInOut(board.CE0)
dc_pin = digitalio.DigitalInOut(board.D25)
reset_pin = None

# Config for display baudrate (default max is 24mhz):
BAUDRATE = 64000000

# Setup SPI bus using hardware SPI:
spi = board.SPI()


print( "Configuring session.." )

strCmd = "/sbin/ifconfig | /bin/grep ether"
strMAC = subprocess.check_output( strCmd, shell=True ).decode( "utf-8" )
strMAC = strMAC.strip()

strCmd = "/bin/hostname -I | cut -d\' \' -f1 | cut -d\'.\' -f3,4"
strIPShort = subprocess.check_output( strCmd, shell=True ).decode( "utf-8" )
strIPShort = strIPShort.strip()
strSub = strIPShort[0]

print( "Test MAC string: " + strMAC )
print( "Test IP (short) string: " + strIPShort )

if ( "6" == strSub ):
        # strURL = 'http://192.168.6.231:1080/overhead'
        strURL = 'http://192.168.6.211:1080/overhead'
if ( "7" == strSub ):
        strURL = 'http://192.168.7.230:1080/overhead'


# select one of these:

bMicroTFT = False
bMiniTFT = False
bSimulatedMicroTFT = False

if ( strMAC.find( "1a:00:46" ) > -1 ):
	print( "Device: Test RPi3" )

	bSimulatedMicroTFT = True


if ( strMAC.find( "72:07:ce" ) > -1 ):
	print( "Device: (RPiZ) Overhead Panel" )

	bMicroTFT = True
	if ( "6" == strSub ):
	        strURL = 'http://192.168.6.231:1080/overhead'
	if ( "7" == strSub ):
	        strURL = 'http://192.168.7.230:1080/overhead'

	# override; point to dev machine regardless
	# strURL = 'http://192.168.6.211:1080/overhead'


print( "Host URL: " + strURL )


# Create the ST7789 display


if ( bMicroTFT ):
	disp = st7789.ST7789( spi, cs=cs_pin, dc=dc_pin, rst=reset_pin, baudrate=BAUDRATE,
		width=135, height=240, x_offset=53, y_offset=40 )
	rotation = 90

elif ( bMiniTFT ):
	disp = st7789.ST7789( spi, cs=cs_pin, dc=dc_pin, rst=reset_pin, baudrate=BAUDRATE,
		width=240, height=240, x_offset=0, y_offset=80 )
	rotation = 180

elif ( bSimulatedMicroTFT ):
	disp = st7789.ST7789( spi, cs=cs_pin, dc=dc_pin, rst=reset_pin, baudrate=BAUDRATE,
		width=240, height=240, x_offset=0, y_offset=80 )
	rotation = 180

else:
	print( "WARNING: Display not resolved." )



height = disp.width
width = disp.height


# Create blank image for drawing.
# Make sure to create image with mode 'RGB' for full color.
# height = disp.width   # we swap height/width to rotate it to landscape!
# width = disp.height
image = Image.new( 'RGB', (width, height) )
# rotation = 90
# rotation = 180

# Get drawing object to draw on image.
draw = ImageDraw.Draw(image)

# Draw a black filled box to clear the image.
draw.rectangle( (0, 0, width, height), outline=0, fill=(0, 0, 0) )
disp.image( image, rotation )

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


print( "Showing startup screen.." )

# show startup screen
if ( True ):

        image = Image.new('RGB', (width, height))
        draw = ImageDraw.Draw( image )

        strMessage = 'Starting up..\n    Attempting to\n    connect..'
        draw.text( ( 0, 0 ), strMessage, font=font, fill="#F0F0F0" )

        cmd = "hostname -I | cut -d\' \' -f1"
        strIPFull = subprocess.check_output( cmd, shell=True ).decode( "utf-8" )
        draw.text( ( 0, 90 ), "IP: " + strIPFull, font=font, fill="#90FF90" )

        # Display image.
        disp.image( image, rotation )



# set up input buttons
btnTop    = digitalio.DigitalInOut( board.D23 )
btnBottom = digitalio.DigitalInOut( board.D24 )

strParams = ""


print( "Running main loop.." )

# strLastKey = "none"
iTicker = 0;
strTickColor = "#A0A0FF"
# bLastUpdated = True


# loop, showing screen from server
while True:
# if ( True ):


        # Draw a black filled box to clear the image.
        # draw.rectangle((0, 0, width, height), outline=0, fill=0)

        try:
                # if ( not bLastUpdated ):
                #         resKey = requests.get( strURL + "/key?" + strParams, stream = True )
                #         strKey = resKey.content.decode( 'utf-8' );
                # else:
                #         strKey = iTicker

                # if ( strKey != strLastKey ):
                if ( True ):  # seems no faster .. just pull & paint every time

                        response = requests.get( strURL + "/image?" + strParams, stream = True )
                        memfile = io.BytesIO( response.content )

                        image = Image.open( memfile )
                        draw = ImageDraw.Draw( image )

                        # strKey = response.headers[ "ImageKey" ]
                        bLastUpdated = True

                        # strTickColor = "#FFFFB0"
                        strTickColor = "#A0A0FF"
                # else:
                #         bLastUpdated = False
                #         strTickColor = "#A0A0FF"

                # strLastKey = strKey

        # except requests.exceptions.RequestException as e:
        except Exception as e:
                # print( e )

                image = Image.new('RGB', (width, height))
                draw = ImageDraw.Draw( image )

                strMessage = 'Failed to retrieve\nimage from server'
                draw.text( ( 0, 0 ), e, font=font, fill="#FF9090" )
                draw.text( ( 0, 30 ), strMessage, font=font, fill="#FF9090" )


        draw.rectangle( (  170, 80, 240, 135 ), outline=1, fill=0 )


        draw.text( ( 170, 110 ), strIPShort, font=font, fill="#909090" )
        # draw.text( ( 170, 110 ), strIP, font=font, fill="#FFFFFF" )

        # draw.text( ( 160, 80 ), strLastKey, font=fontS, fill="#909090" )


        strParams = ""
        if ( not btnTop.value ):
                draw.text( ( 180, 80 ), "A", font=font, fill="#FFFFFF" )
                strParams = strParams + "a=1&"
        if ( not btnBottom.value ):
                draw.text( ( 210, 80 ), "B", font=font, fill="#FFFFFF" )
                strParams = strParams + "b=1&"
        # strParams = strParams + "c=0"

        if ( "" == strParams ):
                strCmd = "/usr/bin/vcgencmd get_throttled | cut -d'x' -f2"
                strThrottle = subprocess.check_output( strCmd, shell=True ).decode( "utf-8" )
                strThrottle = strThrottle.strip()
                if ( "0" == strThrottle ):
                        draw.text( ( 180, 80 ), "ok", font=font, fill="#808080" )
                if ( not "0" == strThrottle ):
                        draw.text( ( 170, 80 ), strThrottle, font=font, fill="#FFE0E0" )


        iY = 104
        draw.rectangle( (  170 + iTicker, iY, 
                           176 + iTicker, iY + 6 ), outline=1, fill=strTickColor )

        # Display image.
        disp.image( image, rotation )

        iTicker = iTicker + 2
        if ( 50 == iTicker ):
                iTicker = 0

        time.sleep( 0.01 )

