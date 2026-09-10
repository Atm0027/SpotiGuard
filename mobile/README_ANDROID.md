# SpotiGuard Mobile (Android)

Aplicación nativa de Android que detecta en tiempo real los anuncios de Spotify y los neutraliza de forma automática mediante **Reinicio Rápido (Skip & Relaunch)** o **Silenciador Furtivo (Auto-Mute Inteligente)**.

---

## 📱 ¿Cómo Funciona en el Móvil?

1. **Detección Automática 24/7**:
   - Utiliza `NotificationListenerService`, un servicio oficial del sistema Android que el sistema operativo mantiene activo de forma desatendida.
   - En cuanto Spotify empieza a reproducir música o un anuncio, Android envía los metadatos de la notificación (`EXTRA_TITLE` y `EXTRA_TEXT`) a SpotiGuard.
2. **Identificación de Anuncios**:
   - Detecta palabras clave publicitarias (`Advertisement`, `Publicidad`, etc.) o títulos que no contienen artista mientras la pista se reproduce.
3. **Mecanismos de Neutralización**:
   - **Modo Silenciador Inteligente (Recomendado para Móvil)**: Silencia el flujo multimedia (`STREAM_MUSIC`) a 0 al milisegundo en que entra el anuncio publicitario y restaura el volumen exacto original al comenzar la siguiente canción, sin ninguna interrupción visual.
   - **Modo Reinicio Rápido**: Cierra el proceso de Spotify (`killBackgroundProcesses`) y relanza la aplicación enviando el evento multimedia `KEYCODE_MEDIA_PLAY_PAUSE` para saltar a la siguiente canción.

---

## 📲 Descarga e Instalación del APK Oficial

- **Descarga Directa del APK**: **[Descargar SpotiGuard-Android.apk (v1.0.0)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.0/SpotiGuard-Android.apk)**
- **Firma Oficial de Producción**: Firmado con Keystore Release propio con esquemas **v1 (JAR), v2 y v3** activos para máxima compatibilidad con Android 8.0 hasta Android 14+.

### ⚠️ Cómo resolver avisos de instalación en Android (Sideloading):

1. **Google Play Protect ("Bloqueado por Play Protect / Desarrollador desconocido")**:
   - Al instalar APKs fuera de la tienda oficial, Play Protect muestra un aviso.
   - Pulsa en **"Más detalles"** (o la pequeña flecha desplegable).
   - Pulsa en **"Instalar de todas formas"**.
2. **Permiso de Fuentes Desconocidas**:
   - Si tu navegador (Chrome) o gestor de archivos te dice que no tiene permiso:
   - Ve a **Ajustes -> Aplicaciones -> Acceso especial -> Instalar aplicaciones desconocidas** -> Activa el permiso para tu navegador o explorador de archivos.
3. **Bloqueador automático en Samsung (OneUI 6 / Android 14)**:
   - Si tienes un dispositivo Samsung con esta opción activada:
   - Ve a **Ajustes -> Seguridad y privacidad -> Bloqueador automático** y desactívalo temporalmente para permitir la instalación.

---

## ⚙️ Configuración Inicial en el Teléfono

Una vez instalada la app en tu móvil:
1. Abre **SpotiGuard**.
2. Pulsa en **"1. Activar Acceso a Notificaciones"**:
   - Te llevará a los ajustes del sistema de Android.
   - Busca **SpotiGuard** en la lista y activa la casilla para permitir el acceso.
3. *(Opcional)* Pulsa en **"2. Activar Servicio de Accesibilidad"** si deseas usar el modo de reinicio asistido en pantalla.
4. Desactiva la optimización agresiva de batería para SpotiGuard:
   - En Ajustes del teléfono -> Batería -> Optimización de batería -> SpotiGuard -> **"Sin restricciones"** (para evitar que Android cierre el monitor al apagar la pantalla).
5. ¡Listo! Abre Spotify y pon música. Cada vez que entre un anuncio publicitario, SpotiGuard lo detectará y actuará automáticamente.
