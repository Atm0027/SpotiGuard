package com.spotiskip.guardian.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent

object SpotifyController {
    const val SPOTIFY_PACKAGE = "com.spotify.music"

    fun killSpotify(context: Context) {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.killBackgroundProcesses(SPOTIFY_PACKAGE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun relaunchSpotify(context: Context) {
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(SPOTIFY_PACKAGE)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
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
