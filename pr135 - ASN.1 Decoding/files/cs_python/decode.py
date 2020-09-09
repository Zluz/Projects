
import asn1tools
import pickle
import sys
import os
import datetime
import fileinput

# This script is derived from asn1tools_003.py

print( "# decode.py", flush=True )


def show_help():
  print( "# Setting parameters:", flush=True )
  print( "#   f <format>     Set the format/protocol (LTE/UMTS/etc) to read", flush=True )
  print( "#   k <keyword>    Set the keyword to resolve", flush=True )
  print( "#   h <hex>        Set the hex string to decode", flush=True )
  print( "# Executing commands:", flush=True )
  print( "#   decode         Decode using the given parameters", flush=True )
  print( "#   help           Show this help text", flush=True )
  print( "#   exit           Terminate this program", flush=True )


def show_parameters():
  print( "# Current parameters:", flush=True )
  
  if ( "" == strFormatFile ):
    print( "#   Format not specified", flush=True )
  else:
    print( "#   Format: ", strFormatFile, flush=True )
    
  if ( "" == strKeyword ):
    print( "#   Keyword not specified", flush=True )
  else:
    print( "#   Keyword: ", strKeyword, flush=True )

  if ( "" == strHexString ):
    print( "#   Hex string not specified", flush=True )
  else:
    if ( len( strHexString ) > 50 ):
      print( "#   Hex string: ", strHexString[:50] + "..", flush=True )
    else:
      print( "#   Hex string: ", strHexString, flush=True )


strFormatFile = ""
strKeyword = ""
strHexString = ""
pklSchemaIn = ""
pklSchemaOut = ""

print( "#", flush=True )
show_help()
print( "\n# Ready (an empty line means ready for input)\n", flush=True )
# print( "", flush=True )

for strLine in fileinput.input():
  strLine = strLine.rstrip()

  bBlankLine = True

  # print( "# input: ", strLine, flush=True )
  
  strCommand = strLine.rsplit(' ')[0]
  # print( "# strCommand = ", strCommand, flush=True )
  
  if ( "exit" == strCommand ):
    # print( "# Terminating.", flush=True )
    sys.exit( 0 )
    
  elif ( "f" == strCommand ):
  
    strFormatFile = strLine[2:]
    
    if ( "\"" == strFormatFile[:1] and "\"" == strFormatFile[-1:] ):
      strFormatFile = strFormatFile[1:-1]
  
    print( "# Format file set to:", strFormatFile, flush=True )
    try:
    
      strSchemaInFile = "Schema_" + strFormatFile + "_UPER.pk4";
      print( "# Trying to read file: ", strSchemaInFile, flush=True );
    
      with open( strSchemaInFile, 'rb' ) as inputInFile:
        pklSchemaIn = pickle.load( inputInFile )

      strSchemaOutFile = "Schema_" + strFormatFile + "_JER.pk4";
      print( "# Trying to read file: ", strSchemaOutFile, flush=True );
    
      with open( strSchemaOutFile, 'rb' ) as inputOutFile:
        pklSchemaOut = pickle.load( inputOutFile )
                
      print( "# Schema files loaded.", flush=True );
    except:
      print( "# Exception encoutered: ", sys.exc_info()[0] )

  elif ( "k" == strCommand ):
  
    strKeyword = strLine[2:].strip()
    print( "# Keyword set to:", strKeyword, flush=True )

  elif ( "h" == strCommand ):
  
    strHexString = strLine[2:].strip()
    print( "# Hex string set to:", strHexString, flush=True )

  elif ( "help" == strCommand ):
  
    show_help()
    show_parameters()

  elif ( "decode" == strCommand ):
  
    bOk = True
  
    if ( "" == strFormatFile ):
      print( "# Format file not specified", flush=True )
      bOk = False
    if ( "" == strKeyword ):
      print( "# Keyword not specified", flush=True )
      bOk = False
    if ( "" == strHexString ):
      print( "# Hex string not specified", flush=True )
      bOk = False

    if ( "" == pklSchemaIn ):
      print( "# Schema-In not loaded. Check format file.", flush=True )
      bOk = False
    if ( "" == pklSchemaOut ):
      print( "# Schema-Out not loaded. Check format file.", flush=True )
      bOk = False
      
    if ( not bOk ):
      # print( "# Failed to decode due to missing parameters", flush=True )
      pass
    else:
        
      timeStart = datetime.datetime.now()

      try:
        bytesEncoded = bytearray.fromhex( strHexString );
        print( "# Decoding from UPER", flush=True )
        bytesDecoded = pklSchemaIn.decode( strKeyword, bytesEncoded )
        print( "# Encoding to JER (JSON)", flush=True )
        bytesResult = pklSchemaOut.encode( strKeyword, bytesDecoded )

        strResult = str( bytesResult )
    
        print( strResult, flush=True )

        timeFinished = datetime.datetime.now()
        timeElapsed = timeFinished - timeStart
        iElapsedMS = int( timeElapsed.total_seconds() * 1000 )

        print( "# Done. Parsed in", iElapsedMS, "milliseconds.", flush=True )

      except:
        print( "# Exception encoutered: ", sys.exc_info()[0] )

  elif ( "#" == strCommand or "" == strCommand ):
  
    # print( "# (skipping comment)", flush=True )
    bBlankLine = False

  else:
  
    print( "# Unknown command: ", strLine, flush=True )
    print( "# Enter 'help' for command help", flush=True )

  if ( bBlankLine ):
    print( "", flush=True )
