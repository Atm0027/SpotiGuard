# ⚡ SpotiGuard: Autonomous Spotify Ad Guardian & Bypass (PC & Android)

[![GitHub Repository](https://img.shields.io/badge/GitHub-Atm0027%2FSpotiGuard-blue?logo=github)](https://github.com/Atm0027/SpotiGuard)
[![Latest Release](https://img.shields.io/github/v/release/Atm0027/SpotiGuard?color=orange&logo=github)](https://github.com/Atm0027/SpotiGuard/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Android-green)](#)
[![Python](https://img.shields.io/badge/Python-3.10%2B-blue?logo=python)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-Android%20Native-purple?logo=kotlin)](#)
[![License](https://img.shields.io/badge/License-MIT-yellow)](#)

**SpotiGuard** es una solución de protección multimedia independiente, ultrarrápida y multiplataforma para **PC (Windows)** y **Móvil (Android)** que neutraliza automáticamente los anuncios publicitarios de Spotify mediante la técnica de **Cierre Forzoso y Relanzamiento Asistido (Windows SMTC / Android Accessibility & Root)**.

El sistema se ejecuta en segundo plano y **se activa de forma 100% autónoma en cuanto detecta que Spotify se ha iniciado**, tanto en tu ordenador como en tu dispositivo móvil.

---

### 📥 Descargas Directas Listas para Usar

* 📱 **Móvil (Android)**: **[`SpotiGuard-1.0.8-19.apk`](SpotiGuard-1.0.8-19.apk)** *(4.53 MB — Cierre forzoso silencioso a nivel de sistema idéntico a PC, cero ventanas de Ajustes, Shizuku & Root, sin mutear)*
* 🖥️ **PC (Windows)**: **[`SpotiGuard-Windows-1.0.0-4.zip`](SpotiGuard-Windows-1.0.0-4.zip)** *(47.9 MB — Paquete portable con ejecutable de 64 bits y reanudación Windows SMTC)*
* 🚀 **Lanzador Conjunto en PC (1 Clic)**: **[`Spotify (Protegido con SpotiGuard).lnk`](Spotify%20(Protegido%20con%20SpotiGuard).lnk)** *(Abre Spotify y SpotiGuard juntos de forma simultánea)*
* 🔌 **Instalador Rápido por ADB para Android desde PC**: **[`instalar_android.bat`](instalar_android.bat)** *(Instalación en 1 clic de SpotiGuard y Shizuku en tu móvil vía USB)*
* ⚡ **Activador Shizuku en 1 Clic**: **[`activar_shizuku.bat`](activar_shizuku.bat)** *(Arranca el servicio de cierre silencioso en tu móvil en 2 segundos)*
* 🌐 **Repositorio Oficial en GitHub**: **[https://github.com/Atm0027/SpotiGuard](https://github.com/Atm0027/SpotiGuard)**
* 🚀 **Última Release Oficial (v1.0.8-19)**: **[https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.8-19](https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.8-19)**

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
4. **Detección Dinámica de Requisitos en Interfaz**: Los botones de *"Estado de emisión"*, *"Optimización de batería"* y *"Cierre Silencioso (Shizuku)"* **se ocultan automáticamente de la app en cuanto el sistema detecta que están concedidos**, mostrando un recuadro verde de verificación completa.

---

## 🔬 ¿Cómo Funciona Técnicamente la Neutralización de Anuncios en Android?

### 1. Bypass Puro sin Muteo (Cierre Forzoso Real a Nivel de Kernel):
* **Cero Silenciamiento**: La app no silencia el audio ni enmascara la publicidad; destruye el proceso de Spotify en el acto para purgar el anuncio del búfer de reproducción.
* **Cierre Silencioso Instantáneo mediante Shizuku (`am force-stop`)**: Al detectar un anuncio, ejecuta la orden de sistema `am force-stop com.spotify.music` a nivel de proceso en 0.05 segundos en segundo plano.
* **Cero Pantallas de Ajustes**: Se ha eliminado por completo la apertura de la ventana de Información de la Aplicación y los clics de accesibilidad. Spotify se cierra de forma 100% invisible sin interrumpir tus juegos ni tus aplicaciones en uso.
* **Soporte Root Directo**: En teléfonos con permisos Root, ejecuta `su am force-stop com.spotify.music` instantáneamente.

### 2. Detección Dual de Anuncios en Android:
1. **Watchdog de Fin de Canción**: Spotify **no emite broadcasts al comenzar un anuncio**. SpotiGuard calcula la duración exacta de la pista musical activa y programa un watchdog. Si al terminar no llega una nueva canción, deduce la presencia del anuncio y activa el cierre forzoso de inmediato.
2. **Detección Reactiva por ID y Metadatos**: Captura cuñas comerciales con IDs no estándar (`!id.startsWith("spotify:track:")`), menciones publicitarias ("Publicidad", "Advertisement", "Spotify Free", "Werbung", etc.).

### 3. Secuencia de Neutralización y Reanudación Garantizada (Triple Play Dispatch):
1. **Detención multimedia**: `sendMediaStop()` (`KEYCODE_MEDIA_PAUSE` y `STOP`). Reset de estados (`isPlaybackActive = false`).
2. **Cierre forzoso de Spotify**: Mediante Shizuku (`am force-stop` atómico) o Root en segundo plano (<50ms) sin desplegar ventanas ni tocar la pantalla.
3. **Reactivación 100% en Segundo Plano (Sin Interrumpir tu Pantalla)**: Despertar silencioso del proceso y motor de audio de Spotify mediante broadcast explícito a `MediaButtonReceiver` y Shizuku en segundo plano. Cero ventanas emergentes: si estás jugando (por ejemplo, a Brawl Stars) o usando otra app, Spotify se reactiva de fondo sin quitarte el foco visual.
4. **Purga y salto multi-canal**: `KEYCODE_MEDIA_NEXT` + broadcast interno de widget `ui.widget.NEXT` + `cmd media_session dispatch next`.
5. **Reanudación garantizada (Triple Play Dispatch)**: Envío simultáneo de `KEYCODE_MEDIA_PLAY` nativo + broadcast de widget `ui.widget.PLAY` exclusivo de Spotify + `cmd media_session dispatch play` en 4 pulsos adaptativos (1.8s, 2.8s, 3.5s y 4.5s) que despiertan el motor de audio en frío sin intervención del usuario.

---

## 🖥️ Aplicación para PC (Windows)

Ubicada en la carpeta [`pc/`](pc/).

### Características Principales:
* **Interfaz Gráfica HUD Futurista**: Diseñada en PyQt6 con tema oscuro moderno, indicadores de estado LED, título de pista en tiempo real y contador de anuncios evitados y tiempo ahorrado.
* **Integración con la Bandeja del Sistema (System Tray)**: Permite minimizar la aplicación discretamente junto al reloj de Windows. Notifica cada vez que un anuncio es saltado.
* **Auto-activación con Spotify**: Un vigilante (*Watcher*) en segundo plano detecta cuándo abres Spotify y pone en marcha la protección sin que tengas que pulsar nada.
* **Auto-arranque con Windows**: Registrado en el inicio de Windows para vigilar de fondo sin ventanas molestas.
* **Modo Reinicio Rápido (Skip & Relaunch)**: Cierra y reabre Spotify instantáneamente en ~2-4 segundos, saltando a la siguiente canción real.

---

## 📱 Aplicación para Móvil (Android)

Ubicada en la carpeta [`mobile/`](mobile/). Proyecto nativo completo en **Kotlin**.

### Novedades v1.0.8-19:
* **Reapertura Silenciosa 100% en Segundo Plano (Zero UI / Cero Interrupciones)**:
  - Al neutralizar un anuncio, Spotify se reactiva **completamente en segundo plano** mediante broadcasts dirigidos a `MediaButtonReceiver` y Shizuku.
  - **Cero ventanas emergentes**: No interrumpe tus juegos en pantalla (Brawl Stars, etc.), ni tus chats ni la navegación web. La música vuelve a sonar sin que la pantalla cambie en absoluto.
* **Reanudación Multi-Canal (Triple Play Dispatch & Widget Broadcast)**:
  - **Triple canal de reproducción**: Envío coordinado de `KeyEvent.KEYCODE_MEDIA_PLAY` (idempotente), broadcast oficial de widget de Spotify (`com.spotify.mobile.android.ui.widget.PLAY`) y despacho por consola del sistema (`cmd media_session dispatch play`).
  - **4 Pulsos Escalonados de Despertar (1.8s, 2.8s, 3.8s, 4.8s)**: Garantiza que la música vuelva a sonar en cuanto el motor de audio en frío de Spotify termina de inicializarse.
* **Cierre Forzoso Silencioso Corregido (Shizuku & Root)**:
  - Corregido el manejo de terminación de proceso remoto (`ShizukuRemoteProcess.waitFor()`) eliminando excepciones de Binder para un cierre atómico en <50ms.
* **Aislamiento Total de Privacidad (Cero Accesibilidad)**:
  - Servicio de accesibilidad y permisos invasivos eliminados 100% de la app. SpotiGuard solo interactúa con el paquete `com.spotify.music`.

### 📲 Descarga e Instalación del APK Oficial:
* **Descarga directa**: **[`SpotiGuard-1.0.8-19.apk`](SpotiGuard-1.0.8-19.apk)** *(4.53 MB)*.
* **Firma Oficial**: Almacén de claves RSA 2048-bit (`CN=SpotiGuard`) con esquemas **v2** y **v3**.

---

## 🏷️ Sistema de Control de Versiones Heredado de JARVIS

| Componente | Formato | Ejemplo en SpotiGuard | Significado |
| :--- | :--- | :--- | :--- |
| **`versionName`** | SemVer `X.Y.Z` | `1.0.8` | Nombre semántico deducido automáticamente de Conventional Commits. |
| **`versionCode`** | Entero creciente | `18` | Recuento estricto de commits (`git rev-list --count HEAD`). Nunca retrocede. |
| **Etiqueta Git (Tag)** | `v<versionName>-<versionCode>` | `v1.0.8-19` | El estado vive en las etiquetas de git. |
| **Título de Release** | `<App> <versionName> (<versionCode>)` | `SpotiGuard 1.0.8 (18)` | Título estandarizado para publicaciones de GitHub. |
| **Paquete Windows** | `<App>-Windows-<versionName>-<versionCode>.zip` | `SpotiGuard-Windows-1.0.0-4.zip` | Binario portable empaquetado para PC. |
| **Paquete Android** | `<App>-<versionName>-<versionCode>.apk` | `SpotiGuard-1.0.8-19.apk` | Paquete APK firmado para móviles. |

---

## 📁 Estructura Limpia del Proyecto

```text
Ads Spotify/
├── Spotify (Protegido con SpotiGuard).lnk # Acceso directo conjunto para abrir Spotify + SpotiGuard
├── SpotiGuard-1.0.8-19.apk        # Paquete APK oficial firmado para Android (4.53 MB)
├── shizuku.apk                    # APK oficial de Shizuku para cierre silencioso (v13.6.0)
├── activar_shizuku.bat            # Activador en 1 clic de Shizuku vía ADB
├── SpotiGuard-Windows-1.0.0-4.zip # Paquete ZIP portable para Windows (47.9 MB)
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
        ├── AndroidManifest.xml    # Manifiesto con BootReceiver, SpotifyLaunchReceiver y ShizukuProvider
        ├── java/com/spotiskip/guardian/
        │   ├── MainActivity.kt    # UI reactiva con autodesaparición de requisitos cumplidos
        │   ├── receivers/
        │   │   ├── BootReceiver.kt          # Auto-arranque al encender el teléfono
        │   │   └── SpotifyLaunchReceiver.kt # Auto-activación al recibir emisiones de Spotify
        │   ├── services/
        │   │   ├── SpotiGuardService.kt               # Foreground Service con Watchdog de fin de pista
        │   │   └── SpotiGuardAccessibilityService.kt  # Servicio de accesibilidad para forzar cierre rápido
        │   └── utils/
        │       └── SpotifyController.kt     # Cierre forzoso, Root, relanzamiento y play
        └── res/                   # Layouts con autocomprobación dinámica de requisitos
```

---

## ⚖️ Aviso Legal
Este software ha sido desarrollado con fines de investigación educativa en automatización de interfaces, gestión de procesos en sistemas operativos y accesibilidad de audio.\n