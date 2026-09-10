@echo off
title Compilar SpotiSkip Mobile (APK)
cd /d "%~dp0"
echo ========================================================
echo         Compilador de SpotiSkip Mobile para Android
echo ========================================================
echo.
echo Para generar el APK instalable en tu movil:
echo.
echo METODO 1 (Recomendado - 1 Clic):
echo 1. Abre Android Studio.
echo 2. Elige "Open" y selecciona esta carpeta: %~dp0
echo 3. Pulsa "Build" -> "Build Bundle(s) / APK(s)" -> "Build APK(s)".
echo 4. El archivo .apk se generara en: app\build\outputs\apk\debug\app-debug.apk
echo.
echo METODO 2 (Instalacion directa por cable USB):
echo Conecta tu movil con Depuracion USB activada y pulsa "Run" (boton verde de Play) en Android Studio.
echo ========================================================
pause
