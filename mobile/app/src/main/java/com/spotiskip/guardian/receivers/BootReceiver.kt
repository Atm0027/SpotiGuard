package com.spotiskip.guardian.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.spotiskip.guardian.services.SpotiGuardService

/**
 * Inicia automáticamente SpotiGuardService al encender el teléfono móvil.
 * Permite que SpotiGuard esté siempre en espera en segundo plano sin intervención del usuario.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val serviceIntent = Intent(context, SpotiGuardService::class.java).apply {
                this.action = SpotiGuardService.ACTION_START
            }
            try {
                ContextCompat.startForegroundService(context, serviceIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
