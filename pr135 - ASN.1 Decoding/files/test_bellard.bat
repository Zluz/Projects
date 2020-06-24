

cd /d "C:\Development\CM\Git_20190124.002\Projects__20190125\pr135 - ASN.1 Decoding\files"

rem - ffasn1dump -I uper "3GPP RRC v15.3.0.asn" "SystemInformation" data.bin

ffasn1dump -I uper "3GPP RRC v15.3.0.asn" "SystemInformationBlockType7" SIB7.bin
rem - ffasn1dump -I uper "3GPP RRC v15.3.0.asn" "BCCH-DL-SCH-Message" BCCH-DL-SCH-Message.bin
