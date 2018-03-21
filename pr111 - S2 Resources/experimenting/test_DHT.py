#!/usr/bin/env python

import Adafruit_DHT as dht
import time

for x in range( 0, 100 ):
  h,t = dht.read_retry(dht.DHT11, 5)
  print 'Temp={0:0.1f}*C  Humidity={1:0.1f}%'.format(t, h)
  time.sleep(2)

