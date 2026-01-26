@REM Maven Wrapper script for Windows
@REM https://maven.apache.org/wrapper/

@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

if exist "%MAVEN_WRAPPER_PROPERTIES%" (
    for /f "tokens=1,* delims==" %%a in ('findstr "distributionUrl" "%MAVEN_WRAPPER_PROPERTIES%"') do set "DISTRIBUTION_URL=%%b"
)

if not defined DISTRIBUTION_URL set "DISTRIBUTION_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"

set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists"

if defined JAVA_HOME (
    set "JAVACMD=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVACMD=java.exe"
)

"%JAVACMD%" -version >nul 2>&1
if errorlevel 1 (
    echo Error: JAVA_HOME is not defined and java is not in the PATH.
    exit /b 1
)

if not exist "%MAVEN_HOME%" mkdir "%MAVEN_HOME%"

REM Download and run Maven
set "MAVEN_BIN="
for /r "%MAVEN_HOME%" %%f in (mvn.cmd) do if exist "%%f" set "MAVEN_BIN=%%f"

if not defined MAVEN_BIN (
    echo Downloading Maven from %DISTRIBUTION_URL%
    powershell -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%MAVEN_HOME%\maven.zip'"
    powershell -Command "Expand-Archive -Path '%MAVEN_HOME%\maven.zip' -DestinationPath '%MAVEN_HOME%'"
    del "%MAVEN_HOME%\maven.zip"
    for /r "%MAVEN_HOME%" %%f in (mvn.cmd) do if exist "%%f" set "MAVEN_BIN=%%f"
)

if not defined MAVEN_BIN (
    echo Error: Could not find Maven executable.
    exit /b 1
)

"%MAVEN_BIN%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" %*
