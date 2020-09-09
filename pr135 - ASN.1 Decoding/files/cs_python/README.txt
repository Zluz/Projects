
[Directory contents]

This directory contains two Python scripts and a few supporting files for 
decoding ASN.1 fields.

decode.py - Interactive utility that can decode a field given a binary 
    schema file, a keyword, and binary data.

compile_schema.py - Utility to convert any *.asn text schema files into
    binary schema files that are used by decode.py

Schema_*.asn - Schema files for different protocols.

README.txt - This README file.


Running the Python scripts obviously depends on having Python installed.
The "asn1tools" package is also required for decoding.
The "pyinstaller" package is required for making Python scripts executable.

[Creating the decode executable]

Requirements
  * Must be run on Windows
  * Must have Python installed with packages asn1tools and pyinstaller
  * Must have WinRAR installed

Procedure (to create decode_run)
  1.  Run pytinstaller decode.py
  	This pull all dependencies and the Python runtime itself into 
  	subdirectories and create a native executable called decode.exe.
  	Make note of the "dist" directory.
  2.  Open a command prompt and go into the "dist" directory.
  	Run "dir /s > all.dir"
  	The 'all.dir' file can be used by a hosting application to detect 
  	that the extraction process has completed.
  3.  Run WinRAR
  4.  In the address bar, provide the path to the "dist" directory
  5.  Right click on "decode", select "Add files to archive"
  6.  Select options:
  	General
  		Archive name (*1): decode_run.exe
  		Archive format: ZIP
	  	Compression method: Fast
	  	Archiving options: Create SFX archive
	Advanced - SFX options
	  	Setup - Run after (*2): decode\decode.exe
	  	Update - Overwrite mode: Skip existing files
	  	Modes - Silent mode: Hide all
  7.  Click OK. 
  	A progress dialog will appear momentarily.
  	"decode_run.exe" will be created in the current directory.

  This is to create "decode_run".
  To create "decode_extract" follow the same procedure but (*1) enter 
  	"decode_extract.exe" as the Archive name, and leave the Run after (*2)
  	field empty.


[Example run]

(Run compile_schema.py to generate the pk4 files from the Schema*.asn files)

# decode.py
..

f Schema_LTE.pk4
# Format file set to: Schema_LTE.pk4

k BCCH-DL-SCH-Message
# Keyword set to: BCCH-DL-SCH-Message

h 000E8409D590440590808FC208817A100802411016C1E00242348261892228880800
# Hex string set to: 000E8409D590440590808FC208817A100802411016C1E00242348261892228880800

decode
{'message': ('c1', ('systemInformation', {'criticalExtensions': ('systemInformation-r8', {'sib-TypeAndInfo': [('sib5', {'interFreqCarrierFreqList': [{'dl-CarrierFreq': 5035, 'q-RxLevMin': -62, 't-ReselectionEUTRA': 1, 'threshX-High': 2, 'threshX-Low': 0, 'allowedMeasBandwidth': 'mbw25', 'presenceAntennaPort1': True, 'cellReselectionPriority': 4, 'neighCellConfig': (b'\x80', 2), 'q-OffsetFreq': 'dB0'}, {'dl-CarrierFreq': 2300, 'q-RxLevMin': -62, 't-ReselectionEUTRA': 1, 'threshX-High': 2, 'threshX-Low': 0, 'allowedMeasBandwidth': 'mbw100', 'presenceAntennaPort1': True, 'cellReselectionPriority': 6, 'neighCellConfig': (b'\x80', 2), 'q-OffsetFreq': 'dB0'}, {'dl-CarrierFreq': 1025, 'q-RxLevMin': -62, 't-ReselectionEUTRA': 1, 'threshX-High': 2, 'threshX-Low': 0, 'allowedMeasBandwidth': 'mbw25', 'presenceAntennaPort1': True, 'cellReselectionPriority': 5, 'neighCellConfig': (b'\x80', 2), 'q-OffsetFreq': 'dB0'}], 'lateNonCriticalExtension': b'\xd2\t\x86$\x88\xa2  '})]})}))}
# Parsed in 3 milliseconds.


('Paste'-able content)

f Schema_LTE.pk4
k BCCH-DL-SCH-Message
h 000E8409D590440590808FC208817A100802411016C1E00242348261892228880800
decode

