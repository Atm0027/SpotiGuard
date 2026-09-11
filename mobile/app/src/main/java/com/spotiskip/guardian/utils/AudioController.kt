package com.spotiskip.guardian.utils

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log

/**
 * Controlador de audio de bajo nivel para SpotiGuard.
 * Silencia el stream multimedia instantáneamente (latencia 0ms) al detectar un anuncio
 * para proteger los oídos del usuario mientras se ejecuta el cierre y relanzamiento de Spotify,
 * y restaura el volumen exacto una vez que la música legítima comienza a sonar.
 */
object AudioController {
    private const val TAG = "AudioController"
    private var isMuted = false
    private var previousVolume = -1

    @Synchronized
    fun mute(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            if (!isMuted) {
                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (currentVol > 0) {
                    previousVolume = currentVol
                }
                Log.d(TAG, "Silenciando audio multimedia instantáneamente (volumen previo guardado: $previousVolume)")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
                } else {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                }
                isMuted = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error silenciando audio: ${e.message}", e)
        }
    }

    @Synchronized
    fun unmute(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            if (isMuted) {
                Log.d(TAG, "Restaurando volumen multimedia")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
                }
                val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val targetVol = if (previousVolume > 0) {
                    previousVolume.coerceAtMost(max)
                } else {
                    (max * 0.65).toInt()
                }
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                isMuted = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restaurando audio: ${e.message}", e)
        }
    }

    fun isCurrentlyMuted(): Boolean = isMuted
}
