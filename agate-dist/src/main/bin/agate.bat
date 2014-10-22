@echo off

if "%JAVA_OPTS%" == "" goto DEFAULT_JAVA_OPTS

:INVOKE
echo JAVA_HOME=%JAVA_HOME%
echo JAVA_OPTS=%JAVA_OPTS%
echo AGATE_HOME=%AGATE_HOME%

if "%AGATE_HOME%" == "" goto AGATE_HOME_NOT_SET

setlocal ENABLEDELAYEDEXPANSION

set AGATE_DIST=%~dp0..
echo AGATE_DIST=%AGATE_DIST%

set AGATE_LOG=%AGATE_HOME%\logs
IF NOT EXIST "%AGATE_LOG%" mkdir "%AGATE_LOG%"
echo AGATE_LOG=%AGATE_LOG%

rem Java 7 supports wildcard classpaths
rem http://docs.oracle.com/javase/7/docs/technotes/tools/windows/classpath.html
set CLASSPATH=%AGATE_HOME%\conf;%AGATE_DIST%\lib\*

set JAVA_DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n

rem Add %JAVA_DEBUG% to this line to enable remote JVM debugging (for developers)
java %JAVA_OPTS% -cp "%CLASSPATH%" -DAGATE_HOME="%AGATE_HOME%" -DAGATE_DIST=%AGATE_DIST% org.obiba.agate.Application %*
goto :END

:DEFAULT_JAVA_OPTS
set JAVA_OPTS=-Xms1G -Xmx2G -XX:MaxPermSize=256M -XX:+UseG1GC
goto :INVOKE

:JAVA_HOME_NOT_SET
echo JAVA_HOME not set
goto :END

:AGATE_HOME_NOT_SET
echo AGATE_HOME not set
goto :END

:END
