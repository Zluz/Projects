""" Display magnetometer data once per second """

import time
import board
import busio
import adafruit_lis3mdl
import math


i2c = busio.I2C(board.SCL, board.SDA)
sensor = adafruit_lis3mdl.LIS3MDL(i2c)
mag_x, mag_y, mag_z = 0, 0, 0

while True:
    old_x = mag_x
    old_y = mag_y
    old_z = mag_z
    mag_x, mag_y, mag_z = sensor.magnetic

    dif_x = mag_x - old_x
    dif_y = mag_y - old_y
    dif_z = mag_z - old_z

    diff = math.sqrt( dif_x * dif_x + dif_y * dif_y + dif_z * dif_z )

    print( 'X:{0:10.2f}, Y:{1:10.2f}, Z:{2:10.2f} uT   diff:{3:4.5}'
            .format( mag_x, mag_y, mag_z, diff ) )

    time.sleep( 0.2 )

