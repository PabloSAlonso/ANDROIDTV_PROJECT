package net.emite.androidtv_project.core.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.io.File
import java.net.NetworkInterface

object DeviceUtils {

    private const val TAG = "DeviceUtils"

    /**
     * Intenta obtener la dirección MAC del dispositivo usando varios métodos.
     * 1. WifiManager (Requiere ACCESS_FINE_LOCATION en Android 8+)
     * 2. NetworkInterface
     * 3. Lectura de archivos de sistema
     */
    fun getMacAddress(context: Context): String {
        Log.d(TAG, "Iniciando proceso de obtención de MAC...")

        // Intento 1: WifiManager (Requiere permisos de Localización)
        try {
            val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (manager != null) {
                val info = manager.connectionInfo
                val mac = info.macAddress
                Log.d(TAG, "WifiManager reporta MAC: $mac")
                
                if (!mac.isNullOrEmpty() && mac != "02:00:00:00:00:00") {
                    val finalMac = mac.replace(":", "").lowercase()
                    Log.d(TAG, "MAC válida detectada vía WifiManager: $finalMac")
                    return finalMac
                } else {
                    Log.w(TAG, "WifiManager devolvió MAC restringida (02:00:00...) o nula.")
                }
            } else {
                Log.w(TAG, "WifiManager no está disponible en este dispositivo.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al usar WifiManager", e)
        }

        // Intento 2: API Estándar (puede fallar o devolver 02:00... en Android 10+)
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                if (networkInterface.name.equals("wlan0", ignoreCase = true) || 
                    networkInterface.name.equals("eth0", ignoreCase = true)) {
                    val macBytes = networkInterface.hardwareAddress
                    if (macBytes != null) {
                        val sb = StringBuilder()
                        for (b in macBytes) {
                            sb.append(String.format("%02X", b))
                        }
                        val detectedMac = sb.toString().lowercase()
                        if (detectedMac != "020000000000" && detectedMac.isNotEmpty()) {
                            Log.d(TAG, "MAC detectada (${networkInterface.name}) vía API nativa: $detectedMac")
                            return detectedMac
                        } else {
                            Log.w(TAG, "API nativa devolvió MAC restringida para ${networkInterface.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo MAC vía API nativa", e)
        }

        // Intento 3: Fallback leyendo archivos de sistema (común en TV Box/Ethernet)
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
        Log.w(TAG, "No se pudo detectar MAC real por ningún método, usando respaldo: $defaultMac")
        return defaultMac 
    }
}
