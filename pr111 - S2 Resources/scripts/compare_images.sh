# reference
#	http://www.imagemagick.org/Usage/compare/#statistics
#	http://www.imagemagick.org/Usage/compose/#mask

export LHS=$1
export RHS=$2
export MASK=$3

if [ "$#" -eq 3 ] && [ -f $MASK ]; then

	export LHS="$1_mask"
	export RHS="$2_mask"

	/usr/bin/composite $1 $MASK $MASK $LHS
	/usr/bin/composite $2 $MASK $MASK $RHS
fi

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
#echo
#echo "processing:"
#echo $OUT3
#echo $OUT2
#echo $OUT1

echo "Command:  $CMD"
