
# this script is used to tune the image comparison.
# this will adjust one parameter in edge-detection,
# showing the evaluation of a control-diff and also
# a diff of a car and normal background

echo "  (--   edge detect params    --)   (--  process   --)   [01]     [02]     [C1]      (diff)   (div)"

START=2
END=10
for (( VAR=$START; VAR<=$END; )); do

#	export CONV_PARAM="-canny 0x05+50%+60% -negate"
#	export CONV_PARAM="-canny 0x05+40%+60% -negate"
#	export CONV_PARAM="-canny 0x05+40%+62% -negate"
#	export CONV_PARAM="-canny 0x06+16%+62% -negate"
#	export CONV_PARAM="-canny 0x06+16%+55% -negate"
#	export CONV_PARAM="-canny 0x08+16%+55% -negate"
	export CONV_PARAM="-canny 0x08+16%+55% -negate"

	VAR=$(( VAR + 1 ))

	echo -n "  [ $CONV_PARAM ] - Gen..."
	convert r1.jpg $CONV_PARAM r1_edge.jpg
	convert 01.jpg $CONV_PARAM 01_edge.jpg
	convert 02.jpg $CONV_PARAM 02_edge.jpg
	convert c1.jpg $CONV_PARAM c1_edge.jpg

	echo -n "Comp..."
#	export D07=`compare -verbose -metric MAE 03_edge.jpg 07_edge.jpg null: 2>&1`
#	export DC3=`compare -verbose -metric MAE 03_edge.jpg c3_edge.jpg null: 2>&1`
	export D01=`compare -verbose -metric MSE r1_edge.jpg 01_edge.jpg null: 2>&1`
	export D02=`compare -verbose -metric MSE r1_edge.jpg 02_edge.jpg null: 2>&1`
	export DC1=`compare -verbose -metric MSE r1_edge.jpg c1_edge.jpg null: 2>&1`

	# good:
	#	MSE - good diff
	#	MAE - good
	#	RMSE
	#	FUZZ
	# no good:
	#	PAE - always same
	#	MEPP - bad diff
	#	NCC - varies
	#	PSNR - low

	export D01=`echo $D01 | tr 'all' '\n' | tr '=' '\n' | tail -2 | head -1`
	export D02=`echo $D02 | tr 'all' '\n' | tr '=' '\n' | tail -2 | head -1`
	export DC1=`echo $DC1 | tr 'all' '\n' | tr '=' '\n' | tail -2 | head -1`
	export D01=`echo $D01 | tr ' ' '\n' | head -2 | tail -1`
	export D02=`echo $D02 | tr ' ' '\n' | head -2 | tail -1`
	export DC1=`echo $DC1 | tr ' ' '\n' | head -2 | tail -1`

	echo -n "Done.  "
#	echo "    D07 $D07"
#	echo "    DC3 $DC3"
	echo -n "$D01, $D02, $DC1  "

	export CALC_S=`bc <<< "scale = 5; $DC1 - $D01"`
	export CALC_D=`bc <<< "scale = 5; $DC1 / ( $D01 + 0.0001 ) "`
	echo "  $CALC_S, $CALC_D"

done
echo "Loop finished."

