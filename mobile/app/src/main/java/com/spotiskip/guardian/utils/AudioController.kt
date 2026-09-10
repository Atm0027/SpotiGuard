package com.spotiskip.guardian.utils

import android.content.Context
import android.media.AudioManager

class AudioController(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var previousVolume: Int = -1
    private var isMuted: Boolean = false

    fun mute() {
        if (isMuted) return
        try {
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (currentVol > 0) {
                previousVolume = currentVol
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            isMuted = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unmute() {
        if (!isMuted) return
        try {
            val restoreVol = if (previousVolume > 0) previousVolume else {
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, restoreVol, 0)
            isMuted = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isCurrentlyMuted(): Boolean = isMuted
}
