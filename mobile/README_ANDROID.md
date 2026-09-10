# SpotiGuard Mobile (Android)

Aplicación nativa de Android que detecta en tiempo real los anuncios de Spotify y los neutraliza de forma automática mediante **Silenciador Inteligente (Auto-Mute)** o **Reinicio Rápido (Skip & Relaunch)**.

---

## 📱 ¿Cómo Funciona en el Móvil? (Nueva Arquitectura v1.0.2-8)

1. **Servicio en Primer Plano Estándar (`SpotiGuardService`)**:
   - Funciona a través de un servicio en primer plano oficial de Android (`mediaPlayback`).
   - Mantiene una notificación de estado silenciosa y elegante, garantizando que el sistema operativo nunca congele la app en segundo plano.
2. **Escucha de Emisiones Nativas de Spotify (`BroadcastReceiver`)**:
   - Cuando activas **"Estado de emisión del dispositivo"** en Spotify, la app oficial de Spotify envía automáticamente eventos del sistema:
     - `com.spotify.music.metadatachanged`
     - `com.spotify.music.playbackstatechanged`
     - `com.spotify.music.queuechanged`
   - SpotiGuard intercepta estos eventos al instante, analizando los identificadores de pista (`id`, `track`, `artist`, `album`).
3. **Mecanismos de Neutralización**:
   - **Modo Silenciador Inteligente (Recomendado)**: Silencia el flujo multimedia (`STREAM_MUSIC`) a 0 en el milisegundo en que entra el anuncio publicitario y restaura el volumen exacto al comenzar la siguiente canción, sin ninguna interrupción visual ni parpadeos.
   - **Modo Reinicio Rápido**: Cierra el proceso de Spotify y relanza la aplicación enviando el evento multimedia de reanudación para saltar a la siguiente canción.

---

## 📲 Descarga e Instalación del APK Oficial

- **Descarga Directa del APK**: **[Descargar SpotiGuard-1.0.2-8.apk (Release v1.0.2-8)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.2-8/SpotiGuard-1.0.2-8.apk)** *(4.51 MB)*
- **Firma Oficial de Producción**: Firmado con Keystore Release propio con esquemas **v2 y v3** activos para máxima compatibilidad con Android 8.0 hasta Android 15+.
- **Cero Permisos Sensibles (Solución Definitiva de Bloqueo)**: Se ha **eliminado por completo `BIND_NOTIFICATION_LISTENER_SERVICE` y `BIND_ACCESSIBILITY_SERVICE`**. Al no solicitar ningún permiso de interceptación de notificaciones ni accesibilidad, **Google Play Protect ya no bloquea la instalación**.

### 🛡️ Opciones de Instalación en Android:

#### Opción 1: Instalación Normal en el Móvil (Recomendada)
1. Descarga el APK en tu teléfono.
2. Abre la app **Mis Archivos / Files / Gestor de archivos** de tu teléfono, ve a la carpeta **Descargas (Downloads)** y pulsa el archivo `SpotiGuard-1.0.2-8.apk`.
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
   - Pulsa el botón grande: **"🛡️ Activar Protección SpotiGuard"**.
   - Opcional: Pulsa en **"Desactivar Optimización de Batería"** y marca "Sin restricciones".
3. ¡Listo! Cada vez que comience un anuncio en Spotify, SpotiGuard lo detectará y lo silenciará automáticamente en segundo plano.
