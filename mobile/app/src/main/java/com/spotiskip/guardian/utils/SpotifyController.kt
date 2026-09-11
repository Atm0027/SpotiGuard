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

    fun tryRootForceStop(): Boolean {
        return try {
            val cmd = "am force-stop $SPOTIFY_PACKAGE && am force-stop $SPOTIFY_LITE_PACKAGE"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Log.i(TAG, "Spotify detenido exitosamente vía Root.")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

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

    fun tryShizukuForceStop(context: Context): Boolean {
        return try {
            if (!rikka.shizuku.Shizuku.pingBinder()) {
                Log.d(TAG, "Shizuku no está en ejecución.")
                return false
            }
            if (rikka.shizuku.Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permiso de Shizuku no concedido.")
                return false
            }

            val pkg = if (isSpotifyInstalled(context)) SPOTIFY_PACKAGE else SPOTIFY_LITE_PACKAGE
            val cmd = arrayOf("am", "force-stop", pkg)

            val method = rikka.shizuku.Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, cmd, null, null) as Process
            val exitCode = process.waitFor()
            Log.i(TAG, "Spotify cerrado limpiamente mediante Shizuku (exitCode: $exitCode).")
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error en tryShizukuForceStop: ${e.message}", e)
            false
        }
    }

    /**
     * Maniobra Kill & Relaunch pura (Cierre real a nivel de sistema idéntico a PC):
     * 1. Pausa y detiene Spotify.
     * 2. Fuerza el cierre de Spotify inmediatamente mediante Shizuku o Root (100% silencioso).
     * 3. Vuelve a abrir Spotify en pantalla limpia.
     * 4. Avanza el búfer con Next y pulsa Play para reanudar la música.
     */
    fun restartAndResume(context: Context, onComplete: (() -> Unit)? = null) {
        Log.i(TAG, "Iniciando cierre forzoso, reapertura y play de Spotify...")
        sendMediaStop(context)

        val mainHandler = Handler(Looper.getMainLooper())

        // 1. Si hay Root, forzar detención directa por comando (100% silencioso)
        if (tryRootForceStop()) {
            mainHandler.postDelayed({
                relaunchAndResumePlayback(context, onComplete)
            }, 350)
            return
        }

        // 2. Si hay Shizuku activo, forzar detención directa a nivel de sistema (100% silencioso sin pantallas)
        if (tryShizukuForceStop(context)) {
            mainHandler.postDelayed({
                relaunchAndResumePlayback(context, onComplete)
            }, 350)
            return
        }

        // 3. Fallback limpio: Relanzar inmediatamente con Next y Play
        killSpotify(context)
        mainHandler.postDelayed({
            relaunchAndResumePlayback(context, onComplete)
        }, 400)
    }

    private fun relaunchAndResumePlayback(context: Context, onComplete: (() -> Unit)? = null) {
        val mainHandler = Handler(Looper.getMainLooper())

        Log.i(TAG, "Relanzando Spotify...")
        relaunchSpotify(context)

        mainHandler.postDelayed({
            Log.i(TAG, "Enviando Media Next a Spotify...")
            sendMediaNext(context)
            mainHandler.postDelayed({
                Log.i(TAG, "Enviando Media Play a Spotify...")
                sendMediaPlay(context)
                onComplete?.invoke()
            }, 350)
        }, 1200)
    }
}
