@echo off
chcp 65001 >nul
title Instalador Oficial de Metrolist (Android)
echo ========================================================
echo        INSTALADOR OFICIAL DE METROLIST (ANDROID)
echo ========================================================
echo.
echo Comprobando conexion con tu telefono...
adb devices
echo.
echo Instalando Metrolist v13.7.0 en tu dispositivo...
adb install -r "Metrolist.apk"
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================================
    echo  [OK] Metrolist instalado correctamente en tu telefono!
    echo ========================================================
    echo.
    echo Ventajas de Metrolist:
    echo 1. Cero errores 401 (no requiere cuentas ni tokens de Spotify).
    echo 2. Cero publicidad y reproduccion en segundo plano / pantalla apagada.
    echo 3. Puedes pegar cualquier enlace de playlist de Spotify para importarla.
) else (
    echo.
    echo [ERROR] No se pudo instalar por ADB. Comprueba que el telefono este desbloqueado.
)
echo.
pause
