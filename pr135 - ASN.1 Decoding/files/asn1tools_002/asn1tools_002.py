
import sys
import os
import asn1tools
import pickle
from array import array
# import json


print( "Arguments: ", str( sys.argv ) )
for arg in sys.argv:
  print( "\targ: ", arg )

# read schema text, generate binary

# schemaLTE = asn1tools.compile_files( '3GPP RRC v15.3.0.asn', 'uper' )
# with open( 'Schema_LTE.pkl', 'wb' ) as output:
#   pickle.dump( schemaLTE, output, pickle.HIGHEST_PROTOCOL )


# load prepared schema binary

with open( 'Schema_LTE.pkl', 'rb' ) as input:
  pklLTE = pickle.load( input )


# parse data

# print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )

# Binary input files named "LTE_*.bin" will be processed.
# Output files will be created named "LTE_*.out".


dir = r'.'
for entry in os.scandir( dir ):
  if ( entry.is_file() and entry.path.endswith( ".bin" ) ):
    print( "\nProcessing: ", entry.path )
    
    size = os.path.getsize( entry.path )

    arrayInput = array( 'B' )
    
    with open( entry.path, 'rb' ) as f:
      arrayInput.fromfile( f, size )

      # print ( pklLTE.decode( 'BCCH-DL-SCH-Message', arrayInput ) )

      # iterate over input keywords
      for keyword in sys.argv:
        if not ".py" in keyword:

          if "LTE_" in entry.path:
            bytesJson = pklLTE.decode( keyword, arrayInput )

          print( bytesJson )
          
          # print( bytearray( bytesJson ).decode() )
          
          strJson = str( bytesJson )
          
          filename = entry.path.rsplit('.bin')[0] + ".out"
          print( filename )
          
          # file_out = open( filename, 'wt' )
          # file_out.write( json )
          # file_out.close()

#          with open( filename, 'w' ) as outfile:
#            json.dump( [ bytesJson ], outfile )

          file_out = open( filename, 'w' )
#          file_out = open( filename, 'w+b' )
#          binary = bytearray( bytesJson )
          file_out.write( strJson )
#          pickle.dump( file_out, bytesJson )
          file_out.close()

#          with open( filename, 'wb' ) as outfile:
#            pickle.dump( bytesJson, outfile )
          
