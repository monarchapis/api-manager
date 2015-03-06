@echo off

setLocal EnableDelayedExpansion

set "SCRIPT_HOME=%~dp0\\.."
pushd %SCRIPT_HOME%
set "MONARCH_HOME=%CD%"
popd

if "!JAVA_HOME!"=="" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\javaw.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\jdb.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\javac.exe" goto noJavaHome
set _JARCMD=%JAVA_HOME%\bin\jar.exe
goto okJava

:noJavaHome
for %%X in (jar.exe) do (set FOUND=%%~$PATH:X)
if defined FOUND goto onPath
echo Could not find jar.exe.  You need to set JAVA_HOME or add JDK/JRE bin directory to your path.
exit /B -1

:onPath
set _JARCMD=jar.exe

:okJava
if exist "%MONARCH_HOME%\tmp" rd /q /s "%MONARCH_HOME%\tmp"
mkdir "%MONARCH_HOME%\tmp"
xcopy "%MONARCH_HOME%\webapp" "%MONARCH_HOME%\tmp" /s /e
md "%MONARCH_HOME%\tmp\WEB-INF\lib"
copy "%MONARCH_HOME%\lib\"*.jar "%MONARCH_HOME%\tmp\WEB-INF\lib"
del "%MONARCH_HOME%\tmp\WEB-INF\lib\"org.eclipse.jetty.*

cd "%MONARCH_HOME%\tmp"
"%_JARCMD%" cf "%MONARCH_HOME%\api-manager.war" .
cd "%MONARCH_HOME%"

rd /q /s "%MONARCH_HOME%\tmp"

echo Created %MONARCH_HOME%\api-manager.war