package net.emite.androidtv_project.core.boot

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

data class NetworkStatus(
    val connected: Boolean,
    val type: String?,
    val message: String
)

class BootNetworkChecker(private val context: Context) {

    fun checkNetwork(): NetworkStatus {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkStatus(false, null, "Sin red activa")
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus(false, null, "Sin capacidades de red")

        val isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        val type = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Móvil"
            else -> "Otros"
        }

        return NetworkStatus(
            connected = isConnected,
            type = type,
            message = if (isConnected) "Conectado" else "Sin conexión validada"
        )
    }
}
