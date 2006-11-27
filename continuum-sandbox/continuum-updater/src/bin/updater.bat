@REM ----------------------------------------------------------------------------
@REM Copyright 2001-2005 The Apache Software Foundation.
@REM 
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM 
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM 
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM ----------------------------------------------------------------------------
@REM 

@REM ----------------------------------------------------------------------------
@REM Continuum Updater Start Up Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM ----------------------------------------------------------------------------

@echo off

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

set ERROR_CODE=0

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
set ERROR_CODE=1
goto end

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto chkMHome

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = %JAVA_HOME%
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
set ERROR_CODE=1
goto end

:chkMHome
if not "%C1_UPDATER_HOME%"=="" goto valMHome

if "%OS%"=="Windows_NT" SET C1_UPDATER_HOME=%~dps0\..
if not "%C1_UPDATER_HOME%"=="" goto valMHome

echo.
echo ERROR: C1_UPDATER_HOME not found in your environment.
echo Please set the C1_UPDATER_HOME variable in your environment to match the
echo location of the Continuum Updater installation
echo.
set ERROR_CODE=1
goto end

:valMHome
if exist "%C1_UPDATER_HOME%\bin\updater.bat" goto init

echo.
echo ERROR: C1_UPDATER_HOME is set to an invalid directory.
echo C1_UPDATER_HOME = %C1_UPDATER_HOME%
echo Please set the C1_UPDATER_HOME variable in your environment to match the
echo location of the Continuum Updater installation
echo.
set ERROR_CODE=1
goto end
@REM ==== END VALIDATION ====

:init
@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set C1_UPDATER_CMD_LINE_ARGS=%*
goto endInit

@REM The 4NT Shell from jp software
:4NTArgs
set C1_UPDATER_CMD_LINE_ARGS=%$
goto endInit

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of agruments (up to the command line limit, anyway).
set C1_UPDATER_CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto endInit
set C1_UPDATER_CMD_LINE_ARGS=%C1_UPDATER_CMD_LINE_ARGS% %1
shift
goto Win9xApp

@REM Reaching here means variables are defined and arguments have been captured
:endInit
SET C1_UPDATER_JAVA_EXE="%JAVA_HOME%\bin\java.exe"

@REM Start C1_UPDATER2
for %%i in ("%C1_UPDATER_HOME%"\core\boot\classworlds-*) do set CLASSWORLDS_JAR="%%i"
%C1_UPDATER_JAVA_EXE% -classpath %CLASSWORLDS_JAR% "-Dclassworlds.conf=%C1_UPDATER_HOME%\bin\updater.conf" "-Dcontinuum.home=%C1_UPDATER_HOME%" org.codehaus.classworlds.Launcher %C1_UPDATER_CMD_LINE_ARGS%
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
set ERROR_CODE=1

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set C1_UPDATER_JAVA_EXE=
set C1_UPDATER_CMD_LINE_ARGS=
goto postExec

:endNT
@endlocal

:postExec

exit /B %ERROR_CODE%

