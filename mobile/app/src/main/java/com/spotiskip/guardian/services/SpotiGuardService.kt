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
import com.spotiskip.guardian.utils.SpotifyController

/**
 * Servicio en primer plano de SpotiGuard.
 * Modo de operación único y exclusivo:
 * Al detectar un anuncio, CIERRA Spotify, lo VUELVE A ABRIR y le DA AL PLAY.
 * Sin silenciamientos de audio ni mutaciones.
 */
class SpotiGuardService : Service() {

    private var isReceiverRegistered = false
    private var lastTrack = ""
    private var isAdActive = false
    private var lastSkipTimestamp = 0L

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
    }

    private val spotifyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            when (intent.action) {
                "com.spotify.music.metadatachanged" -> handleMetadataChanged(intent)
                "com.spotify.music.playbackstatechanged" -> {
                    val isPlaying = intent.getBooleanExtra("playing", false)
                    if (!isPlaying) {
                        isAdActive = false
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
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
        val notification = buildForegroundNotification("Protección Activa", "Saltando anuncios mediante Reinicio y Play")
        
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
        val id = intent.getStringExtra("id")?.trim() ?: ""
        val track = intent.getStringExtra("track")?.trim() ?: ""
        val artist = intent.getStringExtra("artist")?.trim() ?: ""
        val album = intent.getStringExtra("album")?.trim() ?: ""
        val isPlaying = intent.getBooleanExtra("playing", true)

        if (!isPlaying) {
            isAdActive = false
            return
        }

        val isAd = isAdvertisement(id, track, artist, album)

        if (isAd) {
            val now = System.currentTimeMillis()
            // Evitar bucles continuos (cooldown de 3.5 segundos entre reinicios)
            if (now - lastSkipTimestamp < 3500) {
                return
            }
            lastSkipTimestamp = now
            isAdActive = true
            totalAdsSkipped++

            val adLabel = track.ifEmpty { "Anuncio" }
            updateNotification("⚡ Saltando anuncio...", adLabel)
            broadcastUpdate("⚡ Saltando: $adLabel", true)

            // Maniobra pura: Cerrar Spotify -> Abrir Spotify -> Darle al Play
            SpotifyController.restartAndResume(applicationContext) {
                isAdActive = false
            }
        } else {
            isAdActive = false
            val displayTrack = if (artist.isNotEmpty()) "$track - $artist" else track
            if (displayTrack.isNotEmpty() && displayTrack != lastTrack) {
                lastTrack = displayTrack
                updateNotification("SpotiGuard Activo", "▶ $displayTrack")
                broadcastUpdate(displayTrack, false)
            }
        }
    }

    /**
     * Detección infalible de anuncios:
     * - Toda canción real en Spotify empieza obligatoriamente por 'spotify:track:'.
     * - Todo podcast real empieza por 'spotify:episode:'.
     * - Cualquier cuña comercial o anuncio (Vinted, Amazon, Spotify, etc.) tiene un URI
     *   que NO es de pista musical estándar, o contiene palabras clave publicitarias.
     */
    private fun isAdvertisement(
        id: String,
        track: String,
        artist: String,
        album: String
    ): Boolean {
        val idLower = id.lowercase().trim()

        // 1. REGLA DE ORO: Si no es un track musical ni episodio, ES UN ANUNCIO
        if (idLower.isNotEmpty()) {
            if (!idLower.startsWith("spotify:track:") && !idLower.startsWith("spotify:episode:")) {
                return true
            }
        } else {
            // Si el ID está completamente vacío, es una cuña comercial
            return true
        }

        // 2. Si el ID fuera un track simulado, comprobar palabras clave publicitarias
        val trackLower = track.lowercase()
        val artistLower = artist.lowercase()
        val albumLower = album.lowercase()

        val adKeywords = listOf(
            "advertisement", "publicidad", "anuncio", "anuncios", "promo",
            "spotify free", "werbung", "publicite", "publicité", "publicidade", "pubblicità"
        )
        for (kw in adKeywords) {
            if (trackLower.contains(kw) || artistLower.contains(kw) || albumLower.contains(kw)) {
                return true
            }
        }

        // 3. Título genérico "Spotify" sin artista real
        if (trackLower == "spotify" && (artist.isEmpty() || artistLower == "spotify")) {
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
