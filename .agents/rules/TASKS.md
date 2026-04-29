---
trigger: model_decision
description: Tareas del proyecto en android tv
---

Visto lo visto, no podemos detectar por codigo la MAC del dispositivo, debemos no tratar de coger la MAC a partir de la IP, sino recoger los datos del dispositivo y hacer una funcion estilo: 

fun getMac(context: Context): String {
 val manager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
 val info = manager.connectionInfo
 return info.macAddress.toUpperCase()
}

your app must now have the ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permissions.

default value if you don't have grant access is 02:00:00:00:00:00.

con estos tips revisa que podamos acceder a la MAC y hacer la llamada correctamente, añade logs en el proceso para debuggin mas sencillo

