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

* 📱 **Móvil (Android)**: **[Descargar SpotiGuard-1.0.2-8.apk](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.2-8/SpotiGuard-1.0.2-8.apk)** *(4.51 MB — Nueva arquitectura segura sin `BIND_NOTIFICATION_LISTENER_SERVICE`, compatible con Android 8 a 15, cero bloqueos de Play Protect)*
* 🖥️ **PC (Windows)**: **[Descargar SpotiGuard-Windows-1.0.0-4.zip](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.0-4/SpotiGuard-Windows-1.0.0-4.zip)** *(47.9 MB — Paquete portable con ejecutable de 64 bits y reanudación Windows SMTC)*
* 🔌 **Instalador Rápido por ADB para Android desde PC**: **[`instalar_android.bat`](instalar_android.bat)** *(Instalación en 1 clic en tu móvil vía cable USB o Wi-Fi sin advertencias de navegador)*
* 🔗 **Acceso Directo Local en PC**: **[`SpotiSkip (Iniciar App).lnk`](SpotiSkip%20(Iniciar%20App).lnk)** *(Lanzamiento directo en 1 clic con icono de sistema)*
* 🌐 **Repositorio Oficial en GitHub**: **[https://github.com/Atm0027/SpotiGuard](https://github.com/Atm0027/SpotiGuard)**
* 🚀 **Última Release Oficial (v1.0.2-8)**: **[https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.2-8](https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.2-8)**

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
* **Auto-arranque con Windows**: Opción integrada para iniciar SpotiGuard minimizado automáticamente al encender el ordenador.
* **Dos Modos de Operación**:
  1. **Reinicio Rápido (Skip & Relaunch)**: Cierra y reabre Spotify instantáneamente en ~2-4 segundos, saltando a la siguiente canción real.
  2. **Silenciador Furtivo (Stealth Mute)**: Muta al 0% el volumen exclusivo de Spotify mientras dura el anuncio y lo restablece exactamente cuando empieza la canción.

### 🚀 Ejecución de la Aplicación en PC (Sin necesidad de Scripts):
* **Opción Rápida (Doble Clic directo)**: 
  - Haz doble clic en el acceso directo raíz: **[`SpotiSkip (Iniciar App).lnk`](SpotiSkip%20(Iniciar%20App).lnk)**
  - O ejecuta directamente el binario compilado: **[`dist/SpotiSkip/SpotiSkip.exe`](dist/SpotiSkip/SpotiSkip.exe)**
  - O descarga y descomprime el paquete ZIP portable: **[`SpotiGuard-Windows-1.0.0-4.zip`](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.0-4/SpotiGuard-Windows-1.0.0-4.zip)**

---

## 📱 Aplicación para Móvil (Android)

Ubicada en la carpeta [`mobile/`](mobile/). Proyecto nativo completo en **Kotlin**.

### Características Principales:
* **Servicio en Primer Plano Estándar (`SpotiGuardService`)**: Servicio oficial `mediaPlayback` con notificación de estado de baja prioridad. Garantiza que Android nunca detenga el servicio en segundo plano.
* **Receptor Dinámico de Eventos Spotify**: Captura broadcasts de metadatos (`com.spotify.music.metadatachanged`, `playbackstatechanged`, `queuechanged`) emitidos nativamente por Spotify.
* **Modos de Funcionamiento**:
  1. **Silenciador Inteligente (Recomendado para Móvil)**: Silencia el volumen multimedia (`STREAM_MUSIC`) en el milisegundo en que entra el anuncio y lo restaura sin interrumpir la pantalla ni tu navegación.
  2. **Reinicio Rápido Asistido**: Reinicia el proceso de Spotify y reanuda la cola con eventos multimedia.

### 📲 Descarga e Instalación del APK Oficial:
* **Descarga directa**: **[Descargar SpotiGuard-1.0.2-8.apk (v1.0.2-8)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.2-8/SpotiGuard-1.0.2-8.apk)** *(4.51 MB)*.
* **Firma Oficial**: Almacén de claves RSA 2048-bit (`CN=SpotiGuard`) con esquemas **v2** y **v3**.

---

### 🛡️ Erradicación Definitiva del Bloqueo: *"Aplicación bloqueada para proteger tu dispositivo"*

#### 1. ¿Por qué ocurría el bloqueo en versiones anteriores?
En las versiones más recientes de Android (Android 13, 14 y 15 con Google Play Protect Enhanced Fraud Protection), Google implementa un **bloqueo estricto e ineludible** (sin botón de "Instalar de todas formas") para cualquier APK descargado desde un navegador web que declare en su manifiesto permisos catalogados como de alto riesgo de fraude/interceptación:
- `BIND_ACCESSIBILITY_SERVICE` (Servicios de accesibilidad)
- `BIND_NOTIFICATION_LISTENER_SERVICE` (Lectura completa de notificaciones y 2FA)
- Permisos de lectura de SMS

Aunque el usuario intentara desactivar Play Protect, muchos dispositivos Samsung (Knox) o cuentas con "Protección mejorada" bloquean el interruptor de desactivación.

#### 2. ¿Cómo se ha solucionado en `v1.0.2-8`?
- Se ha **eliminado por completo `BIND_NOTIFICATION_LISTENER_SERVICE`** y cualquier componente clasificado como sensible.
- La aplicación ha sido rediseñada para usar **`SpotiGuardService`** (Foreground Service estándar) + **`BroadcastReceiver` dinámico con `RECEIVER_EXPORTED`**.
- El APK resultante contiene **únicamente permisos estándar**: `MODIFY_AUDIO_SETTINGS`, `POST_NOTIFICATIONS` y `FOREGROUND_SERVICE`.
- **Resultado comprobado**: `provides-component:'notification-listener'` ha desaparecido. Google Play Protect **ya no aplica el filtro de fraude y permite la instalación fluida**.

#### 3. Métodos de Instalación:
* **Método 1 (Móvil directo - Recomendado)**:
  1. Descarga el APK desde el enlace oficial.
  2. Abre la app **Mis Archivos / Files** de tu teléfono, entra en **Descargas** y pulsa el APK.
  3. Pulsa **Instalar**.
* **Método 2 (Desde PC en 1 Clic con ADB)**:
  1. Conecta tu teléfono al PC por cable USB con la **Depuración por USB** activada.
  2. Ejecuta el script **[`instalar_android.bat`](instalar_android.bat)**. El instalador detectará tu móvil e instalará el APK directamente sin pasar por filtros de descarga de navegadores.

---

### ⚙️ Configuración Inicial en el Móvil (2 sencillos pasos):
1. **Paso 1: Activar "Estado de emisión" en Spotify (Solo 1 vez)**:
   - Abre **Spotify** -> Toca el icono de **Ajustes (⚙️)**.
   - Desplázate hacia abajo y activa el interruptor: **"Estado de emisión del dispositivo"** *(Permite a otras aplicaciones saber lo que estás escuchando)*.
2. **Paso 2: Iniciar la Protección en SpotiGuard**:
   - Abre **SpotiGuard**.
   - Pulsa en **"🛡️ Activar Protección SpotiGuard"** (el botón cambiará a verde confirmando que está activo y protegiendo).
   - Opcional: Pulsa en **"Desactivar Optimización de Batería"** y selecciona **"Sin restricciones"**.
3. ¡Listo! Abre Spotify y disfruta de tu música. Cada vez que entre un anuncio publicitario, SpotiGuard lo silenciará automáticamente.

---

## 🏷️ Sistema de Control de Versiones Heredado de JARVIS

SpotiGuard adopta la arquitectura exacta de nomenclatura y versionado del proyecto **JARVIS**:

| Componente | Formato | Ejemplo en SpotiGuard | Significado |
| :--- | :--- | :--- | :--- |
| **`versionName`** | SemVer `X.Y.Z` | `1.0.2` | Nombre semántico deducido automáticamente de Conventional Commits (`feat:` minor, `fix:` patch, `BREAKING CHANGE:` major). |
| **`versionCode`** | Entero creciente | `8` | Recuento estricto de commits (`git rev-list --count HEAD`). Nunca retrocede y garantiza actualizaciones válidas. |
| **Etiqueta Git (Tag)** | `v<versionName>-<versionCode>` | `v1.0.2-8` | El estado vive en las etiquetas de git. |
| **Título de Release** | `<App> <versionName> (<versionCode>)` | `SpotiGuard 1.0.2 (8)` | Título estandarizado para las publicaciones de GitHub. |
| **Paquete Windows** | `<App>-Windows-<versionName>-<versionCode>.zip` | `SpotiGuard-Windows-1.0.0-4.zip` | Binario portable empaquetado para PC. |
| **Paquete Android** | `<App>-<versionName>-<versionCode>.apk` | `SpotiGuard-1.0.2-8.apk` | Paquete APK firmado para móviles. |

---

## 📂 Estructura del Proyecto

```text
Ads Spotify/
├── SpotiSkip (Iniciar App).lnk    # Acceso directo para ejecutar la app de PC con 1 doble clic
├── SpotiGuard-1.0.2-8.apk         # Paquete APK oficial limpio y firmado para Android (4.51 MB)
├── instalar_android.bat           # Instalador automático en 1 clic para móvil vía ADB
├── SpotiGuard-Windows.zip         # Aplicación ejecutable portable para PC en ZIP (47.9 MB)
├── README.md                      # Documentación centralizada del proyecto
├── dist/                          # Aplicación de PC compilada (.exe nativo de Windows)
│   └── SpotiSkip/
│       └── SpotiSkip.exe          # Ejecutable principal de 64 bits
├── pc/                            # Código fuente y componentes de PC
│   ├── app.py                     # Controlador principal y punto de entrada con Mutex
│   ├── config.json                # Configuración persistente del usuario
│   ├── run.bat                    # Lanzador rápido en 1 clic
│   ├── core/
│   │   ├── detector.py            # Inspección híbrida SMTC + títulos Win32 + pycaw
│   │   ├── skipper.py             # Cierre atómico dual, Next Track y reanudación SMTC
│   │   ├── audio_muter.py         # Control de volumen individual con CoreAudio
│   │   ├── version.py             # Sistema unificado de versiones de JARVIS
│   │   └── watcher.py             # Vigilante reactivo de procesos y registro de autoarranque
│   └── ui/
│       ├── main_window.py         # Interfaz HUD moderna con estadísticas y logs
│       └── tray_icon.py           # Icono dinámico y menú de la bandeja del sistema
└── mobile/                        # Aplicación nativa para Móvil (Android)
    ├── README_ANDROID.md          # Guía técnica y de instalación para Android
    ├── release.keystore           # Clave de firma oficial RSA 2048-bit
    └── app/
        ├── build.gradle.kts       # Configuración Gradle con esquemas de firma v2 y v3
        └── src/main/
            ├── AndroidManifest.xml # Manifiesto limpio sin permisos sensibles
            ├── java/com/spotiskip/guardian/
            │   ├── MainActivity.kt               # Pantalla de control y arranque de servicio
            │   ├── services/
            │   │   └── SpotiGuardService.kt      # Foreground Service y receptor Broadcast de Spotify
            │   └── utils/
            │       ├── AudioController.kt         # Control de volumen STREAM_MUSIC
            │       └── SpotifyController.kt       # Lanzamiento y control multimedia
            └── res/                               # Layouts, strings, colores y temas adaptativos
```

---

## ⚖️ Aviso Legal
Este software ha sido desarrollado con fines de investigación educativa en automatización de interfaces, gestión de procesos en sistemas operativos y accesibilidad de audio.
