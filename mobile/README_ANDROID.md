# SpotiGuard Mobile (Android)

Aplicación nativa de Android que detecta en tiempo real los anuncios de Spotify y los neutraliza de forma automática mediante la técnica de **Cierre Forzoso Automatizado y Relanzamiento Limpio (Force Stop -> Relaunch -> Purge Buffer -> Play)** con **Servicio de Accesibilidad / Root**, **Watchdog de fin de pista** y **detección dinámica de requisitos**.

> **Nota**: De acuerdo con las especificaciones de diseño, SpotiGuard no silencia ni enmascara los anuncios: fuerza el cierre inmediato de Spotify (purgando la publicidad de la memoria), relanza la app al instante y reanuda la música automáticamente.

---

## 📱 ¿Cómo Funciona en el Móvil? (Arquitectura v1.0.6-16)

### 1. El Reto de Android 14+ / 15 / 16: Cierre Forzoso Real
* En versiones modernas de Android, Google revocó a las aplicaciones de terceros la posibilidad de utilizar `killBackgroundProcesses` contra otras apps.
* Si Spotify no se cierra físicamente, permanece en memoria con el anuncio en búfer. Al volver a abrirla y pulsar play, el anuncio continuaría sonando.
* **Solución v1.0.6-16**:
  - **Servicio de Accesibilidad (`SpotiGuardAccessibilityService`)**: Al interceptar un anuncio publicitario, SpotiGuard abre la ventana de información de Spotify en Ajustes y pulsa automáticamente en milisegundos **"Forzar detención"** o **"Forzar cierre"** (compatible con Samsung One UI, Xiaomi HyperOS/MIUI, Pixel, etc.) y confirma el diálogo del sistema.
  - **Cierre directo por Root**: Si el dispositivo dispone de privilegios Root, ejecuta de inmediato `su -c am force-stop com.spotify.music` en segundo plano sin desplegar pantallas.
  - **Relanzamiento Inmediato y Reanudación**: Una vez cerrado el proceso, SpotiGuard relanza Spotify de inmediato mediante Intent del sistema y envía los comandos multimedia `NEXT` y `PLAY` para arrancar la siguiente canción sin anuncios.

### 2. Detección Dual de Anuncios:
1. **Watchdog de Expiración de Pista**: Resuelve el problema de que Spotify no emite broadcasts al inicio de un anuncio. Monitorea la duración y posición exacta de la pista musical. Si la canción termina y no entra una nueva pista en la ventana de tolerancia, deduce inmediatamente la entrada del bloque publicitario y dispara el bypass.
2. **Detección por Metadatos y Palabras Clave**: Analiza los IDs (anuncios no tienen URI `spotify:track:`) y títulos/artistas como "Publicidad", "Advertisement", "Spotify Free", etc.

### 3. Secuencia de Bypass Atómica (Sin Mutear):
1. **Detección Instantánea**: Intercepción del anuncio por metadatos o por el Watchdog de duración.
2. **Detención multimedia**: Envío de `KEYCODE_MEDIA_PAUSE` y `STOP`.
3. **Cierre forzoso de Spotify**: A través de `SpotiGuardAccessibilityService` ("Forzar cierre" / "Forzar detención") o comando Root.
4. **Relanzamiento limpio**: Apertura de Spotify con búfer limpio de publicidad.
5. **Purga del búfer**: Envío de `KEYCODE_MEDIA_NEXT`.
6. **Reanudación de música**: Envío de `KEYCODE_MEDIA_PLAY`.

### 4. Detección Dinámica de Requisitos en la App:
* **Estado de Emisión de Spotify**: En cuanto SpotiGuard detecta el primer evento procedente de Spotify, el botón **1** desaparece de la pantalla.
* **Batería sin Restricciones**: Si SpotiGuard está excluido de las restricciones de ahorro de energía, el botón **2** desaparece de la pantalla.
* **Servicio de Accesibilidad**: Una vez concedido el permiso en Ajustes -> Accesibilidad, el botón **3** desaparece de la pantalla.
* **Distintivo de Éxito**: Cuando todos los requisitos están cumplidos, se muestra una tarjeta verde de confirmación: *"✅ Requisitos listos: Emisión, Batería y Accesibilidad verificados"*.

---

## 📲 Descarga e Instalación del APK Oficial

