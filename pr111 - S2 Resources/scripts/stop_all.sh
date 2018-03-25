
echo "#JSON  {\"caption\":\"Stopping audio\"}"

LSOF_TEST_ALL=`lsof /dev/snd/* 2>/dev/null`

LSOF_TEST_WEB=`echo "$LSOF_TEST_ALL" | grep chromium`
LSOF_TEST_VLC=`echo "$LSOF_TEST_ALL" | grep vlc | head -1`

if [[ "$LSOF_TEST_WEB" == "" ]]
then
	echo "No web audio detected."
else
	echo "#JSON  {\"caption\":\"Stopping web\"}"
	echo "Web audio detected:"
	echo "    $LSOF_TEST_WEB"
	/Local/scripts/stop_web.sh
fi

if [[ "$LSOF_TEST_VLC" == "" ]]
then
	echo "No VLC audio detected."
else
	echo "#JSON  {\"caption\":\"Stopping VLC\"}"
	echo "VLC audio detected:"
	echo "    $LSOF_TEST_VLC"
	/Local/scripts/stop_vlc.sh
fi

echo "#JSON  {\"caption\":\"All audio stopped\",\"status\":\"done\"}"

# /Local/scripts/stop_vlc.sh
# /Local/scripts/stop_web.sh

