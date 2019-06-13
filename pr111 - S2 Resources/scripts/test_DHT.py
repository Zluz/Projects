#!/usr/bin/env python

#        _ _
#   +3V [+|_] +5V
#       (_|+] +5V
#       (_|_] GND
#GPIO04-[s|_)
#   GND [g|_)
#       (_|_)
#       (...)

import Adafruit_DHT as dht
import time

for x in range( 0, 100 ):
  h,t = dht.read_retry(dht.DHT11, 4)
  print 'Temp={0:0.1f}*C  Humidity={1:0.1f}%'.format(t, h)
  time.sleep(2)

