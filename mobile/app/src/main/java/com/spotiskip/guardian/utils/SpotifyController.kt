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

    /**
     * Obtiene el Intent de lanzamiento para Spotify usando múltiples estrategias
     * para asegurar compatibilidad con Android 11, 12, 13, 14 y 15 (Package Visibility).
     */
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

        // 4. Intent genérico selector de música
        try {
            val musicIntent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (musicIntent.resolveActivity(pm) != null) {
                return musicIntent
            }
        } catch (e: Exception) {
            // Ignorado
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

    fun sendMediaPlay(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager.dispatchMediaKeyEvent(downEvent)
            audioManager.dispatchMediaKeyEvent(upEvent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restartAndResume(context: Context, onComplete: (() -> Unit)? = null) {
        killSpotify(context)
        Handler(Looper.getMainLooper()).postDelayed({
            relaunchSpotify(context)
            Handler(Looper.getMainLooper()).postDelayed({
                sendMediaPlay(context)
                onComplete?.invoke()
            }, 1200)
        }, 500)
    }
}
