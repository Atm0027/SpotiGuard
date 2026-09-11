package com.spotiskip.guardian.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.spotiskip.guardian.utils.SpotifyController

/**
 * Servicio de Accesibilidad de SpotiGuard para cierre forzoso (Force-Stop).
 * Totalmente compatible con Samsung One UI, Pixel AOSP, Xiaomi MIUI/HyperOS, etc.
 *
 * Flujo:
 * 1. Al detectar anuncio, abre Ajustes de Spotify.
 * 2. Pulsa "Forzar cierre" / "Forzar detención" / "Force stop" en <150ms.
 * 3. Acepta el diálogo de confirmación ("Forzar cierre" / "Aceptar").
 * 4. Cierra Ajustes inmediatamente (Back) y relanza Spotify en limpio con Play.
 */
class SpotiGuardAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "SpotiGuardA11y"

        var instance: SpotiGuardAccessibilityService? = null
            private set

        fun isServiceRunning(): Boolean = instance != null

        private var isForceStopping = false
        private var clickedForceStopButton = false
        private var forceStopClickTime = 0L
        private var onKilledCallback: (() -> Unit)? = null
        private val mainHandler = Handler(Looper.getMainLooper())

        private val timeoutRunnable = Runnable {
            if (isForceStopping) {
                Log.w(TAG, "⏰ Timeout en proceso de forzar detención por accesibilidad.")
                instance?.performGlobalAction(GLOBAL_ACTION_BACK)
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

            // Despertar la pantalla brevemente si está apagada para permitir la interacción UI
            try {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (pm != null && !pm.isInteractive) {
                    val wl = pm.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        "spotiguard:wake_for_kill"
                    )
                    wl.acquire(3000)
                }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo adquirir WakeLock: ${e.message}")
            }

            isForceStopping = true
            clickedForceStopButton = false
            forceStopClickTime = 0L
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
            forceStopClickTime = 0L
            mainHandler.removeCallbacks(timeoutRunnable)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "Servicio de Accesibilidad SpotiGuard CONECTADO y LISTO")
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
                // Paso 1: Buscar y pulsar el botón "Forzar cierre" / "Forzar detención" / "Force stop"
                val forceStopNode = findForceStopButton(rootNode)
                if (forceStopNode != null) {
                    if (!forceStopNode.isEnabled) {
                        Log.i(TAG, "El botón Forzar detención/cierre está deshabilitado (Spotify ya estaba cerrado).")
                        completeKill()
                        return
                    }

                    val clicked = performClick(forceStopNode)
                    if (clicked) {
                        Log.i(TAG, "Botón 'Forzar cierre / detención' pulsado con éxito.")
                        clickedForceStopButton = true
                        forceStopClickTime = System.currentTimeMillis()
                    }
                }
            } else {
                // Paso 2: Buscar y pulsar la confirmación en el diálogo ("Forzar cierre", "Aceptar", "OK")
                // Esperar al menos 80ms para que el diálogo del sistema se dibuje y evitar falsos positivos
                if (System.currentTimeMillis() - forceStopClickTime < 80) {
                    return
                }

                val confirmNode = findConfirmDialogButton(rootNode)
                if (confirmNode != null) {
                    val clicked = performClick(confirmNode)
                    if (clicked) {
                        Log.i(TAG, "Diálogo de confirmación aceptado. Spotify liquidado por completo.")
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
        forceStopClickTime = 0L

        // Cerrar Ajustes de inmediato mediante botón Atrás global para que la pantalla no se quede abierta
        performGlobalAction(GLOBAL_ACTION_BACK)

        val cb = onKilledCallback
        onKilledCallback = null
        cb?.invoke()
    }

    private fun findForceStopButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // En Samsung One UI es "Forzar cierre"; en AOSP/Pixel "Forzar detención"; en inglés "Force stop"
        val candidateKeywords = listOf(
            "forzar cierre", "forzar detención", "forzar detencion", "force stop",
            "detener", "forzar parada", "cierre forzado", "arrêter", "beenden"
        )
        for (text in candidateKeywords) {
            val list = root.findAccessibilityNodeInfosByText(text)
            if (!list.isNullOrEmpty()) {
                for (node in list) {
                    val nText = node.text?.toString()?.lowercase() ?: ""
                    val nDesc = node.contentDescription?.toString()?.lowercase() ?: ""
                    if (candidateKeywords.any { nText.contains(it) || nDesc.contains(it) }) {
                        return node
                    }
                }
            }
        }

        // Búsqueda heurística por subcadenas clave
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (!queue.isEmpty()) {
            val current = queue.removeFirst()
            val text = current.text?.toString()?.lowercase() ?: ""
            val desc = current.contentDescription?.toString()?.lowercase() ?: ""
            if ((text.contains("cierre") || text.contains("deten") || text.contains("stop")) &&
                !text.contains("desinstalar") && !text.contains("abrir") && !text.contains("cancelar")) {
                if (current.isClickable || (current.parent?.isClickable == true)) {
                    return current
                }
            }
            if ((desc.contains("cierre") || desc.contains("deten") || desc.contains("stop")) &&
                !desc.contains("desinstalar") && !desc.contains("abrir") && !desc.contains("cancelar")) {
                if (current.isClickable || (current.parent?.isClickable == true)) {
                    return current
                }
            }
            for (i in 0 until current.childCount) {
                current.getChild(i)?.let { queue.add(it) }
            }
        }

        // IDs comunes de fabricantes (Samsung One UI, Pixel, Xiaomi)
        val candidateIds = listOf(
            "com.android.settings:id/forcestop_button",
            "com.android.settings:id/force_stop_button",
            "com.android.settings:id/button1_negative",
            "com.android.settings:id/right_button",
            "com.android.settings:id/button1",
            "com.samsung.android.settings:id/forcestop_button",
            "com.samsung.android.settings:id/force_stop_button",
            "com.samsung.android.settings:id/button1_negative",
            "com.samsung.android.settings:id/right_button",
            "com.samsung.android.settings:id/button1"
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
        // En Android estándar y Samsung One UI, el botón positivo de confirmación del diálogo siempre es android:id/button1
        val confirmIds = listOf(
            "android:id/button1",
            "com.android.settings:id/button1",
            "com.samsung.android.settings:id/button1"
        )
        for (id in confirmIds) {
            val list = root.findAccessibilityNodeInfosByViewId(id)
            if (!list.isNullOrEmpty()) {
                for (node in list) {
                    val t = node.text?.toString()?.lowercase() ?: ""
                    if (!t.contains("cancel") && !t.contains("cancelar")) {
                        return node
                    }
                }
            }
        }

        // Búsqueda por texto dentro de un diálogo (evitando los botones de la barra inferior de InstalledAppDetails)
        val confirmKeywords = listOf(
            "forzar cierre", "forzar detención", "forzar detencion",
            "aceptar", "ok", "force stop", "confirmar", "sí", "si"
        )
        for (text in confirmKeywords) {
            val list = root.findAccessibilityNodeInfosByText(text)
            if (!list.isNullOrEmpty()) {
                for (node in list) {
                    val nId = node.viewIdResourceName ?: ""
                    // Excluir el botón principal de la app info para no volverlo a pulsar
                    if (nId.contains("forcestop_button") || nId.contains("force_stop_button")) {
                        continue
                    }
                    val nText = node.text?.toString()?.lowercase()?.trim() ?: ""
                    if (confirmKeywords.any { nText == it || nText.contains(it) }) {
                        if (!nText.contains("cancel")) {
                            return node
                        }
                    }
                }
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
