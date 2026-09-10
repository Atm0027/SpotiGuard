package com.spotiskip.guardian

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.button.MaterialButton
import com.spotiskip.guardian.services.SpotifyNotificationListener
import com.spotiskip.guardian.utils.SpotifyController

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatusBadge: TextView
    private lateinit var tvCurrentTrack: TextView
    private lateinit var tvSkipCount: TextView
    private lateinit var tvTimeSaved: TextView
    private lateinit var btnNotificationPermission: MaterialButton
    private lateinit var btnBatteryOptimization: MaterialButton
    private lateinit var btnOpenSpotify: MaterialButton

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SpotifyNotificationListener.ACTION_SPOTIFY_UPDATE) {
                val title = intent.getStringExtra(SpotifyNotificationListener.EXTRA_TRACK_TITLE) ?: ""
                val isAd = intent.getBooleanExtra(SpotifyNotificationListener.EXTRA_IS_AD, false)

                tvCurrentTrack.text = title

                if (isAd) {
                    tvStatusBadge.text = "⚡ BYPASS EN CURSO"
                    tvStatusBadge.setTextColor(Color.parseColor("#FB7185"))
                    tvStatusBadge.setBackgroundColor(Color.parseColor("#881337"))
                } else {
                    tvStatusBadge.text = "● ACTIVO"
                    tvStatusBadge.setTextColor(Color.parseColor("#34D399"))
                    tvStatusBadge.setBackgroundColor(Color.parseColor("#064E3B"))
                }

                updateStats()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatusBadge = findViewById(R.id.tvStatusBadge)
        tvCurrentTrack = findViewById(R.id.tvCurrentTrack)
        tvSkipCount = findViewById(R.id.tvSkipCount)
        tvTimeSaved = findViewById(R.id.tvTimeSaved)
        btnNotificationPermission = findViewById(R.id.btnNotificationPermission)
        btnBatteryOptimization = findViewById(R.id.btnBatteryOptimization)
        btnOpenSpotify = findViewById(R.id.btnOpenSpotify)

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        updateStats()

        val filter = IntentFilter(SpotifyNotificationListener.ACTION_SPOTIFY_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(updateReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(updateReceiver)
        } catch (e: Exception) {
            // Ya desregistrado
        }
    }

    private fun setupListeners() {
        btnNotificationPermission.setOnClickListener {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Permite a SpotiGuard escuchar notificaciones", Toast.LENGTH_LONG).show()
        }

        btnBatteryOptimization.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_SETTINGS)
                startActivity(intent)
            }
            Toast.makeText(this, "Selecciona 'Sin restricciones' para SpotiGuard", Toast.LENGTH_LONG).show()
        }

        btnOpenSpotify.setOnClickListener {
            SpotifyController.relaunchSpotify(this)
        }
    }

    private fun checkPermissions() {
        val hasNotificationAccess = NotificationManagerCompat.getEnabledListenerPackages(this)
            .contains(packageName)

        if (hasNotificationAccess) {
            btnNotificationPermission.text = "✓ Acceso a Notificaciones Concedido"
            btnNotificationPermission.setBackgroundColor(Color.parseColor("#065F46"))
            btnNotificationPermission.isEnabled = false

            tvStatusBadge.text = "● ACTIVO (ESCUCHANDO)"
            tvStatusBadge.setTextColor(Color.parseColor("#34D399"))
            tvStatusBadge.setBackgroundColor(Color.parseColor("#064E3B"))
        } else {
            btnNotificationPermission.text = "1. Activar Acceso a Notificaciones"
            btnNotificationPermission.setBackgroundColor(Color.parseColor("#0284C7"))
            btnNotificationPermission.isEnabled = true

            tvStatusBadge.text = "○ PERMISO REQUERIDO"
            tvStatusBadge.setTextColor(Color.parseColor("#FBBF24"))
            tvStatusBadge.setBackgroundColor(Color.parseColor("#1E293B"))
        }
    }

    private fun updateStats() {
        val count = SpotifyNotificationListener.totalAdsSkipped
        tvSkipCount.text = count.toString()

        val savedSec = count * 30
        val min = savedSec / 60
        val sec = savedSec % 60
        if (min > 0) {
            tvTimeSaved.text = "${min}m ${sec}s"
        } else {
            tvTimeSaved.text = "${sec}s"
        }
    }
}
