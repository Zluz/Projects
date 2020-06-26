
import asn1tools
import pickle
import sys
import os
import datetime
import fileinput


print( "# asn1tools_003.py", flush=True )

# schemaLTE = asn1tools.compile_files( '3GPP RRC v15.3.0.asn', 'uper' )
# with open( 'Schema_LTE.pkl', 'wb' ) as output:
#   pickle.dump( schemaLTE, output, pickle.HIGHEST_PROTOCOL )

with open( 'Schema_LTE.pkl', 'rb' ) as input:
  pklLTE = pickle.load( input )


# print ( "# ", pklLTE.decode( 
#               'BCCH-DL-SCH-Message', 
#               bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ), 
#         flush=True )

def show_help():
  print( "# Setting parameters:", flush=True )
  print( "#   f <format_file>  Set the format to use (file will be loaded)", flush=True )
  print( "#   k <keyword>      Set the keyword to resolve", flush=True )
  print( "#   h <hex>          Set the hex string to decode", flush=True )
  print( "# Executing commands:", flush=True )
  print( "#   decode           Decode using the given parameters", flush=True )
  print( "#   help             Show this help text", flush=True )
  print( "#   exit             Terminate this program", flush=True )



with open( 'Schema_LTE.pkl', 'rb' ) as input:
  pklSchema = pickle.load( input )

strFormatFile = ""
strKeyword = ""
strHexString = ""

print( "#", flush=True )
show_help()
print( "\n# Ready (an empty line means ready for input)\n", flush=True )
# print( "", flush=True )

for strLine in fileinput.input():
  strLine = strLine.rstrip()


  # print( "# input: ", strLine, flush=True )
  
  strCommand = strLine.rsplit(' ')[0]
  # print( "# strCommand = ", strCommand, flush=True )
  
  if ( "exit" == strCommand ):
    # print( "# Terminating.", flush=True )
    sys.exit( 0 )
    
  elif ( "f" == strCommand ):
    strFormatFile = strLine.rsplit(' ')[1]
    print( "# Format file set to:", strFormatFile, flush=True )
    try:
      with open( strFormatFile, 'rb' ) as input:
        pklSchema = pickle.load( input )
    except:
      print( "# Exception encoutered: ", sys.exc_info()[0] )

  elif ( "k" == strCommand ):
    strKeyword = strLine.rsplit(' ')[1]
    print( "# Keyword set to:", strKeyword, flush=True )

  elif ( "h" == strCommand ):
    strHexString = strLine.rsplit(' ')[1]
    print( "# Hex string set to:", strHexString, flush=True )

  elif ( "help" == strCommand ):
    show_help()

  elif ( "decode" == strCommand ):
    if ( "" == strKeyword ):
      print( "# Keyword not specified", flush=True )
    elif ( "" == strHexString ):
      print( "# Hex string not specified", flush=True )
    else:
        
      timeStart = datetime.datetime.now()

      try:
        bytesResult = pklSchema.decode( strKeyword, bytearray.fromhex( strHexString ) )
      
        strResult = str( bytesResult )
    
        print( strResult, flush=True )

        timeFinished = datetime.datetime.now()
        timeElapsed = timeFinished - timeStart
        iElapsedMS = int( timeElapsed.total_seconds() * 1000 )

        print( "# Parsed in", iElapsedMS, "milliseconds.", flush=True )

      except:
        print( "# Exception encoutered: ", sys.exc_info()[0] )

  else:
    print( "# Unknown command: ", strLine, flush=True )

  print( "", flush=True )
