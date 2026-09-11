# SpotiGuard Mobile (Android)

Aplicación nativa de Android que detecta en tiempo real los anuncios de Spotify y los neutraliza de forma automática mediante la técnica de **Cierre Forzoso a Nivel de Sistema y Relanzamiento Limpio (Force Stop -> Relaunch -> Purge Buffer -> Play)** con **Shizuku API (Shell ADB) / Root**, **Watchdog de fin de pista**, **detección dinámica de requisitos** y **cero permisos invasivos (Servicio de Accesibilidad eliminado por completo)**.

> **Nota de Diseño**: De acuerdo con las directrices del proyecto, SpotiGuard **NO silencia el audio ni enmascara los anuncios**: destruye físicamente el proceso de Spotify en segundo plano (purgando la publicidad de la memoria), relanza la app al instante y reanuda la música automáticamente, exactamente igual que en PC.

---

## 📱 ¿Cómo Funciona en el Móvil? (Arquitectura v1.0.8-20)

### 1. Cierre Forzoso Real a Nivel de Kernel (Sin Pantallas de Ajustes):
* En Android moderno (Android 14, 15 y 16), las apps convencionales de terceros no pueden detener procesos ajenos con `killBackgroundProcesses`.
* Intentar abrir los Ajustes del sistema ("Información de la aplicación") para pulsar "Forzar detención" fue completamente descartado: interrumpe al usuario (por ejemplo, mientras juega o usa otra app), es inestable y no funciona con pantalla apagada.
* **Solución de Grado de Sistema v1.0.8-20**:
  - **Integración con Shizuku API (`dev.rikka.shizuku:api:13.1.5`)**: SpotiGuard se comunica directamente con el daemon de Shizuku para ejecutar la orden nativa de Android `am force-stop com.spotify.music` con permisos ADB (UID 2000).
  - **Cierre Instantáneo en 0.05s**: Spotify es cerrado a nivel de proceso en milisegundos en segundo plano, sin abrir absolutamente ninguna ventana ni diálogo en pantalla.
  - **Soporte Root Directo**: En terminales con Magisk / KernelSU / APatch, ejecuta `su am force-stop com.spotify.music` inmediatamente sin requerir Shizuku.
  - **Relanzamiento Limpio y Reanudación**: En cuanto el proceso es eliminado, SpotiGuard abre de nuevo Spotify de forma limpia, purga el búfer con `KEYCODE_MEDIA_NEXT` y arranca la música con `KEYCODE_MEDIA_PLAY`.

### 2. Detección Dual de Anuncios:
1. **Watchdog de Expiración de Pista**: Resuelve el problema fundamental de que Spotify **no emite eventos de broadcast al iniciar un anuncio publicitario**. SpotiGuard calcula la duración y posición exacta de la pista musical. Si la canción termina y no entra una nueva pista en la ventana de tolerancia, deduce la entrada del bloque comercial e inicia la neutralización.
2. **Detección por Metadatos y Palabras Clave**: Analiza los identificadores de pista (`!id.startsWith("spotify:track:")`) y títulos/artistas característicos ("Publicidad", "Advertisement", "Spotify Free", "Werbung", etc.).

### 3. Secuencia de Bypass Atómica y Reanudación Multi-Canal (Triple Play Dispatch):
1. **Detección Instantánea**: Intercepción del anuncio por metadatos o por el Watchdog de duración. Los indicadores de estado se resetean inmediatamente (`isPlaybackActive = false`).
2. **Detención multimedia**: Envío de `KEYCODE_MEDIA_PAUSE` y `STOP` dirigidos a Spotify.
3. **Cierre forzoso de Spotify (<50ms)**: Ejecución atómica de `am force-stop com.spotify.music` mediante Shizuku (`waitFor()` sin timeout que elimina excepciones Binder) o Root en segundo plano, sin abrir ninguna ventana de Ajustes ni tocar la pantalla.
4. **Despertar 100% en Segundo Plano (Zero UI / Cero Ventanas)**: Reactivación silenciosa de Spotify mediante broadcast a su receptor interno `MediaButtonReceiver` y Shizuku Shell. Spotify se reinicia en el fondo sin robar el foco ni interrumpir lo que estés haciendo en la pantalla (juegos como Brawl Stars, navegación o mensajería).
5. **Purga del búfer publicitario**: Envío multi-canal de salto de pista (`KeyEvent.KEYCODE_MEDIA_NEXT` + broadcast interno de widget `com.spotify.mobile.android.ui.widget.NEXT` + `cmd media_session dispatch next`).
6. **Reanudación Garantizada (Triple Play Dispatch)**:
   - **Canal 1 (AudioManager)**: `KeyEvent.KEYCODE_MEDIA_PLAY` estrictamente idempotente (nunca pausa si ya está sonando).
   - **Canal 2 (Spotify Widget Broadcast)**: `Intent("com.spotify.mobile.android.ui.widget.PLAY").setPackage("com.spotify.music")` que despierta directamente el servicio de audio en frío de Spotify.
   - **Canal 3 (System MediaSession Shizuku)**: `cmd media_session dispatch play` despachado a nivel de sistema.
   - **Secuencia de 4 Pulsos Escalonados (1.8s, 2.8s, 3.5s, 4.5s)**: Garantiza que la música vuelva a sonar inmediatamente en cuanto el motor de audio de Spotify completa su arranque en frío (~2.7s en terminales modernos), incluso con la pantalla apagada o en ahorro de energía.

