package net.emite.androidtv_project.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.emite.androidtv_project.R
import net.emite.androidtv_project.core.boot.BootLogger
import net.emite.androidtv_project.core.boot.BootNetworkChecker

class BootForegroundService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var networkChecker: BootNetworkChecker

    companion object {
        const val CHANNEL_ID = "boot_service_channel"
        const val NOTIFICATION_ID = 100
        const val DEFAULT_BOOT_DELAY = 15_000L
        const val RETRY_INTERVAL = 10_000L
        const val MAX_RETRIES = 6
    }

    override fun onCreate() {
        super.onCreate()
        networkChecker = BootNetworkChecker(this)
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Iniciando sistema..."))

        serviceScope.launch {
            bootSequence()
        }

        return START_NOT_STICKY
    }

    private suspend fun bootSequence() {
        BootLogger.logService(this, "Iniciando secuencia de boot (delay 15s)")
        
        // 1. Espera inicial para que el sistema se estabilice
        delay(DEFAULT_BOOT_DELAY)

        // 2. Verificación de red con retries
        var retries = 0
        var connected = false
        
        while (retries < MAX_RETRIES && !connected) {
            val status = networkChecker.checkNetwork()
            connected = status.connected
            BootLogger.logNetwork(this, retries + 1, connected, status.type)
            
            if (!connected) {
                retries++
                if (retries < MAX_RETRIES) {
                    delay(RETRY_INTERVAL)
                }
            }
        }

        if (!connected) {
            BootLogger.logDebug(this, "Se agotaron los reintentos de red. Intentando abrir app de todos modos.")
        }

        // 3. Lanzar la Activity
        launchApp()
    }

    private fun launchApp() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            try {
                startActivity(launchIntent)
                BootLogger.logActivity(this, true)
            } catch (e: Exception) {
                BootLogger.logActivity(this, false, e)
            }
        } else {
            BootLogger.logDebug(this, "No se encontró Launch Intent para el paquete.")
        }

        // Finalizar el servicio tras el intento
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Arranque del Sistema",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maneja el auto-inicio de la aplicación tras el encendido"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wappa TV")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
