@echo off
chcp 65001 >nul
title Spotify Protegido con SpotiGuard

:: 1. Iniciar SpotiGuard en segundo plano en la bandeja si no está abierto
set APP_DIR=%~dp0
set EXECUTABLE=%APP_DIR%dist\SpotiSkip\SpotiSkip.exe

if exist "%EXECUTABLE%" (
    start "" "%EXECUTABLE%" --minimized
) else (
    where python >nul 2>nul
    if %errorlevel% equ 0 (
        start "" python "%APP_DIR%pc\app.py" --minimized
    )
)

:: 2. Iniciar Spotify de inmediato
explorer.exe shell:AppsFolder\SpotifyAB.SpotifyMusic_zpdnekdrzrea0!Spotify >nul 2>nul
if %errorlevel% neq 0 (
    start "" "spotify:"
)
