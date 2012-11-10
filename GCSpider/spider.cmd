@echo off
setlocal ENABLEDELAYEDEXPANSION
if defined CLASSPATH (set CLASSPATH=%CLASSPATH%;%CD%) else (set CLASSPATH=%CD%)
FOR /R .\lib %%G IN (*.jar) DO set CLASSPATH=!CLASSPATH!;%%G
Echo The Classpath definition is %CLASSPATH%
java -cp %CLASSPATH% -jar lib\listingparser-1.0.jar
pause