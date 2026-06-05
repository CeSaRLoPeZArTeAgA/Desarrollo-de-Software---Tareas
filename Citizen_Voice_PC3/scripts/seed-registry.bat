@echo off
setlocal EnableExtensions

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."

pushd "%PROJECT_ROOT%" >nul
if errorlevel 1 exit /b 1

call "%SCRIPT_DIR%compile.bat"
if errorlevel 1 (
    popd >nul
    exit /b 1
)

rem Valores por defecto de la PC3: 100000 ciudadanos, RSA 2048 bits.
rem Puede cambiarse pasando parametros:
rem scripts\seed-registry.bat 1000 1024 4
set "COUNT=%~1"
set "KEY_SIZE=%~2"
set "WORKERS=%~3"
if "%COUNT%"=="" set "COUNT=100000"
if "%KEY_SIZE%"=="" set "KEY_SIZE=2048"
if "%WORKERS%"=="" set "WORKERS=8"

echo Generando padron ciudadano: %COUNT% personas, RSA %KEY_SIZE%, workers=%WORKERS%
java -cp "build\classes" pe.edu.uni.fc.cc.citizenvoice.app.CitizenVoiceCli seed --count %COUNT% --key-size %KEY_SIZE% --workers %WORKERS%
if errorlevel 1 (
    echo ERROR: Fallo la generacion del padron.
    popd >nul
    exit /b 1
)

popd >nul
endlocal
