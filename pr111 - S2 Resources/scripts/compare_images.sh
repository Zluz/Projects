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
	echo "Comparison aborted. Image file corrupt (zeroes): $LHS"
	echo ""
	exit 100
fi

export RHS_ZEROES=`hexdump $RHS | head -1100 | tail -1000 | grep ' 0000 0000 0000 ' | wc -l`
if [ ! "$RHS_ZEROES" -eq "0" ]; then
	echo ""
	echo "Comparison aborted. Image file corrupt (zeroes): $RHS"
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

	export LHS="$1_mask"
	export RHS="$2_mask"

	/usr/bin/composite $1 $MASK $MASK $LHS
	/usr/bin/composite $2 $MASK $MASK $RHS
fi


/usr/bin/convert $LHS -set colorspace Gray -separate -average -auto-gamma -auto-level $LHS
/usr/bin/convert $RHS -set colorspace Gray -separate -average -auto-gamma -auto-level $RHS


export CMD="/usr/bin/compare -verbose -metric MAE  $LHS $RHS null:"
export OUT1=`$CMD 2>&1`
export OUT2=`echo $OUT1`
export OUT3=`echo $OUT2 | tr ' ' '\n' | grep '(' | tail -1`
export OUT4=`echo $OUT3 | tr '(' '\n' | tr ')' ' '`

if [ "$#" -eq 3 ] && [ -f $MASK ]; then
	/bin/rm $LHS
	/bin/rm $RHS
fi

echo $OUT4
echo
echo "Command:"
echo "$CMD"
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

