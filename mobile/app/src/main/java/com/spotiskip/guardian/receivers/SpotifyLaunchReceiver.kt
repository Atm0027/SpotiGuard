package com.spotiskip.guardian.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.spotiskip.guardian.services.SpotiGuardService

/**
 * Receptor estático de emisiones de Spotify.
 * Si Spotify se abre y comienza a reproducir música mientras SpotiGuard estaba detenido,
 * este receptor lo detecta de inmediato e inicia SpotiGuardService en segundo plano.
 */
class SpotifyLaunchReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action ?: return
        if (action == "com.spotify.music.metadatachanged" || action == "com.spotify.music.playbackstatechanged") {
            if (!SpotiGuardService.isRunning) {
                val serviceIntent = Intent(context, SpotiGuardService::class.java).apply {
                    this.action = SpotiGuardService.ACTION_START
                }
                try {
                    ContextCompat.startForegroundService(context, serviceIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
