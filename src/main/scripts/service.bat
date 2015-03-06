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
rem NT Service Install/Uninstall script
rem
rem Options
rem install                Install the service using Monach as service name.
rem                        Service is installed using default settings.
rem remove                 Remove the service from the System.
rem
rem name        (optional) If the second argument is present it is considered
rem                        to be new service name
rem ---------------------------------------------------------------------------

setlocal

set "SELF=%~dp0%service.bat"
rem Guess MONARCH_HOME if not defined
set "CURRENT_DIR=%cd%"
if not "%MONARCH_HOME%" == "" goto gotHome
set "MONARCH_HOME=%cd%"
if exist "%MONARCH_HOME%\bin\monarch.exe" goto okHome
rem CD to the upper dir
cd ..
set "MONARCH_HOME=%cd%"
:gotHome
if exist "%MONARCH_HOME%\bin\monarch.exe" goto okHome
echo The monarch.exe was not found...
echo The MONARCH_HOME environment variable is not defined correctly.
echo This environment variable is needed to run this program
goto end
:okHome
rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJdkHome
if not "%JRE_HOME%" == "" goto gotJreHome
echo Neither the JAVA_HOME nor the JRE_HOME environment variable is defined
echo Service will try to guess them from the registry.
goto okJavaHome
:gotJreHome
if not exist "%JRE_HOME%\bin\java.exe" goto noJavaHome
if not exist "%JRE_HOME%\bin\javaw.exe" goto noJavaHome
goto okJavaHome
:gotJdkHome
if not exist "%JAVA_HOME%\jre\bin\java.exe" goto noJavaHome
if not exist "%JAVA_HOME%\jre\bin\javaw.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\javac.exe" goto noJavaHome
if not "%JRE_HOME%" == "" goto okJavaHome
set "JRE_HOME=%JAVA_HOME%\jre"
goto okJavaHome
:noJavaHome
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
echo NB: JAVA_HOME should point to a JDK not a JRE
goto end
:okJavaHome

if not "%MONARCH_CONFIG%" == "" goto gotConfig
set "MONARCH_CONFIG=%MONARCH_HOME%\conf"
:gotConfig

if not "%MONARCH_LOGS%" == "" goto gotLogs
set "MONARCH_LOGS=%MONARCH_HOME%\logs"
:gotLogs

set "EXECUTABLE=%MONARCH_HOME%\bin\monarch.exe"

rem Set default Service name
set SERVICE_NAME=Monarch
set DISPLAYNAME=Monarch API Mananger

if "x%1x" == "xx" goto displayUsage
set SERVICE_CMD=%1
shift
if "x%1x" == "xx" goto checkServiceCmd
:checkUser
if "x%1x" == "x/userx" goto runAsUser
if "x%1x" == "x--userx" goto runAsUser
set SERVICE_NAME=%1
set DISPLAYNAME=Monarch API Mananger %1
shift
if "x%1x" == "xx" goto checkServiceCmd
goto checkUser
:runAsUser
shift
if "x%1x" == "xx" goto displayUsage
set SERVICE_USER=%1
shift
runas /env /savecred /user:%SERVICE_USER% "%COMSPEC% /K \"%SELF%\" %SERVICE_CMD% %SERVICE_NAME%"
goto end
:checkServiceCmd
if /i %SERVICE_CMD% == install goto doInstall
if /i %SERVICE_CMD% == remove goto doRemove
if /i %SERVICE_CMD% == uninstall goto doRemove
echo Unknown parameter "%SERVICE_CMD%"
:displayUsage
echo.
echo Usage: service.bat install/remove [service_name] [/user username]
goto end

:doRemove
rem Remove the service
echo Removing the service '%SERVICE_NAME%' ...
echo Using MONARCH_HOME:     "%MONARCH_HOME%"

"%EXECUTABLE%" //DS//%SERVICE_NAME% ^
    --LogPath "%MONARCH_HOME%\logs"
if not errorlevel 1 goto removed
echo Failed removing '%SERVICE_NAME%' service
goto end
:removed
echo The service '%SERVICE_NAME%' has been removed
goto end

:doInstall
rem Install the service
echo Installing the service '%SERVICE_NAME%' ...
echo Using MONARCH_HOME:     "%MONARCH_HOME%"
echo Using MONARCH_CONFIG:   "%MONARCH_CONFIG%"
echo Using MONARCH_LOGS:     "%MONARCH_LOGS%"
echo Using JAVA_HOME:        "%JAVA_HOME%"
echo Using JRE_HOME:         "%JRE_HOME%"

rem Try to use the server jvm
set "JVM=%JRE_HOME%\bin\server\jvm.dll"
if exist "%JVM%" goto foundJvm
rem Try to use the client jvm
set "JVM=%JRE_HOME%\bin\client\jvm.dll"
if exist "%JVM%" goto foundJvm
echo Warning: Neither 'server' nor 'client' jvm.dll was found at JRE_HOME.
set JVM=auto
:foundJvm
echo Using JVM:              "%JVM%"

set "CLASSPATH=%MONARCH_HOME%\bin\bootstrap.jar"

"%EXECUTABLE%" //IS//%SERVICE_NAME% ^
    --Description "Monarch API Manager - http://www.monarchapis.com/" ^
    --DisplayName "%DISPLAYNAME%" ^
    --Install "%EXECUTABLE%" ^
    --LogPath "%MONARCH_LOGS%" ^
    --StdOutput auto ^
    --StdError auto ^
    --Classpath "%CLASSPATH%" ^
    --Jvm "%JVM%" ^
    --StartMode jvm ^
    --StopMode jvm ^
    --StartPath "%MONARCH_HOME%" ^
    --StopPath "%MONARCH_HOME%" ^
    --StartClass com.monarchapis.apimanager.startup.Bootstrap ^
    --StopClass com.monarchapis.apimanager.startup.Bootstrap ^
    --StartParams start ^
    --StopParams stop ^
    --JvmOptions "-Dmonarch.home=%MONARCH_HOME%;-Dmonarch.config=%MONARCH_HOME%\conf;-Dmonarch.logs=%MONARCH_HOME%\logs;-Djava.endorsed.dirs=%MONARCH_HOME%\endorsed" ^
    --JvmMs 128 ^
    --JvmMx 256
if not errorlevel 1 goto installed
echo Failed installing '%SERVICE_NAME%' service
goto end
:installed
echo The service '%SERVICE_NAME%' has been installed.

:end
cd "%CURRENT_DIR%"
