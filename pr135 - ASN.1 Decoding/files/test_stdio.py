
import sys
import os
import datetime
import fileinput


print( "Arguments: ", str( sys.argv ), flush=True )
print( "Enter 'exit' to terminate.", flush=True )

timeStart = datetime.datetime.now()

# bLoop = 1
# while bLoop:
#   
#   # do stuff
#   
#   timeNow = datetime.datetime.now()
#   timeElapsed = timeNow - timeStart
#   iElapsedMS = timeElapsed.total_seconds()
#   bLoop = iElapsedMS < 10

for strLine in fileinput.input():
  strLine = strLine.rstrip()

  print( "input: ", strLine, flush=True )
  
  if ( "exit" == strLine ):
    sys.exit( 0 )

