#!/usr/bin/env python

import Adafruit_DHT as dht
import time
import sys

p = sys.argv[1]
print 'GPIO port=', p

sys.stdout.write( 'Reading...' )
h,t = dht.read_retry( dht.DHT11, p )
sys.stdout.write( 'Done.\n' )

print 'Temperature (C)={0:0.1f}'.format( t, h )
print 'Humidity (%)={1:0.1f}'.format( t, h )

