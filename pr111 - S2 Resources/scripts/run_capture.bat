
rem - this file is CM'd here:
rem -		S:\Development\CM\Git_Projects\pr111 - S2 Resources\scripts
rem - and is run on S112 (and others?) from:
rem - 		C:\Development\scripts


if not exist S:\ net use S: \\192.168.1.200\Share /user:pi rpi

if exist D:\TEMP set TEMP=D:\TEMP
if exist D:\TEMP cd /d D:\TEMP

set PR125_JAR=pr125_20190602_001.jar

if not exist D:\TEMP\%PR125_JAR% copy S:\Development\Export\%PR125_JAR% D:\TEMP

rem - "C:\WinApps\Runtimes\Java JDK 64b 11.0.2\bin"\java -jar  S:\Development\Export\pr125_20190602_001.jar
"C:\WinApps\Runtimes\Java JDK 64b 11.0.2\bin"\java -jar  D:\TEMP\%PR125_JAR%
