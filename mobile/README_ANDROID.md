# SpotiGuard Mobile (Android)

Aplicación nativa de Android que detecta en tiempo real los anuncios de Spotify y los neutraliza de forma automática mediante la técnica de **Cierre Forzoso Automatizado y Relanzamiento Limpio (Force Stop -> Relaunch -> Purge Buffer -> Play)** con **Servicio de Accesibilidad / Root**, **Watchdog de fin de pista**, **silenciamiento de seguridad 0 ms** y **detección dinámica de requisitos**.

---

## 📱 ¿Cómo Funciona en el Móvil? (Arquitectura v1.0.4-14)

### 1. El Reto de Android 14+: ¿Por qué Spotify no se cerraba?
* En Android 14 y versiones superiores, Google revocó a las aplicaciones de terceros la posibilidad de utilizar `killBackgroundProcesses` contra otras aplicaciones.
* Al no cerrarse Spotify, la aplicación seguía en segundo plano con el anuncio cargado en su búfer de reproducción. Al abrirla y darle al play, el anuncio volvía a sonar.
* **Solución v1.0.4-14**:
  - **Servicio de Accesibilidad (`SpotiGuardAccessibilityService`)**: Al interceptar un anuncio, SpotiGuard abre la ventana de información de la app de Spotify en Ajustes del sistema y pulsa automáticamente "Forzar detención" y "Aceptar" en menos de 250 milisegundos.
  - **Cierre directo por Root**: Si tu móvil está rooteado, se ejecuta `su -c am force-stop com.spotify.music` instantáneamente en segundo plano sin abrir pantallas.
  - **Silenciamiento de Seguridad (0 ms)**: En el milisegundo exacto en que se detecta el anuncio publicitario, el audio multimedia se reduce a cero (`AudioController.mute()`) para que jamás se escuche ni un fragmento de publicidad mientras Spotify se cierra y se vuelve a abrir.

### 2. Detección Dual de Anuncios:
1. **Watchdog de Expiración de Pista**: Resuelve el problema de que Spotify no emite broadcasts al inicio de un anuncio. Monitorea la duración y posición exacta de la pista musical. Si la canción termina y no entra una nueva canción, deduce inmediatamente el anuncio y lanza el bypass.
2. **Detección por Metadatos y Palabras Clave**: Analiza los IDs (anuncios no tienen URI `spotify:track:`) y títulos/artistas como "Publicidad", "Advertisement", "Spotify Free", etc.

### 3. Secuencia de Bypass Atómica:
1. **Silenciamiento inmediato**: `AudioController.mute(context)`.
2. **Detención multimedia**: Envío de `KEYCODE_MEDIA_PAUSE` y `STOP`.
3. **Cierre forzoso**: A través de `SpotiGuardAccessibilityService` o Root.
4. **Relanzamiento limpio**: Apertura de Spotify sin anuncio congelado.
5. **Purga del búfer**: Envío de `KEYCODE_MEDIA_NEXT`.
6. **Reanudación**: Envío de `KEYCODE_MEDIA_PLAY`.
7. **Restauración de volumen**: `AudioController.unmute(context)`.

### 4. Detección Dinámica de Requisitos en la App:
* **Estado de Emisión de Spotify**: En cuanto SpotiGuard detecta el primer evento procedente de Spotify, el botón **1** desaparece de la pantalla.
* **Batería sin Restricciones**: Si SpotiGuard está excluido de las restricciones de ahorro de energía, el botón **2** desaparece de la pantalla.
* **Servicio de Accesibilidad**: Una vez concedido el permiso en Ajustes -> Accesibilidad, el botón **3** desaparece de la pantalla.
* **Distintivo de Éxito**: Cuando todos los requisitos están completados, se muestra una tarjeta verde de confirmación: *"✅ Requisitos listos: Emisión, Batería y Accesibilidad verificados"*.

---

## 📲 Descarga e Instalación del APK Oficial

