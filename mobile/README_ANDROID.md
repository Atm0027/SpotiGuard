# SpotiSkip Mobile Guardian (Android)

Aplicación nativa de Android que detecta en tiempo real los anuncios de Spotify y los neutraliza de forma automática mediante **Reinicio Rápido (Skip & Relaunch)** o **Silenciador Furtivo (Auto-Mute)**.

---

## 📱 ¿Cómo Funciona en el Móvil?

1. **Detección Automática 24/7**:
   - Utiliza `NotificationListenerService`, un servicio oficial del sistema Android que el sistema operativo mantiene activo de forma desatendida.
   - En cuanto Spotify empieza a reproducir música o un anuncio, Android envía los metadatos de la notificación (`EXTRA_TITLE` y `EXTRA_TEXT`) a SpotiSkip.
2. **Identificación de Anuncios**:
   - Detecta palabras clave publicitarias (`Advertisement`, `Publicidad`, etc.) o títulos que no contienen artista mientras la pista se reproduce.
3. **Mecanismos de Neutralización**:
   - **Modo Silenciador Inteligente (Recomendado para Móvil)**: Silencia el flujo multimedia (`STREAM_MUSIC`) a 0 al milisegundo en que entra el anuncio publicitario y restaura el volumen exacto original al comenzar la siguiente canción, sin ninguna interrupción visual.
   - **Modo Reinicio Rápido**: Cierra el proceso en segundo plano de Spotify (`killBackgroundProcesses`) y relanza la aplicación enviando el evento multimedia `KEYCODE_MEDIA_PLAY_PAUSE` para saltar a la siguiente canción.

---

## 🛠️ Cómo Compilar e Instalar el APK

### Opción 1: Automático en la Nube con GitHub (Sin instalar nada en tu PC)
1. Sube este repositorio a tu cuenta de **GitHub**.
2. GitHub ejecutará automáticamente el workflow [`.github/workflows/build-apk.yml`](../.github/workflows/build-apk.yml).
3. Entra en la pestaña **Actions -> Compilar APK de SpotiSkip Android -> Artifacts**.
4. Descarga el archivo **`SpotiSkip-Mobile-APK`**, que contiene el archivo `.apk` compilado listo para instalar en tu teléfono móvil.

### Opción 2: Con Android Studio (Recomendado para desarrollo local)
1. Abre **Android Studio**.
2. Selecciona **File -> Open...** y elige la carpeta `mobile/`.
3. Conecta tu teléfono móvil por cable USB (con **Depuración por USB** activada en Opciones de Desarrollador) o inicia un emulador.
4. Pulsa en el botón verde **Run (▶️)** para instalar y abrir la app directamente en tu dispositivo.
5. Para generar el archivo APK instalable independiente:
   - Ve a **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.
   - El archivo se generará en `mobile/app/build/outputs/apk/debug/app-debug.apk`.

### Opción 3: Compilación por Consola (Gradle / ADB)
Si tienes el SDK de Android configurado en tus variables de entorno:
```bash
cd mobile
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ Configuración Inicial en el Teléfono

Una vez instalada la app en tu móvil:
1. Abre **SpotiSkip Guardian**.
2. Pulsa en **"1. Activar Acceso a Notificaciones"**:
   - Te llevará a los ajustes del sistema de Android.
   - Busca **SpotiSkip Guardian** en la lista y activa la casilla para permitir el acceso.
3. *(Opcional)* Pulsa en **"2. Activar Servicio de Accesibilidad"** si deseas usar el modo de reinicio asistido en pantalla.
4. Desactiva la optimización agresiva de batería para SpotiSkip:
   - En Ajustes del teléfono -> Batería -> Optimización de batería -> SpotiSkip -> **"Sin restricciones"** (para evitar que Android cierre el monitor al apagar la pantalla).
5. ¡Listo! Abre Spotify y pon música. Cada vez que entre un anuncio publicitario, SpotiSkip lo detectará y actuará automáticamente.
