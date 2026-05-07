package net.emite.androidtv_project.core.boot

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BootLogger {
    private const val TAG = "BOOT_DEBUG"
    private const val LOG_FILE_NAME = "boot_logs.txt"
    private const val MAX_LOG_LINES = 200

    fun logReceiver(context: Context, action: String) {
        val message = "BOOT_RECEIVER: Acción [$action] recibida. " +
                "Device: ${Build.MANUFACTURER} ${Build.MODEL}, API: ${Build.VERSION.SDK_INT}"
        log(context, message)
    }

    fun logNetwork(context: Context, attempt: Int, connected: Boolean, networkType: String?) {
        val message = "BOOT_NETWORK: Intento $attempt/6. Conectado: $connected, Tipo: ${networkType ?: "Ninguno"}"
        log(context, message)
    }

    fun logActivity(context: Context, success: Boolean, error: Throwable? = null) {
        val message = if (success) {
            "BOOT_ACTIVITY: App lanzada con éxito."
        } else {
            "BOOT_ACTIVITY: Error al lanzar la app: ${error?.message}"
        }
        log(context, message, isError = !success)
    }

    fun logService(context: Context, event: String) {
        log(context, "BOOT_SERVICE: $event")
    }

    fun logDebug(context: Context, message: String) {
        log(context, "BOOT_DEBUG: $message")
    }

    fun getRecentLogs(context: Context): List<String> {
        return readAllLines(context)
    }

    fun clearLogs(context: Context) {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists()) file.delete()
            Log.d(TAG, "Logs de boot borrados.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al borrar logs: ${e.message}")
        }
    }

    private fun log(context: Context, message: String, isError: Boolean = false) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "[$timestamp] $message"

        if (isError) Log.e(TAG, logEntry) else Log.d(TAG, logEntry)

        writeToFile(context, logEntry)
    }

    private fun writeToFile(context: Context, entry: String) {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            val lines = if (file.exists()) file.readLines().takeLast(MAX_LOG_LINES - 1) else emptyList()
            val updatedLines = lines + entry
            file.writeText(updatedLines.joinToString("\n"))
        } catch (e: Exception) {
            Log.e(TAG, "Error escribiendo log a archivo: ${e.message}")
        }
    }

    private fun readAllLines(context: Context): List<String> {
        return try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists()) file.readLines().reversed() else emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error leyendo logs de archivo: ${e.message}")
            emptyList()
        }
    }
}
