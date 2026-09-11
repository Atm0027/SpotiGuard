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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.spotiskip.guardian.MainActivity
import com.spotiskip.guardian.R
import com.spotiskip.guardian.utils.SpotifyController

/**
 * Servicio en primer plano de SpotiGuard.
 * Modo de operación:
 * Al detectar un anuncio:
 * 1. Silencia el audio instantáneamente para cero molestias sonoras.
 * 2. Cierra forzosamente Spotify (mediante Accesibilidad o Root).
 * 3. Vuelve a abrir Spotify en menos de un segundo y reanuda la reproducción.
 * 4. Restaura el volumen.
 *
 * Implementa detección dual infalible:
 * 1. Detección reactiva por Broadcast (anuncios explícitos, IDs no-track, palabras clave).
 * 2. Watchdog por expiración de pista: Cuando una canción termina y Spotify reproduce
 *    un anuncio, Spotify NO EMITE broadcasts. El watchdog detecta el fin de la canción
 *    y ejecuta inmediatamente el salto de anuncio si no llega una nueva canción.
 */
class SpotiGuardService : Service() {

    private var isReceiverRegistered = false
    private var lastTrack = ""
    private var lastSkipTimestamp = 0L

    // Estado de la pista actual para el Watchdog
    private var currentTrackId = ""
    private var currentTrackDurationMs = 0L
    private var currentPlaybackPosition = 0L
    private var lastStateTimestamp = 0L

    private val watchdogHandler = Handler(Looper.getMainLooper())
    private val watchdogRunnable = Runnable {
        Log.w(TAG, "⏰ WATCHDOG EXPIRADO: Duración de canción agotada sin nuevo tema -> ANUNCIO DETECTADO")
        handleAdDetected("Anuncio publicitario de Spotify")
    }

    companion object {
        private const val TAG = "SpotiGuardService"

        const val CHANNEL_ID = "spotiguard_monitor_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_SPOTIFY_UPDATE = "com.spotiskip.guardian.SPOTIFY_UPDATE"
        const val EXTRA_TRACK_TITLE = "extra_track_title"
        const val EXTRA_IS_AD = "extra_is_ad"
        const val EXTRA_SERVICE_RUNNING = "extra_service_running"
        const val EXTRA_REQUIREMENTS_CHANGED = "extra_requirements_changed"

        const val ACTION_START = "com.spotiskip.guardian.action.START"
        const val ACTION_STOP = "com.spotiskip.guardian.action.STOP"

        var isRunning = false
            private set

        var totalAdsSkipped = 0
        var isPlaybackActive: Boolean = false
    }

