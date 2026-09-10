# SpotiGuard Mobile (Android)

AplicaciÃ³n nativa de Android que detecta en tiempo real los anuncios de Spotify y los neutraliza de forma automÃ¡tica mediante **Silenciador Inteligente (Auto-Mute)** o **Reinicio RÃ¡pido (Skip & Relaunch)**.

---

## ðŸ“± Â¿CÃ³mo Funciona en el MÃ³vil? (Nueva Arquitectura v1.0.2-11)

1. **Servicio en Primer Plano EstÃ¡ndar (`SpotiGuardService`)**:
   - Funciona a travÃ©s de un servicio en primer plano oficial de Android (`mediaPlayback`).
   - Mantiene una notificaciÃ³n de estado silenciosa y elegante, garantizando que el sistema operativo nunca congele la app en segundo plano.
2. **Escucha de Emisiones Nativas de Spotify (`BroadcastReceiver`)**:
   - Cuando activas **"Estado de emisiÃ³n del dispositivo"** en Spotify, la app oficial de Spotify envÃ­a automÃ¡ticamente eventos del sistema:
     - `com.spotify.music.metadatachanged`
     - `com.spotify.music.playbackstatechanged`
     - `com.spotify.music.queuechanged`
   - SpotiGuard intercepta estos eventos al instante, analizando los identificadores de pista (`id`, `track`, `artist`, `album`).
3. **Mecanismos de NeutralizaciÃ³n**:
   - **Modo Silenciador Inteligente (Recomendado)**: Silencia el flujo multimedia (`STREAM_MUSIC`) a 0 en el milisegundo en que entra el anuncio publicitario y restaura el volumen exacto al comenzar la siguiente canciÃ³n, sin ninguna interrupciÃ³n visual ni parpadeos.
   - **Modo Reinicio RÃ¡pido**: Cierra el proceso de Spotify y relanza la aplicaciÃ³n enviando el evento multimedia de reanudaciÃ³n para saltar a la siguiente canciÃ³n.

---

## ðŸ“² Descarga e InstalaciÃ³n del APK Oficial

- **Descarga Directa del APK**: **[Descargar SpotiGuard-1.0.2-11.apk (Release v1.0.2-11)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.2-11/SpotiGuard-1.0.2-11.apk)** *(4.51 MB)*
- **Firma Oficial de ProducciÃ³n**: Firmado con Keystore Release propio con esquemas **v2 y v3** activos para mÃ¡xima compatibilidad con Android 8.0 hasta Android 15+.
- **Cero Permisos Sensibles (SoluciÃ³n Definitiva de Bloqueo)**: Se ha **eliminado por completo `BIND_NOTIFICATION_LISTENER_SERVICE` y `BIND_ACCESSIBILITY_SERVICE`**. Al no solicitar ningÃºn permiso de interceptaciÃ³n de notificaciones ni accesibilidad, **Google Play Protect ya no bloquea la instalaciÃ³n**.

### ðŸ›¡ï¸ Opciones de InstalaciÃ³n en Android:

#### OpciÃ³n 1: InstalaciÃ³n Normal en el MÃ³vil (Recomendada)
1. Descarga el APK en tu telÃ©fono.
2. Abre la app **Mis Archivos / Files / Gestor de archivos** de tu telÃ©fono, ve a la carpeta **Descargas (Downloads)** y pulsa el archivo `SpotiGuard-1.0.2-11.apk`.
3. Pulsa **Instalar**. *(Si aparece aviso de origen desconocido, concede permiso a tu app de Archivos).*

#### OpciÃ³n 2: InstalaciÃ³n Directa en 1 Clic desde el PC (Script ADB)
1. Conecta tu telÃ©fono al ordenador con cable USB y asegÃºrate de tener activada la **DepuraciÃ³n por USB**.
2. En la carpeta del proyecto en tu PC, haz doble clic en **`instalar_android.bat`**.
3. El script detectarÃ¡ tu dispositivo e instalarÃ¡ el APK directamente en el mÃ³vil, omitiendo cualquier pantalla de confirmaciÃ³n.

---

## âš™ï¸ ConfiguraciÃ³n Inicial en 2 Pasos (Solo la primera vez)

Una vez instalada la app en tu mÃ³vil:
1. **Activar transmisiÃ³n en Spotify**:
   - Abre la app oficial de **Spotify**.
   - Entra en **Ajustes** (icono de rueda dentada âš™ï¸ arriba a la derecha).
   - Ve a la pestaÃ±a **Â«ReproducciÃ³nÂ»** (o el apartado **Â«DispositivosÂ»** segÃºn tu versiÃ³n).
   - Activa el interruptor: **Â«Estado de transmisiÃ³n del dispositivoÂ»** (o *Â«Estado de emisiÃ³n del dispositivoÂ»* / *Â«Device Broadcast StatusÂ»*).
     *(Debajo pone: "Permite que otras apps de este dispositivo vean lo que estÃ¡s escuchando")*.
2. **Iniciar SpotiGuard**:
   - Abre **SpotiGuard**.
   - Pulsa el botÃ³n grande: **"ðŸ›¡ï¸ Activar ProtecciÃ³n SpotiGuard"**.
   - Opcional: Pulsa en **"Desactivar OptimizaciÃ³n de BaterÃ­a"** y marca "Sin restricciones".
3. Â¡Listo! Cada vez que comience un anuncio en Spotify, SpotiGuard lo detectarÃ¡ y lo silenciarÃ¡ automÃ¡ticamente en segundo plano.