- **Descarga Directa del APK**: **[Descargar SpotiGuard-1.0.4-14.apk (Release v1.0.4-14)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.4-14/SpotiGuard-1.0.4-14.apk)** *(4.74 MB)*
- **Firma Oficial de Producción**: Firmado con Keystore Release propio con esquemas **v2 y v3** activos para máxima compatibilidad con Android 8.0 hasta Android 15+.

### 🛡️ Opciones de Instalación en Android:

#### Opción 1: Instalación Normal en el Móvil (Recomendada)
1. Descarga el archivo APK en tu teléfono.
2. Abre la app **Mis Archivos / Files / Gestor de archivos** de tu teléfono, ve a la carpeta **Descargas (Downloads)** y pulsa el archivo `SpotiGuard-1.0.4-14.apk`.
3. Pulsa **Instalar**. *(Si aparece aviso de origen desconocido, concede permiso a tu app de Archivos).*

#### Opción 2: Instalación Directa en 1 Clic desde el PC (Script ADB)
1. Conecta tu teléfono al ordenador con cable USB y asegúrate de tener activada la **Depuración por USB**.
2. En la carpeta del proyecto en tu PC, haz doble clic en **`instalar_android.bat`**.
3. El script detectará tu dispositivo e instalará el APK directamente en el móvil (las instalaciones ADB nunca son bloqueadas por Play Protect).

---

### 🛑 Si Android muestra "Bloqueada por Play Protect" (Causa y Solución en 10s)

#### ¿Por qué salta el aviso?
A partir de 2024, Google Play Protect introdujo un sistema de protección automática que **bloquea la instalación de cualquier aplicación descargada fuera de Google Play que declare permisos de Accesibilidad (`BIND_ACCESSIBILITY_SERVICE`)**, clasificándola automáticamente como riesgo porque la accesibilidad permite automatizar toques en pantalla (que en SpotiGuard se usan exclusivamente para forzar el cierre de Spotify).

#### Cómo resolverlo en tu teléfono:
* **Método 1 (Directo en la pantalla de aviso)**:
  - Si aparece el texto **"Más detalles"** o una flecha hacia abajo en el aviso de bloqueo, tócalo.
  - Pulsa en **"Instalar de todas formas (no seguro)"**.
* **Método 2 (Desactivar temporalmente el análisis de Play Protect)**:
  1. Abre la aplicación **Google Play Store**.
  2. Toca tu **foto de perfil** (arriba a la derecha) y pulsa en **Play Protect**.
  3. Toca el icono de la **rueda de ajustes ⚙️** (arriba a la derecha).
  4. Desactiva el interruptor: **"Analizar las aplicaciones con Play Protect"**.
  5. Vuelve a pulsar el archivo `SpotiGuard-1.0.4-14.apk` e instálalo normalmente.
  6. *(Opcional)*: Una vez instalada, puedes volver a activar Play Protect.
* **Método 3 (Instalación por ADB sin advertencias)**:
  - Conecta el móvil al PC por USB y ejecuta `instalar_android.bat`. Al ser una instalación directa de desarrollo, Play Protect no interfiere.

#### Si en Android 13/14+ la Accesibilidad dice "Ajuste restringido":
1. Ve a **Ajustes de Android -> Aplicaciones -> SpotiGuard**.
2. Toca los **tres puntos verticales ⋮** en la esquina superior derecha.
3. Selecciona **"Permitir ajustes restringidos"** y confirma con tu PIN o huella.
4. Vuelve a **Accesibilidad** y activa el interruptor de SpotiGuard.

---

## ⚙️ Configuración Inicial Rápida:

1. Abre **SpotiGuard**.
2. Si aparece el botón de batería, púlsalo y selecciona "Permitir" (el botón se ocultará automáticamente).
3. Si aparece el botón de Spotify, púlsalo para verificar que en Spotify -> Ajustes ⚙️ -> Reproducción esté activo *"Estado de transmisión del dispositivo"*. En cuanto suene música, el botón se ocultará solo.
4. Si aparece el botón de accesibilidad, púlsalo y activa **SpotiGuard** en la lista de servicios de accesibilidad (el botón se ocultará automáticamente).
5. Cuando veas el mensaje verde *"✅ Requisitos listos"*, la protección estará 100% activa e infalible.
