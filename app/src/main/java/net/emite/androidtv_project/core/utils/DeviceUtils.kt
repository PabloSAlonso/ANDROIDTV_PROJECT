package net.emite.androidtv_project.core.utils

import java.net.NetworkInterface
import java.util.*

object DeviceUtils {

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
                        return sb.toString().lowercase()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // MAC por defecto para el emulador o si falla la detección real
        return "dca632798fd0" 
    }
}
