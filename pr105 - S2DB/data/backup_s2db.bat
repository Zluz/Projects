
call s2db_login.bat

set PATH="C:\Program Files\MySQL\MySQL Server 5.7\bin";%PATH%

rem - cd /d "H:\Share\xfer\Development_xfer"
cd /d "H:\Share\Development\CM\Git_Projects\pr105 - S2DB\data"


rem - table "device"
mysqldump.exe s2db -u %DB_USR% --password=%DB_PWD% device > device_backup.sql
mysql.exe -u %DB_USR% --password=%DB_PWD% s2db -e "select * from device \G" > device_report.txt

rem - database schema
mysqldump.exe s2db -u %DB_USR% --password=%DB_PWD% -d --no-data > schema_backup.sql

