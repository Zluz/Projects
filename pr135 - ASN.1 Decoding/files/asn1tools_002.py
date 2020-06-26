
import sys
import os
import asn1tools
import pickle
from array import array
# import json
import datetime


timeStart = datetime.datetime.now()

print( "Arguments: ", str( sys.argv ) )
# for arg in sys.argv:
#   print( "\targ: ", arg )

# read schema text, generate binary

# schemaLTE = asn1tools.compile_files( '3GPP RRC v15.3.0.asn', 'uper' )
# with open( 'Schema_LTE.pkl', 'wb' ) as output:
#   pickle.dump( schemaLTE, output, pickle.HIGHEST_PROTOCOL )


# load prepared schema binary

with open( 'Schema_LTE.pkl', 'rb' ) as input:
  pklLTE = pickle.load( input )


# parse data

# print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )

# Binary input files named "LTE_<id>.bin" will be processed.
# Output files will be created named "LTE_<id>__<keyword>.out".

iCount = 0

dir = r'.'
for entry in os.scandir( dir ):
  if ( entry.is_file() and entry.path.endswith( ".bin" ) ):
    print( "\nProcessing:     ", entry.path )
    iCount = iCount + 1
    
    size = os.path.getsize( entry.path )

    arrayInput = array( 'B' )
    
    with open( entry.path, 'rb' ) as f:
      arrayInput.fromfile( f, size )

      # print ( pklLTE.decode( 'BCCH-DL-SCH-Message', arrayInput ) )

      # iterate over input keywords
      for keyword in sys.argv:
        if not ".py" in keyword:

          # bytesJson = ''

          try:

            bytesJson = ''
          
            if "LTE_" in entry.path:
              bytesJson = pklLTE.decode( keyword, arrayInput )
            # TODO add other formats here

            strJson = str( bytesJson )
            
          except:
            # print( "Exception encoutered: ", sys.exc_info()[0] )
            strJson = ''


          if ( strJson ):

            # print( bytesJson )
          
            # print( bytearray( bytesJson ).decode() )
          
            filename = entry.path.rsplit('.bin')[0] + "__" + keyword + ".out"
            
            print( "Saving to file: ", filename )
          
            file_out = open( filename, 'w' )
            file_out.write( strJson )
            file_out.close()


timeFinished = datetime.datetime.now()
timeElapsed = timeFinished - timeStart
iElapsedMS = int( timeElapsed.total_seconds() * 1000 )

print( "\n", iCount, "file(s) processed in", iElapsedMS, "milliseconds." )
