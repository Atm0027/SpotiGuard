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
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.spotiskip.guardian.services.SpotiGuardService
import com.spotiskip.guardian.utils.SpotifyController
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private val SHIZUKU_REQUEST_CODE = 1002

    private lateinit var tvStatusBadge: TextView
    private lateinit var tvCurrentTrack: TextView
    private lateinit var tvSkipCount: TextView
    private lateinit var tvTimeSaved: TextView
    private lateinit var btnToggleService: MaterialButton
    private lateinit var btnSpotifySettings: MaterialButton
    private lateinit var btnBatteryOptimization: MaterialButton
    private lateinit var btnAccessibilityService: MaterialButton
    private lateinit var layoutRequirementsSuccess: LinearLayout
    private lateinit var btnOpenSpotify: MaterialButton

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            startSpotiGuardService()
        }

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SpotiGuardService.ACTION_SPOTIFY_UPDATE) {
                if (intent.hasExtra(SpotiGuardService.EXTRA_SERVICE_RUNNING)) {
                    val running = intent.getBooleanExtra(SpotiGuardService.EXTRA_SERVICE_RUNNING, false)
                    updateServiceUI(running)
                }

                if (intent.getBooleanExtra(SpotiGuardService.EXTRA_REQUIREMENTS_CHANGED, false)) {
                    updateRequirementsUI()
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
        btnSpotifySettings = findViewById(R.id.btnSpotifySettings)
        btnBatteryOptimization = findViewById(R.id.btnBatteryOptimization)
        btnAccessibilityService = findViewById(R.id.btnAccessibilityService)
        layoutRequirementsSuccess = findViewById(R.id.layoutRequirementsSuccess)
        btnOpenSpotify = findViewById(R.id.btnOpenSpotify)

        setupListeners()

        // Auto-iniciar la protección si no está iniciada
        if (!SpotiGuardService.isRunning) {
            checkPermissionAndStart()
        }
    }

    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_REQUEST_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Cierre silencioso tipo PC activado con éxito", Toast.LENGTH_SHORT).show()
            }
            updateRequirementsUI()
        }
    }

    private val shizukuBinderReceivedListener = Shizuku.OnBinderReceivedListener {
        updateRequirementsUI()
    }

    private val shizukuBinderDeadListener = Shizuku.OnBinderDeadListener {
        updateRequirementsUI()
    }

    override fun onResume() {
        super.onResume()
        try {
            Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
            Shizuku.addBinderReceivedListenerSticky(shizukuBinderReceivedListener)
            Shizuku.addBinderDeadListener(shizukuBinderDeadListener)
        } catch (e: Exception) {
            // Shizuku no disponible aún
        }

        updateServiceUI(SpotiGuardService.isRunning)
        updateStats()
        updateRequirementsUI()

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
            Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
            Shizuku.removeBinderReceivedListener(shizukuBinderReceivedListener)
            Shizuku.removeBinderDeadListener(shizukuBinderDeadListener)
        } catch (e: Exception) {
            // Ignorar
        }
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

        btnSpotifySettings.setOnClickListener {
            Toast.makeText(
                this,
                "En Spotify: Ajustes ⚙️ -> Reproducción -> Activa 'Estado de transmisión del dispositivo'",
                Toast.LENGTH_LONG
            ).show()
            SpotifyController.relaunchSpotify(this)
        }

        btnBatteryOptimization.setOnClickListener {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } else {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(intent)
                } catch (e2: Exception) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            }
        }

        btnAccessibilityService.setOnClickListener {
            handleShizukuClick()
        }

        btnOpenSpotify.setOnClickListener {
            if (!SpotiGuardService.isRunning) {
                startSpotiGuardService()
            }
            val launched = SpotifyController.relaunchSpotify(this)
            if (!launched) {
                Toast.makeText(this, "No se pudo abrir Spotify automáticamente. Ábrelo desde tus aplicaciones.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleShizukuClick() {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "✅ Cierre silencioso ya está autorizado", Toast.LENGTH_SHORT).show()
                    updateRequirementsUI()
                } else {
                    Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
                }
                return
            }
        } catch (e: Exception) {
            // Shizuku no activo
        }

        showShizukuInfoDialog()
    }

    private fun showShizukuInfoDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚡ Cierre Silencioso 100% Sin PC")
            .setMessage(
                "Para saltar anuncios en cualquier lugar sin depender de un ordenador ni cables, Shizuku se activa por Wi-Fi directamente en tu móvil:\n\n" +
                "1. Ve a Ajustes del móvil ⚙️ -> Opciones de desarrollador.\n" +
                "2. Activa 'Depuración inalámbrica' (conectado a Wi-Fi).\n" +
                "3. Abre Shizuku, pulsa 'Emparejamiento' e introduce el código numérico.\n" +
                "4. En Shizuku, pulsa 'Iniciar'.\n\n" +
                "¡Y listo! Quedará activo de forma continua y permanente en tu teléfono sin cables."
            )
            .setPositiveButton("Entendido", null)

        val shizukuLaunchIntent = packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
        if (shizukuLaunchIntent != null) {
            builder.setNeutralButton("Abrir App Shizuku") { _, _ ->
                startActivity(shizukuLaunchIntent)
            }
        }

        builder.show()
    }

    private fun isBatteryOptimizedIgnored(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } else {
            true
        }
    }

    private fun isPrivilegedForceStopReady(): Boolean {
        if (SpotifyController.isRootAvailable()) return true
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    private fun updateRequirementsUI() {
        val prefs = getSharedPreferences("spotiguard_prefs", Context.MODE_PRIVATE)
        val broadcastConfigured = prefs.getBoolean("spotify_broadcast_enabled", false)
        val batteryOptimized = isBatteryOptimizedIgnored(this)
        val privilegedReady = isPrivilegedForceStopReady()

        btnSpotifySettings.visibility = if (broadcastConfigured) View.GONE else View.VISIBLE
        btnBatteryOptimization.visibility = if (batteryOptimized) View.GONE else View.VISIBLE
        btnAccessibilityService.visibility = if (privilegedReady) View.GONE else View.VISIBLE

        if (!privilegedReady) {
            try {
                if (Shizuku.pingBinder()) {
                    btnAccessibilityService.text = "⚡ Conceder Permiso Shizuku a SpotiGuard"
                } else {
                    btnAccessibilityService.text = "3. Activar Cierre Silencioso (Shizuku)"
                }
            } catch (e: Exception) {
                btnAccessibilityService.text = "3. Activar Cierre Silencioso (Shizuku)"
            }
        }

        if (broadcastConfigured && batteryOptimized && privilegedReady) {
            layoutRequirementsSuccess.visibility = View.VISIBLE
        } else {
            layoutRequirementsSuccess.visibility = View.GONE
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
