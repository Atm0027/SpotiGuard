# SpotiGuard Mobile (Android)

Aplicación nativa de Android que detecta en tiempo real los anuncios de Spotify y los neutraliza de forma automática mediante la técnica de **Cierre Forzoso a Nivel de Sistema y Relanzamiento Limpio (Force Stop -> Relaunch -> Purge Buffer -> Play)** con **Shizuku API (Shell ADB) / Root**, **Watchdog de fin de pista** y **detección dinámica de requisitos**.

> **Nota de Diseño**: De acuerdo con las directrices del proyecto, SpotiGuard **NO silencia el audio ni enmascara los anuncios**: destruye físicamente el proceso de Spotify en segundo plano (purgando la publicidad de la memoria), relanza la app al instante y reanuda la música automáticamente, exactamente igual que en PC.

---

## 📱 ¿Cómo Funciona en el Móvil? (Arquitectura v1.0.8-18)

### 1. Cierre Forzoso Real a Nivel de Kernel (Sin Pantallas de Ajustes):
* En Android moderno (Android 14, 15 y 16), las apps convencionales de terceros no pueden detener procesos ajenos con `killBackgroundProcesses`.
* Intentar abrir los Ajustes del sistema ("Información de la aplicación") para pulsar "Forzar detención" fue completamente descartado: interrumpe al usuario (por ejemplo, mientras juega o usa otra app), es inestable y no funciona con pantalla apagada.
* **Solución de Grado de Sistema v1.0.8-18**:
  - **Integración con Shizuku API (`dev.rikka.shizuku:api:13.1.5`)**: SpotiGuard se comunica directamente con el daemon de Shizuku para ejecutar la orden nativa de Android `am force-stop com.spotify.music` con permisos ADB (UID 2000).
  - **Cierre Instantáneo en 0.05s**: Spotify es cerrado a nivel de proceso en milisegundos en segundo plano, sin abrir absolutamente ninguna ventana ni diálogo en pantalla.
  - **Soporte Root Directo**: En terminales con Magisk / KernelSU / APatch, ejecuta `su am force-stop com.spotify.music` inmediatamente sin requerir Shizuku.
  - **Relanzamiento Limpio y Reanudación**: En cuanto el proceso es eliminado, SpotiGuard abre de nuevo Spotify de forma limpia, purga el búfer con `KEYCODE_MEDIA_NEXT` y arranca la música con `KEYCODE_MEDIA_PLAY`.

### 2. Detección Dual de Anuncios:
1. **Watchdog de Expiración de Pista**: Resuelve el problema fundamental de que Spotify **no emite eventos de broadcast al iniciar un anuncio publicitario**. SpotiGuard calcula la duración y posición exacta de la pista musical. Si la canción termina y no entra una nueva pista en la ventana de tolerancia, deduce la entrada del bloque comercial e inicia la neutralización.
2. **Detección por Metadatos y Palabras Clave**: Analiza los identificadores de pista (`!id.startsWith("spotify:track:")`) y títulos/artistas característicos ("Publicidad", "Advertisement", "Spotify Free", "Werbung", etc.).

### 3. Secuencia de Bypass Atómica (Idéntica a PC):
1. **Detección Instantánea**: Intercepción del anuncio por metadatos o por el Watchdog de duración.
2. **Detención multimedia**: Envío de `KEYCODE_MEDIA_PAUSE` y `STOP`.
3. **Cierre forzoso de Spotify**: Ejecución de `am force-stop com.spotify.music` mediante Shizuku o Root en segundo plano (<50ms).
4. **Relanzamiento limpio**: Apertura de Spotify con búfer limpio de publicidad.
5. **Purga del búfer**: Envío de `KEYCODE_MEDIA_NEXT`.
6. **Reanudación de música**: Envío de `KEYCODE_MEDIA_PLAY`.

### 4. Detección Dinámica de Requisitos en la App:
* **Estado de Emisión de Spotify**: En cuanto SpotiGuard detecta el primer evento procedente de Spotify, el botón **1** desaparece de la pantalla.
* **Batería sin Restricciones**: Si SpotiGuard está excluido del ahorro de energía del sistema, el botón **2** desaparece de la pantalla.
* **Cierre Silencioso (Shizuku)**: Concede autorización a SpotiGuard con 1 toque desde la app. Al concederse, el botón **3** desaparece.
* **Distintivo de Éxito**: Cuando todos los requisitos están cumplidos, se muestra una tarjeta verde de confirmación: *"✅ Requisitos listos: Emisión, Batería y Shizuku verificados"*.

---

## 📲 Descarga e Instalación del APK Oficial

- **Descarga Directa de SpotiGuard**: **[`SpotiGuard-1.0.8-18.apk`](../SpotiGuard-1.0.8-18.apk)** *(4.76 MB — Release v1.0.8-18)*
- **Descarga de Shizuku Oficial**: **[`shizuku.apk`](../shizuku.apk)** *(Oficial v13.6.0)*
- **Firma Oficial de Producción**: Firmado con Keystore Release propio con esquemas **v2 y v3** activos para compatibilidad total con Android 8.0 hasta Android 16.

---

## 🚀 Instalación y Activación Rápida

### Opción 1: Instalación Completa en 1 Clic desde el PC (Recomendada)
1. Conecta tu teléfono al ordenador con cable USB y comprueba que esté activa la **Depuración por USB** (en Ajustes -> Opciones de desarrollador).
2. Haz doble clic en el archivo **`instalar_android.bat`** en la carpeta del proyecto.
3. El script automáticamente:
   - Detectará tu dispositivo.
   - Instalará **SpotiGuard** (`SpotiGuard-1.0.8-18.apk`).
   - Instalará **Shizuku** (`shizuku.apk`) si no la tienes.
   - Iniciará el servicio de Shizuku en tu móvil mediante ADB.
4. Abre **SpotiGuard** en tu móvil y pulsa el botón **"Conceder Permiso Shizuku"** (o "Autorizar" en el diálogo emergente).
5. ¡Listo! Spotify se cerrará y reabrirá silenciosamente en cada anuncio.

### Opción 2: Si ya tienes SpotiGuard instalado y solo necesitas activar Shizuku
1. Conecta el teléfono por USB con depuración activada.
2. Haz doble clic en **`activar_shizuku.bat`**.
3. El servicio Shizuku quedará activo al instante.

### Opción 3: Activación Inalámbrica de Shizuku (Sin PC)
1. Conéctate a una red Wi-Fi en tu móvil.
2. Abre la app **Shizuku** y pulsa en *"Iniciar mediante depuración inalámbrica"*.
3. Sigue los pasos de emparejamiento con el código de 6 dígitos que proporciona Android.

---

## ⚙️ Configuración Inicial en SpotiGuard:

1. Abre **SpotiGuard**.
2. Si aparece el botón de batería, púlsalo y pulsa "Permitir" (el botón se ocultará automáticamente).
3. Si aparece el botón de Spotify, púlsalo para verificar que en Spotify -> Ajustes ⚙️ -> Reproducción esté activado *"Estado de transmisión del dispositivo"*. En cuanto pongas una canción, desaparecerá solo.
4. Pulsa el botón de Shizuku para conceder autorización al servicio (desaparecerá de inmediato).
5. Cuando veas el recuadro verde *"✅ Requisitos listos: Emisión, Batería y Shizuku verificados"*, el sistema estará completamente operativo en segundo plano.
