package com.spotiskip.guardian.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import com.spotiskip.guardian.utils.SpotifyController

class SpotifyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.packageName == SpotifyController.SPOTIFY_PACKAGE) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                // Notificar que Spotify está visible y en primer plano
                val intent = Intent(SpotifyNotificationListener.ACTION_SPOTIFY_UPDATE).apply {
                    putExtra(SpotifyNotificationListener.EXTRA_TRACK_TITLE, "Spotify en primer plano")
                    putExtra(SpotifyNotificationListener.EXTRA_IS_AD, false)
                    setPackage(packageName)
                }
                sendBroadcast(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Interrupción del servicio
    }
}
