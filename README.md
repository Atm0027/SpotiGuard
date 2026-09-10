# ⚡ SpotiGuard: Autonomous Spotify Ad Guardian & Bypass (PC & Android)

[![GitHub Repository](https://img.shields.io/badge/GitHub-Atm0027%2FSpotiGuard-blue?logo=github)](https://github.com/Atm0027/SpotiGuard)
[![Latest Release](https://img.shields.io/github/v/release/Atm0027/SpotiGuard?color=orange&logo=github)](https://github.com/Atm0027/SpotiGuard/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Android-green)](#)
[![Python](https://img.shields.io/badge/Python-3.10%2B-blue?logo=python)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-Android%20Native-purple?logo=kotlin)](#)
[![License](https://img.shields.io/badge/License-MIT-yellow)](#)

**SpotiGuard** es una solución de protección multimedia independiente, ultrarrápida y multiplataforma para **PC (Windows)** y **Móvil (Android)** que neutraliza automáticamente los anuncios publicitarios de Spotify mediante la técnica de **Reinicio Rápido Asistido (Kill & Relaunch Bypass con Windows SMTC / Android Watchdog Controller)**.

El sistema se ejecuta en segundo plano y **se activa de forma 100% autónoma en cuanto detecta que Spotify se ha iniciado**, tanto en tu ordenador como en tu dispositivo móvil.

---

### 📥 Descargas Directas Listas para Usar

* 📱 **Móvil (Android)**: **[Descargar SpotiGuard-1.0.3-13.apk](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.3-13/SpotiGuard-1.0.3-13.apk)** *(4.51 MB — Watchdog anti-anuncios silenciosos, detección dinámica de requisitos en interfaz, auto-activación al abrir Spotify o encender el móvil, compatible con Android 8 a 15)*
* 🖥️ **PC (Windows)**: **[Descargar SpotiGuard-Windows-1.0.0-4.zip](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.0-4/SpotiGuard-Windows-1.0.0-4.zip)** *(47.9 MB — Paquete portable con ejecutable de 64 bits y reanudación Windows SMTC)*
* 🚀 **Lanzador Conjunto en PC (1 Clic)**: **[`Spotify (Protegido con SpotiGuard).lnk`](Spotify%20(Protegido%20con%20SpotiGuard).lnk)** *(Abre Spotify y SpotiGuard juntos de forma simultánea)*
* 🔌 **Instalador Rápido por ADB para Android desde PC**: **[`instalar_android.bat`](instalar_android.bat)** *(Instalación en 1 clic en tu móvil vía cable USB o Wi-Fi sin advertencias de navegador)*
* 🌐 **Repositorio Oficial en GitHub**: **[https://github.com/Atm0027/SpotiGuard](https://github.com/Atm0027/SpotiGuard)**
* 🚀 **Última Release Oficial (v1.0.3-13)**: **[https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.3-13](https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.3-13)**

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
4. **Detección Dinámica de Requisitos en Interfaz**: Los botones de *"Estado de emisión"* y *"Optimización de batería"* **se ocultan automáticamente de la app en cuanto el sistema detecta que están activados**, dejando la interfaz limpia con un indicador de verificación verde.

---

## 🔬 ¿Cómo Funciona Técnicamente el Salto y la Reanudación?

### 1. El Gran Descubrimiento: ¿Por qué Spotify cuela anuncios silenciosos en Android?
* **Supresión deliberada de broadcasts en anuncios**: Spotify en Android **NO emite ningún evento de broadcast cuando empieza una cuña publicitaria**. Únicamente emite eventos cuando comienza una canción real.
* **El fallo de los detectores pasivos**: Si una aplicación solo espera a recibir un broadcast para actuar, Spotify reproduce el anuncio completo en silencio absoluto para el sistema de broadcasts.
* **La Solución SpotiGuard: Watchdog de Precisión por Expiración de Pista**:
  - Al sonar una canción legítima, SpotiGuard lee su duración (`length` en milisegundos) y su posición de reproducción (`playbackPosition`).
  - SpotiGuard programa un temporizador interno (*Watchdog*) con la cuenta atrás exacta del final de la pista (`remainingMs + 350ms`).
  - Si al expirar la canción no llega inmediatamente una nueva pista legítima, SpotiGuard deduce con 100% de certeza que **ha comenzado una cuña publicitaria**.
  - De inmediato ejecuta el salto automático: **Kill -> Relaunch -> Purge Buffer (`Next Track`) -> Play**.

### 2. Detección Dual en Android
1. **Watchdog de Fin de Canción**: Captura todas las cuñas publicitarias silenciosas entre canciones.
2. **Detección Reactiva por ID y Metadatos**: Si Spotify emite algún evento publicitario (cuñas de patrocinadores con IDs no estándar, marcas publicitarias en artista/álbum como Vinted o Amazon, o palabras clave como "Publicidad", "Advertisement", "Spotify Free"), SpotiGuard lo intercepta al milisegundo sin esperar al temporizador.

### 3. Secuencia de Salto Atómico en Android
1. **Detención inmediata**: Envío de `KEYCODE_MEDIA_PAUSE` y `KEYCODE_MEDIA_STOP` mediante `AudioManager` y direct intents a `com.spotify.music`.
2. **Cierre de procesos**: `killBackgroundProcesses("com.spotify.music")`.
3. **Relanzamiento**: Apertura limpia de Spotify tras 500 ms.
4. **Purga del búfer**: Envío de `KEYCODE_MEDIA_NEXT` a los 1000 ms (elimina el anuncio congelado en cola).
5. **Reanudación automática**: Envío de `KEYCODE_MEDIA_PLAY` a los 300 ms (arranca la siguiente canción real).

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

### Novedades v1.0.3-13:
* **Watchdog de Expiración de Pista**: Resuelve de forma definitiva los anuncios que Spotify reproduce entre canciones sin emitir broadcasts.
* **Ocultación Automática de Requisitos Cumplidos**:
  - Si la batería ya está configurada "Sin restricciones", el botón desaparece de la app.
  - Si Spotify ya emite eventos hacia la app, el botón de configuración de Spotify desaparece de la app.
  - Se muestra un recuadro verde de confirmación: *"✅ Requisitos configurados: Emisión de Spotify y Batería sin restricciones verificados"*.
* **Emisión Dual de Teclas Multimedia**: Tanto por `AudioManager.dispatchMediaKeyEvent` como por `ACTION_MEDIA_BUTTON` dirigido específicamente a Spotify.
* **Cero Permisos Invasivos**: Sin permisos de accesibilidad ni lectura de notificaciones. 100% compatible con Google Play Protect.

### 📲 Descarga e Instalación del APK Oficial:
* **Descarga directa**: **[Descargar SpotiGuard-1.0.3-13.apk (v1.0.3-13)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.3-13/SpotiGuard-1.0.3-13.apk)** *(4.51 MB)*.
* **Firma Oficial**: Almacén de claves RSA 2048-bit (`CN=SpotiGuard`) con esquemas **v2** y **v3**.

---

## 🏷️ Sistema de Control de Versiones Heredado de JARVIS

| Componente | Formato | Ejemplo en SpotiGuard | Significado |
| :--- | :--- | :--- | :--- |
| **`versionName`** | SemVer `X.Y.Z` | `1.0.3` | Nombre semántico deducido automáticamente de Conventional Commits. |
| **`versionCode`** | Entero creciente | `13` | Recuento estricto de commits (`git rev-list --count HEAD`). Nunca retrocede. |
| **Etiqueta Git (Tag)** | `v<versionName>-<versionCode>` | `v1.0.3-13` | El estado vive en las etiquetas de git. |
| **Título de Release** | `<App> <versionName> (<versionCode>)` | `SpotiGuard 1.0.3 (13)` | Título estandarizado para publicaciones de GitHub. |
| **Paquete Windows** | `<App>-Windows-<versionName>-<versionCode>.zip` | `SpotiGuard-Windows-1.0.0-4.zip` | Binario portable empaquetado para PC. |
| **Paquete Android** | `<App>-<versionName>-<versionCode>.apk` | `SpotiGuard-1.0.3-13.apk` | Paquete APK firmado para móviles. |

---

## 📁 Estructura del Proyecto

```text
Ads Spotify/
├── Spotify (Protegido con SpotiGuard).lnk # Acceso directo conjunto para abrir Spotify + SpotiGuard
├── SpotiSkip (Iniciar App).lnk    # Acceso directo para abrir la app de SpotiGuard en PC
├── SpotiGuard-1.0.3-13.apk        # Paquete APK oficial limpio y firmado para Android (4.51 MB)
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
        ├── AndroidManifest.xml    # Manifiesto limpio con BootReceiver y SpotifyLaunchReceiver
        ├── java/com/spotiskip/guardian/
        │   ├── MainActivity.kt    # Pantalla de control y verificación dinámica de requisitos
        │   ├── receivers/
        │   │   ├── BootReceiver.kt          # Auto-arranque al encender el teléfono
        │   │   └── SpotifyLaunchReceiver.kt # Auto-activación al recibir emisiones de Spotify
        │   ├── services/
        │   │   └── SpotiGuardService.kt     # Foreground Service con Watchdog de fin de pista
        │   └── utils/
        │       └── SpotifyController.kt     # Kill, Relaunch, Buffer Purge y Play dual
        └── res/                   # Layouts e interfaz con autocomprobación de requisitos
```

---

## ⚖️ Aviso Legal
Este software ha sido desarrollado con fines de investigación educativa en automatización de interfaces, gestión de procesos en sistemas operativos y accesibilidad de audio.
