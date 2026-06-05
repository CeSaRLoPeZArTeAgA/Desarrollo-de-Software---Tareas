@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "PROJECT_ROOT=%%~fI"

pushd "%PROJECT_ROOT%" >nul
if errorlevel 1 (
    echo ERROR: No se pudo acceder a la carpeta raiz del proyecto.
    exit /b 1
)

call "%SCRIPT_DIR%compile.bat"
if errorlevel 1 (
    popd >nul
    exit /b 1
)

if not exist "src\test\java" (
    echo ERROR: No existe src\test\java.
    popd >nul
    exit /b 1
)

if not exist "build\test-classes" mkdir "build\test-classes"
if exist "build\test-sources.txt" del "build\test-sources.txt"

rem Se usan rutas relativas con slash (/) para evitar errores de javac con rutas Windows con espacios.
for /R "src\test\java" %%F in (*.java) do (
    set "ABS=%%~fF"
    set "REL=!ABS:%CD%\=!"
    set "REL=!REL:\=/!"
    echo !REL!>>"build\test-sources.txt"
)

if not exist "build\test-sources.txt" (
    echo ERROR: No se encontraron pruebas en src\test\java.
    popd >nul
    exit /b 1
)

for %%A in ("build\test-sources.txt") do (
    if %%~zA==0 (
        echo ERROR: No se encontraron pruebas en src\test\java.
        popd >nul
        exit /b 1
    )
)

javac -encoding UTF-8 -cp "build/classes" -d "build/test-classes" @"build/test-sources.txt"
if errorlevel 1 (
    echo ERROR: Fallo la compilacion de pruebas.
    popd >nul
    exit /b 1
)

java -cp "build/classes;build/test-classes" pe.edu.uni.fc.cc.citizenvoice.TestRunner
if errorlevel 1 (
    echo ERROR: Las pruebas fallaron.
    popd >nul
    exit /b 1
)

popd >nul
endlocal
