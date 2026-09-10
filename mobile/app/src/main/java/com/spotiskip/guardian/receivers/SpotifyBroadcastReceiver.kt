package com.spotiskip.guardian.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spotiskip.guardian.services.SpotifyNotificationListener
import com.spotiskip.guardian.utils.AudioController
import com.spotiskip.guardian.utils.SpotifyController

class SpotifyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action ?: return
        if (action == "com.spotify.music.metadatachanged") {
            val track = intent.getStringExtra("track") ?: ""
            val artist = intent.getStringExtra("artist") ?: ""
            val isPlaying = intent.getBooleanExtra("playing", false)

            if (!isPlaying) return

            val trackLower = track.lowercase()
            val isAd = trackLower.contains("advertisement") ||
                       trackLower.contains("publicidad") ||
                       (trackLower == "spotify" && artist.isEmpty())

            val audioController = AudioController(context)
            if (isAd) {
                SpotifyNotificationListener.totalAdsSkipped++
                if (SpotifyNotificationListener.operationMode == "restart") {
                    SpotifyController.restartAndResume(context)
                } else {
                    audioController.mute()
                }
            } else {
                audioController.unmute()
            }
        }
    }
}
