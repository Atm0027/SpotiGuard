@echo off
chcp 65001 >nul
title Instalador Oficial de Spotube (Android)
echo ========================================================
echo        INSTALADOR OFICIAL DE SPOTUBE (ANDROID)
echo ========================================================
echo.
echo Comprobando conexion con tu dispositivo...
adb devices
echo.
echo Instalando Spotube v5.1.2 en tu telefono...
adb install -r "Spotube-android-all-arch.apk"
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================================
    echo  [OK] Spotube instalado correctamente en tu telefono!
    echo ========================================================
    echo.
    echo Pasos para disfrutar de tu musica sin anuncios:
    echo 1. Abre Spotube en tu telefono.
    echo 2. Pulsa en "Conectar con Spotify" para ver todas tus playlists.
    echo 3. Disfruta de toda tu musica con CERO anuncios y sin cortes.
) else (
    echo.
    echo [ERROR] No se pudo instalar por ADB. Comprueba que el telefono este desbloqueado.
)
echo.
pause
