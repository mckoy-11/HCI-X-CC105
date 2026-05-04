@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================
echo Building WCMS Project...
echo ========================================
echo.

cd /d "c:\Users\Micko Jay\Documents\NetBeansProjects\wcms" || exit /b 1

set JAVA_HOME=C:\Program Files\Java\jdk-11
set PATH=!JAVA_HOME!\bin;!PATH!

REM Compile all Java source files - javac can handle -sourcepath to find all files
echo Compiling all Java sources...

javac -encoding UTF-8 -sourcepath src/main -d build/classes/main -cp "build/classes/main;jars/*" @src_files.txt 2>&1

if !errorlevel! neq 0 (
    echo.
    echo BUILD FAILED with compilation errors!
    echo.
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo.
echo ========================================
echo Running WCMS Application...
echo ========================================
echo.

java -cp "build/classes/main;jars/*" main.app.MainFrame

pause
