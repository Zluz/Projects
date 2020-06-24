
import asn1tools
import pickle

# schemaLTE = asn1tools.compile_files( '3GPP RRC v15.3.0.asn', 'uper' )
# with open( 'Schema_LTE.pkl', 'wb' ) as output:
#   pickle.dump( schemaLTE, output, pickle.HIGHEST_PROTOCOL )

with open( 'Schema_LTE.pkl', 'rb' ) as input:
  pklLTE = pickle.load( input )


print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType1', bytearray.fromhex( 'A1310A0844908467442209D41090818843B668000000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType2', bytearray.fromhex( '135267E1200456039880C80102003402515AAAC0080C01EDD8D268A82040' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType3', bytearray.fromhex( '1210BC275FD4' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType4', bytearray.fromhex( '4942A4F749248B51171E2ECC4B081730421CD4C000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType6', bytearray.fromhex( '52512020781288A20E40F0251144229C004A2200000000000000000000000000CC000000FAAFFAAF1AE90BE980E82BE998D40E2E36030080BCE8FCE8DFE8B6E824E903E9AEE828E9D1E895E8000000000000000000000000000000000000000074010000FAAFFAAF43002B005B002E00160046000A003A' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType7', bytearray.fromhex( '3C1194C4671A46B1B46F1C4731D4771E47B1F47F20483214872248B2348F24497C000000FAAFFAAF' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType8', bytearray.fromhex( 'EC6B5EACC6128828040790480F080060000008000000504806100000000000' ) ) )
# 
# print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType1', bytearray.fromhex( 'A1310A0844908467442209D41090818843B668000000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType2', bytearray.fromhex( '135267E1200456039880C80102003402515AAAC0080C01EDD8D268A82040' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType3', bytearray.fromhex( '1210BC275FD4' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType4', bytearray.fromhex( '4942A4F749248B51171E2ECC4B081730421CD4C000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType6', bytearray.fromhex( '52512020781288A20E40F0251144229C004A2200000000000000000000000000CC000000FAAFFAAF1AE90BE980E82BE998D40E2E36030080BCE8FCE8DFE8B6E824E903E9AEE828E9D1E895E8000000000000000000000000000000000000000074010000FAAFFAAF43002B005B002E00160046000A003A' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType7', bytearray.fromhex( '3C1194C4671A46B1B46F1C4731D4771E47B1F47F20483214872248B2348F24497C000000FAAFFAAF' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType8', bytearray.fromhex( 'EC6B5EACC6128828040790480F080060000008000000504806100000000000' ) ) )
# 
# print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType1', bytearray.fromhex( 'A1310A0844908467442209D41090818843B668000000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType2', bytearray.fromhex( '135267E1200456039880C80102003402515AAAC0080C01EDD8D268A82040' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType3', bytearray.fromhex( '1210BC275FD4' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType4', bytearray.fromhex( '4942A4F749248B51171E2ECC4B081730421CD4C000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType6', bytearray.fromhex( '52512020781288A20E40F0251144229C004A2200000000000000000000000000CC000000FAAFFAAF1AE90BE980E82BE998D40E2E36030080BCE8FCE8DFE8B6E824E903E9AEE828E9D1E895E8000000000000000000000000000000000000000074010000FAAFFAAF43002B005B002E00160046000A003A' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType7', bytearray.fromhex( '3C1194C4671A46B1B46F1C4731D4771E47B1F47F20483214872248B2348F24497C000000FAAFFAAF' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType8', bytearray.fromhex( 'EC6B5EACC6128828040790480F080060000008000000504806100000000000' ) ) )
# 
# print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType1', bytearray.fromhex( 'A1310A0844908467442209D41090818843B668000000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType2', bytearray.fromhex( '135267E1200456039880C80102003402515AAAC0080C01EDD8D268A82040' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType3', bytearray.fromhex( '1210BC275FD4' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType4', bytearray.fromhex( '4942A4F749248B51171E2ECC4B081730421CD4C000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType6', bytearray.fromhex( '52512020781288A20E40F0251144229C004A2200000000000000000000000000CC000000FAAFFAAF1AE90BE980E82BE998D40E2E36030080BCE8FCE8DFE8B6E824E903E9AEE828E9D1E895E8000000000000000000000000000000000000000074010000FAAFFAAF43002B005B002E00160046000A003A' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType7', bytearray.fromhex( '3C1194C4671A46B1B46F1C4731D4771E47B1F47F20483214872248B2348F24497C000000FAAFFAAF' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType8', bytearray.fromhex( 'EC6B5EACC6128828040790480F080060000008000000504806100000000000' ) ) )
# 
# print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType1', bytearray.fromhex( 'A1310A0844908467442209D41090818843B668000000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType2', bytearray.fromhex( '135267E1200456039880C80102003402515AAAC0080C01EDD8D268A82040' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType3', bytearray.fromhex( '1210BC275FD4' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType4', bytearray.fromhex( '4942A4F749248B51171E2ECC4B081730421CD4C000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType6', bytearray.fromhex( '52512020781288A20E40F0251144229C004A2200000000000000000000000000CC000000FAAFFAAF1AE90BE980E82BE998D40E2E36030080BCE8FCE8DFE8B6E824E903E9AEE828E9D1E895E8000000000000000000000000000000000000000074010000FAAFFAAF43002B005B002E00160046000A003A' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType7', bytearray.fromhex( '3C1194C4671A46B1B46F1C4731D4771E47B1F47F20483214872248B2348F24497C000000FAAFFAAF' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType8', bytearray.fromhex( 'EC6B5EACC6128828040790480F080060000008000000504806100000000000' ) ) )
# 
# print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType1', bytearray.fromhex( 'A1310A0844908467442209D41090818843B668000000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType2', bytearray.fromhex( '135267E1200456039880C80102003402515AAAC0080C01EDD8D268A82040' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType3', bytearray.fromhex( '1210BC275FD4' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType4', bytearray.fromhex( '4942A4F749248B51171E2ECC4B081730421CD4C000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType6', bytearray.fromhex( '52512020781288A20E40F0251144229C004A2200000000000000000000000000CC000000FAAFFAAF1AE90BE980E82BE998D40E2E36030080BCE8FCE8DFE8B6E824E903E9AEE828E9D1E895E8000000000000000000000000000000000000000074010000FAAFFAAF43002B005B002E00160046000A003A' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType7', bytearray.fromhex( '3C1194C4671A46B1B46F1C4731D4771E47B1F47F20483214872248B2348F24497C000000FAAFFAAF' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType8', bytearray.fromhex( 'EC6B5EACC6128828040790480F080060000008000000504806100000000000' ) ) )
# 
# print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType1', bytearray.fromhex( 'A1310A0844908467442209D41090818843B668000000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType2', bytearray.fromhex( '135267E1200456039880C80102003402515AAAC0080C01EDD8D268A82040' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType3', bytearray.fromhex( '1210BC275FD4' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType4', bytearray.fromhex( '4942A4F749248B51171E2ECC4B081730421CD4C000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType6', bytearray.fromhex( '52512020781288A20E40F0251144229C004A2200000000000000000000000000CC000000FAAFFAAF1AE90BE980E82BE998D40E2E36030080BCE8FCE8DFE8B6E824E903E9AEE828E9D1E895E8000000000000000000000000000000000000000074010000FAAFFAAF43002B005B002E00160046000A003A' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType7', bytearray.fromhex( '3C1194C4671A46B1B46F1C4731D4771E47B1F47F20483214872248B2348F24497C000000FAAFFAAF' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType8', bytearray.fromhex( 'EC6B5EACC6128828040790480F080060000008000000504806100000000000' ) ) )
# 
# print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType1', bytearray.fromhex( 'A1310A0844908467442209D41090818843B668000000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType2', bytearray.fromhex( '135267E1200456039880C80102003402515AAAC0080C01EDD8D268A82040' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType3', bytearray.fromhex( '1210BC275FD4' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType4', bytearray.fromhex( '4942A4F749248B51171E2ECC4B081730421CD4C000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType6', bytearray.fromhex( '52512020781288A20E40F0251144229C004A2200000000000000000000000000CC000000FAAFFAAF1AE90BE980E82BE998D40E2E36030080BCE8FCE8DFE8B6E824E903E9AEE828E9D1E895E8000000000000000000000000000000000000000074010000FAAFFAAF43002B005B002E00160046000A003A' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType7', bytearray.fromhex( '3C1194C4671A46B1B46F1C4731D4771E47B1F47F20483214872248B2348F24497C000000FAAFFAAF' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType8', bytearray.fromhex( 'EC6B5EACC6128828040790480F080060000008000000504806100000000000' ) ) )
# 
# print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType1', bytearray.fromhex( 'A1310A0844908467442209D41090818843B668000000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType2', bytearray.fromhex( '135267E1200456039880C80102003402515AAAC0080C01EDD8D268A82040' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType3', bytearray.fromhex( '1210BC275FD4' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType4', bytearray.fromhex( '4942A4F749248B51171E2ECC4B081730421CD4C000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType6', bytearray.fromhex( '52512020781288A20E40F0251144229C004A2200000000000000000000000000CC000000FAAFFAAF1AE90BE980E82BE998D40E2E36030080BCE8FCE8DFE8B6E824E903E9AEE828E9D1E895E8000000000000000000000000000000000000000074010000FAAFFAAF43002B005B002E00160046000A003A' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType7', bytearray.fromhex( '3C1194C4671A46B1B46F1C4731D4771E47B1F47F20483214872248B2348F24497C000000FAAFFAAF' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType8', bytearray.fromhex( 'EC6B5EACC6128828040790480F080060000008000000504806100000000000' ) ) )
# 
# print ( pklLTE.decode( 'BCCH-DL-SCH-Message', bytearray.fromhex( '000E8409D590440590808FC208817A100802411016C1E00242348261892228880800' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType1', bytearray.fromhex( 'A1310A0844908467442209D41090818843B668000000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType2', bytearray.fromhex( '135267E1200456039880C80102003402515AAAC0080C01EDD8D268A82040' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType3', bytearray.fromhex( '1210BC275FD4' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType4', bytearray.fromhex( '4942A4F749248B51171E2ECC4B081730421CD4C000' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType5', bytearray.fromhex( '9502D284EA80074A8265C2754A03C4040C0A305880' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType6', bytearray.fromhex( '52512020781288A20E40F0251144229C004A2200000000000000000000000000CC000000FAAFFAAF1AE90BE980E82BE998D40E2E36030080BCE8FCE8DFE8B6E824E903E9AEE828E9D1E895E8000000000000000000000000000000000000000074010000FAAFFAAF43002B005B002E00160046000A003A' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType7', bytearray.fromhex( '3C1194C4671A46B1B46F1C4731D4771E47B1F47F20483214872248B2348F24497C000000FAAFFAAF' ) ) )
# print ( pklLTE.decode( 'SystemInformationBlockType8', bytearray.fromhex( 'EC6B5EACC6128828040790480F080060000008000000504806100000000000' ) ) )
# 
# 
# print ( pklLTE.decode( '', bytearray.fromhex( '' ) ) )