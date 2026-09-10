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

- **Descarga Directa del APK**: **[Descargar SpotiGuard-1.0.1-5.apk (Release v1.0.1-5)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.1-5/SpotiGuard-1.0.1-5.apk)**
- **Firma Oficial de Producción**: Firmado con Keystore Release propio con esquemas **v1 (JAR), v2 y v3** activos para máxima compatibilidad con Android 8.0 hasta Android 14+.
- **Permisos 100% Seguros y Limpios**: Se ha eliminado cualquier servicio de accesibilidad (`canRetrieveWindowContent`) para no activar la heurística de seguridad de Play Protect (*"Aplicación bloqueada para proteger tu dispositivo"*).

### 🛡️ Pasos de Instalación en Android:

1. **Aviso de Google Play Protect ("Desarrollador desconocido")**:
   - Al ser una aplicación descargada fuera de la tienda oficial, Play Protect puede pedir confirmación.
   - Pulsa en **"Más detalles"** (o la pequeña flecha desplegable).
   - Pulsa en **"Instalar de todas formas"**.
2. **Permiso de Fuentes Desconocidas**:
   - Si tu navegador (Chrome) o gestor de archivos solicita permisos:
   - Ve a **Ajustes -> Aplicaciones -> Acceso especial -> Instalar aplicaciones desconocidas** -> Activa el permiso para tu navegador o explorador de archivos.
3. **Bloqueador automático en Samsung (OneUI 6 / Android 14)**:
   - Si utilizas Samsung con Knox y el bloqueador automático activo:
   - Ve a **Ajustes -> Seguridad y privacidad -> Bloqueador automático** y desactívalo temporalmente para permitir la instalación.

---

## ⚙️ Configuración Inicial en el Teléfono

Una vez instalada la app en tu móvil:
1. Abre **SpotiGuard**.
2. Pulsa en **"1. Activar Acceso a Notificaciones"**:
   - Te llevará a los ajustes del sistema de Android.
   - Busca **SpotiGuard** en la lista y activa la casilla para permitir el acceso.
3. Pulsa en **"2. Desactivar Optimización de Batería"**:
   - Selecciona **"Sin restricciones"** para SpotiGuard (evita que Android congele el monitor al apagar la pantalla).
4. ¡Listo! Abre Spotify y pon música. Cada vez que entre un anuncio publicitario, SpotiGuard lo silenciará automáticamente.