- **Descarga Directa del APK**: **[`SpotiGuard-1.0.6-16.apk`](../SpotiGuard-1.0.6-16.apk)** *(4.74 MB — Release v1.0.6-16)*
- **Firma Oficial de Producción**: Firmado con Keystore Release propio con esquemas **v2 y v3** activos para máxima compatibilidad desde Android 8.0 hasta Android 16.

### 🛡️ Opciones de Instalación en Android:

#### Opción 1: Instalación Directa en 1 Clic desde el PC (Script ADB - Recomendada)
1. Conecta tu teléfono al ordenador con cable USB y asegúrate de tener activada la **Depuración por USB** (en Ajustes -> Opciones de desarrollador).
2. En la carpeta del proyecto en tu PC, haz doble clic en **`instalar_android.bat`**.
3. El script detectará tu dispositivo e instalará el APK directamente en el móvil (las instalaciones ADB no se ven afectadas por bloqueos de descarga de navegadores).

#### Opción 2: Instalación Manual en el Móvil
1. Transfiere o descarga el archivo `SpotiGuard-1.0.6-16.apk` en tu teléfono.
2. Abre la app **Mis Archivos / Files / Gestor de archivos** de tu teléfono, ve a la carpeta **Descargas (Downloads)** y pulsa el archivo APK.
3. Pulsa **Instalar**. *(Si aparece aviso de origen desconocido, concede permiso a tu app de Archivos).*

---

### 🛑 Si Android muestra "Bloqueada por Play Protect" (Causa y Solución en 10s)

#### ¿Por qué salta el aviso?
A partir de 2024, Google Play Protect introdujo una directiva que analiza los APKs instalados fuera de Google Play que declaren permisos de Accesibilidad (`BIND_ACCESSIBILITY_SERVICE`), clasificándolos preventivamente porque la accesibilidad permite automatizar pulsaciones en pantalla (que en SpotiGuard se usan estrictamente para pulsar "Forzar cierre" en Spotify).

#### Cómo resolverlo en tu teléfono:
* **Método 1 (Instalación por cable USB / ADB - Recomendado)**:
  - Ejecuta `instalar_android.bat` en tu PC con el móvil conectado. Al tratarse de una instalación en modo desarrollador, se instala limpiamente sin interferencias de Play Protect.
* **Método 2 (Directo en la pantalla de aviso)**:
  - Si aparece el texto **"Más detalles"** o una flecha hacia abajo en el aviso de bloqueo, tócalo.
  - Pulsa en **"Instalar de todas formas (no seguro)"**.
* **Método 3 (Desactivar temporalmente el análisis de Play Protect)**:
  1. Abre la aplicación **Google Play Store**.
  2. Toca tu **foto de perfil** (arriba a la derecha) y pulsa en **Play Protect**.
  3. Toca el icono de la **rueda de ajustes ⚙️** (arriba a la derecha).
  4. Desactiva temporalmente el interruptor: **"Analizar las aplicaciones con Play Protect"**.
  5. Instala `SpotiGuard-1.0.6-16.apk` y luego puedes volver a activarlo.

#### Si en Android 13/14/15/16 la Accesibilidad muestra "Ajuste restringido":
1. Ve a **Ajustes de Android -> Aplicaciones -> SpotiGuard**.
2. Toca los **tres puntos verticales ⋮** en la esquina superior derecha.
3. Selecciona **"Permitir ajustes restringidos"** y confirma con tu PIN o huella.
4. Vuelve a **Accesibilidad** y activa el interruptor del servicio **SpotiGuard**.

---

## ⚙️ Configuración Inicial Rápida:

1. Abre **SpotiGuard**.
2. Si aparece el botón de batería, púlsalo y selecciona "Permitir" (el botón se ocultará automáticamente).
3. Si aparece el botón de Spotify, púlsalo para verificar que en Spotify -> Ajustes ⚙️ -> Reproducción esté activo *"Estado de transmisión del dispositivo"*. En cuanto suene música, el botón desaparecerá solo.
4. Si aparece el botón de accesibilidad, púlsalo y activa **SpotiGuard** en la lista de servicios de accesibilidad (el botón desaparecerá automáticamente).
5. Cuando veas el mensaje verde *"✅ Requisitos listos: Emisión, Batería y Accesibilidad verificados"*, la protección estará 100% activa.\n