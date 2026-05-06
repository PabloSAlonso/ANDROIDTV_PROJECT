package net.emite.androidtv_project.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import java.util.UUID

object DeviceUtils {

    private const val TAG = "DeviceUtils"
    private const val PREFS_NAME = "device_prefs"
    private const val PREF_DEVICE_UUID = "device_uuid"

    // Bug conocido en dispositivos antiguos (Android 2.2), buena práctica filtrarlo
    private const val INVALID_ANDROID_ID = "9774d56d682e549c"

    /**
     * Obtiene un identificador único persistente para este dispositivo.
     * Prioriza SharedPreferences por retrocompatibilidad. Si se pierden los datos,
     * utiliza el ANDROID_ID (persistente a borrado de datos/reinstalaciones).
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        Log.d(TAG, "Obteniendo ID único de dispositivo...")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. Intentar recuperar UUID existente (Retrocompatibilidad)
        val existingId = prefs.getString(PREF_DEVICE_UUID, null)
        if (!existingId.isNullOrEmpty()) {
            Log.d(TAG, "ID recuperado de SharedPreferences: $existingId")
            return existingId
        }

        // 2. Si no hay ID en SharedPreferences (datos borrados o nueva instalación), 
        // usar ANDROID_ID que es persistente
        var deviceId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo ANDROID_ID", e)
            null
        }

        // 3. Validar ANDROID_ID y Fallback
        if (deviceId.isNullOrEmpty() || deviceId == INVALID_ANDROID_ID) {
            Log.w(TAG, "ANDROID_ID inválido o no disponible. Usando randomUUID como fallback.")
            deviceId = UUID.randomUUID().toString()
        } else {
            Log.d(TAG, "ANDROID_ID obtenido correctamente: $deviceId")
        }

        // Guardar el nuevo ID en SharedPreferences para acelerar futuras lecturas
        prefs.edit().putString(PREF_DEVICE_UUID, deviceId).apply()

        return deviceId
    }
}
