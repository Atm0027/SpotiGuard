package com.spotiskip.guardian.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent

object SpotifyController {
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
            e.printStackTrace()
            false
        }
    }

    fun killSpotify(context: Context) {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.killBackgroundProcesses(SPOTIFY_PACKAGE)
            am.killBackgroundProcesses(SPOTIFY_LITE_PACKAGE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMediaKey(context: Context, keyCode: Int) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))

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
            e.printStackTrace()
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
     * Maniobra Kill & Relaunch pura:
     * 1. Detiene y mata Spotify.
     * 2. Vuelve a abrir Spotify tras 500ms.
     * 3. Descarta el buffer del anuncio con Next y envía Play para reanudar la música.
     */
    fun restartAndResume(context: Context, onComplete: (() -> Unit)? = null) {
        sendMediaStop(context)
        killSpotify(context)

        Handler(Looper.getMainLooper()).postDelayed({
            relaunchSpotify(context)

            Handler(Looper.getMainLooper()).postDelayed({
                sendMediaNext(context)
                Handler(Looper.getMainLooper()).postDelayed({
                    sendMediaPlay(context)
                    onComplete?.invoke()
                }, 300)
            }, 1000)
        }, 500)
    }
}
