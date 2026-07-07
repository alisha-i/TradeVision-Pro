@echo off
echo ==============================================
echo TradeVision Pro - Launching
echo ==============================================

set JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot

if not exist "apache-maven-3.9.6\bin\mvn.cmd" (
    echo [ERROR] Maven not found. Please wait for the initial setup to complete.
    pause
    exit /b
)

echo Compiling and Starting Application...
call apache-maven-3.9.6\bin\mvn.cmd clean javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Application crashed or failed to start.
    pause
)
