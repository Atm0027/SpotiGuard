# ⚡ SpotiGuard: Autonomous Spotify Ad Guardian & Bypass (PC & Android)

[![GitHub Repository](https://img.shields.io/badge/GitHub-Atm0027%2FSpotiGuard-blue?logo=github)](https://github.com/Atm0027/SpotiGuard)
[![Latest Release](https://img.shields.io/github/v/release/Atm0027/SpotiGuard?color=orange&logo=github)](https://github.com/Atm0027/SpotiGuard/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Android-green)](#)
[![Python](https://img.shields.io/badge/Python-3.10%2B-blue?logo=python)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-Android%20Native-purple?logo=kotlin)](#)
[![License](https://img.shields.io/badge/License-MIT-yellow)](#)

**SpotiGuard** es una solución de protección multimedia independiente, ultrarrápida y multiplataforma para **PC (Windows)** y **Móvil (Android)** que neutraliza automáticamente los anuncios publicitarios de Spotify mediante la técnica de **Reinicio Rápido Asistido (Kill & Relaunch Bypass con Windows SMTC)** o **Silenciamiento Furtivo en Segundo Plano (Auto-Mute Inteligente)**.

El sistema se ejecuta en segundo plano y **se activa de forma 100% autónoma en cuanto detecta que Spotify se ha iniciado**, tanto en tu ordenador como en tu dispositivo móvil.

---

### 📥 Descargas Directas Listas para Usar

* 📱 **Móvil (Android)**: **[Descargar SpotiGuard-1.0.2-9.apk](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.2-9/SpotiGuard-1.0.2-9.apk)** *(4.51 MB — Auto-activación al abrir Spotify o encender el móvil, cero permisos invasivos, compatible con Android 8 a 15)*
* 🖥️ **PC (Windows)**: **[Descargar SpotiGuard-Windows-1.0.0-4.zip](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.0-4/SpotiGuard-Windows-1.0.0-4.zip)** *(47.9 MB — Paquete portable con ejecutable de 64 bits y reanudación Windows SMTC)*
* 🚀 **Lanzador Conjunto en PC (1 Clic)**: **[`Spotify (Protegido con SpotiGuard).lnk`](Spotify%20(Protegido%20con%20SpotiGuard).lnk)** *(Abre Spotify y SpotiGuard juntos de forma simultánea)*
* 🔌 **Instalador Rápido por ADB para Android desde PC**: **[`instalar_android.bat`](instalar_android.bat)** *(Instalación en 1 clic en tu móvil vía cable USB o Wi-Fi sin advertencias de navegador)*
* 🌐 **Repositorio Oficial en GitHub**: **[https://github.com/Atm0027/SpotiGuard](https://github.com/Atm0027/SpotiGuard)**
* 🚀 **Última Release Oficial (v1.0.2-9)**: **[https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.2-9](https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.2-9)**

---

## ⚡ Activación Automática al Abrir Spotify (PC y Android)

Tanto en **Windows** como en **Android**, SpotiGuard está diseñado para funcionar **sin que tengas que acordarte de abrir la app**:

### 🖥️ En PC (Windows):
1. **Auto-arranque con Windows**: SpotiGuard se inicia automáticamente con el ordenador minimizado en la bandeja del sistema (`System Tray`), consumiendo 0% de CPU en reposo.
2. **Detección Reactiva Rápida (0.7s)**: En cuanto abres la app oficial de Spotify (o la versión web/Store), SpotiGuard detecta el proceso de inmediato y activa la protección.
3. **Acceso Directo Unificado**: Puedes usar **`Spotify (Protegido con SpotiGuard).lnk`** o el script **`Lanzar Spotify Protegido.bat`** para lanzar ambos programas a la vez con un solo clic.

### 📱 En Móvil (Android):
1. **Auto-inicio al encender el móvil (`BootReceiver`)**: SpotiGuard se reactiva automáticamente al reiniciar el teléfono y permanece en espera de bajo consumo.
2. **Auto-despertar con Spotify (`SpotifyLaunchReceiver`)**: En cuanto Spotify empieza a reproducir música o un anuncio, Android emite el evento `com.spotify.music.metadatachanged` y SpotiGuard se activa automáticamente si no estaba corriendo.
3. **Lanzador Directo desde la App**: Si abres SpotiGuard y pulsas **"🚀 Abrir Spotify"**, el servicio se inicia y abre Spotify en pantalla simultáneamente.

---

## 🔬 ¿Cómo Funciona Técnicamente el Salto y la Reanudación?

### 1. El Truco del Cierre y Relanzamiento (Bypass Trick)
* **Inyección Volátil**: En las cuentas Spotify Free, las cuñas publicitarias se insertan de manera transitoria en la memoria RAM y el búfer del reproductor.
* **Detección Inmediata y Multilingüe**:
  - En **PC**, combina la inspección de la API nativa de Windows **System Media Transport Controls (SMTC / WinRT)**, títulos de ventanas Win32 y sesiones de audio activas (**CoreAudio / pycaw**). Reconoce cuñas publicitarias en español (`"ESCÚCHALO AHORA"`, `"Escucha sin límites"`, `"Hazte Premium"`, `"Publicidad"`, `"Anuncio"`), inglés (`"Advertisement"`, `"Spotify Free"`) y formatos internacionales.
  - En **Móvil**, utiliza un **Foreground Service** (`SpotiGuardService`) que escucha de forma dinámica los eventos nativos de reproducción emitidos por Spotify (`com.spotify.music.metadatachanged`).

### 2. Arquitectura de Reanudación de Reproducción y Prevención de Bloqueos en PC
Tras analizar en profundidad el comportamiento de Spotify en Windows 10/11 (especialmente la versión de Microsoft Store / WindowsApps):

1. **Eliminación Atómica de Lanzadores Huérfanos**:
   - Spotify en Windows utiliza dos procesos: `Spotify.exe` y `SpotifyLauncher.exe`.
   - Si se cerraba únicamente `Spotify.exe`, `SpotifyLauncher` quedaba como proceso huérfano y generaba un cuadro de diálogo del sistema `#32770` con el mensaje: *"The Spotify application is not responding"*, impidiendo que Spotify volviera a abrirse.
   - **Solución implementada**: SpotiGuard termina de forma atómica tanto `Spotify.exe` como `SpotifyLauncher.exe`, garantizando un arranque en frío 100% limpio.

2. **Purga del Búfer del Anuncio Interrumpido (`Next Track`)**:
   - Al reiniciar Spotify tras cortar una cuña publicitaria, la app suele recordar el elemento que estaba sonando (el anuncio pausado).
   - **Solución implementada**: SpotiGuard envía primero una señal de salto de pista (`Next Track` / `VK_MEDIA_NEXT_TRACK` / `APPCOMMAND_MEDIA_NEXTTRACK`), lo que obliga al motor de Spotify a descartar la cuña comercial en cola y cargar de inmediato la siguiente canción real de la lista del usuario.

3. **Reanudación Nativa con Windows SMTC (`winsdk`)**:
   - SpotiGuard implementa una tubería de reanudación en 3 capas (SMTC Nativo WinRT -> Mensajes Win32 Directos `WM_APPCOMMAND` -> Inyección de Teclas Extendidas de Hardware) con verificación activa en bucle hasta confirmar `PlaybackStatus = Playing`.

4. **Protección Contra Instancias Múltiples (Mutex de Windows)**:
   - Se ha implementado un `CreateMutexW` con nombre global (`SpotiGuard_SingleInstance_App_Mutex`). Si el usuario abre la app dos veces, la segunda instancia trae la existente al frente y se cierra silenciosamente.

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

### 🚀 Ejecución de la Aplicación en PC:
* **Opción Rápida (Lanzar Spotify con Protección en 1 Clic)**: 
  - Haz doble clic en: **[`Spotify (Protegido con SpotiGuard).lnk`](Spotify%20(Protegido%20con%20SpotiGuard).lnk)**
* **Abrir solo SpotiGuard**:
  - Haz doble clic en: **[`SpotiSkip (Iniciar App).lnk`](SpotiSkip%20(Iniciar%20App).lnk)** o **[`dist/SpotiSkip/SpotiSkip.exe`](dist/SpotiSkip/SpotiSkip.exe)**

---

## 📱 Aplicación para Móvil (Android)

Ubicada en la carpeta [`mobile/`](mobile/). Proyecto nativo completo en **Kotlin**.

### Características Principales:
* **Auto-Activación con Spotify**: Escucha las emisiones nativas (`com.spotify.music.metadatachanged`, `playbackstatechanged`, `queuechanged`) y se activa sola.
* **Servicio en Primer Plano Seguro (`SpotiGuardService`)**: Servicio oficial `mediaPlayback` con notificación de estado de baja prioridad. Garantiza que Android nunca detenga el servicio en segundo plano.
* **Receptor de Arranque (`BootReceiver`)**: Se reactiva solo al encender el teléfono.
* **Modos de Funcionamiento**:
  1. **Silenciador Inteligente (Recomendado para Móvil)**: Silencia el volumen multimedia (`STREAM_MUSIC`) en el milisegundo en que entra el anuncio y lo restaura sin interrumpir la pantalla ni tu navegación.
  2. **Reinicio Rápido Asistido**: Reinicia el proceso de Spotify y reanuda la cola con eventos multimedia.

### 📲 Descarga e Instalación del APK Oficial:
* **Descarga directa**: **[Descargar SpotiGuard-1.0.2-9.apk (v1.0.2-9)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.2-9/SpotiGuard-1.0.2-9.apk)** *(4.51 MB)*.
* **Firma Oficial**: Almacén de claves RSA 2048-bit (`CN=SpotiGuard`) con esquemas **v2** y **v3**. Cero bloqueos de Google Play Protect.

---

### ⚙️ Configuración Inicial en el Móvil (2 sencillos pasos):
1. **Paso 1: Activar "Estado de emisión" en Spotify (Solo 1 vez)**:
   - Abre **Spotify** -> Toca el icono de **Ajustes (⚙️)**.
   - Desplázate hacia abajo y activa el interruptor: **"Estado de emisión del dispositivo"** *(Permite a otras aplicaciones saber lo que estás escuchando)*.
2. **Paso 2: Iniciar la Protección en SpotiGuard**:
   - Abre **SpotiGuard**.
   - Pulsa en **"🛡️ Activar Protección SpotiGuard"** (o simplemente pulsa "🚀 Abrir Spotify").
   - Opcional: Pulsa en **"Desactivar Optimización de Batería"** y selecciona **"Sin restricciones"**.
3. ¡Listo! A partir de ese momento, cada vez que abras Spotify o enciendas el móvil, SpotiGuard estará activo protegiéndote.

---

## 🏷️ Sistema de Control de Versiones Heredado de JARVIS

SpotiGuard adopta la arquitectura exacta de nomenclatura y versionado del proyecto **JARVIS**:

| Componente | Formato | Ejemplo en SpotiGuard | Significado |
| :--- | :--- | :--- | :--- |
| **`versionName`** | SemVer `X.Y.Z` | `1.0.2` | Nombre semántico deducido automáticamente de Conventional Commits (`feat:` minor, `fix:` patch, `BREAKING CHANGE:` major). |
| **`versionCode`** | Entero creciente | `9` | Recuento estricto de commits (`git rev-list --count HEAD`). Nunca retrocede y garantiza actualizaciones válidas. |
| **Etiqueta Git (Tag)** | `v<versionName>-<versionCode>` | `v1.0.2-9` | El estado vive en las etiquetas de git. |
| **Título de Release** | `<App> <versionName> (<versionCode>)` | `SpotiGuard 1.0.2 (9)` | Título estandarizado para las publicaciones de GitHub. |
| **Paquete Windows** | `<App>-Windows-<versionName>-<versionCode>.zip` | `SpotiGuard-Windows-1.0.0-4.zip` | Binario portable empaquetado para PC. |
| **Paquete Android** | `<App>-<versionName>-<versionCode>.apk` | `SpotiGuard-1.0.2-9.apk` | Paquete APK firmado para móviles. |

---

## 📂 Estructura del Proyecto

```text
Ads Spotify/
├── Spotify (Protegido con SpotiGuard).lnk # Acceso directo conjunto para abrir Spotify + SpotiGuard
├── SpotiSkip (Iniciar App).lnk    # Acceso directo para abrir la app de SpotiGuard en PC
├── SpotiGuard-1.0.2-9.apk         # Paquete APK oficial limpio y firmado para Android (4.51 MB)
├── Lanzar Spotify Protegido.bat   # Script para lanzar Spotify y SpotiGuard en PC
├── instalar_android.bat           # Instalador automático en 1 clic para móvil vía ADB
├── README.md                      # Documentación centralizada del proyecto
├── pc/                            # Código fuente y componentes de PC
│   ├── app.py                     # Controlador principal con Mutex e inicio con Windows
│   ├── core/
│   │   ├── watcher.py             # Vigilante ultrarrápido (0.7s) reactivo a Spotify
│   │   ├── detector.py            # Inspección híbrida SMTC + títulos Win32 + pycaw
│   │   ├── skipper.py             # Cierre atómico dual, Next Track y reanudación SMTC
│   │   ├── audio_muter.py         # Control de volumen individual con CoreAudio
│   │   └── version.py             # Sistema unificado de versiones de JARVIS
└── mobile/                        # Aplicación nativa para Móvil (Android)
    ├── README_ANDROID.md          # Guía técnica y de instalación para Android
    └── app/src/main/
        ├── AndroidManifest.xml    # Manifiesto limpio con BootReceiver y SpotifyLaunchReceiver
        └── java/com/spotiskip/guardian/
            ├── MainActivity.kt    # Pantalla de control y auto-arranque
            ├── receivers/
            │   ├── BootReceiver.kt          # Auto-arranque al encender el teléfono
            │   └── SpotifyLaunchReceiver.kt # Auto-activación al recibir emisiones de Spotify
            └── services/
                └── SpotiGuardService.kt     # Foreground Service y receptor Broadcast de Spotify
```

---

## ⚖️ Aviso Legal
Este software ha sido desarrollado con fines de investigación educativa en automatización de interfaces, gestión de procesos en sistemas operativos y accesibilidad de audio.
