package com.spotiskip.guardian.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.spotiskip.guardian.MainActivity
import com.spotiskip.guardian.R
import com.spotiskip.guardian.utils.AudioController
import com.spotiskip.guardian.utils.SpotifyController

/**
 * Servicio en primer plano de SpotiGuard.
 * Escucha los eventos nativos que emite la app oficial de Spotify (Broadcast Intents)
 * cuando la opción "Estado de emisión del dispositivo" está activa en Spotify.
 *
 * Arquitectura 100% estándar y segura:
 * - NO requiere permisos de accesibilidad (BIND_ACCESSIBILITY_SERVICE).
 * - NO requiere permisos de escucha de notificaciones (BIND_NOTIFICATION_LISTENER_SERVICE).
 * - No activa alertas de Google Play Protect por ser un servicio de reproducción multimedia estándar.
 */
class SpotiGuardService : Service() {

    private lateinit var audioController: AudioController
    private var isReceiverRegistered = false
    private var lastTrack = ""
    private var isAdActive = false

    companion object {
        const val CHANNEL_ID = "spotiguard_monitor_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_SPOTIFY_UPDATE = "com.spotiskip.guardian.SPOTIFY_UPDATE"
        const val EXTRA_TRACK_TITLE = "extra_track_title"
        const val EXTRA_IS_AD = "extra_is_ad"
        const val EXTRA_SERVICE_RUNNING = "extra_service_running"

        const val ACTION_START = "com.spotiskip.guardian.action.START"
        const val ACTION_STOP = "com.spotiskip.guardian.action.STOP"

        var isRunning = false
            private set

        var totalAdsSkipped = 0
        var operationMode = "mute" // "mute" o "restart"
    }

    private val spotifyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            when (intent.action) {
                "com.spotify.music.metadatachanged" -> handleMetadataChanged(intent)
                "com.spotify.music.playbackstatechanged" -> handlePlaybackStateChanged(intent)
                "com.spotify.music.queuechanged" -> {
                    // Actualización de cola
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioController = AudioController(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForegroundService()
            return START_NOT_STICKY
        }

        startInForeground()
        registerSpotifyReceiver()
        isRunning = true
        broadcastServiceState(true)

        return START_STICKY
    }

    private fun startInForeground() {
        val notification = buildForegroundNotification("Protegiendo tu reproducción de Spotify", "● Activo y escuchando")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildForegroundNotification(title: String, text: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        val notification = buildForegroundNotification(title, text)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitor de SpotiGuard",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra el estado activo de la protección de anuncios"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun registerSpotifyReceiver() {
        if (isReceiverRegistered) return

        val filter = IntentFilter().apply {
            addAction("com.spotify.music.metadatachanged")
            addAction("com.spotify.music.playbackstatechanged")
            addAction("com.spotify.music.queuechanged")
        }

        ContextCompat.registerReceiver(
            this,
            spotifyReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        isReceiverRegistered = true
    }

    private fun unregisterSpotifyReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(spotifyReceiver)
            } catch (e: Exception) {
                // Ya desregistrado
            }
            isReceiverRegistered = false
        }
    }

    private fun handleMetadataChanged(intent: Intent) {
        val id = intent.getStringExtra("id") ?: ""
        val track = intent.getStringExtra("track")?.trim() ?: ""
        val artist = intent.getStringExtra("artist")?.trim() ?: ""
        val album = intent.getStringExtra("album")?.trim() ?: ""
        val isPlaying = intent.getBooleanExtra("playing", true)
        val length = intent.getIntExtra("length", 0)

        val isAd = isAdvertisement(id, track, artist, album, length)

        if (isAd) {
            if (!isAdActive) {
                isAdActive = true
                totalAdsSkipped++

                if (operationMode == "restart") {
                    updateNotification("⚡ Saltando anuncio...", track.ifEmpty { "Spotify Ad" })
                    SpotifyController.restartAndResume(applicationContext) {
                        isAdActive = false
                    }
                } else {
                    audioController.mute()
                    updateNotification("⚡ Silenciando anuncio", track.ifEmpty { "Anuncio de Spotify" })
                }

                broadcastUpdate("⚡ Anuncio detectado: ${track.ifEmpty { "Publicidad" }}", true)
            }
        } else {
            if (isAdActive) {
                isAdActive = false
                audioController.unmute()
            }
            val displayTrack = if (artist.isNotEmpty()) "$track - $artist" else track
            if (displayTrack.isNotEmpty() && displayTrack != lastTrack) {
                lastTrack = displayTrack
                updateNotification("SpotiGuard Activo", "▶ $displayTrack")
                broadcastUpdate(displayTrack, false)
            }
        }
    }

    private fun handlePlaybackStateChanged(intent: Intent) {
        val isPlaying = intent.getBooleanExtra("playing", false)
        if (!isPlaying && isAdActive) {
            isAdActive = false
            audioController.unmute()
            updateNotification("SpotiGuard Activo", "Spotify en pausa")
            broadcastUpdate("Spotify en pausa", false)
        }
    }

    private fun isAdvertisement(
        id: String,
        track: String,
        artist: String,
        album: String,
        length: Int
    ): Boolean {
        // 1. Detección por URI de Spotify
        val idLower = id.lowercase()
        if (idLower.startsWith("spotify:ad:") || idLower.contains(":ad:") || idLower.contains("advertisement")) {
            return true
        }

        val trackLower = track.lowercase()
        val artistLower = artist.lowercase()
        val albumLower = album.lowercase()

        // 2. Palabras clave conocidas en títulos de anuncios en varios idiomas
        val adKeywords = listOf(
            "advertisement", "publicidad", "anuncio", "anuncios", "promo",
            "spotify free", "werbung", "publicite", "publicité", "publicidade", "pubblicità"
        )
        for (kw in adKeywords) {
            if (trackLower.contains(kw) || artistLower.contains(kw) || albumLower.contains(kw)) {
                return true
            }
        }

        // 3. Título genérico "Spotify" sin artista
        if (trackLower == "spotify" && (artist.isEmpty() || artistLower == "spotify")) {
            return true
        }

        // 4. Si no es un track estándar y carece de artista y álbum
        if (!idLower.startsWith("spotify:track:") && (artist.isEmpty() || album.isEmpty())) {
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

    private fun broadcastServiceState(running: Boolean) {
        val intent = Intent(ACTION_SPOTIFY_UPDATE).apply {
            putExtra(EXTRA_SERVICE_RUNNING, running)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun stopForegroundService() {
        unregisterSpotifyReceiver()
        if (::audioController.isInitialized && audioController.isCurrentlyMuted()) {
            audioController.unmute()
        }
        isRunning = false
        broadcastServiceState(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForegroundService()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
