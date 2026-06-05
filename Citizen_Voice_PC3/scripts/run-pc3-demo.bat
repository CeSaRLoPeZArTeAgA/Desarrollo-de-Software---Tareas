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

rem Uso: scripts\run-pc3-demo.bat [proposal_id] [sample_size]
set "PROPOSAL_ID=%~1"
set "SAMPLE_SIZE=%~2"
if "%PROPOSAL_ID%"=="" set "PROPOSAL_ID=1"
if "%SAMPLE_SIZE%"=="" set "SAMPLE_SIZE=25000"

java -cp "build\classes" pe.edu.uni.fc.cc.citizenvoice.app.CitizenVoiceCli run-demo --proposal-id %PROPOSAL_ID% --sample-size %SAMPLE_SIZE%
if errorlevel 1 (
    echo ERROR: Fallo la demo PC3.
    popd >nul
    exit /b 1
)

popd >nul
endlocal
