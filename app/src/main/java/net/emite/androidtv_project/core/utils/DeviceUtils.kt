package net.emite.androidtv_project.core.utils

import android.content.Context
import android.util.Log
import java.util.UUID

object DeviceUtils {

    private const val TAG = "DeviceUtils"
    private const val PREFS_NAME = "device_prefs"
    private const val PREF_DEVICE_UUID = "device_uuid"

    /**
     * Obtiene o genera un UUID único para este dispositivo.
     * El UUID se genera la primera vez y se almacena en SharedPreferences
     * para que persista incluso si se borra la configuración de la instancia.
     */
    fun getDeviceId(context: Context): String {
        Log.d(TAG, "Obteniendo ID único de dispositivo (UUID)...")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var uuid = prefs.getString(PREF_DEVICE_UUID, null)

        if (uuid.isNullOrEmpty()) {
            uuid = UUID.randomUUID().toString()
            prefs.edit().putString(PREF_DEVICE_UUID, uuid).apply()
            Log.d(TAG, "Nuevo UUID generado y guardado: $uuid")
        } else {
            Log.d(TAG, "UUID recuperado de SharedPreferences: $uuid")
        }

        return uuid
    }
}
