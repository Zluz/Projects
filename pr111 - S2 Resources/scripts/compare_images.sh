# see	http://www.imagemagick.org/Usage/compare/#statistics

export LHS=$1
export RHS=$2

# export OUT1=`/usr/bin/convert $LHS $RHS  -compose Difference -composite -colorspace gray -verbose info:`
# export OUT2=`echo $OUT1 | sed -n '/statistics:/,/^  [^ ]/p'`
# export OUT1=`/usr/bin/convert $LHS $RHS  -compose Difference -composite fuzz 10% -colorspace gray -verbose info: | sed -n '/statistics:/,/^  [^ ]/p' | grep mean`
# export OUT1=`/usr/bin/convert $LHS $RHS  -compose  Difference  -composite -fuzz 5%  -verbose info: | sed -n '/statistics:/,/^  [^ ]/p' | grep mean`
# export OUT1=`/usr/bin/compare -verbose -metric MAE  $LHS $RHS null:`
export OUT1=`/usr/bin/compare -verbose -metric MAE  $LHS $RHS null: 2>&1`
export OUT2=`echo $OUT1`
export OUT3=`echo $OUT2 | tr ' ' '\n' | grep '(' | tail -1`
export OUT4=`echo $OUT3 | tr '(' '\n' | tr ')' ' '`

# echo $OUT2
#echo "result:"
echo $OUT4
#echo
#echo "processing:"
#echo $OUT3
#echo $OUT2
#echo $OUT1

