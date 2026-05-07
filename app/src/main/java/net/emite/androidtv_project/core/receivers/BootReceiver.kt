package net.emite.androidtv_project.core.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import net.emite.androidtv_project.core.boot.BootLogger
import net.emite.androidtv_project.core.services.BootForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        
        BootLogger.logReceiver(context, action)
        
        // Lanzar el servicio de primer plano para manejar la secuencia de arranque
        val serviceIntent = Intent(context, BootForegroundService::class.java)
        try {
            ContextCompat.startForegroundService(context, serviceIntent)
            BootLogger.logService(context, "Lanzando BootForegroundService desde Receiver")
        } catch (e: Exception) {
            BootLogger.logDebug(context, "Error al lanzar servicio: ${e.message}")
        }
    }
}
