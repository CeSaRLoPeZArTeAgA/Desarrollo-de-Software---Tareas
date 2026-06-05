@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem ==========================================================
rem Citizen Voice PC3 - Compilador Java para Windows
rem Funciona aunque el proyecto este en rutas con espacios, por ejemplo:
rem D:\CICLO 2026-1 UNI\Desarrollo-de-Software---Tareas\Citizen_Voice_PC3
rem ==========================================================

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "PROJECT_ROOT=%%~fI"

pushd "%PROJECT_ROOT%" >nul
if errorlevel 1 (
    echo ERROR: No se pudo acceder a la carpeta raiz del proyecto: "%PROJECT_ROOT%"
    exit /b 1
)

if not exist "src\main\java" (
    echo ERROR: No existe src\main\java. Abra la carpeta Citizen_Voice_PC3 como raiz del proyecto.
    popd >nul
    exit /b 1
)

if not exist "build" mkdir "build"
if not exist "build\classes" mkdir "build\classes"
if exist "build\sources.txt" del "build\sources.txt"

rem IMPORTANTE:
rem En archivos @argfile de javac, las rutas absolutas de Windows con espacios y backslashes
rem pueden interpretarse mal. Por eso se escriben rutas RELATIVAS con slash (/).
for /R "src\main\java" %%F in (*.java) do (
    set "ABS=%%~fF"
    set "REL=!ABS:%CD%\=!"
    set "REL=!REL:\=/!"
    echo !REL!>>"build\sources.txt"
)

if not exist "build\sources.txt" (
    echo ERROR: No se encontraron archivos Java en src\main\java.
    popd >nul
    exit /b 1
)

for %%A in ("build\sources.txt") do (
    if %%~zA==0 (
        echo ERROR: No se encontraron archivos Java en src\main\java.
        popd >nul
        exit /b 1
    )
)

echo Compilando proyecto desde:
echo %CD%
echo.

javac -encoding UTF-8 -d "build/classes" @"build/sources.txt"
if errorlevel 1 (
    echo.
    echo ERROR: Fallo la compilacion. Revise los errores mostrados por javac.
    popd >nul
    exit /b 1
)

echo.
echo Compilacion completada correctamente en build\classes
popd >nul
endlocal
