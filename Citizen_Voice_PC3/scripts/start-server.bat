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

if "%CITIZEN_VOICE_PORT%"=="" set "CITIZEN_VOICE_PORT=8080"
echo Iniciando Citizen Voice PC3 en http://127.0.0.1:%CITIZEN_VOICE_PORT%
java -cp "build\classes" pe.edu.uni.fc.cc.citizenvoice.app.CitizenVoiceServer

popd >nul
endlocal
