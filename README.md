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

* 📱 **Móvil (Android)**: **[`SpotiGuard-1.0.7-17.apk`](SpotiGuard-1.0.7-17.apk)** *(4.74 MB — Bypass puro por cierre forzoso y reinicio automático sin mutear, watchdog anti-anuncios silenciosos, compatible con Samsung One UI / Android 16)*
* 🖥️ **PC (Windows)**: **[`SpotiGuard-Windows-1.0.0-4.zip`](SpotiGuard-Windows-1.0.0-4.zip)** *(47.9 MB — Paquete portable con ejecutable de 64 bits y reanudación Windows SMTC)*
* 🚀 **Lanzador Conjunto en PC (1 Clic)**: **[`Spotify (Protegido con SpotiGuard).lnk`](Spotify%20(Protegido%20con%20SpotiGuard).lnk)** *(Abre Spotify y SpotiGuard juntos de forma simultánea)*
* 🔌 **Instalador Rápido por ADB para Android desde PC**: **[`instalar_android.bat`](instalar_android.bat)** *(Instalación en 1 clic en tu móvil vía cable USB o Wi-Fi sin advertencias de navegador)*
* 🌐 **Repositorio Oficial en GitHub**: **[https://github.com/Atm0027/SpotiGuard](https://github.com/Atm0027/SpotiGuard)**
* 🚀 **Última Release Oficial (v1.0.7-17)**: **[https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.7-17](https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.7-17)**

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
4. **Detección Dinámica de Requisitos en Interfaz**: Los botones de *"Estado de emisión"*, *"Optimización de batería"* y *"Accesibilidad"* **se ocultan automáticamente de la app en cuanto el sistema detecta que están concedidos**, mostrando un recuadro verde de verificación completa.

---

## 🔬 ¿Cómo Funciona Técnicamente la Neutralización de Anuncios en Android?

### 1. Bypass Puro sin Muteo (Cierre Forzoso y Relanzamiento Limpio):
* **Cero Silenciamiento**: La app no silencia el audio ni enmascara la publicidad; destruye el proceso de Spotify en el acto para purgar el anuncio del búfer de reproducción.
* **Servicio de Accesibilidad Rápido (`SpotiGuardAccessibilityService`)**: Pulsa automáticamente "Forzar cierre" / "Forzar detención" y confirma el diálogo del sistema en ~150 ms. Optimizado para Samsung One UI, Xiaomi HyperOS, Google Pixel y Android estándar. Cierra inmediatamente la pantalla de Ajustes con botón Atrás del sistema para no dejar ventanas abiertas.
* **Soporte Root Directo**: En teléfonos con permisos Root, ejecuta `su am force-stop com.spotify.music` instantáneamente sin necesidad de interacción visual.

### 2. Detección Dual de Anuncios en Android:
1. **Watchdog de Fin de Canción**: Spotify **no emite broadcasts al comenzar un anuncio**. SpotiGuard calcula la duración exacta de la pista musical activa y programa un watchdog. Si al terminar no llega una nueva canción, deduce la presencia del anuncio y activa el cierre forzoso de inmediato.
2. **Detección Reactiva por ID y Metadatos**: Captura cuñas comerciales con IDs no estándar (`!id.startsWith("spotify:track:")`), menciones publicitarias ("Publicidad", "Advertisement", "Spotify Free", "Werbung", etc.).

### 3. Secuencia de Neutralización:
1. **Detención multimedia**: `sendMediaStop()` (`KEYCODE_MEDIA_PAUSE` y `STOP`).
2. **Cierre forzoso de Spotify**: Mediante `SpotiGuardAccessibilityService` o Root.
3. **Cierre de Ajustes**: Retorno instantáneo (`GLOBAL_ACTION_BACK`) para no dejar abierta la información de Spotify.
4. **Relanzamiento limpio**: Apertura de Spotify con el búfer publicitario purgado.
5. **Purga y salto**: `KEYCODE_MEDIA_NEXT` para cargar la siguiente pista legítima.
6. **Reanudación automática**: `KEYCODE_MEDIA_PLAY` para iniciar la música sin interrupciones.

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

### Novedades v1.0.7-17:
* **Corrección de Apertura Persistente de Información de la Aplicación**:
  - Resuelto el problema por el cual la ventana de Ajustes de Spotify se quedaba abierta en la pantalla.
  - Implementado cierre inmediato mediante acción global Atrás (`GLOBAL_ACTION_BACK`) al confirmar el forzado o en caso de timeout.
* **Diferenciación Estricta del Diálogo de Confirmación en Samsung One UI**:
  - Detección prioritaria del botón afirmativo `android:id/button1` del sistema.
  - Filtrado para ignorar botones de la barra de acciones inferior (`forcestop_button`), eliminando falsos positivos.
* **Despertar Automático de Pantalla (`WakeLock`)**:
  - Encendido breve de la pantalla si entra un anuncio con el móvil bloqueado o apagado, permitiendo ejecutar la accesibilidad sin quedar atrapado en la pantalla de bloqueo.
* **Blindaje contra Limpieza de Tareas Recientes**:
  - Añadido `stopWithTask="false"` a los servicios y `excludeFromRecents="true"` a `MainActivity` para evitar que deslizar SpotiGuard en Recientes rompa el servicio de accesibilidad.

### 📲 Descarga e Instalación del APK Oficial:
* **Descarga directa**: **[`SpotiGuard-1.0.7-17.apk`](SpotiGuard-1.0.7-17.apk)** *(4.74 MB)*.
* **Firma Oficial**: Almacén de claves RSA 2048-bit (`CN=SpotiGuard`) con esquemas **v2** y **v3**.

---

## 🏷️ Sistema de Control de Versiones Heredado de JARVIS

| Componente | Formato | Ejemplo en SpotiGuard | Significado |
| :--- | :--- | :--- | :--- |
| **`versionName`** | SemVer `X.Y.Z` | `1.0.7` | Nombre semántico deducido automáticamente de Conventional Commits. |
| **`versionCode`** | Entero creciente | `17` | Recuento estricto de commits (`git rev-list --count HEAD`). Nunca retrocede. |
| **Etiqueta Git (Tag)** | `v<versionName>-<versionCode>` | `v1.0.7-17` | El estado vive en las etiquetas de git. |
| **Título de Release** | `<App> <versionName> (<versionCode>)` | `SpotiGuard 1.0.7 (17)` | Título estandarizado para publicaciones de GitHub. |
| **Paquete Windows** | `<App>-Windows-<versionName>-<versionCode>.zip` | `SpotiGuard-Windows-1.0.0-4.zip` | Binario portable empaquetado para PC. |
| **Paquete Android** | `<App>-<versionName>-<versionCode>.apk` | `SpotiGuard-1.0.7-17.apk` | Paquete APK firmado para móviles. |

---

## 📁 Estructura Limpia del Proyecto

```text
Ads Spotify/
├── Spotify (Protegido con SpotiGuard).lnk # Acceso directo conjunto para abrir Spotify + SpotiGuard
├── SpotiGuard-1.0.6-16.apk        # Paquete APK oficial firmado para Android (4.74 MB)
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
        ├── AndroidManifest.xml    # Manifiesto con BootReceiver, SpotifyLaunchReceiver y Accessibility
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