### ❓ ¿Por qué se necesita Shizuku y no se puede integrar dentro de SpotiGuard?

1. **El Sandbox de Seguridad de Linux / Android (Aislamiento de UID)**:
   - Toda aplicación que instalas en Android corre dentro de una "cárcel" o sandbox con un usuario Linux propio (por ejemplo `u0_a245`).
   - Por diseño estricto del kernel de Linux y de SELinux, un usuario normal **jamás puede matar o detener procesos pertenecientes a otro usuario** (como Spotify, que corre bajo `u0_a180`).
   - Da igual el código que escribamos o las librerías que compilemos dentro de SpotiGuard: cualquier proceso que SpotiGuard inicie por sí mismo heredará su mismo nivel de privilegios restringido (`u0_a245`).

2. **¿De dónde sale el poder de matar procesos sin Root?**:
   - Proviene del usuario del sistema **`shell` (UID 2000)**, que es el usuario con el que opera el puente **ADB**.
   - El sistema operativo Android **solo permite ejecutar `am force-stop` a Root (UID 0) o a Shell (UID 2000)**.
   - Ninguna app de usuario puede auto-concederse UID 2000; ese proceso tiene que ser iniciado **desde fuera de Android** (por cable USB con un PC o mediante la depuración inalámbrica).

3. **Shizuku como "Driver de Sistema" Invisible**:
   - Shizuku no es una app común: es un servidor puente que se ejecuta con **UID 2000** y permite a SpotiGuard pedirle: *"Por favor, mata el proceso de Spotify ahora mismo"*.
   - Si intentáramos programar nuestro propio servidor en SpotiGuard, **seguirías teniendo que conectarlo por cable USB al PC para arrancarlo con comandos ADB exactamente igual**, pero con la desventaja de que las políticas de SELinux de Samsung One UI / Xiaomi HyperOS bloquearían un servidor casero.
   - Con **`instalar_android.bat`**, Shizuku se instala y arranca en 1 segundo de forma totalmente automática y desatendida, actuando como un "driver" en segundo plano que no necesitas volver a abrir.

### 4. Detección Dinámica de Requisitos en la App:
* **Estado de Emisión de Spotify**: En cuanto SpotiGuard detecta el primer evento procedente de Spotify, el botón **1** desaparece de la pantalla.
* **Batería sin Restricciones**: Si SpotiGuard está excluido del ahorro de energía del sistema, el botón **2** desaparece de la pantalla.
* **Cierre Silencioso (Shizuku)**: Concede autorización a SpotiGuard con 1 toque desde la app. Al concederse, el botón **3** desaparece.
* **Distintivo de Éxito**: Cuando todos los requisitos están cumplidos, se muestra una tarjeta verde de confirmación: *"✅ Requisitos listos: Emisión, Batería y Shizuku verificados"*.

---

## 📲 Descarga e Instalación del APK Oficial

- **Descarga Directa de SpotiGuard**: **[`SpotiGuard-1.0.8-20.apk`](../SpotiGuard-1.0.8-20.apk)** *(4.53 MB — Release v1.0.8-20)*
- **Descarga de Shizuku Oficial**: **[`shizuku.apk`](../shizuku.apk)** *(Oficial v13.6.0)*
- **Firma Oficial de Producción**: Firmado con Keystore Release propio con esquemas **v2 y v3** activos para compatibilidad total con Android 8.0 hasta Android 16.

---

## 🚀 Instalación y Activación Rápida

### Opción 1: Instalación Completa en 1 Clic desde el PC (Recomendada)
1. Conecta tu teléfono al ordenador con cable USB y comprueba que esté activa la **Depuración por USB** (en Ajustes -> Opciones de desarrollador).
2. Haz doble clic en el archivo **`instalar_android.bat`** en la carpeta del proyecto.
3. El script automáticamente:
   - Detectará tu dispositivo.
   - Instalará **SpotiGuard** (`SpotiGuard-1.0.8-20.apk`).
   - Instalará **Shizuku** (`shizuku.apk`) si no la tienes.
   - Iniciará el servicio de Shizuku en tu móvil mediante ADB.
4. Abre **SpotiGuard** en tu móvil y pulsa el botón **"Conceder Permiso Shizuku"** (o "Autorizar" en el diálogo emergente).
5. ¡Listo! Spotify se cerrará y reabrirá silenciosamente en cada anuncio.

### Opción 2: Si ya tienes SpotiGuard instalado y solo necesitas activar Shizuku
1. Conecta el teléfono por USB con depuración activada.
2. Haz doble clic en **`activar_shizuku.bat`**.
3. El servicio Shizuku quedará activo al instante.

