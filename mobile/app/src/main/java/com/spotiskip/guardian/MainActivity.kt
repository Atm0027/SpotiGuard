package com.spotiskip.guardian

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.spotiskip.guardian.services.SpotiGuardService
import com.spotiskip.guardian.utils.SpotifyController

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatusBadge: TextView
    private lateinit var tvCurrentTrack: TextView
    private lateinit var tvSkipCount: TextView
    private lateinit var tvTimeSaved: TextView
    private lateinit var btnToggleService: MaterialButton
    private lateinit var btnOperationMode: MaterialButton
    private lateinit var btnSpotifySettings: MaterialButton
    private lateinit var btnBatteryOptimization: MaterialButton
    private lateinit var btnOpenSpotify: MaterialButton

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startSpotiGuardService()
            } else {
                startSpotiGuardService()
            }
        }

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SpotiGuardService.ACTION_SPOTIFY_UPDATE) {
                if (intent.hasExtra(SpotiGuardService.EXTRA_SERVICE_RUNNING)) {
                    val running = intent.getBooleanExtra(SpotiGuardService.EXTRA_SERVICE_RUNNING, false)
                    updateServiceUI(running)
                }

                val title = intent.getStringExtra(SpotiGuardService.EXTRA_TRACK_TITLE)
                val isAd = intent.getBooleanExtra(SpotiGuardService.EXTRA_IS_AD, false)

                if (title != null) {
                    tvCurrentTrack.text = title

                    if (isAd) {
                        tvStatusBadge.text = "⚡ BYPASS EN CURSO"
                        tvStatusBadge.setTextColor(Color.parseColor("#FB7185"))
                        tvStatusBadge.setBackgroundColor(Color.parseColor("#881337"))
                    } else if (SpotiGuardService.isRunning) {
                        tvStatusBadge.text = "● ACTIVO (PROTEGIENDO)"
                        tvStatusBadge.setTextColor(Color.parseColor("#34D399"))
                        tvStatusBadge.setBackgroundColor(Color.parseColor("#064E3B"))
                    }
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
        btnToggleService = findViewById(R.id.btnToggleService)
        btnOperationMode = findViewById(R.id.btnOperationMode)
        btnSpotifySettings = findViewById(R.id.btnSpotifySettings)
        btnBatteryOptimization = findViewById(R.id.btnBatteryOptimization)
        btnOpenSpotify = findViewById(R.id.btnOpenSpotify)

        setupListeners()

        // Auto-iniciar la protección si no está iniciada
        if (!SpotiGuardService.isRunning) {
            checkPermissionAndStart()
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceUI(SpotiGuardService.isRunning)
        updateStats()

        val filter = IntentFilter(SpotiGuardService.ACTION_SPOTIFY_UPDATE)
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
        btnToggleService.setOnClickListener {
            if (SpotiGuardService.isRunning) {
                stopSpotiGuardService()
            } else {
                checkPermissionAndStart()
            }
        }

        btnOperationMode.setOnClickListener {
            if (SpotiGuardService.operationMode == "mute") {
                SpotiGuardService.operationMode = "restart"
                btnOperationMode.text = "Modo: Saltar Rápido (Skip)"
                Toast.makeText(this, "Modo: Reiniciar Spotify para saltar el anuncio", Toast.LENGTH_SHORT).show()
            } else {
                SpotiGuardService.operationMode = "mute"
                btnOperationMode.text = "Modo: Silenciar Anuncio (Mute)"
                Toast.makeText(this, "Modo: Silenciar anuncio en segundo plano", Toast.LENGTH_SHORT).show()
            }
        }

        btnSpotifySettings.setOnClickListener {
            Toast.makeText(
                this,
                "En Spotify: Ajustes -> Reproducción -> Activa 'Estado de transmisión del dispositivo'",
                Toast.LENGTH_LONG
            ).show()
            val launched = SpotifyController.relaunchSpotify(this)
            if (!launched) {
                Toast.makeText(this, "Abre Spotify para verificar el ajuste", Toast.LENGTH_SHORT).show()
            }
        }

        btnBatteryOptimization.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            Toast.makeText(this, "Selecciona 'Sin restricciones' para SpotiGuard", Toast.LENGTH_LONG).show()
        }

        btnOpenSpotify.setOnClickListener {
            // Garantizar que SpotiGuard esté activo al lanzar Spotify
            if (!SpotiGuardService.isRunning) {
                startSpotiGuardService()
            }
            val launched = SpotifyController.relaunchSpotify(this)
            if (!launched) {
                Toast.makeText(this, "No se pudo abrir Spotify automáticamente. Ábrelo desde tus aplicaciones.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                startSpotiGuardService()
            } else {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startSpotiGuardService()
        }
    }

    private fun startSpotiGuardService() {
        val intent = Intent(this, SpotiGuardService::class.java).apply {
            action = SpotiGuardService.ACTION_START
        }
        ContextCompat.startForegroundService(this, intent)
        updateServiceUI(true)
    }

    private fun stopSpotiGuardService() {
        val intent = Intent(this, SpotiGuardService::class.java).apply {
            action = SpotiGuardService.ACTION_STOP
        }
        startService(intent)
        updateServiceUI(false)
        Toast.makeText(this, "Protección de SpotiGuard pausada", Toast.LENGTH_SHORT).show()
    }

    private fun updateServiceUI(running: Boolean) {
        if (running) {
            btnToggleService.text = "⏹️ Detener Protección SpotiGuard"
            btnToggleService.setBackgroundColor(Color.parseColor("#475569"))
            btnToggleService.setTextColor(Color.WHITE)

            tvStatusBadge.text = "● ACTIVO (PROTEGIENDO)"
            tvStatusBadge.setTextColor(Color.parseColor("#34D399"))
            tvStatusBadge.setBackgroundColor(Color.parseColor("#064E3B"))
        } else {
            btnToggleService.text = "🛡️ Activar Protección SpotiGuard"
            btnToggleService.setBackgroundColor(Color.parseColor("#38BDF8"))
            btnToggleService.setTextColor(Color.parseColor("#0B0F19"))

            tvStatusBadge.text = "○ DETENIDO"
            tvStatusBadge.setTextColor(Color.parseColor("#94A3B8"))
            tvStatusBadge.setBackgroundColor(Color.parseColor("#1E293B"))
        }
    }

    private fun updateStats() {
        val count = SpotiGuardService.totalAdsSkipped
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