    private val spotifyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            when (intent.action) {
                "com.spotify.music.metadatachanged" -> handleMetadataChanged(intent)
                "com.spotify.music.playbackstatechanged" -> handlePlaybackStateChanged(intent)
                "com.spotify.music.queuechanged" -> markBroadcastConfigured()
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

    private fun markBroadcastConfigured() {
        val prefs = getSharedPreferences("spotiguard_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("spotify_broadcast_enabled", false)) {
            prefs.edit().putBoolean("spotify_broadcast_enabled", true).apply()
            Log.i(TAG, "Transmisión de Spotify verificada con éxito.")
            val updateIntent = Intent(ACTION_SPOTIFY_UPDATE).apply {
                putExtra(EXTRA_REQUIREMENTS_CHANGED, true)
                setPackage(packageName)
            }
            sendBroadcast(updateIntent)
        }
    }

    private fun handleMetadataChanged(intent: Intent) {
        markBroadcastConfigured()

        val id = intent.getStringExtra("id")?.trim() ?: ""
        val track = intent.getStringExtra("track")?.trim() ?: ""
        val artist = intent.getStringExtra("artist")?.trim() ?: ""
        val album = intent.getStringExtra("album")?.trim() ?: ""
        val rawLength = intent.getIntExtra("length", 0)

        Log.d(TAG, "metadataChanged: track='$track', artist='$artist', id='$id', length=$rawLength")

        // 1. Si es un anuncio explícito por metadatos
        if (isAdvertisement(id, track, artist, album)) {
            cancelWatchdog()
            handleAdDetected(track.ifEmpty { "Anuncio" })
            return
        }

        // 2. Si es una canción legítima
        val durationMs = if (rawLength in 1..9999) rawLength * 1000L else rawLength.toLong()
        currentTrackId = id
        currentTrackDurationMs = durationMs
        currentPlaybackPosition = 0L
        lastStateTimestamp = System.currentTimeMillis()
        isPlaybackActive = true

        val displayTrack = if (artist.isNotEmpty()) "$track - $artist" else track
        if (displayTrack.isNotEmpty() && displayTrack != lastTrack) {
            lastTrack = displayTrack
            updateNotification("SpotiGuard Activo", "▶ $displayTrack")
            broadcastUpdate(displayTrack, false)
        }

        // Armar el watchdog para vigilar la llegada al final de la pista
        scheduleWatchdog()
    }

    private fun handlePlaybackStateChanged(intent: Intent) {
        markBroadcastConfigured()

        val isPlaying = intent.getBooleanExtra("playing", false)
        val position = intent.getIntExtra("playbackPosition", 0)

        val id = intent.getStringExtra("id")?.trim() ?: ""
        val track = intent.getStringExtra("track")?.trim() ?: ""
        val artist = intent.getStringExtra("artist")?.trim() ?: ""
        val album = intent.getStringExtra("album")?.trim() ?: ""

        if (id.isNotEmpty() && isAdvertisement(id, track, artist, album)) {
            cancelWatchdog()
            handleAdDetected(track.ifEmpty { "Anuncio" })
            return
        }

        isPlaybackActive = isPlaying
        currentPlaybackPosition = position.toLong()
        lastStateTimestamp = System.currentTimeMillis()

        Log.d(TAG, "playbackStateChanged: playing=$isPlaying, pos=$position ms")

        if (isPlaying) {
            scheduleWatchdog()
        } else {
            cancelWatchdog()
        }
    }

    private fun scheduleWatchdog() {
        watchdogHandler.removeCallbacks(watchdogRunnable)
        if (!isPlaybackActive || currentTrackDurationMs <= 0) return

        val now = System.currentTimeMillis()
        val elapsed = (now - lastStateTimestamp).coerceAtLeast(0L)
        val estimatedPos = currentPlaybackPosition + elapsed
        val remainingMs = (currentTrackDurationMs - estimatedPos).coerceAtLeast(0L)

        // Margen de seguridad de 350 ms para permitir transición fluida si la siguiente pista no es anuncio
        val delayMs = remainingMs + 350L
        Log.d(TAG, "Watchdog armado para dentro de ${delayMs}ms (Pista restante: ${remainingMs}ms)")
        watchdogHandler.postDelayed(watchdogRunnable, delayMs)
    }

    private fun cancelWatchdog() {
        watchdogHandler.removeCallbacks(watchdogRunnable)
    }

    private fun handleAdDetected(label: String) {
        val now = System.currentTimeMillis()
        if (now - lastSkipTimestamp < 4000) {
            return
        }
        lastSkipTimestamp = now
        totalAdsSkipped++
        isPlaybackActive = false

        val adLabel = label.ifEmpty { "Anuncio" }
        updateNotification("⚡ Saltando anuncio...", adLabel)
        broadcastUpdate("⚡ Saltando: $adLabel", true)

        Log.w(TAG, "🚨 ANUNCIO INTERCEPTADO ($adLabel). Ejecutando cierre forzoso y reinicio de Spotify...")

        SpotifyController.restartAndResume(applicationContext) {
            Log.i(TAG, "Reinicio y reanudación de Spotify completados.")
        }
    }

    /**
     * Detección infalible de anuncios:
     * - Toda canción real en Spotify empieza obligatoriamente por 'spotify:track:'.
     * - Todo podcast real empieza por 'spotify:episode:'.
     * - Cualquier cuña comercial o anuncio tiene un URI
     *   que NO es de pista musical estándar, o contiene palabras clave publicitarias.
     */
    private fun isAdvertisement(
        id: String,
        track: String,
        artist: String,
        album: String
    ): Boolean {
        val idLower = id.lowercase().trim()

        if (idLower.isNotEmpty()) {
            if (!idLower.startsWith("spotify:track:") && !idLower.startsWith("spotify:episode:")) {
                return true
            }
        }

        val trackLower = track.lowercase().trim()
        val artistLower = artist.lowercase().trim()
        val albumLower = album.lowercase().trim()

        val adKeywords = listOf(
            "advertisement", "publicidad", "anuncio", "anuncios", "promo",
            "spotify free", "werbung", "publicite", "publicité", "publicidade", "pubblicità"
        )
        for (kw in adKeywords) {
            if (trackLower.contains(kw) || artistLower.contains(kw) || albumLower.contains(kw)) {
                return true
            }
        }

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
        cancelWatchdog()
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
