# SpotiGuard Mobile (Android)

Aplicación nativa de Android que detecta en tiempo real los anuncios de Spotify y los neutraliza de forma automática mediante la técnica de **Reinicio Rápido y Salto Automático (Kill -> Relaunch -> Purge Buffer -> Play)** con **Watchdog de fin de pista** y **detección dinámica de requisitos**.

---

## 📱 ¿Cómo Funciona en el Móvil? (Arquitectura v1.0.3-13)

### 1. El Secreto Revelado de los Anuncios en Android:
Spotify en Android **NO emite ningún evento de broadcast cuando empieza una cuña publicitaria** (`metadatachanged` se silencia deliberadamente durante los anuncios). Las herramientas convencionales que solo escuchan eventos se quedan esperando indefinidamente mientras suena el anuncio.

### 2. Watchdog de Expiración de Pista de SpotiGuard:
* Cuando una canción legítima empieza, SpotiGuard calcula su duración exacta (`length` en milisegundos) y posición de reproducción (`playbackPosition`).
* Arma un temporizador interno (*Watchdog*) sincronizado con el final de la pista (`remainingMs + 350ms`).
* Si al finalizar la canción Spotify no emite inmediatamente la siguiente canción real, SpotiGuard sabe matemáticamente que se ha iniciado un anuncio publicitario.
* De inmediato ejecuta el salto automático sin esperar:
  1. Detiene la reproducción (`KEYCODE_MEDIA_PAUSE` y `STOP`).
  2. Mata el proceso de Spotify en segundo plano (`killBackgroundProcesses`).
  3. Relanza Spotify en 500 ms.
  4. Purga la cuña comercial congelada en el búfer enviando `KEYCODE_MEDIA_NEXT` a los 1000 ms.
  5. Arranca la siguiente canción real con `KEYCODE_MEDIA_PLAY` a los 300 ms.

### 3. Detección Dinámica de Requisitos en la App:
* **Batería sin Restricciones**: La app consulta al sistema operativo si SpotiGuard está excluido de las restricciones de ahorro de energía. Si ya está concedido, el botón **desaparece automáticamente de la pantalla**.
* **Estado de Emisión de Spotify**: En cuanto SpotiGuard intercepta el primer evento procedente de Spotify, confirma la conexión y el botón **desaparece automáticamente de la pantalla**.
* **Distintivo de Éxito**: Cuando ambos requisitos están listos, se muestra una tarjeta verde de confirmación: *"✅ Requisitos configurados: Emisión de Spotify y Batería sin restricciones verificados"*.

---

## 📲 Descarga e Instalación del APK Oficial

- **Descarga Directa del APK**: **[Descargar SpotiGuard-1.0.3-13.apk (Release v1.0.3-13)](https://github.com/Atm0027/SpotiGuard/releases/download/v1.0.3-13/SpotiGuard-1.0.3-13.apk)** *(4.51 MB)*
- **Firma Oficial de Producción**: Firmado con Keystore Release propio con esquemas **v2 y v3** activos para máxima compatibilidad con Android 8.0 hasta Android 15+.
- **Cero Permisos Sensibles**: Sin permisos de accesibilidad ni interceptación de notificaciones. **100% libre de advertencias de Google Play Protect**.

### 🛡️ Opciones de Instalación en Android:

#### Opción 1: Instalación Normal en el Móvil (Recomendada)
1. Descarga el APK en tu teléfono.
2. Abre la app **Mis Archivos / Files / Gestor de archivos** de tu teléfono, ve a la carpeta **Descargas (Downloads)** y pulsa el archivo `SpotiGuard-1.0.3-13.apk`.
3. Pulsa **Instalar**. *(Si aparece aviso de origen desconocido, concede permiso a tu app de Archivos).*

#### Opción 2: Instalación Directa en 1 Clic desde el PC (Script ADB)
1. Conecta tu teléfono al ordenador con cable USB y asegúrate de tener activada la **Depuración por USB**.
2. En la carpeta del proyecto en tu PC, haz doble clic en **`instalar_android.bat`**.
3. El script detectará tu dispositivo e instalará el APK directamente en el móvil, omitiendo cualquier pantalla de confirmación.

---

## ⚙️ Configuración Inicial Rápida:

1. Abre **SpotiGuard**.
2. Si aparece el botón de batería, púlsalo y pulsa "Permitir" (el botón se quitará solo).
3. Si aparece el botón de Spotify, púlsalo para verificar que en Spotify -> Ajustes ⚙️ -> Reproducción esté activo *"Estado de transmisión del dispositivo"*. En cuanto suene música, el botón se quitará solo.
4. Cuando veas el mensaje verde *"✅ Requisitos configurados"*, ¡la protección está completa y nunca más tendrás que tocarlo!
