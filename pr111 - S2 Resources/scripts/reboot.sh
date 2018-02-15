if zenity --question --text "Reboot - Are you sure?"
then
	echo "reboot!"
	reboot
else
	echo "reboot aborted"
	exit 1
fi

