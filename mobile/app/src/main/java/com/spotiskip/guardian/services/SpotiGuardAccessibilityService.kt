package com.spotiskip.guardian.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.spotiskip.guardian.utils.SpotifyController

/**
 * Servicio de Accesibilidad para SpotiGuard.
 * Automatiza el cierre forzoso (Force Stop) de Spotify en Android 14+ donde
 * killBackgroundProcesses fue revocado por el sistema operativo.
 *
 * Flujo:
 * 1. SpotiGuard solicita forzar detención cuando detecta un anuncio.
 * 2. Se abre instantáneamente la pantalla de Ajustes de Spotify.
 * 3. Este servicio pulsa "Forzar detención" y "Aceptar" en menos de 200ms.
 * 4. Invoca el callback para relanzar Spotify y continuar la música limpia sin anuncios.
 */
class SpotiGuardAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "SpotiGuardA11y"

        var instance: SpotiGuardAccessibilityService? = null
            private set

        fun isServiceRunning(): Boolean = instance != null

        private var isForceStopping = false
        private var clickedForceStopButton = false
        private var onKilledCallback: (() -> Unit)? = null
        private val mainHandler = Handler(Looper.getMainLooper())

        private val timeoutRunnable = Runnable {
            if (isForceStopping) {
                Log.w(TAG, "⏰ Timeout en proceso de forzar detención por accesibilidad.")
                resetState()
                onKilledCallback?.invoke()
                onKilledCallback = null
            }
        }

        fun requestForceStop(context: Context, onKilled: () -> Unit) {
            val service = instance
            if (service == null) {
                Log.w(TAG, "SpotiGuardAccessibilityService no está conectado aún.")
                onKilled()
                return
            }

            isForceStopping = true
            clickedForceStopButton = false
            onKilledCallback = onKilled

            mainHandler.removeCallbacks(timeoutRunnable)
            mainHandler.postDelayed(timeoutRunnable, 2500)

            val pkg = if (SpotifyController.isSpotifyInstalled(context)) {
                SpotifyController.SPOTIFY_PACKAGE
            } else {
                SpotifyController.SPOTIFY_LITE_PACKAGE
            }

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$pkg")
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                )
            }
            context.startActivity(intent)
        }

        private fun resetState() {
            isForceStopping = false
            clickedForceStopButton = false
            mainHandler.removeCallbacks(timeoutRunnable)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "Servicio de Accesibilidad SpotiGuard CONECTADO")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
        resetState()
        Log.i(TAG, "Servicio de Accesibilidad SpotiGuard DESTRUIDO")
    }

    override fun onInterrupt() {
        Log.w(TAG, "Servicio de Accesibilidad interrumpido")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isForceStopping || event == null) return

        val pkgName = event.packageName?.toString() ?: ""
        if (!pkgName.contains("settings", ignoreCase = true)) {
            return
        }

        val rootNode = rootInActiveWindow ?: return

        try {
            if (!clickedForceStopButton) {
                // Paso 1: Buscar y pulsar el botón "Forzar detención" / "Force stop"
                val forceStopNode = findForceStopButton(rootNode)
                if (forceStopNode != null) {
                    if (!forceStopNode.isEnabled) {
                        Log.i(TAG, "El botón Forzar detención está deshabilitado (Spotify ya estaba detenido).")
                        completeKill()
                        return
                    }

                    val clicked = performClick(forceStopNode)
                    if (clicked) {
                        Log.i(TAG, "Botón 'Forzar detención' pulsado con éxito.")
                        clickedForceStopButton = true
                    }
                }
            } else {
                // Paso 2: Buscar y pulsar la confirmación en el diálogo ("Aceptar", "OK", "Forzar detención")
                val confirmNode = findConfirmDialogButton(rootNode)
                if (confirmNode != null) {
                    val clicked = performClick(confirmNode)
                    if (clicked) {
                        Log.i(TAG, "Diálogo de confirmación aceptado. Spotify forzado a cerrar.")
                        completeKill()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando evento de accesibilidad: ${e.message}", e)
        }
    }

    private fun completeKill() {
        mainHandler.removeCallbacks(timeoutRunnable)
        isForceStopping = false
        clickedForceStopButton = false
        val cb = onKilledCallback
        onKilledCallback = null
        cb?.invoke()
    }

    private fun findForceStopButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val candidateTexts = listOf(
            "forzar detención", "forzar detencion", "force stop",
            "detener", "forzar parada", "arrêter", "beenden"
        )
        for (text in candidateTexts) {
            val list = root.findAccessibilityNodeInfosByText(text)
            if (!list.isNullOrEmpty()) {
                for (node in list) {
                    val nText = node.text?.toString()?.lowercase() ?: ""
                    val nDesc = node.contentDescription?.toString()?.lowercase() ?: ""
                    if (candidateTexts.any { nText.contains(it) || nDesc.contains(it) }) {
                        return node
                    }
                }
            }
        }

        val candidateIds = listOf(
            "com.android.settings:id/button1_negative",
            "com.android.settings:id/right_button",
            "com.android.settings:id/button1",
            "com.android.settings:id/force_stop_button"
        )
        for (id in candidateIds) {
            val list = root.findAccessibilityNodeInfosByViewId(id)
            if (!list.isNullOrEmpty()) {
                return list[0]
            }
        }

        return null
    }

    private fun findConfirmDialogButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val confirmTexts = listOf("aceptar", "ok", "forzar detención", "forzar detencion", "force stop")
        for (text in confirmTexts) {
            val list = root.findAccessibilityNodeInfosByText(text)
            if (!list.isNullOrEmpty()) {
                for (node in list) {
                    val nText = node.text?.toString()?.lowercase() ?: ""
                    if (confirmTexts.any { nText == it }) {
                        return node
                    }
                }
            }
        }

        val confirmIds = listOf(
            "android:id/button1",
            "com.android.settings:id/button1"
        )
        for (id in confirmIds) {
            val list = root.findAccessibilityNodeInfosByViewId(id)
            if (!list.isNullOrEmpty()) {
                return list[0]
            }
        }

        return null
    }

    private fun performClick(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        var parent = node.parent
        while (parent != null) {
            if (parent.isClickable) {
                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            parent = parent.parent
        }
        return false
    }
}
