# SpotiGuard Mobile (Android)

Aplicación nativa de Android que detecta en tiempo real los anuncios de Spotify y los neutraliza de forma automática mediante la técnica de **Reinicio Rápido y Salto Automático (Kill -> Relaunch -> Purge Buffer -> Play)**.

---

## 📱 ¿Cómo Funciona en el Móvil? (Arquitectura v1.0.2-12)

1. **Servicio en Primer Plano Estándar (`SpotiGuardService`)**:
   - Funciona a través de un servicio en primer plano oficial de Android (`mediaPlayback`).
   - Mantiene una notificación de estado silenciosa y elegante, garantizando que el sistema operativo nunca congele la app en segundo plano.
2. **Escucha de Emisiones Nativas de Spotify (`BroadcastReceiver`)**:
   - Cuando activas **"Estado de transmisión del dispositivo"** en Spotify, la app oficial de Spotify envía automáticamente eventos del sistema:
     - `com.spotify.music.metadatachanged`
     - `com.spotify.music.playbackstatechanged`
     - `com.spotify.music.queuechanged`
   - SpotiGuard intercepta estos eventos al instante, analizando los identificadores de pista (`id`, `track`, `artist`, `album`).
3. **Nueva Heurística Universal Anti-Anuncios**:
   - En las versiones recientes de Spotify Free, los anuncios comerciales de marcas patrocinadoras (por ejemplo, Vinted, Amazon, Coca-Cola) completan el campo `artist` con el nombre del patrocinador y el campo `album` con el nombre de la campaña.
   - La heurística anterior que comprobaba si `artist` o `album` estaban vacíos dejaba pasar estos anuncios comerciales.
   - **Solución implementada**: SpotiGuard analiza el formato canónico del URI. En Spotify, solo las pistas reales de música (`spotify:track:`) y podcasts (`spotify:episode:`) tienen este formato de ID. Cualquier emisión que carezca de este prefijo se clasifica con 100% de precisión como anuncio o contenido publicitario.
4. **Mecanismo de Salto y Reanudación (Sin Silenciamiento)**:
   - Se ha **eliminado completamente la opción de silenciar (*mute*)**.
   - SpotiGuard envía un corte inmediato de audio (`KEYCODE_MEDIA_STOP`), liquida el proceso en segundo plano de Spotify (`killBackgroundProcesses`), espera 500 ms y relanza Spotify en primer plano.
   - Tras 1000 ms envía `KEYCODE_MEDIA_NEXT` para purgar el anuncio congelado en el búfer del reproductor, y a continuación envía `KEYCODE_MEDIA_PLAY` para que tu música continúe sin interrupciones.

---

## 📲 Descarga e Instalación del APK Oficial

- **Descarga Directa del APK**: **[Descargar SpotiGuard-1.0.2-12.apk (Release v1.0.2-12)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.2-12/SpotiGuard-1.0.2-12.apk)** *(4.51 MB)*
- **Firma Oficial de Producción**: Firmado con Keystore Release propio con esquemas **v2 y v3** activos para máxima compatibilidad con Android 8.0 hasta Android 15+.
- **Cero Permisos Sensibles (Solución Definitiva de Bloqueo)**: Se ha **eliminado por completo `BIND_NOTIFICATION_LISTENER_SERVICE` y `BIND_ACCESSIBILITY_SERVICE`**. Al no solicitar ningún permiso de interceptación de notificaciones ni accesibilidad, **Google Play Protect ya no bloquea la instalación**.

### 🛡️ Opciones de Instalación en Android:

#### Opción 1: Instalación Normal en el Móvil (Recomendada)
1. Descarga el APK en tu teléfono.
2. Abre la app **Mis Archivos / Files / Gestor de archivos** de tu teléfono, ve a la carpeta **Descargas (Downloads)** y pulsa el archivo `SpotiGuard-1.0.2-12.apk`.
3. Pulsa **Instalar**. *(Si aparece aviso de origen desconocido, concede permiso a tu app de Archivos).*

#### Opción 2: Instalación Directa en 1 Clic desde el PC (Script ADB)
1. Conecta tu teléfono al ordenador con cable USB y asegúrate de tener activada la **Depuración por USB**.
2. En la carpeta del proyecto en tu PC, haz doble clic en **`instalar_android.bat`**.
3. El script detectará tu dispositivo e instalará el APK directamente en el móvil, omitiendo cualquier pantalla de confirmación.

---

## ⚙️ Configuración Inicial en 2 Pasos (Solo la primera vez)

Una vez instalada la app en tu móvil:
1. **Activar transmisión en Spotify**:
   - Abre la app oficial de **Spotify**.
   - Entra en **Ajustes** (icono de rueda dentada ⚙️ arriba a la derecha).
   - Ve a la pestaña **«Reproducción»** (o el apartado **«Dispositivos»** según tu versión).
   - Activa el interruptor: **«Estado de transmisión del dispositivo»** (o *«Estado de emisión del dispositivo»* / *«Device Broadcast Status»*).
     *(Debajo pone: "Permite que otras apps de este dispositivo vean lo que estás escuchando")*.
2. **Iniciar SpotiGuard**:
   - Abre **SpotiGuard**.
   - Pulsa el botón: **"🛡️ Activar Protección SpotiGuard"** (o pulsa "🚀 Abrir Spotify").
   - Opcional: Pulsa en **"Desactivar Optimización de Batería"** y marca "Sin restricciones".
3. ¡Listo! Cada vez que comience un anuncio en Spotify, SpotiGuard lo interceptará, cerrará Spotify, lo abrirá y reanudará tu música saltándose el anuncio.
