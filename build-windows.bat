@echo off
REM Build script for Windows MSI installer

echo ========================================
echo Garden Simulator - Windows Build Script
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 21 JDK
    pause
    exit /b 1
)

REM Check if jpackage is available
where jpackage >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: jpackage is not available
    echo Please install Java 21 JDK (jpackage is included)
    pause
    exit /b 1
)

REM Check if WiX Toolset is installed
where candle >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: WiX Toolset not found in PATH
    echo MSI creation may fail. Install from https://wixtoolset.org/
    echo.
)

echo Step 1: Cleaning previous builds...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven clean failed
    pause
    exit /b 1
)

echo.
echo Step 2: Building JAR with dependencies...
call mvn package
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven package failed
    pause
    exit /b 1
)

echo.
echo Step 3: Creating MSI installer...
call jpackage ^
  --input target ^
  --name GardenSimulator ^
  --main-jar garden-simulator-1.0.0-jar-with-dependencies.jar ^
  --main-class ua.notion.presentation.GardenSimulatorApp ^
  --type msi ^
  --dest target/dist ^
  --app-version 1.0.0 ^
  --vendor Notion ^
  --win-menu ^
  --win-dir-chooser ^
  --win-shortcut ^
  --java-options "-Dfile.encoding=UTF-8"

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: jpackage failed
    echo Make sure WiX Toolset is installed and in PATH
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build completed successfully!
echo ========================================
echo.
echo MSI installer location:
echo target\dist\GardenSimulator-1.0.0.msi
echo.
pause
