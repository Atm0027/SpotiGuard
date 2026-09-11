@echo off
chcp 65001 >nul
echo ========================================================
echo    SpotiGuard - Activador de Cierre Silencioso (Shizuku)
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

echo [1/3] Verificando telefono Android conectado por USB...
"%ADB_PATH%" devices

for /f "tokens=1,2" %%A in ('"%ADB_PATH%" devices') do (
    if "%%B"=="device" (
        set DEVICE_FOUND=1
    )
)

if not defined DEVICE_FOUND (
    echo.
    echo [AVISO] No se detecto ningun dispositivo Android conectado.
    echo Pasos:
    echo  1. Conecta tu telefono al PC con el cable USB.
    echo  2. Asegurate de tener activada 'Depuracion por USB' en Opciones de desarrollador.
    echo  3. Acepta el aviso 'Permitir depuracion USB' en la pantalla de tu movil.
    echo.
    pause
    exit /b 1
)

echo.
echo [2/3] Verificando instalacion de la app oficial Shizuku...
"%ADB_PATH%" shell pm path moe.shizuku.privileged.api >nul 2>&1
if %errorlevel% neq 0 (
    echo Instalando Shizuku oficial en tu telefono...
    if exist "%~dp0shizuku.apk" (
        "%ADB_PATH%" install -r "%~dp0shizuku.apk"
    ) else (
        echo [AVISO] Descarga o instala Shizuku desde Google Play Store en tu movil.
    )
) else (
    echo Shizuku ya esta instalada en tu telefono.
)

echo.
echo [3/3] Iniciando el servicio Shizuku en segundo plano (sin tocar la pantalla)...
"%ADB_PATH%" shell "PKG_DIR=$(pm path moe.shizuku.privileged.api | head -n 1 | cut -d: -f2 | sed 's/base.apk//'); if [ -n "$PKG_DIR" ]; then ${PKG_DIR}lib/arm64/libshizuku.so 2>/dev/null || ${PKG_DIR}lib/arm/libshizuku.so 2>/dev/null || sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh 2>/dev/null; fi" 

echo.
echo ========================================================
echo   [OK] SHIZUKU HA SIDO INICIADO CON EXITO EN TU MOVIL
echo ========================================================
echo.
echo Proximos pasos (solo se hace una vez):
echo  1. Abre la aplicacion SpotiGuard en tu telefono.
echo  2. Pulsa en 'Conceder Permiso Shizuku' y selecciona 'Permitir siempre'.
echo.
echo A partir de ahora, cuando Spotify ponga un anuncio:
echo  - Spotify se CERRARA POR COMPLETO en 0.05s (identico a PC).
echo  - Se volvera a abrir limpia al instante.
echo  - Pulsara Play automaticamente para reanudar tu musica.
echo  - NUNCA MAS se abrira ninguna pantalla de Ajustes.
echo.
pause
