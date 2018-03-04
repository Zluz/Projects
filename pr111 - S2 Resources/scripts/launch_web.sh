
BROWSER_TEST=`ps -ef | grep "chromium-browser" | grep "type=renderer"`

if [[ "$BROWSER_TEST" == "" ]]
then
	echo "No web browser detected."
	/usr/lib/chromium-browser/chromium-browser &
else
	echo "Web browser detected."
	# echo "    $BROWSER_TEST"
fi

