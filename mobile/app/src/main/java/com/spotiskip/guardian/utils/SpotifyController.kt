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
import android.os.Build
import java.io.File
import java.util.concurrent.TimeUnit
import android.content.pm.PackageManager
import com.spotiskip.guardian.services.SpotiGuardService

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

    fun isRootAvailable(): Boolean {
        return try {
            val paths = arrayOf(
                "/system/bin/su",
                "/system/xbin/su",
                "/sbin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/data/local/su"
            )
            paths.any { File(it).exists() }
        } catch (e: Exception) {
            false
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

    fun execShizukuCommand(cmd: Array<String>): Boolean {
        return try {
            if (!rikka.shizuku.Shizuku.pingBinder()) return false
            if (rikka.shizuku.Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) return false

            val method = rikka.shizuku.Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, cmd, null, null) as Process
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error ejecutando comando Shizuku [${cmd.joinToString(" ")}]: ${e.message}", e)
            false
        }
    }

    fun sendMediaNext(context: Context) {
        // 1. Tecla nativa NEXT
        sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT)

        // 2. Control interno de Spotify (widget oficial)
        try {
            val widgetIntent = Intent("com.spotify.mobile.android.ui.widget.NEXT").apply {
                setPackage(SPOTIFY_PACKAGE)
            }
            context.sendBroadcast(widgetIntent)
        } catch (e: Exception) {
            // Ignorar
        }

        // 3. Dispatch de MediaSession por Shizuku
        Thread {
            execShizukuCommand(arrayOf("cmd", "media_session", "dispatch", "next"))
        }.start()
    }

    fun sendMediaPlay(context: Context) {
        // 1. Tecla nativa PLAY (idempotente: nunca pausa si ya está sonando)
        sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY)

        // 2. Control interno de Spotify (widget oficial: despierta el motor de audio directamente)
        try {
            val widgetIntent = Intent("com.spotify.mobile.android.ui.widget.PLAY").apply {
                setPackage(SPOTIFY_PACKAGE)
            }
            context.sendBroadcast(widgetIntent)
        } catch (e: Exception) {
            // Ignorar
        }

        // 3. Dispatch de MediaSession a nivel de sistema por Shizuku
        Thread {
            execShizukuCommand(arrayOf("cmd", "media_session", "dispatch", "play"))
        }.start()
    }

    fun tryShizukuForceStop(context: Context): Boolean {
        val pkg = if (isSpotifyInstalled(context)) SPOTIFY_PACKAGE else SPOTIFY_LITE_PACKAGE
        val success = execShizukuCommand(arrayOf("am", "force-stop", pkg))
        if (success) {
            Log.i(TAG, "Spotify cerrado forzosamente vía Shizuku ($pkg).")
        }
        return success
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
        SpotiGuardService.isPlaybackActive = false
        sendMediaStop(context)

        val mainHandler = Handler(Looper.getMainLooper())

        // Ejecutar en hilo de fondo para no bloquear el hilo principal
        Thread {
            // 1. Si hay Root, forzar detención directa por comando (100% silencioso)
            if (tryRootForceStop()) {
                mainHandler.postDelayed({
                    relaunchAndResumePlayback(context, onComplete)
                }, 350)
                return@Thread
            }

            // 2. Si hay Shizuku activo, forzar detención directa a nivel de sistema (100% silencioso sin pantallas)
            if (tryShizukuForceStop(context)) {
                mainHandler.postDelayed({
                    relaunchAndResumePlayback(context, onComplete)
                }, 350)
                return@Thread
            }

            // 3. Fallback limpio: Relanzar inmediatamente con Next y Play
            killSpotify(context)
            mainHandler.postDelayed({
                relaunchAndResumePlayback(context, onComplete)
            }, 400)
        }.start()
    }

    private fun relaunchAndResumePlayback(context: Context, onComplete: (() -> Unit)? = null) {
        val mainHandler = Handler(Looper.getMainLooper())

        Log.i(TAG, "Relanzando Spotify...")
        relaunchSpotify(context)

        // 1. A los 1200ms enviar NEXT para avanzar sobre el anuncio
        mainHandler.postDelayed({
            Log.i(TAG, "Enviando Media Next a Spotify...")
            sendMediaNext(context)
        }, 1200)

        // 2. A los 1800ms enviar primer PLAY
        mainHandler.postDelayed({
            Log.i(TAG, "Enviando Media Play a Spotify (pulso 1)...")
            sendMediaPlay(context)
        }, 1800)

        // 3. A los 2800ms enviar segundo PLAY de refuerzo si aún no suena
        mainHandler.postDelayed({
            if (!SpotiGuardService.isPlaybackActive) {
                Log.i(TAG, "Refuerzo de Media Play a los 2800ms (pulso 2)...")
                sendMediaPlay(context)
            }
        }, 2800)

        // 4. A los 3800ms enviar tercer PLAY si aún no suena
        mainHandler.postDelayed({
            if (!SpotiGuardService.isPlaybackActive) {
                Log.i(TAG, "Refuerzo de Media Play a los 3800ms (pulso 3)...")
                sendMediaPlay(context)
            }
        }, 3800)

        // 5. A los 4800ms reintento final
        mainHandler.postDelayed({
            if (!SpotiGuardService.isPlaybackActive) {
                Log.i(TAG, "Refuerzo final de Media Play a los 4800ms (pulso 4)...")
                sendMediaPlay(context)
            }
            onComplete?.invoke()
        }, 4800)
    }
}
