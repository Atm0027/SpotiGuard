# ⚡ SpotiGuard: Autonomous Spotify Ad Guardian & Bypass (PC & Android)

[![GitHub Repository](https://img.shields.io/badge/GitHub-Atm0027%2FSpotiGuard-blue?logo=github)](https://github.com/Atm0027/SpotiGuard)
[![Latest Release](https://img.shields.io/github/v/release/Atm0027/SpotiGuard?color=orange&logo=github)](https://github.com/Atm0027/SpotiGuard/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Android-green)](#)
[![Python](https://img.shields.io/badge/Python-3.10%2B-blue?logo=python)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-Android%20Native-purple?logo=kotlin)](#)
[![License](https://img.shields.io/badge/License-MIT-yellow)](#)

**SpotiGuard** es una solución de protección multimedia independiente, ultrarrápida y multiplataforma para **PC (Windows)** y **Móvil (Android)** que neutraliza automáticamente los anuncios publicitarios de Spotify mediante la técnica de **Reinicio Rápido Asistido (Kill & Relaunch Bypass con Windows SMTC)** o **Silenciamiento Furtivo en Segundo Plano (Auto-Mute Inteligente)**.

El sistema se ejecuta en segundo plano y **se activa de forma 100% automática en cuanto detecta que Spotify se ha iniciado**, tanto en tu ordenador como en tu dispositivo móvil.

---

### 📥 Descargas Directas Listas para Usar

* 📱 **Móvil (Android)**: **[Descargar SpotiSkip-Android.apk (v1.0.0)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.0/SpotiSkip-Android.apk)** *(5.73 MB — Paquete APK listo para instalar en cualquier teléfono Android)*
* 🖥️ **PC (Windows)**: **[Descargar SpotiGuard-Windows.zip (v1.0.0)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.0/SpotiGuard-Windows.zip)** *(47.9 MB — Paquete comprimido portable de 64 bits con ejecutable nativo, listo para descomprimir y usar)*
* 🔗 **Acceso Directo Local en PC**: **[`SpotiSkip (Iniciar App).lnk`](SpotiSkip%20(Iniciar%20App).lnk)** *(Lanzamiento directo en 1 clic con icono de sistema)*
* 🌐 **Repositorio Oficial en GitHub**: **[https://github.com/Atm0027/SpotiGuard](https://github.com/Atm0027/SpotiGuard)**
* 🚀 **Página de Releases Oficiales**: **[https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.0](https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.0)**

---

## 🔬 ¿Cómo Funciona Técnicamente el Salto y la Reanudación?

### 1. El Truco del Cierre y Relanzamiento (Bypass Trick)
* **Inyección Volátil**: En las cuentas Spotify Free, las cuñas publicitarias se insertan de manera transitoria en la memoria RAM y el búfer del reproductor.
* **Detección Inmediata y Multilingüe**:
  - En **PC**, combina la inspección de la API nativa de Windows **System Media Transport Controls (SMTC / WinRT)**, títulos de ventanas Win32 y sesiones de audio activas (**CoreAudio / pycaw**). Reconoce cuñas publicitarias en español (`"ESCÚCHALO AHORA"`, `"Escucha sin límites"`, `"Hazte Premium"`, `"Publicidad"`, `"Anuncio"`), inglés (`"Advertisement"`, `"Spotify Free"`) y formatos internacionales.
  - En **Móvil**, analiza los metadatos de emisión de Spotify a través de un `NotificationListenerService` del sistema operativo Android.

### 2. Arquitectura de Reanudación de Reproducción y Prevención de Bloqueos (Actualización Crítica)
Tras analizar en profundidad el comportamiento de Spotify en Windows 10/11 (especialmente la versión de Microsoft Store / WindowsApps):

1. **Eliminación Atómica de Lanzadores Huérfanos**:
   - Spotify en Windows utiliza dos procesos: `Spotify.exe` y `SpotifyLauncher.exe`.
   - Si se cerraba únicamente `Spotify.exe`, `SpotifyLauncher` quedaba como proceso huérfano y generaba un cuadro de diálogo del sistema `#32770` con el mensaje: *"The Spotify application is not responding"*, impidiendo que Spotify volviera a abrirse.
   - **Solución implementada**: SpotiGuard termina de forma atómica tanto `Spotify.exe` como `SpotifyLauncher.exe`, garantizando un arranque en frío 100% limpio y descartando cualquier ventana de diálogo residual automáticamente.

2. **Purga del Búfer del Anuncio Interrumpido (`Next Track`)**:
   - Al reiniciar Spotify tras cortar una cuña publicitaria, la app suele recordar el elemento que estaba sonando (el anuncio pausado, titulado `"ESCÚCHALO AHORA"` o `"Spotify Free"`).
   - Si simplemente se envía la orden de reproducir (`Play`), Spotify intenta retomar el anuncio publicitario interrumpido o se queda en silencio esperando interacción del usuario.
   - **Solución implementada**: SpotiGuard envía primero una señal de salto de pista (`Next Track` / `VK_MEDIA_NEXT_TRACK` / `APPCOMMAND_MEDIA_NEXTTRACK`), lo que obliga al motor de Spotify a descartar la cuña comercial en cola y cargar de inmediato la siguiente canción real de la lista del usuario.

3. **Reanudación Nativa con Windows SMTC (`winsdk`)**:
   - Anteriormente, las llamadas a teclas multimedia virtuales (`keybd_event`) podían perderse si se enviaban en el primer segundo, antes de que el motor web Chromium (CEF) de Spotify se hubiera registrado en el sistema de transporte multimedia de Windows.
   - Además, Windows 10 y 11 exigen el indicador de hardware extendido `KEYEVENTF_EXTENDEDKEY (0x0001)` en las teclas multimedia (`0xB3`, `0xB0`).
   - **Solución implementada**: SpotiGuard implementa una tubería de reanudación en 3 capas:
     - **Capa 1 (SMTC Nativo)**: Control directo mediante la API oficial de Windows Runtime (`winsdk.windows.media.control` -> `try_skip_next_async()` y `try_play_async()`).
     - **Capa 2 (Mensajes Win32 Directos)**: Envío de mensajes `WM_APPCOMMAND` (`APPCOMMAND_MEDIA_PLAY` y `APPCOMMAND_MEDIA_NEXTTRACK`) directamente al manejador de ventana (`HWND`) de Spotify.
     - **Capa 3 (Teclas Extendidas Win32)**: Inyección de `VK_MEDIA_PLAY_PAUSE` con flag extendido y mapeo de hardware scan code.
     - **Bucle Activo de Verificación**: Verifica el estado real de reproducción durante 4 intentos hasta confirmar que la música está sonando.

4. **Protección Contra Instancias Múltiples (Mutex de Windows)**:
   - Se ha implementado un `CreateMutexW` con nombre global (`SpotiGuard_SingleInstance_App_Mutex`). Si el usuario hace doble clic varias veces, la segunda instancia trae la ventana existente al frente y se cierra silenciosamente, evitando conflictos entre procesos vigilantes.

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
  2. **Silenciador Furtivo (Stealth Mute)**: Muta al 0% el volumen exclusivo de Spotify mientras dura el anuncio y lo restablece exactamente cuando empieza la canción, sin parpadeo de ventanas ni reinicios.

### 🚀 Ejecución de la Aplicación en PC (Sin necesidad de Scripts):
La aplicación para PC está compilada como un **ejecutable nativo de Windows (`.exe`)** independiente de 64 bits con icono de sistema:

* **Opción Rápida (Doble Clic directo)**: 
  - Haz doble clic en el acceso directo raíz: **[`SpotiSkip (Iniciar App).lnk`](SpotiSkip%20(Iniciar%20App).lnk)**
  - O ejecuta directamente el binario compilado: **[`dist/SpotiSkip/SpotiSkip.exe`](dist/SpotiSkip/SpotiSkip.exe)**
  - O descarga y descomprime el paquete ZIP portable: **[`SpotiGuard-Windows.zip`](SpotiGuard-Windows.zip)**
* **No requiere abrir consola ni terminal**: Funciona como un programa nativo de Windows con interfaz HUD, iconos y bandeja de sistema.
* **Auto-arranque**: Marca la casilla **"Iniciar con Windows"** dentro de la app para que vigile siempre de fondo.

---

## 📱 Aplicación para Móvil (Android)

Ubicada en la carpeta [`mobile/`](mobile/). Proyecto nativo completo en **Kotlin**.

### Características Principales:
* **Servicio Desatendido 24/7 (`NotificationListenerService`)**: El propio sistema operativo Android mantiene vivo el servicio de escucha de notificaciones de Spotify.
* **Receptor de Inicio (`BootReceiver`)**: Se reactiva automáticamente tras reiniciar el teléfono móvil.
* **Modos Móviles**:
  1. **Silenciador Inteligente (Recomendado para Móvil)**: Silencia el volumen multimedia (`STREAM_MUSIC`) en el milisegundo en que entra el anuncio y lo restaura sin interrumpir la pantalla ni tu navegación.
  2. **Reinicio Rápido Asistido**: Reinicia el proceso de Spotify y reanuda la cola con eventos de tecla multimedia.

### 📲 Instalación del APK en Android:
* Descarga el archivo compilado listo para usar: **[Descargar SpotiSkip-Android.apk (v1.0.0)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.0/SpotiSkip-Android.apk)**.
* Transfiérelo o descárgalo directamente en tu teléfono móvil e instálalo permitiendo la instalación de aplicaciones desconocidas en los ajustes de tu terminal.
* Abre SpotiGuard en tu móvil y pulsa en **"1. Activar Acceso a Notificaciones"** para permitir que vigile en segundo plano de manera continua.

---

## 📂 Estructura del Proyecto

```text
Ads Spotify/
├── SpotiSkip (Iniciar App).lnk    # Acceso directo para ejecutar la app de PC con 1 doble clic
├── SpotiSkip-Android.apk          # Paquete instalable APK para Android (5.73 MB)
├── SpotiGuard-Windows.zip         # Aplicación ejecutable portable para PC en ZIP (47.9 MB)
├── README.md                      # Documentación centralizada del proyecto
├── dist/                          # Aplicación de PC compilada (.exe nativo de Windows)
│   └── SpotiSkip/
│       └── SpotiSkip.exe          # Ejecutable principal de 64 bits
├── pc/                            # Código fuente y componentes de PC
│   ├── app.py                     # Controlador principal y punto de entrada con Mutex
│   ├── config.json                # Configuración persistente del usuario
│   ├── run.bat                    # Lanzador rápido en 1 clic
│   ├── requirements.txt           # Dependencias de Python (pywin32, pycaw, PyQt6, psutil, winsdk)
│   ├── core/
│   │   ├── detector.py            # Inspección híbrida SMTC + títulos Win32 + pycaw
│   │   ├── skipper.py             # Cierre atómico dual, Next Track y reanudación SMTC
│   │   ├── audio_muter.py         # Control de volumen individual con CoreAudio
│   │   └── watcher.py             # Vigilante reactivo de procesos y registro de autoarranque
│   ├── test_suite.py              # Batería de pruebas unitarias automatizadas
│   ├── test_advanced.py           # Batería de pruebas avanzadas de estrés y edge cases
│   └── ui/
│       ├── main_window.py         # Interfaz HUD moderna con estadísticas y logs
│       └── tray_icon.py           # Icono dinámico y menú de la bandeja del sistema
└── mobile/                        # Aplicación nativa para Móvil (Android)
    ├── README_ANDROID.md          # Guía técnica y de instalación para Android
    ├── settings.gradle.kts        # Configuración de módulos Gradle
    ├── build.gradle.kts           # Gradle raíz
    └── app/
        ├── build.gradle.kts       # Configuración de dependencias Kotlin/Android
        └── src/main/
            ├── AndroidManifest.xml # Declaración de servicios y permisos
            ├── java/com/spotiskip/guardian/
            │   ├── MainActivity.kt               # Pantalla de control y permisos
            │   ├── services/
            │   │   ├── SpotifyNotificationListener.kt # Detección reactiva de anuncios
            │   │   └── SpotifyAccessibilityService.kt # Reinicio asistido en pantalla
            │   ├── receivers/
            │   │   ├── BootReceiver.kt            # Autoarranque tras encendido
            │   │   └── SpotifyBroadcastReceiver.kt# Receptor de emisiones de Spotify
            │   └── utils/
            │       ├── AudioController.kt         # Manejo de volumen STREAM_MUSIC
            │       └── SpotifyController.kt       # Lanzamiento y control multimedia
            └── res/                               # Layouts, strings, colores y temas
```

---

## 🔄 Mejoras Técnicas y Adaptaciones Frente a Scripts Clásicos

| Mejora Técnica | ¿Qué soluciona / mejora en el script? | Impacto |
| :--- | :--- | :--- |
| **Purga de Búfer con `Next Track`** | **Elimina canciones no reanudadas**: Al cortar un anuncio, Spotify retiene la cuña en pausa. Enviar `Next Track` antes de `Play` descarta el anuncio congelado y fuerza la carga de la pista musical real. | **Crítico** (Reproducción) |
| **Cierre Atómico Dual (`Spotify.exe` + `SpotifyLauncher.exe`)** | **Elimina cuadros de error '#32770'**: Previene el mensaje *"The Spotify application is not responding"* que bloqueaba la apertura de Spotify al relanzarlo. | **Crítico** (Estabilidad) |
| **Integración Oficial Windows SMTC (`winsdk`)** | **Reanudación 100% fiable**: Usa la API nativa de Windows Runtime en lugar de depender únicamente de pulsaciones de teclado que el cliente web de Spotify descartaba al iniciar. | **Crítico** (Compatibilidad) |
| **Mutex de Instancia Única de Windows** | **Evita conflictos de procesos duplicados**: Garantiza que solo un vigilante esté activo a la vez, evitando guerras de reinicios. | **Alto** (Robustez) |
| **Detección Multilingüe Avanzada** | **Captura anuncios en español**: Reconoce eslóganes como `"ESCÚCHALO AHORA"`, `"Escucha sin límites"` y `"Hazte Premium"`. | **Alto** (Precisión) |
| **Detección de Audio Activo (`pycaw` CoreAudio)** | **Elimina bucles infinitos al pausar**: Solo actúa cuando hay audio real circulando, distinguiendo pausas intencionales de anuncios. | **Alto** (Estabilidad) |

---

## ✅ Verificación y Pruebas Automatizadas

1. **Pruebas del Motor de PC (`pc/test_suite.py`)**:
   - `5/5` pruebas unitarias superadas (detección, cooldown, muter pycaw, motor guardián y protocolo URL).
2. **Batería Avanzada de Estrés y Casos Límite (`pc/test_advanced.py`)**:
   - `5/5` suites superadas:
     - `SUITE 1`: Detección multilingüe con 27 casos (español, inglés, alemán, francés, portugués, italiano, sin falsos positivos).
     - `SUITE 2`: Simulación End-to-End de ciclo completo de salto y métricas.
     - `SUITE 3`: Concurrencia con 150 cambios de modo y 20 ciclos de inicio/parada sin deadlocks.
     - `SUITE 4`: Tolerancia a fallos de configuración con fallback transparente.
     - `SUITE 5`: Equivalencia de lógica de notificaciones Android.
3. **Pruebas Reales en Vivo con Spotify en Windows**:
   - **Detección en tiempo real**: Identificación de pistas activas (`iZaak - TOY`, `Clarent - BIENVENIDA`) a través de la integración híbrida de Windows SMTC y `Chrome_WidgetWin_1`.
   - **Maniobra real de bypass**: Verificación del ciclo completo de cierre atómico de procesos, relanzamiento con ventana visible, purga de buffer residual (`Next Track`) y reanudación confirmada (`PlaybackStatus = 4 / Playing`).
   - **Tiempo de reanudación medido**: **4.60 segundos**, con audio activo y línea de tiempo avanzando en tiempo real.
4. **Compilación de Ejecutable y APK**:
   - `dist/SpotiSkip/SpotiSkip.exe` (PE 64-bit ejecutable nativo para Windows 10/11).
   - `SpotiSkip-Android.apk` (5.73 MB, SDK 34, Android 8.0 - 14+).

---

## ⚖️ Aviso Legal
Este software ha sido desarrollado con fines de investigación educativa en automatización de interfaces, gestión de procesos en sistemas operativos y accesibilidad de audio.
