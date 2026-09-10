# âš¡ SpotiGuard: Autonomous Spotify Ad Guardian & Bypass (PC & Android)

[![GitHub Repository](https://img.shields.io/badge/GitHub-Atm0027%2FSpotiGuard-blue?logo=github)](https://github.com/Atm0027/SpotiGuard)
[![Latest Release](https://img.shields.io/github/v/release/Atm0027/SpotiGuard?color=orange&logo=github)](https://github.com/Atm0027/SpotiGuard/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Android-green)](#)
[![Python](https://img.shields.io/badge/Python-3.10%2B-blue?logo=python)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-Android%20Native-purple?logo=kotlin)](#)
[![License](https://img.shields.io/badge/License-MIT-yellow)](#)

**SpotiGuard** es una soluciÃ³n de protecciÃ³n multimedia independiente, ultrarrÃ¡pida y multiplataforma para **PC (Windows)** y **MÃ³vil (Android)** que neutraliza automÃ¡ticamente los anuncios publicitarios de Spotify mediante la tÃ©cnica de **Reinicio RÃ¡pido Asistido (Kill & Relaunch Bypass con Windows SMTC)** o **Silenciamiento Furtivo en Segundo Plano (Auto-Mute Inteligente)**.

El sistema se ejecuta en segundo plano y **se activa de forma 100% autÃ³noma en cuanto detecta que Spotify se ha iniciado**, tanto en tu ordenador como en tu dispositivo mÃ³vil.

---

### ðŸ“¥ Descargas Directas Listas para Usar

* ðŸ“± **MÃ³vil (Android)**: **[Descargar SpotiGuard-1.0.2-11.apk](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.2-11/SpotiGuard-1.0.2-11.apk)** *(4.51 MB â€” Auto-activaciÃ³n al abrir Spotify o encender el mÃ³vil, cero permisos invasivos, compatible con Android 8 a 15)*
* ðŸ–¥ï¸ **PC (Windows)**: **[Descargar SpotiGuard-Windows-1.0.0-4.zip](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.0-4/SpotiGuard-Windows-1.0.0-4.zip)** *(47.9 MB â€” Paquete portable con ejecutable de 64 bits y reanudaciÃ³n Windows SMTC)*
* ðŸš€ **Lanzador Conjunto en PC (1 Clic)**: **[`Spotify (Protegido con SpotiGuard).lnk`](Spotify%20(Protegido%20con%20SpotiGuard).lnk)** *(Abre Spotify y SpotiGuard juntos de forma simultÃ¡nea)*
* ðŸ”Œ **Instalador RÃ¡pido por ADB para Android desde PC**: **[`instalar_android.bat`](instalar_android.bat)** *(InstalaciÃ³n en 1 clic en tu mÃ³vil vÃ­a cable USB o Wi-Fi sin advertencias de navegador)*
* ðŸŒ **Repositorio Oficial en GitHub**: **[https://github.com/Atm0027/SpotiGuard](https://github.com/Atm0027/SpotiGuard)**
* ðŸš€ **Ãšltima Release Oficial (v1.0.2-11)**: **[https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.2-11](https://github.com/Atm0027/SpotiGuard/releases/tag/v1.0.2-11)**

---

## âš¡ ActivaciÃ³n AutomÃ¡tica al Abrir Spotify (PC y Android)

Tanto en **Windows** como en **Android**, SpotiGuard estÃ¡ diseÃ±ado para funcionar **sin que tengas que acordarte de abrir la app**:

### ðŸ–¥ï¸ En PC (Windows):
1. **Auto-arranque con Windows**: SpotiGuard se inicia automÃ¡ticamente con el ordenador minimizado en la bandeja del sistema (`System Tray`), consumiendo 0% de CPU en reposo.
2. **DetecciÃ³n Reactiva RÃ¡pida (0.7s)**: En cuanto abres la app oficial de Spotify (o la versiÃ³n web/Store), SpotiGuard detecta el proceso de inmediato y activa la protecciÃ³n.
3. **Acceso Directo Unificado**: Puedes usar **`Spotify (Protegido con SpotiGuard).lnk`** o el script **`Lanzar Spotify Protegido.bat`** para lanzar ambos programas a la vez con un solo clic.

### ðŸ“± En MÃ³vil (Android):
1. **Auto-inicio al encender el mÃ³vil (`BootReceiver`)**: SpotiGuard se reactiva automÃ¡ticamente al reiniciar el telÃ©fono y permanece en espera de bajo consumo.
2. **Auto-despertar con Spotify (`SpotifyLaunchReceiver`)**: En cuanto Spotify empieza a reproducir mÃºsica o un anuncio, Android emite el evento `com.spotify.music.metadatachanged` y SpotiGuard se activa automÃ¡ticamente si no estaba corriendo.
3. **Lanzador Directo desde la App**: Si abres SpotiGuard y pulsas **"ðŸš€ Abrir Spotify"**, el servicio se inicia y abre Spotify en pantalla simultÃ¡neamente.

---

## ðŸ”¬ Â¿CÃ³mo Funciona TÃ©cnicamente el Salto y la ReanudaciÃ³n?

### 1. El Truco del Cierre y Relanzamiento (Bypass Trick)
* **InyecciÃ³n VolÃ¡til**: En las cuentas Spotify Free, las cuÃ±as publicitarias se insertan de manera transitoria en la memoria RAM y el bÃºfer del reproductor.
* **DetecciÃ³n Inmediata y MultilingÃ¼e**:
  - En **PC**, combina la inspecciÃ³n de la API nativa de Windows **System Media Transport Controls (SMTC / WinRT)**, tÃ­tulos de ventanas Win32 y sesiones de audio activas (**CoreAudio / pycaw**). Reconoce cuÃ±as publicitarias en espaÃ±ol (`"ESCÃšCHALO AHORA"`, `"Escucha sin lÃ­mites"`, `"Hazte Premium"`, `"Publicidad"`, `"Anuncio"`), inglÃ©s (`"Advertisement"`, `"Spotify Free"`) y formatos internacionales.
  - En **MÃ³vil**, utiliza un **Foreground Service** (`SpotiGuardService`) que escucha de forma dinÃ¡mica los eventos nativos de reproducciÃ³n emitidos por Spotify (`com.spotify.music.metadatachanged`).

### 2. Arquitectura de ReanudaciÃ³n de ReproducciÃ³n y PrevenciÃ³n de Bloqueos en PC
Tras analizar en profundidad el comportamiento de Spotify en Windows 10/11 (especialmente la versiÃ³n de Microsoft Store / WindowsApps):

1. **EliminaciÃ³n AtÃ³mica de Lanzadores HuÃ©rfanos**:
   - Spotify en Windows utiliza dos procesos: `Spotify.exe` y `SpotifyLauncher.exe`.
   - Si se cerraba Ãºnicamente `Spotify.exe`, `SpotifyLauncher` quedaba como proceso huÃ©rfano y generaba un cuadro de diÃ¡logo del sistema `#32770` con el mensaje: *"The Spotify application is not responding"*, impidiendo que Spotify volviera a abrirse.
   - **SoluciÃ³n implementada**: SpotiGuard termina de forma atÃ³mica tanto `Spotify.exe` como `SpotifyLauncher.exe`, garantizando un arranque en frÃ­o 100% limpio.

2. **Purga del BÃºfer del Anuncio Interrumpido (`Next Track`)**:
   - Al reiniciar Spotify tras cortar una cuÃ±a publicitaria, la app suele recordar el elemento que estaba sonando (el anuncio pausado).
   - **SoluciÃ³n implementada**: SpotiGuard envÃ­a primero una seÃ±al de salto de pista (`Next Track` / `VK_MEDIA_NEXT_TRACK` / `APPCOMMAND_MEDIA_NEXTTRACK`), lo que obliga al motor de Spotify a descartar la cuÃ±a comercial en cola y cargar de inmediato la siguiente canciÃ³n real de la lista del usuario.

3. **ReanudaciÃ³n Nativa con Windows SMTC (`winsdk`)**:
   - SpotiGuard implementa una tuberÃ­a de reanudaciÃ³n en 3 capas (SMTC Nativo WinRT -> Mensajes Win32 Directos `WM_APPCOMMAND` -> InyecciÃ³n de Teclas Extendidas de Hardware) con verificaciÃ³n activa en bucle hasta confirmar `PlaybackStatus = Playing`.

4. **ProtecciÃ³n Contra Instancias MÃºltiples (Mutex de Windows)**:
   - Se ha implementado un `CreateMutexW` con nombre global (`SpotiGuard_SingleInstance_App_Mutex`). Si el usuario abre la app dos veces, la segunda instancia trae la existente al frente y se cierra silenciosamente.

---

## ðŸ–¥ï¸ AplicaciÃ³n para PC (Windows)

Ubicada en la carpeta [`pc/`](pc/).

### CaracterÃ­sticas Principales:
* **Interfaz GrÃ¡fica HUD Futurista**: DiseÃ±ada en PyQt6 con tema oscuro moderno, indicadores de estado LED, tÃ­tulo de pista en tiempo real y contador de anuncios evitados y tiempo ahorrado.
* **IntegraciÃ³n con la Bandeja del Sistema (System Tray)**: Permite minimizar la aplicaciÃ³n discretamente junto al reloj de Windows. Notifica cada vez que un anuncio es saltado.
* **Auto-activaciÃ³n con Spotify**: Un vigilante (*Watcher*) en segundo plano detecta cuÃ¡ndo abres Spotify y pone en marcha la protecciÃ³n sin que tengas que pulsar nada.
* **Auto-arranque con Windows**: Registrado en el inicio de Windows para vigilar de fondo sin ventanas molestas.
* **Dos Modos de OperaciÃ³n**:
  1. **Reinicio RÃ¡pido (Skip & Relaunch)**: Cierra y reabre Spotify instantÃ¡neamente en ~2-4 segundos, saltando a la siguiente canciÃ³n real.
  2. **Silenciador Furtivo (Stealth Mute)**: Muta al 0% el volumen exclusivo de Spotify mientras dura el anuncio y lo restablece exactamente cuando empieza la canciÃ³n.

### ðŸš€ EjecuciÃ³n de la AplicaciÃ³n en PC:
* **OpciÃ³n RÃ¡pida (Lanzar Spotify con ProtecciÃ³n en 1 Clic)**: 
  - Haz doble clic en: **[`Spotify (Protegido con SpotiGuard).lnk`](Spotify%20(Protegido%20con%20SpotiGuard).lnk)**
* **Abrir solo SpotiGuard**:
  - Haz doble clic en: **[`SpotiSkip (Iniciar App).lnk`](SpotiSkip%20(Iniciar%20App).lnk)** o **[`dist/SpotiSkip/SpotiSkip.exe`](dist/SpotiSkip/SpotiSkip.exe)**

---

## ðŸ“± AplicaciÃ³n para MÃ³vil (Android)

Ubicada en la carpeta [`mobile/`](mobile/). Proyecto nativo completo en **Kotlin**.

### CaracterÃ­sticas Principales:
* **Auto-ActivaciÃ³n con Spotify**: Escucha las emisiones nativas (`com.spotify.music.metadatachanged`, `playbackstatechanged`, `queuechanged`) y se activa sola.
* **Servicio en Primer Plano Seguro (`SpotiGuardService`)**: Servicio oficial `mediaPlayback` con notificaciÃ³n de estado de baja prioridad. Garantiza que Android nunca detenga el servicio en segundo plano.
* **Receptor de Arranque (`BootReceiver`)**: Se reactiva solo al encender el telÃ©fono.
* **Modos de Funcionamiento**:
  1. **Silenciador Inteligente (Recomendado para MÃ³vil)**: Silencia el volumen multimedia (`STREAM_MUSIC`) en el milisegundo en que entra el anuncio y lo restaura sin interrumpir la pantalla ni tu navegaciÃ³n.
  2. **Reinicio RÃ¡pido Asistido**: Reinicia el proceso de Spotify y reanuda la cola con eventos multimedia.

### ðŸ“² Descarga e InstalaciÃ³n del APK Oficial:
* **Descarga directa**: **[Descargar SpotiGuard-1.0.2-11.apk (v1.0.2-11)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.2-11/SpotiGuard-1.0.2-11.apk)** *(4.51 MB)*.
* **Firma Oficial**: AlmacÃ©n de claves RSA 2048-bit (`CN=SpotiGuard`) con esquemas **v2** y **v3**. Cero bloqueos de Google Play Protect.

---

### âš™ï¸ ConfiguraciÃ³n Inicial en el MÃ³vil (2 sencillos pasos):
1. **Paso 1: Activar transmisiÃ³n en Spotify (Solo 1 vez)**:
   - Abre **Spotify** -> Toca el icono de **Ajustes (âš™ï¸)**.
   - Ve a la pestaÃ±a **Â«ReproducciÃ³nÂ»** (o el apartado **Â«DispositivosÂ»** segÃºn tu versiÃ³n de Spotify).
   - Activa el interruptor: **Â«Estado de transmisiÃ³n del dispositivoÂ»** (o *Â«Estado de emisiÃ³n del dispositivoÂ»* / *Â«Device Broadcast StatusÂ»*).
     *(Debajo pone: "Permite que otras apps de este dispositivo vean lo que estÃ¡s escuchando")*.
2. **Paso 2: Iniciar la ProtecciÃ³n en SpotiGuard**:
   - Abre **SpotiGuard**.
   - Pulsa en **"ðŸ›¡ï¸ Activar ProtecciÃ³n SpotiGuard"** (o simplemente pulsa "ðŸš€ Abrir Spotify").
   - Opcional: Pulsa en **"Desactivar OptimizaciÃ³n de BaterÃ­a"** y selecciona **"Sin restricciones"**.
3. Â¡Listo! A partir de ese momento, cada vez que abras Spotify o enciendas el mÃ³vil, SpotiGuard estarÃ¡ activo protegiÃ©ndote.

---

## ðŸ·ï¸ Sistema de Control de Versiones Heredado de JARVIS

SpotiGuard adopta la arquitectura exacta de nomenclatura y versionado del proyecto **JARVIS**:

| Componente | Formato | Ejemplo en SpotiGuard | Significado |
| :--- | :--- | :--- | :--- |
| **`versionName`** | SemVer `X.Y.Z` | `1.0.2` | Nombre semÃ¡ntico deducido automÃ¡ticamente de Conventional Commits (`feat:` minor, `fix:` patch, `BREAKING CHANGE:` major). |
| **`versionCode`** | Entero creciente | `9` | Recuento estricto de commits (`git rev-list --count HEAD`). Nunca retrocede y garantiza actualizaciones vÃ¡lidas. |
| **Etiqueta Git (Tag)** | `v<versionName>-<versionCode>` | `v1.0.2-11` | El estado vive en las etiquetas de git. |
| **TÃ­tulo de Release** | `<App> <versionName> (<versionCode>)` | `SpotiGuard 1.0.2 (11)` | TÃ­tulo estandarizado para las publicaciones de GitHub. |
| **Paquete Windows** | `<App>-Windows-<versionName>-<versionCode>.zip` | `SpotiGuard-Windows-1.0.0-4.zip` | Binario portable empaquetado para PC. |
| **Paquete Android** | `<App>-<versionName>-<versionCode>.apk` | `SpotiGuard-1.0.2-11.apk` | Paquete APK firmado para mÃ³viles. |

---

## ðŸ“‚ Estructura del Proyecto

```text
Ads Spotify/
â”œâ”€â”€ Spotify (Protegido con SpotiGuard).lnk # Acceso directo conjunto para abrir Spotify + SpotiGuard
â”œâ”€â”€ SpotiSkip (Iniciar App).lnk    # Acceso directo para abrir la app de SpotiGuard en PC
â”œâ”€â”€ SpotiGuard-1.0.2-11.apk         # Paquete APK oficial limpio y firmado para Android (4.51 MB)
â”œâ”€â”€ Lanzar Spotify Protegido.bat   # Script para lanzar Spotify y SpotiGuard en PC
â”œâ”€â”€ instalar_android.bat           # Instalador automÃ¡tico en 1 clic para mÃ³vil vÃ­a ADB
â”œâ”€â”€ README.md                      # DocumentaciÃ³n centralizada del proyecto
â”œâ”€â”€ pc/                            # CÃ³digo fuente y componentes de PC
â”‚   â”œâ”€â”€ app.py                     # Controlador principal con Mutex e inicio con Windows
â”‚   â”œâ”€â”€ core/
â”‚   â”‚   â”œâ”€â”€ watcher.py             # Vigilante ultrarrÃ¡pido (0.7s) reactivo a Spotify
â”‚   â”‚   â”œâ”€â”€ detector.py            # InspecciÃ³n hÃ­brida SMTC + tÃ­tulos Win32 + pycaw
â”‚   â”‚   â”œâ”€â”€ skipper.py             # Cierre atÃ³mico dual, Next Track y reanudaciÃ³n SMTC
â”‚   â”‚   â”œâ”€â”€ audio_muter.py         # Control de volumen individual con CoreAudio
â”‚   â”‚   â””â”€â”€ version.py             # Sistema unificado de versiones de JARVIS
â””â”€â”€ mobile/                        # AplicaciÃ³n nativa para MÃ³vil (Android)
    â”œâ”€â”€ README_ANDROID.md          # GuÃ­a tÃ©cnica y de instalaciÃ³n para Android
    â””â”€â”€ app/src/main/
        â”œâ”€â”€ AndroidManifest.xml    # Manifiesto limpio con BootReceiver y SpotifyLaunchReceiver
        â””â”€â”€ java/com/spotiskip/guardian/
            â”œâ”€â”€ MainActivity.kt    # Pantalla de control y auto-arranque
            â”œâ”€â”€ receivers/
            â”‚   â”œâ”€â”€ BootReceiver.kt          # Auto-arranque al encender el telÃ©fono
            â”‚   â””â”€â”€ SpotifyLaunchReceiver.kt # Auto-activaciÃ³n al recibir emisiones de Spotify
            â””â”€â”€ services/
                â””â”€â”€ SpotiGuardService.kt     # Foreground Service y receptor Broadcast de Spotify
```

---

## âš–ï¸ Aviso Legal
Este software ha sido desarrollado con fines de investigaciÃ³n educativa en automatizaciÃ³n de interfaces, gestiÃ³n de procesos en sistemas operativos y accesibilidad de audio.