### Opción 3: Activación 100% Inalámbrica en el Móvil (Sin Ningún PC — Recomendada para la calle)
Esta es la opción para usar SpotiGuard sin depender de ningún ordenador ni cable:
1. Conéctate a tu red Wi-Fi en tu teléfono.
2. Ve a **Ajustes ⚙️ -> Opciones de desarrollador**.
3. Activa el interruptor **Depuración inalámbrica**.
4. Pulsa sobre el texto *"Depuración inalámbrica"* y entra en **"Vincular dispositivo con código de vinculación"**.
5. Abre la notificación emergente de **Shizuku** (o pon la pantalla dividida) e introduce el código de 6 dígitos.
6. En la app **Shizuku**, pulsa en **"Iniciar"**.
¡Listo! Shizuku se ejecutará de forma totalmente autónoma en tu teléfono: puedes desconectarte de la Wi-Fi, salir a la calle con datos móviles o apagar el PC, que el servicio se mantendrá activo hasta que reinicies el teléfono.

---

## ⚙️ Configuración Inicial en SpotiGuard:

1. Abre **SpotiGuard**.
2. Si aparece el botón de batería, púlsalo y pulsa "Permitir" (el botón se ocultará automáticamente).
3. Si aparece el botón de Spotify, púlsalo para verificar que en Spotify -> Ajustes ⚙️ -> Reproducción esté activado *"Estado de transmisión del dispositivo"*. En cuanto pongas una canción, desaparecerá solo.
4. Pulsa el botón de Shizuku para conceder autorización al servicio (desaparecerá de inmediato).
5. Cuando veas el recuadro verde *"✅ Requisitos listos: Emisión, Batería y Shizuku verificados"*, el sistema estará completamente operativo en segundo plano.

---

## 🛡️ Compatibilidad con Samsung One UI 6 / 6.1 (Bloqueador Automático / Auto Blocker)

> [!WARNING]
> **Aviso Crítico para Dispositivos Samsung Galaxy (One UI 6.0+)**:
> Si tienes activado el **Bloqueador Automático** (*Ajustes > Seguridad y privacidad > Bloqueador automático*), Samsung Knox bloquea activamente:
> 1. La **Depuración inalámbrica** (queda desactivada o inaccesible).
> 2. La ejecución de comandos por cable USB (**ADB**).
> 3. La persistencia de cualquier proceso con privilegios de sistema (`UID 2000`), terminando automáticamente el servidor de **Shizuku** a los pocos minutos o al desconectar el cable.
>
> **¿Qué implica esto para SpotiGuard?**
> En Android, una aplicación estándar no puede cerrar forzosamente el proceso de otra app en reproducción sin los privilegios de Shell que proporciona Shizuku. Por tanto:
> - Si deseas utilizar SpotiGuard en tu Samsung: debes **desactivar el Bloqueador Automático** (*Ajustes > Seguridad y privacidad > Bloqueador automático -> Desactivado*).
> - Si por políticas de seguridad necesitas mantener el Bloqueador Automático de Samsung activado al 100%: consulta la sección de **Alternativas Recomendadas** a continuación.

---

## 🌟 Alternativas Recomendadas con Bloqueador Automático Activo

Si requieres mantener el **Bloqueador Automático de Samsung** encendido de forma ininterrumpida y no puedes usar Shizuku/ADB:

1. **Metrolist (Recomendada y Verificada — Todo el catálogo libre sin errores 401)**:
   - Cliente Open Source nativo para Android ([GitHub Oficial](https://github.com/MetrolistGroup/Metrolist)).
   - **Descarga Local Incluida**: **[`Metrolist.apk`](../Metrolist.apk)** *(25.6 MB — Versión v13.7.0 Oficial)*.
   - **Instalador en 1 Clic**: **[`instalar_metrolist.bat`](../instalar_metrolist.bat)** *(Instalación desatendida vía ADB)*.
   - **Cero errores 401**: No depende de las APIs bloqueadas de Spotify ni requiere inicios de sesión obligatorios.
   - **Importación de Playlists de Spotify**: Puedes pegar cualquier enlace de playlist de Spotify para importarla y escucharla de inmediato.
   - **Cero publicidad**, letras sincronizadas, descargas en memoria para la calle y reproducción en segundo plano.
   - 100% compatible con el Bloqueador Automático de Samsung Knox.

2. **Spotube (Estado: Inestable en v5.1.2)**:
   - Cliente Open Source ([GitHub](https://github.com/KRTirtho/spotube)).
   - **Aviso de Diagnóstico**: En las versiones v5+, Spotify bloquea el plugin de metadatos con `DioException 401 (Unauthorized)`, y el motor de YouTube sufre fallos de reproducción por cambios en las firmas de YouTube y bloqueos de `START_FOREGROUND` en Android 14. Se incluye instalador y APK como respaldo.

3. **Spotify Premium (Familiar / Compartido)**:
   - La vía oficial más directa mediante suscripciones compartidas (~2,50 €/mes).
