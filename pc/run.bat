@echo off
title SpotiSkip Launcher
cd /d "%~dp0"
echo ==============================================
echo       Iniciando SpotiSkip Guardian (PC)
echo ==============================================
python app.py %*
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Ocurrio un error al ejecutar SpotiSkip.
    echo Asegurate de haber instalado los requerimientos:
    echo pip install -r requirements.txt
    pause
)
