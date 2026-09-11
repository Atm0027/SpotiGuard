@echo off
chcp 65001 >nul
echo ========================================================
echo    SpotiGuard - Instalador Directo en Dispositivo Android
echo ========================================================
echo.

set ADB_PATH=C:\Users\Alex\AppData\Local\Android\Sdk\platform-tools\adb.exe
if not exist "%ADB_PATH%" (
    where adb >nul 2>nul
    if %errorlevel% equ 0 (
        set ADB_PATH=adb
    ) else (
        echo [ERROR] No se encontro adb.exe en el sistema.
        echo Asegurate de tener activada la Depuracion por USB en tu movil.
        pause
        exit /b 1
    )
)

echo [1/3] Verificando dispositivos Android conectados...
"%ADB_PATH%" devices

for /f "tokens=1,2" %%A in ('"%ADB_PATH%" devices') do (
    if "%%B"=="device" (
        set DEVICE_FOUND=1
    )
)

if not defined DEVICE_FOUND (
    echo.
    echo [AVISO] No se detecto ningun dispositivo Android en modo 'device'.
    echo Pasos para conectar tu telefono:
    echo  1. Conecta tu movil al PC por cable USB.
    echo  2. En el movil ve a: Ajustes ^> Opciones de desarrollador ^> Depuracion por USB.
    echo  3. Acepta el mensaje 'Permitir depuracion USB' en la pantalla del movil.
    echo.
    echo O bien, puedes pasar el archivo .apk a tu movil y abrirlo con la app
    echo 'Mis Archivos' / 'Files' de tu telefono en lugar de descargarlo desde Chrome.
    echo.
    pause
    exit /b 1
)

echo.
echo [2/3] Buscando el APK mas reciente de SpotiGuard...
set APK_FILE=
for /f "delims=" %%F in ('dir /b /o:-d "%~dp0SpotiGuard-*.apk" 2^>nul') do (
    if not defined APK_FILE set APK_FILE=%~dp0%%F
)
if not defined APK_FILE (
    for /f "delims=" %%F in ('dir /b /s /o:-d "%~dp0mobile\app\build\outputs\apk\release\SpotiGuard-*.apk" 2^>nul') do (
        if not defined APK_FILE set APK_FILE=%%F
    )
)

if not defined APK_FILE (
    echo [ERROR] No se encontro ningun archivo APK compilado.
    pause
    exit /b 1
)

echo [INFO] Instalando: %APK_FILE%
echo.
echo [3/3] Ejecutando instalacion via ADB...
"%ADB_PATH%" install -r -d "%APK_FILE%"

if %errorlevel% equ 0 (
    echo.
    echo ========================================================
    echo  INSTALACION DE SPOTIGUARD COMPLETADA CON EXITO!
    echo ========================================================
    
    rem Instalar y arrancar Shizuku para cierre silencioso
    if exist "%~dp0shizuku.apk" (
        echo [INFO] Configurando Shizuku para cierre forzoso silencioso...
        "%ADB_PATH%" install -r "%~dp0shizuku.apk" >nul 2>&1
        "%ADB_PATH%" shell "PKG_DIR=$(pm path moe.shizuku.privileged.api | head -n 1 | cut -d: -f2 | sed 's/base.apk//'); if [ -n "$PKG_DIR" ]; then ${PKG_DIR}lib/arm64/libshizuku.so 2>/dev/null || ${PKG_DIR}lib/arm/libshizuku.so 2>/dev/null || sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh 2>/dev/null; fi" 
    )
    
    echo Ya puedes abrir SpotiGuard en tu movil.
) else (
    echo.
    echo [ERROR] La instalacion por ADB fallo. Codigo de error: %errorlevel%
)

echo.
pause
