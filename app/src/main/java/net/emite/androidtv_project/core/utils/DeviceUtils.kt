package net.emite.androidtv_project.core.utils

import android.util.Log
import java.net.NetworkInterface
import java.util.*

object DeviceUtils {

    private const val TAG = "DeviceUtils"

    /**
     * Intenta obtener la dirección MAC del dispositivo (wlan0 o eth0).
     * Si no es posible, devuelve un ID por defecto para pruebas.
     */
    fun getMacAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                if (networkInterface.name.equals("wlan0", ignoreCase = true) || 
                    networkInterface.name.equals("eth0", ignoreCase = true)) {
                    val mac = networkInterface.hardwareAddress
                    if (mac != null) {
                        val sb = StringBuilder()
                        for (i in mac.indices) {
                            sb.append(String.format("%02X%s", mac[i], if (i < mac.size - 1) "" else ""))
                        }
                        val detectedMac = sb.toString().lowercase()
                        Log.d(TAG, "MAC detectada (${networkInterface.name}): $detectedMac")
                        return detectedMac
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo MAC", e)
        }
        val defaultMac = "dca632798fd0"
        Log.w(TAG, "No se pudo detectar MAC real, usando respaldo: $defaultMac")
        return defaultMac 
    }
}
