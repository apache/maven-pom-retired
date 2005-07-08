@echo off
@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set MAVEN_CMD_LINE_ARGS=%*
goto endInit

@REM The 4NT Shell from jp software
:4NTArgs
set MAVEN_CMD_LINE_ARGS=%$
goto endInit

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of agruments (up to the command line limit, anyway).
set MAVEN_CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto endInit
set MAVEN_CMD_LINE_ARGS=%MAVEN_CMD_LINE_ARGS% %1
shift
goto Win9xApp

@REM Reaching here means variables are defined and arguments have been captured
:endInit

SET INCLUDES=continuum-api/pom.xml,continuum-cc/pom.xml,continuum-core/pom.xml,continuum-model/pom.xml,continuum-notifiers/pom.xml,continuum-notifiers/continuum-notifier-irc/pom.xml,continuum-notifiers/continuum-notifier-jabber/pom.xml,continuum-notifiers/continuum-notifier-msn/pom.xml,continuum-web/pom.xml,continuum-xmlrpc/pom.xml

call m2 -N install %MAVEN_CMD_LINE_ARGS%
cd continuum-notifiers
call m2 -N install %MAVEN_CMD_LINE_ARGS%
cd ..
call m2 -r -Dmaven.reactor.includes=*/pom.xml clean:clean %MAVEN_CMD_LINE_ARGS%
call m2 -r -Dmaven.reactor.includes="%INCLUDES%" install %MAVEN_CMD_LINE_ARGS%
cd continuum-plexus-application
call build.bat %MAVEN_CMD_LINE_ARGS%
cd ..
