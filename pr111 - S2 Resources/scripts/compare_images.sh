
export LHS=$1
export RHS=$2

# export LHS="D035-v2__001-night.jpg"
# export RHS="D035-v2__car-night.jpg"
# export RHS="D035-v2__002-snow.jpg"

# export OUT1=`/usr/bin/convert $LHS $RHS  -compose Difference -composite -colorspace gray -verbose info:`
# export OUT2=`echo $OUT1 | sed -n '/statistics:/,/^  [^ ]/p'`
export OUT1=`/usr/bin/convert $LHS $RHS  -compose Difference -composite -colorspace gray -verbose info: | sed -n '/statistics:/,/^  [^ ]/p' | grep mean`
export OUT3=`echo $OUT1 | tr '(' '\n' | tr ')' ' ' | grep -v mean`

# echo $OUT1
# echo $OUT2
echo $OUT3

