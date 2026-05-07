package net.emite.androidtv_project.core.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import net.emite.androidtv_project.core.services.SlideshowForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            val serviceIntent = Intent(context, SlideshowForegroundService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
