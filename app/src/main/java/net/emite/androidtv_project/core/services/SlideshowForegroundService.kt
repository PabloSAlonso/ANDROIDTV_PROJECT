package net.emite.androidtv_project.core.services

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import net.emite.androidtv_project.MainActivity
import net.emite.androidtv_project.R
import net.emite.androidtv_project.core.di.BaseApplication

@AndroidEntryPoint
class SlideshowForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        launchMainActivity()

        // El servicio puede detenerse una vez lanzada la actividad, 
        // pero lo mantenemos un momento para asegurar el arranque.
        // Opcionalmente, se podría auto-detener tras unos segundos.
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, BaseApplication.CHANNEL_ID)
            .setContentTitle("Iniciando Slideshow")
            .setContentText("Preparando la reproducción automática...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // En caso de fallo (bloqueo por el fabricante), el usuario 
            // verá la notificación si el canal fuera de alta prioridad,
            // pero como es LOW para no molestar, confiamos en el arranque normal.
        }
        
        // Detener el servicio tras lanzar la activity para no consumir recursos
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
