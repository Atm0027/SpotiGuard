package com.spotiskip.guardian.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import com.spotiskip.guardian.services.SpotiGuardAccessibilityService

object SpotifyController {
    private const val TAG = "SpotifyController"

    const val SPOTIFY_PACKAGE = "com.spotify.music"
    const val SPOTIFY_LITE_PACKAGE = "com.spotify.lite"

    fun getSpotifyLaunchIntent(context: Context): Intent? {
        val pm = context.packageManager

        // 1. Intent oficial del paquete Spotify estándar
        pm.getLaunchIntentForPackage(SPOTIFY_PACKAGE)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            return it
        }

        // 2. Intent de Spotify Lite
        pm.getLaunchIntentForPackage(SPOTIFY_LITE_PACKAGE)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            return it
        }

        // 3. Lanzamiento mediante esquema oficial 'spotify:'
        val uriIntent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:home")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (uriIntent.resolveActivity(pm) != null) {
            return uriIntent
        }

        val uriGeneric = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (uriGeneric.resolveActivity(pm) != null) {
            return uriGeneric
        }

        return null
    }

    fun isSpotifyInstalled(context: Context): Boolean {
        return getSpotifyLaunchIntent(context) != null
    }

    fun relaunchSpotify(context: Context): Boolean {
        return try {
            val intent = getSpotifyLaunchIntent(context)
            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo Spotify: ${e.message}", e)
            false
        }
    }

    fun openSpotifyAppDetails(context: Context) {
        try {
            val pkg = if (isSpotifyInstalled(context)) SPOTIFY_PACKAGE else SPOTIFY_LITE_PACKAGE
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$pkg")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo detalles de Spotify: ${e.message}", e)
        }
    }

    /**
     * Intenta forzar detención directa por Root (si el terminal tiene permisos superusuario).
     */
    fun tryRootForceStop(): Boolean {
        return try {
            val cmd = "am force-stop $SPOTIFY_PACKAGE && am force-stop $SPOTIFY_LITE_PACKAGE"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Log.i(TAG, "Spotify detenido exitosamente vía Root (su am force-stop).")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * killBackgroundProcesses estándar (funciona en Android <= 13 y procesos compartidos).
     */
    fun killSpotify(context: Context) {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            am?.killBackgroundProcesses(SPOTIFY_PACKAGE)
            am?.killBackgroundProcesses(SPOTIFY_LITE_PACKAGE)
        } catch (e: Exception) {
            Log.e(TAG, "Error en killBackgroundProcesses: ${e.message}", e)
        }
    }

    fun sendMediaKey(context: Context, keyCode: Int) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))

            for (pkg in listOf(SPOTIFY_PACKAGE, SPOTIFY_LITE_PACKAGE)) {
                val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                    `package` = pkg
                    putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                }
                context.sendBroadcast(downIntent)

                val upIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                    `package` = pkg
                    putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_UP, keyCode))
                }
                context.sendBroadcast(upIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando media key $keyCode: ${e.message}", e)
        }
    }

    fun sendMediaStop(context: Context) {
        sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PAUSE)
        sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_STOP)
    }

    fun sendMediaNext(context: Context) {
        sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    fun sendMediaPlay(context: Context) {
        sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY)
    }

    /**
     * Maniobra Infalible de Salto de Anuncio:
     * 1. Silencia el audio instantáneamente (0ms) para que el anuncio nunca se escuche.
     * 2. Envía stop a la reproducción multimedia.
     * 3. Ejecuta cierre forzoso (Root si disponible, o Servicio de Accesibilidad automatizado).
     * 4. Relanza Spotify limpio.
     * 5. Purgar buffer (Next) y reanudar reproducción (Play).
     * 6. Restaura el volumen exacto original.
     */
    fun restartAndResume(context: Context, onComplete: (() -> Unit)? = null) {
        Log.i(TAG, "Iniciando maniobra de cierre forzoso y reinicio para saltar anuncio...")

        // 1. Silenciar inmediatamente
        AudioController.mute(context)
        sendMediaStop(context)

        val mainHandler = Handler(Looper.getMainLooper())

        // 2. Intentar Root
        if (tryRootForceStop()) {
            mainHandler.postDelayed({
                relaunchAndResumePlayback(context, onComplete)
            }, 600)
            return
        }

        // 3. Si el Servicio de Accesibilidad está activo, usarlo para forzar la detención
        if (SpotiGuardAccessibilityService.isServiceRunning()) {
            Log.i(TAG, "Solicitando forzado de detención mediante Servicio de Accesibilidad...")
            SpotiGuardAccessibilityService.requestForceStop(context) {
                mainHandler.postDelayed({
                    relaunchAndResumePlayback(context, onComplete)
                }, 400)
            }
            return
        }

        // 4. Fallback si no hay Root ni Accesibilidad
        Log.w(TAG, "Accesibilidad ni Root activos. Ejecutando killBackgroundProcesses + relanzamiento con audio silenciado.")
        killSpotify(context)
        mainHandler.postDelayed({
            relaunchAndResumePlayback(context, onComplete)
        }, 700)
    }

    private fun relaunchAndResumePlayback(context: Context, onComplete: (() -> Unit)? = null) {
        val mainHandler = Handler(Looper.getMainLooper())

        relaunchSpotify(context)

        mainHandler.postDelayed({
            sendMediaNext(context)
            mainHandler.postDelayed({
                sendMediaPlay(context)
                mainHandler.postDelayed({
                    AudioController.unmute(context)
                    onComplete?.invoke()
                }, 400)
            }, 400)
        }, 1200)
    }
}
