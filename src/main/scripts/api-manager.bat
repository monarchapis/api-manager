@echo off
rem Copyright (C) 2015 CapTech Ventures, Inc.
rem (http://www.captechconsulting.com) All Rights Reserved.
rem
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

rem ---------------------------------------------------------------------------
rem Start/Stop Script for the Monarch API Manager
rem
rem Environment Variable Prerequisites
rem
rem   Do not set the variables in this script. Instead put them into a script
rem   setenv.bat in MONARCH_HOME/bin to keep your customizations separate.
rem
rem   MONARCH_HOME    May point at your Monarch "build" directory.
rem
rem   MONARCH_CONFIG  (Optional) Full path to a file where the configuration files are located.
rem                   Default is $MONARCH_HOME/conf
rem
rem   MONARCH_LOGS    (Optional) Full path to a file where the log files should be directed.
rem                   Default is $MONARCH_HOME/logs
rem
rem   MONARCH_OPTS    (Optional) Java runtime options used when the "start",
rem                   "run" or "debug" command is executed.
rem                   Include here and not in JAVA_OPTS all options, that should
rem                   only be used by Monarch itself, not by the stop process,
rem                   the version command etc.
rem                   Examples are heap size, GC logging, JMX ports etc.
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem                   Required to run the with the "debug" argument.
rem
rem   JRE_HOME        Must point at your Java Runtime installation.
rem                   Defaults to JAVA_HOME if empty. If JRE_HOME and JAVA_HOME
rem                   are both set, JRE_HOME is used.
rem
rem   JAVA_OPTS       (Optional) Java runtime options used when any command
rem                   is executed.
rem                   Include here and not in MONARCH_OPTS all options, that
rem                   should be used by Monarch and also by the stop process,
rem                   the version command etc.
rem                   Most options should go into MONARCH_OPTS.
rem
rem   JAVA_ENDORSED_DIRS (Optional) Lists of of semi-colon separated directories
rem                   containing some jars in order to allow replacement of APIs
rem                   created outside of the JCP (i.e. DOM and SAX from W3C).
rem                   It can also be used to update the XML parser implementation.
rem                   Defaults to $MONARCH_HOME/endorsed.
rem
rem   JPDA_TRANSPORT  (Optional) JPDA transport used when the "jpda start"
rem                   command is executed. The default is "dt_socket".
rem
rem   JPDA_ADDRESS    (Optional) Java runtime options used when the "jpda start"
rem                   command is executed. The default is localhost:8000.
rem
rem   JPDA_SUSPEND    (Optional) Java runtime options used when the "jpda start"
rem                   command is executed. Specifies whether JVM should suspend
rem                   execution immediately after startup. Default is "n".
rem
rem   JPDA_OPTS       (Optional) Java runtime options used when the "jpda start"
rem                   command is executed. If used, JPDA_TRANSPORT, JPDA_ADDRESS,
rem                   and JPDA_SUSPEND are ignored. Thus, all required jpda
rem                   options MUST be specified. The default is:
rem
rem                   -agentlib:jdwp=transport=%JPDA_TRANSPORT%,
rem                       address=%JPDA_ADDRESS%,server=y,suspend=%JPDA_SUSPEND%
rem
rem   TITLE           (Optional) Specify the title of Monarch window. The default
rem                   TITLE is Monarch if it's not specified.
rem                   Example (all one line)
rem                   set TITLE=Monarch.Cluster#1.Server#1 [%DATE% %TIME%]
rem ---------------------------------------------------------------------------

setlocal

rem Suppress Terminate batch job on CTRL+C
if not ""%1"" == ""run"" goto mainEntry
if "%TEMP%" == "" goto mainEntry
if exist "%TEMP%\%~nx0.run" goto mainEntry
echo Y>"%TEMP%\%~nx0.run"
if not exist "%TEMP%\%~nx0.run" goto mainEntry
echo Y>"%TEMP%\%~nx0.Y"
call "%~f0" %* <"%TEMP%\%~nx0.Y"
rem Use provided errorlevel
set RETVAL=%ERRORLEVEL%
del /Q "%TEMP%\%~nx0.Y" >NUL 2>&1
exit /B %RETVAL%
:mainEntry
del /Q "%TEMP%\%~nx0.run" >NUL 2>&1

rem Guess MONARCH_HOME if not defined
set "CURRENT_DIR=%cd%"
if not "%MONARCH_HOME%" == "" goto gotHome
set "MONARCH_HOME=%CURRENT_DIR%"
if exist "%MONARCH_HOME%\bin\api-manager.bat" goto okHome
cd ..
set "MONARCH_HOME=%cd%"
cd "%CURRENT_DIR%"
:gotHome

if exist "%MONARCH_HOME%\bin\api-manager.bat" goto okHome
echo The MONARCH_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

rem Ensure that neither MONARCH_HOME nor MONARCH_HOME contains a semi-colon
rem as this is used as the separator in the classpath and Java provides no
rem mechanism for escaping if the same character appears in the path. Check this
rem by replacing all occurrences of ';' with '' and checking that neither
rem MONARCH_HOME nor MONARCH_HOME have changed
if "%MONARCH_HOME%" == "%MONARCH_HOME:;=%" goto homeNoSemicolon
echo Using MONARCH_HOME:   "%MONARCH_HOME%"
echo Unable to start as MONARCH_HOME contains a semicolon (;) character
goto end
:homeNoSemicolon

if "%MONARCH_HOME%" == "%MONARCH_HOME:;=%" goto baseNoSemicolon
echo Using MONARCH_HOME:   "%MONARCH_HOME%"
echo Unable to start as MONARCH_HOME contains a semicolon (;) character
goto end
:baseNoSemicolon

rem Ensure that any user defined CLASSPATH variables are not used on startup,
rem but allow them to be specified in setenv.bat, in rare case when it is needed.
set CLASSPATH=

rem Get standard environment variables
if not exist "%MONARCH_HOME%\bin\setenv.bat" goto checkSetenvHome
call "%MONARCH_HOME%\bin\setenv.bat"
goto setenvDone
:checkSetenvHome
if exist "%MONARCH_HOME%\bin\setenv.bat" call "%MONARCH_HOME%\bin\setenv.bat"
:setenvDone

rem Get standard Java environment variables
if exist "%MONARCH_HOME%\bin\setclasspath.bat" goto okSetclasspath
echo Cannot find "%MONARCH_HOME%\bin\setclasspath.bat"
echo This file is needed to run this program
goto end
:okSetclasspath
call "%MONARCH_HOME%\bin\setclasspath.bat" %1
if errorlevel 1 goto end

rem Add on extra jar file to CLASSPATH
rem Note that there are no quotes as we do not want to introduce random
rem quotes into the CLASSPATH
if "%CLASSPATH%" == "" goto emptyClasspath
set "CLASSPATH=%CLASSPATH%;"
:emptyClasspath
set "CLASSPATH=%CLASSPATH%%MONARCH_HOME%\bin\bootstrap.jar"

if not "%MONARCH_CONFIG%" == "" goto gotConfig
set "MONARCH_CONFIG=%MONARCH_HOME%\conf"
:gotConfig

if not "%MONARCH_LOGS%" == "" goto gotLogs
set "MONARCH_LOGS=%MONARCH_HOME%\logs"
:gotLogs


rem ----- Execute The Requested Command ---------------------------------------

echo Using MONARCH_HOME:    "%MONARCH_HOME%"
echo Using MONARCH_CONFIG:  "%MONARCH_CONFIG%"
echo Using MONARCH_LOGS:    "%MONARCH_LOGS%"
if ""%1"" == ""debug"" goto use_jdk
echo Using JRE_HOME:        "%JRE_HOME%"
goto java_dir_displayed
:use_jdk
echo Using JAVA_HOME:       "%JAVA_HOME%"
:java_dir_displayed
echo Using CLASSPATH:       "%CLASSPATH%"

set _EXECJAVA=%_RUNJAVA%
set MAINCLASS=com.monarchapis.apimanager.startup.Bootstrap
set ACTION=start
set DEBUG_OPTS=
set JPDA=

if not ""%1"" == ""jpda"" goto noJpda
set JPDA=jpda
if not "%JPDA_TRANSPORT%" == "" goto gotJpdaTransport
set JPDA_TRANSPORT=dt_socket
:gotJpdaTransport
if not "%JPDA_ADDRESS%" == "" goto gotJpdaAddress
set JPDA_ADDRESS=localhost:8000
:gotJpdaAddress
if not "%JPDA_SUSPEND%" == "" goto gotJpdaSuspend
set JPDA_SUSPEND=n
:gotJpdaSuspend
if not "%JPDA_OPTS%" == "" goto gotJpdaOpts
set JPDA_OPTS=-agentlib:jdwp=transport=%JPDA_TRANSPORT%,address=%JPDA_ADDRESS%,server=y,suspend=%JPDA_SUSPEND%
:gotJpdaOpts
shift
:noJpda

if ""%1"" == ""debug"" goto doDebug
if ""%1"" == ""run"" goto doRun
if ""%1"" == ""start"" goto doStart
if ""%1"" == ""stop"" goto doStop
if ""%1"" == ""version"" goto doVersion

echo Usage:  catalina ( commands ... )
echo commands:
echo   debug             Start Monarch in a debugger
echo   jpda start        Start Monarch under JPDA debugger
echo   run               Start Monarch in the current window
echo   start             Start Monarch in a separate window
echo   stop              Stop Monarch
echo   version           What version of Monarch are you running?
goto end

:doDebug
shift
set _EXECJAVA=%_RUNJDB%
set DEBUG_OPTS=-sourcepath "%MONARCH_HOME%\..\..\java"
goto execCmd

:doRun
shift
goto execCmd

:doStart
shift
if "%TITLE%" == "" set TITLE=Monarch
set _EXECJAVA=start "%TITLE%" %_RUNJAVA%
goto execCmd

:doStop
shift
set ACTION=stop
set MONARCH_OPTS=
goto execCmd

:doVersion
set ACTION=version
set MONARCH_OPTS=
goto end


:execCmd
rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem Execute Java with the applicable properties
if not "%JPDA%" == "" goto doJpda
%_EXECJAVA% %JAVA_OPTS% %MONARCH_OPTS% %DEBUG_OPTS% -Djava.endorsed.dirs="%JAVA_ENDORSED_DIRS%" -classpath "%CLASSPATH%" -Dmonarch.base="%MONARCH_HOME%" -Dmonarch.home="%MONARCH_HOME%" %MAINCLASS% %CMD_LINE_ARGS% %ACTION%
goto end
:doJpda
%_EXECJAVA% %JAVA_OPTS% %MONARCH_OPTS% %JPDA_OPTS% %DEBUG_OPTS% -Djava.endorsed.dirs="%JAVA_ENDORSED_DIRS%" -classpath "%CLASSPATH%" -Dmonarch.base="%MONARCH_HOME%" -Dmonarch.home="%MONARCH_HOME%" %MAINCLASS% %CMD_LINE_ARGS% %ACTION%
goto end

:end
