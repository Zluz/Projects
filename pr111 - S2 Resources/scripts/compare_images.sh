# reference
#	http://www.imagemagick.org/Usage/compare/#statistics
#	http://www.imagemagick.org/Usage/compose/#mask

export LHS=$1
export RHS=$2
export MASK=$3

# validate source image files

export LHS_ZEROES=`hexdump $LHS | head -1100 | tail -1000 | grep ' 0000 0000 0000 ' | wc -l`
if [ ! "$LHS_ZEROES" -eq "0" ]; then
	echo ""
	echo "Comparison aborted. Image file corrupt (zeroes-head): $LHS"
	echo ""
	exit 100
fi
export LHS_ZEROES=`hexdump $LHS | tail -1100 | head -1000 | grep ' 0000 0000 0000 ' | wc -l`
if [ ! "$LHS_ZEROES" -eq "0" ]; then
	echo ""
	echo "Comparison aborted. Image file corrupt (zeroes-tail): $LHS"
	echo ""
	exit 100
fi

export RHS_ZEROES=`hexdump $RHS | head -1100 | tail -1000 | grep ' 0000 0000 0000 ' | wc -l`
if [ ! "$RHS_ZEROES" -eq "0" ]; then
	echo ""
	echo "Comparison aborted. Image file corrupt (zeroes-head): $RHS"
	echo ""
	exit 100
fi
export RHS_ZEROES=`hexdump $RHS | tail -1100 | head -1000 | grep ' 0000 0000 0000 ' | wc -l`
if [ ! "$RHS_ZEROES" -eq "0" ]; then
	echo ""
	echo "Comparison aborted. Image file corrupt (zeroes-tail): $RHS"
	echo ""
	exit 100
fi


export LHS_ERRORS=`/usr/bin/identify -verbose $LHS 2>&1`
export LHS_CHECK=`echo $LHS_ERRORS | grep @ | wc -l`
if [ ! "$LHS_CHECK" -eq "0" ]; then
	echo ""
	echo "Comparison aborted. Image file corrupt (identify): $LHS"
	echo ""
	echo $LHS_ERRORS
	exit 100
fi

export RHS_ERRORS=`/usr/bin/identify -verbose $RHS 2>&1`
export RHS_CHECK=`echo $RHS_ERRORS | grep @ | wc -l`
if [ ! "$RHS_CHECK" -eq "0" ]; then
	echo ""
	echo "Comparison aborted. Image file corrupt (identify): $RHS"
	echo ""
	echo $RHS_ERRORS
	exit 100
fi


if [ "$#" -eq 3 ] && [ -f $MASK ]; then

	export LHS="$1"_mask
	export RHS="$2"_mask

	/usr/bin/composite $1 $MASK $MASK $LHS
	/usr/bin/composite $2 $MASK $MASK $RHS
fi


/usr/bin/convert $LHS -auto-gamma -auto-level $LHS
/usr/bin/convert $RHS -auto-gamma -auto-level $RHS

# /usr/bin/convert $LHS -set colorspace Gray -separate -average -auto-gamma -auto-level $LHS_grey
# /usr/bin/convert $RHS -set colorspace Gray -separate -average -auto-gamma -auto-level $RHS_grey
# export LHS="$LHS_grey"
# export RHS="$RHS_grey"

/usr/bin/convert $LHS -canny 0x08+16%+55% -negate "$LHS"_edge
/usr/bin/convert $RHS -canny 0x08+16%+55% -negate "$RHS"_edge
#/usr/bin/convert $LHS -canny 0x06+16%+62% -negate $LHS
#/usr/bin/convert $RHS -canny 0x06+16%+62% -negate $RHS
export LHS="$LHS"_edge
export RHS="$RHS"_edge


  export CMD="/usr/bin/compare -verbose -metric MAE  $LHS $RHS null:"
# export CMD="/usr/bin/compare -verbose -metric MAE  $LHS_grey $RHS_grey null:"
# export CMD="/usr/bin/compare -verbose -metric MSE  $LHS_edge $RHS_edge null:"
export OUT1=`$CMD 2>&1`
export OUT2=`echo $OUT1`
#export OUT3=`echo $OUT2 | tr ' ' '\n' | grep '(' | tail -1`
#export OUT4=`echo $OUT3 | tr '(' '\n' | tr ')' ' '`
export OUT3=`echo $OUT2 | tr ':' '\n' | tr '(' '\n' | head -8 | tail -1`
export OUT4=`echo $OUT3`

if [ "$#" -eq 3 ] && [ -f $MASK ]; then
	/bin/rm $LHS
	/bin/rm $RHS
fi

echo $OUT4
echo
echo "Command:"
echo "$CMD"
echo
echo "LHS: $LHS"
echo "RHS: $RHS"
echo
echo "Validation:"
echo
echo "[LHS]"
echo "$LHS_ERRORS"
echo
echo "[RHS]"
echo "$RHS_ERRORS"
echo
echo "Processing:"
echo
echo $OUT1
#echo $OUT3
#echo $OUT2
#echo $OUT1

