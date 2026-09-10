package com.spotiskip.guardian.services

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.spotiskip.guardian.utils.AudioController
import com.spotiskip.guardian.utils.SpotifyController

class SpotifyNotificationListener : NotificationListenerService() {

    private lateinit var audioController: AudioController
    private var lastTrack = ""
    private var isAdActive = false

    companion object {
        const val ACTION_SPOTIFY_UPDATE = "com.spotiskip.guardian.SPOTIFY_UPDATE"
        const val EXTRA_TRACK_TITLE = "extra_track_title"
        const val EXTRA_IS_AD = "extra_is_ad"

        var totalAdsSkipped = 0
        var operationMode = "mute" // "mute" o "restart"
    }

    override fun onCreate() {
        super.onCreate()
        audioController = AudioController(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.packageName != SpotifyController.SPOTIFY_PACKAGE) {
            return
        }

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val artist = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""

        if (title.isEmpty()) return

        val isAd = isAdvertisement(title, artist)

        if (isAd) {
            if (!isAdActive) {
                isAdActive = true
                totalAdsSkipped++

                if (operationMode == "restart") {
                    SpotifyController.restartAndResume(applicationContext) {
                        isAdActive = false
                    }
                } else {
                    audioController.mute()
                }

                broadcastUpdate("⚡ Anuncio detectado: $title", true)
            }
        } else {
            if (isAdActive) {
                isAdActive = false
                audioController.unmute()
            }
            val displayTrack = if (artist.isNotEmpty()) "$title - $artist" else title
            if (displayTrack != lastTrack) {
                lastTrack = displayTrack
                broadcastUpdate(displayTrack, false)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn != null && sbn.packageName == SpotifyController.SPOTIFY_PACKAGE) {
            if (isAdActive) {
                isAdActive = false
                audioController.unmute()
            }
            broadcastUpdate("Spotify en pausa / cerrado", false)
        }
    }

    private fun isAdvertisement(title: String, artist: String): Boolean {
        val titleLower = title.lowercase()
        val artistLower = artist.lowercase()

        val adKeywords = listOf(
            "advertisement", "publicidad", "anuncio", "anuncios", "promo",
            "spotify free", "werbung", "publicite", "publicité", "publicidade", "pubblicità"
        )
        for (kw in adKeywords) {
            if (titleLower.contains(kw) || artistLower.contains(kw)) {
                return true
            }
        }

        // Si el título es exactamente "Spotify" y no tiene artista definido
        if (titleLower == "spotify" && (artist.isEmpty() || artistLower == "spotify")) {
            return true
        }

        // Si el artista está vacío y el título no parece una canción estándar
        if (artist.isEmpty() && !title.contains(" - ")) {
            return true
        }

        return false
    }

    private fun broadcastUpdate(title: String, isAd: Boolean) {
        val intent = Intent(ACTION_SPOTIFY_UPDATE).apply {
            putExtra(EXTRA_TRACK_TITLE, title)
            putExtra(EXTRA_IS_AD, isAd)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::audioController.isInitialized && audioController.isCurrentlyMuted()) {
            audioController.unmute()
        }
    }
}
