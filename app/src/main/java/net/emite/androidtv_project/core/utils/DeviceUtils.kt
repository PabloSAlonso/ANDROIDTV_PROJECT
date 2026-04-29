package net.emite.androidtv_project.core.utils

import android.util.Log
import java.io.File
import java.net.NetworkInterface

object DeviceUtils {

    private const val TAG = "DeviceUtils"

    /**
     * Intenta obtener la dirección MAC del dispositivo (wlan0 o eth0).
     * Si no es posible, lee los archivos de sistema como fallback.
     */
    fun getMacAddress(): String {
        // Intento 1: API Estándar (puede fallar o devolver 02:00... en Android 10+)
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                if (networkInterface.name.equals("wlan0", ignoreCase = true) || 
                    networkInterface.name.equals("eth0", ignoreCase = true)) {
                    val mac = networkInterface.hardwareAddress
                    if (mac != null) {
                        val sb = StringBuilder()
                        for (i in mac.indices) {
                            sb.append(String.format("%02X", mac[i]))
                        }
                        val detectedMac = sb.toString().lowercase()
                        if (detectedMac != "020000000000" && detectedMac.isNotEmpty()) {
                            Log.d(TAG, "MAC detectada (${networkInterface.name}): $detectedMac")
                            return detectedMac
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo MAC vía API", e)
        }

        // Intento 2: Fallback leyendo archivos de sistema (común en TV Box/Ethernet)
        val paths = listOf("/sys/class/net/eth0/address", "/sys/class/net/wlan0/address")
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists()) {
                    val macText = file.readText().trim()
                    val cleanedMac = macText.replace(":", "").lowercase()
                    if (cleanedMac.isNotEmpty() && cleanedMac != "020000000000") {
                        Log.d(TAG, "MAC detectada desde archivo $path: $cleanedMac")
                        return cleanedMac
                    }
                }
            } catch (e: Exception) {
                // Ignorar y probar el siguiente
            }
        }

        val defaultMac = "dca632798fd0"
        Log.w(TAG, "No se pudo detectar MAC real, usando respaldo: $defaultMac")
        return defaultMac 
    }
}
