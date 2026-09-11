# ⚡ SpotiGuard: Autonomous Spotify Ad Guardian & Bypass (PC & Android)

[![GitHub Repository](https://img.shields.io/badge/GitHub-Atm0027%2FSpotiGuard-blue?logo=github)](https://github.com/Atm0027/SpotiGuard)
[![Latest Release](https://img.shields.io/github/v/release/Atm0027/SpotiGuard?color=orange&logo=github)](https://github.com/Atm0027/SpotiGuard/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Android-green)](#)
[![Python](https://img.shields.io/badge/Python-3.10%2B-blue?logo=python)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-Android%20Native-purple?logo=kotlin)](#)
[![License](https://img.shields.io/badge/License-MIT-yellow)](#)

**SpotiGuard** es una solución de protección multimedia independiente, ultrarrápida y multiplataforma para **PC (Windows)** y **Móvil (Android)** que neutraliza automáticamente los anuncios publicitarios de Spotify mediante la técnica de **Cierre Forzoso Asistido y Reinicio Limpio (Force-Stop & Relaunch Bypass con Accesibilidad / Root / Windows SMTC y Silenciamiento Cero-Latencia)**.

El sistema se ejecuta en segundo plano y **se activa de forma 100% autónoma en cuanto detecta que Spotify se ha iniciado**, tanto en tu ordenador como en tu dispositivo móvil.

---

### 📥 Descargas Directas Listas para Usar

* 📱 **Móvil (Android)**: **[Descargar SpotiGuard-1.0.4-14.apk](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.4-14/SpotiGuard-1.0.4-14.apk)** *(4.74 MB — Cierre forzoso por Accesibilidad/Root para Android 14+, silenciador de seguridad 0ms, watchdog anti-anuncios silenciosos, ocultación dinámica de requisitos en interfaz)*
* 🖥️ **PC (Windows)**: **[Descargar SpotiGuard-Windows-1.0.0-4.zip](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.0-4/SpotiGuard-Windows-1.0.0-4.zip)** *(47.9 MB — Paquete portable con ejecutable de 64 bits y reanudación Windows SMTC)*
* 🚀 **Lanzador Conjunto en PC (1 Clic)**: **[`Spotify (Protegido con SpotiGuard).lnk`](Spotify%20(Protegido%20con%20SpotiGuard).lnk)** *(Abre Spotify y SpotiGuard juntos de forma simultánea)*
* 🔌 **Instalador Rápido por ADB para Android desde PC**: **[`instalar_android.bat`](instalar_android.bat)** *(Instalación en 1 clic en tu móvil vía cable USB o Wi-Fi sin advertencias de navegador)*
* 🌐 **Repositorio Oficial en GitHub**: **[https://github.com/Atm0027/SpotiGuard](https://github.com/Atm0027/SpotiGuard)**
* 🚀 **Última Release Oficial (v1.0.4-14)**: **[https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.4-14](https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.4-14)**

---

## ⚡ Activación Automática al Abrir Spotify (PC y Android)

Tanto en **Windows** como en **Android**, SpotiGuard está diseñado para funcionar **sin que tengas que acordarte de abrir la app**:

### 🖥️ En PC (Windows):
1. **Auto-arranque con Windows**: SpotiGuard se inicia automáticamente con el ordenador minimizado en la bandeja del sistema (`System Tray`), consumiendo 0% de CPU en reposo.
2. **Detección Reactiva Rápida (0.7s)**: En cuanto abres la app oficial de Spotify (o la versión web/Store), SpotiGuard detecta el proceso de inmediato y activa la protección.
3. **Acceso Directo Unificado**: Puedes usar **`Spotify (Protegido con SpotiGuard).lnk`** o el script **`Lanzar Spotify Protegido.bat`** para lanzar ambos programas a la vez con un solo clic.

### 📱 En Móvil (Android):
1. **Auto-inicio al encender el móvil (`BootReceiver`)**: SpotiGuard se reactiva automáticamente al reiniciar el teléfono y permanece en espera de bajo consumo.
2. **Auto-despertar con Spotify (`SpotifyLaunchReceiver`)**: En cuanto Spotify empieza a reproducir música, Android emite el evento `com.spotify.music.metadatachanged` y SpotiGuard se activa automáticamente si no estaba corriendo.
3. **Lanzador Directo desde la App**: Si abres SpotiGuard y pulsas **"🚀 Abrir Spotify"**, el servicio se inicia y abre Spotify en pantalla simultáneamente.
4. **Detección Dinámica de Requisitos en Interfaz**: Los botones de *"Estado de emisión"*, *"Optimización de batería"* y *"Accesibilidad SpotiGuard"* **se ocultan automáticamente de la app en cuanto el sistema detecta que están concedidos**, mostrando un recuadro verde de verificación completa.

---

## 🔬 ¿Cómo Funciona Técnicamente el Cierre Forzoso y Salto de Anuncios?

### 1. El Reto de Android 14+: ¿Por qué las apps estándar no podían cerrar Spotify?
* **Restricción de seguridad de Google**: A partir de Android 14, el comando clásico `killBackgroundProcesses` fue deshabilitado para paquetes ajenos. Las aplicaciones ordinarias ya no tienen permiso para forzar el cierre de otras apps.
* **Consecuencia anterior**: La app detectaba el anuncio, pero Spotify nunca se cerraba; al volverlo a abrir, el anuncio seguía congelado en su búfer y se reanudaba.
* **La Solución SpotiGuard v1.0.4-14**:
  - **Servicio de Accesibilidad (`SpotiGuardAccessibilityService`)**: Diseñado para automatizar la pulsación de *"Forzar detención"* y *"Aceptar"* en los Ajustes de Android en menos de 250 ms. Spotify es fulminado por completo a nivel de proceso.
  - **Soporte Root Inmediato**: Si el terminal dispone de Root (`su`), ejecuta `am force-stop com.spotify.music` en 15 ms en segundo plano transparente.
  - **Silenciamiento de Seguridad Cero-Latencia (`AudioController`)**: En el instante 0 ms en que se intercepta un anuncio, el volumen del stream multimedia se reduce al 0% para garantizar que el usuario nunca escuche cuñas comerciales mientras se completa el reinicio y relanzamiento.

### 2. Detección Dual de Anuncios en Android
1. **Watchdog de Fin de Canción**: Spotify **no emite broadcasts al comenzar un anuncio**. SpotiGuard calcula la duración exacta de la pista musical activa y programa un watchdog. Si al terminar no llega una nueva canción, deduce la presencia del anuncio y salta inmediatamente.
2. **Detección Reactiva por ID y Metadatos**: Captura cuñas comerciales con IDs no estándar (`!id.startsWith("spotify:track:")`), menciones publicitarias ("Publicidad", "Advertisement", "Spotify Free", "Werbung", etc.).

### 3. Secuencia Atómica de Salto en Android
1. **Silenciamiento instantáneo**: `AudioController.mute()` en 0 ms.
2. **Detención multimedia**: `sendMediaStop()` (`KEYCODE_MEDIA_PAUSE` y `STOP`).
3. **Cierre forzoso de Spotify**: Mediante `su am force-stop` (Root) o automatización de `SpotiGuardAccessibilityService`.
4. **Relanzamiento limpio**: Apertura de Spotify sin anuncio en búfer.
5. **Purga y Reanudación**: Envío de `KEYCODE_MEDIA_NEXT` y `KEYCODE_MEDIA_PLAY`.
6. **Restauración de volumen**: `AudioController.unmute()` restaura el nivel exacto de volumen para la música.

---

## 🖥️ Aplicación para PC (Windows)

Ubicada en la carpeta [`pc/`](pc/).

### Características Principales:
* **Interfaz Gráfica HUD Futurista**: Diseñada en PyQt6 con tema oscuro moderno, indicadores de estado LED, título de pista en tiempo real y contador de anuncios evitados y tiempo ahorrado.
* **Integración con la Bandeja del Sistema (System Tray)**: Permite minimizar la aplicación discretamente junto al reloj de Windows. Notifica cada vez que un anuncio es saltado.
* **Auto-activación con Spotify**: Un vigilante (*Watcher*) en segundo plano detecta cuándo abres Spotify y pone en marcha la protección sin que tengas que pulsar nada.
* **Auto-arranque con Windows**: Registrado en el inicio de Windows para vigilar de fondo sin ventanas molestas.
* **Dos Modos de Operación**:
  1. **Reinicio Rápido (Skip & Relaunch)**: Cierra y reabre Spotify instantáneamente en ~2-4 segundos, saltando a la siguiente canción real.
  2. **Silenciador Furtivo (Stealth Mute)**: Muta al 0% el volumen exclusivo de Spotify mientras dura el anuncio y lo restablece exactamente cuando empieza la canción.

---

## 📱 Aplicación para Móvil (Android)

Ubicada en la carpeta [`mobile/`](mobile/). Proyecto nativo completo en **Kotlin**.

### Novedades v1.0.4-14:
* **Cierre Forzoso Garantizado (Force-Stop Real)**:
  - Resuelve de raíz el fallo de Android 14+ donde Spotify no se cerraba.
  - Implementación de `SpotiGuardAccessibilityService` para forzar la detención limpia de Spotify en milisegundos.
  - Soporte para cierre inmediato vía Root (`su -c am force-stop`).
* **Silenciamiento de Seguridad (0 ms)**: Silencia el audio en el instante de detectar la publicidad y lo devuelve al sonar la música, evitando cualquier sonido publicitario.
* **Ocultación Dinámica de Requisitos en la UI**:
  - Botón de emisión de Spotify desaparece en cuanto Spotify envía el primer evento.
  - Botón de optimización de batería desaparece en cuanto la app tiene permiso sin restricciones.
  - Botón de accesibilidad desaparece en cuanto el servicio de accesibilidad de SpotiGuard está activado.
  - Mensaje verde de éxito cuando todos los requisitos están cumplidos.
* **Purga de Búfer Robusta**: Envío de evento Play/Next garantizado tras la reapertura de la aplicación.

### 📲 Descarga e Instalación del APK Oficial:
* **Descarga directa**: **[Descargar SpotiGuard-1.0.4-14.apk (v1.0.4-14)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.4-14/SpotiGuard-1.0.4-14.apk)** *(4.74 MB)*.
* **Firma Oficial**: Almacén de claves RSA 2048-bit (`CN=SpotiGuard`) con esquemas **v2** y **v3**.

#### 🛑 ¿Android muestra "Bloqueada por Play Protect"? (Solución en 10s):
Debido a que SpotiGuard integra un Servicio de Accesibilidad para forzar el cierre de Spotify automáticamente, Google Play Protect lo clasifica como advertencia preventiva en descargas fuera de la tienda:
1. **Opción Rápida**: Pulsa en **"Más detalles"** y luego en **"Instalar de todas formas (no seguro)"**.
2. **Si no te deja pulsar**: Abre **Google Play Store -> Perfil -> Play Protect -> Ajustes ⚙️** y desactiva temporalmente *"Analizar las aplicaciones con Play Protect"*. Tras instalarlo, puedes volver a activarlo.
3. **Instalación USB sin advertencias**: Conecta el móvil al PC y ejecuta `instalar_android.bat` para instalar directamente por ADB.

---

## 🏷️ Sistema de Control de Versiones Heredado de JARVIS

| Componente | Formato | Ejemplo en SpotiGuard | Significado |
| :--- | :--- | :--- | :--- |
| **`versionName`** | SemVer `X.Y.Z` | `1.0.4` | Nombre semántico deducido automáticamente de Conventional Commits. |
| **`versionCode`** | Entero creciente | `14` | Recuento estricto de commits (`git rev-list --count HEAD`). Nunca retrocede. |
| **Etiqueta Git (Tag)** | `v<versionName>-<versionCode>` | `v1.0.4-14` | El estado vive en las etiquetas de git. |
| **Título de Release** | `<App> <versionName> (<versionCode>)` | `SpotiGuard 1.0.4 (14)` | Título estandarizado para publicaciones de GitHub. |
| **Paquete Windows** | `<App>-Windows-<versionName>-<versionCode>.zip` | `SpotiGuard-Windows-1.0.0-4.zip` | Binario portable empaquetado para PC. |
| **Paquete Android** | `<App>-<versionName>-<versionCode>.apk` | `SpotiGuard-1.0.4-14.apk` | Paquete APK firmado para móviles. |

---

## 📁 Estructura del Proyecto

```text
Ads Spotify/
├── Spotify (Protegido con SpotiGuard).lnk # Acceso directo conjunto para abrir Spotify + SpotiGuard
├── SpotiSkip (Iniciar App).lnk    # Acceso directo para abrir la app de SpotiGuard en PC
├── SpotiGuard-1.0.4-14.apk        # Paquete APK oficial limpio y firmado para Android (4.74 MB)
├── Lanzar Spotify Protegido.bat   # Script para lanzar Spotify y SpotiGuard en PC
├── instalar_android.bat           # Instalador automático en 1 clic para móvil vía ADB
├── README.md                      # Documentación centralizada del proyecto
├── pc/                            # Código fuente y componentes de PC
│   ├── app.py                     # Controlador principal con Mutex e inicio con Windows
│   └── core/
│       ├── watcher.py             # Vigilante ultrarrápido (0.7s) reactivo a Spotify
│       ├── detector.py            # Inspección híbrida SMTC + títulos Win32 + pycaw
│       ├── skipper.py             # Cierre atómico dual, Next Track y reanudación SMTC
│       ├── audio_muter.py         # Control de volumen individual con CoreAudio
│       └── version.py             # Sistema unificado de versiones de JARVIS
└── mobile/                        # Aplicación nativa para Móvil (Android)
    ├── README_ANDROID.md          # Guía técnica y de instalación para Android
    └── app/src/main/
        ├── AndroidManifest.xml    # Manifiesto con AccessibilityService, BootReceiver y SpotifyLaunchReceiver
        ├── java/com/spotiskip/guardian/
        │   ├── MainActivity.kt    # UI reactiva con autodesaparición de requisitos y gestión de accesibilidad
        │   ├── receivers/
        │   │   ├── BootReceiver.kt          # Auto-arranque al encender el teléfono
        │   │   └── SpotifyLaunchReceiver.kt # Auto-activación al recibir emisiones de Spotify
        │   ├── services/
        │   │   ├── SpotiGuardService.kt              # Foreground Service con Watchdog de fin de pista
        │   │   └── SpotiGuardAccessibilityService.kt # Automatización de cierre forzoso (Force-Stop)
        │   └── utils/
        │       ├── AudioController.kt       # Silenciamiento de seguridad instantáneo (0ms)
        │       └── SpotifyController.kt     # Orquestador Force-Stop (A11y/Root) + Relaunch + Play
        └── res/                   # Layouts con botones dinámicos y configuración de accesibilidad
```

---

## ⚖️ Aviso Legal
Este software ha sido desarrollado con fines de investigación educativa en automatización de interfaces, gestión de procesos en sistemas operativos y accesibilidad de audio.
