
import asn1tools
import pickle
import sys
import os
import datetime
import fileinput

# This script is derived from asn1tools_002.py and asn1tools_003.py

timeStart = datetime.datetime.now()

print( "# compile_schema.py - Compile 'pk4' binary files from 'asn' schema files.", flush=True )

# schemaLTE = asn1tools.compile_files( '3GPP RRC v15.3.0.asn', 'uper' )
# with open( 'Schema_LTE.pkl', 'wb' ) as output:
#   pickle.dump( schemaLTE, output, pickle.HIGHEST_PROTOCOL )

# with open( 'Schema_LTE.pkl', 'rb' ) as input:
#    pklLTE = pickle.load( input )


iCount = 0

dir = r'.'
for entry in os.scandir( dir ):
  if ( entry.is_file() and entry.path.endswith( ".asn" ) ):
    print( "Processing:", entry.path, end=' ... ', flush=True )
    iCount = iCount + 1
    
    size = os.path.getsize( entry.path )

    filenameShort = entry.path.rsplit('.asn')[0];


    schemaUPER = asn1tools.compile_files( entry.path, 'uper' )
    
    filenameUPER = filenameShort + "_UPER.pk4"

    print( "writing:", filenameUPER, end=' ... ', flush=True )
            
    with open( filenameUPER, 'wb' ) as output:
      pickle.dump( schemaUPER, output, pickle.HIGHEST_PROTOCOL )


    schemaJER = asn1tools.compile_files( entry.path, 'jer' )
    
    filenameJER = filenameShort + "_JER.pk4"

    print( "writing:", filenameJER, end=' ... ', flush=True )
            
    with open( filenameJER, 'wb' ) as output:
      pickle.dump( schemaJER, output, pickle.HIGHEST_PROTOCOL )

    print( "Done.", flush=True )

timeFinished = datetime.datetime.now()
timeElapsed = timeFinished - timeStart
iElapsedMS = int( timeElapsed.total_seconds() * 1000 )

print( "\n", iCount, "file(s) processed in", iElapsedMS, "milliseconds." )

