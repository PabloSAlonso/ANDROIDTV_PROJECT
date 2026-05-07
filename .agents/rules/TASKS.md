---
trigger: model_decision
description: Tareas del proyecto en android tv
---

Puedes usar este bloque directamente como TASKS.md para que una IA implemente todo el sistema de autoarranque y debugging en Android TV:

# TASKS — Android TV AutoStart + Boot Debug System

## Objetivo

Implementar un sistema robusto de autoarranque para Android TV que:

- detecte el arranque del sistema
- permita abrir automáticamente la app tras encender la TV
- soporte Android TV / Google TV / AOSP
- implemente logging completo para debugging
- permita verificar fácilmente si el problema es:
  - receiver no ejecutado
  - broadcast no recibido
  - bloqueo de Android
  - problema de red
  - problema de Activity launch

---

# 1. Crear BootReceiver

Crear:

```kotlin
BootReceiver.kt

Debe extender:

BroadcastReceiver

Debe escuchar:

Intent.ACTION_BOOT_COMPLETED
Intent.ACTION_LOCKED_BOOT_COMPLETED
"android.intent.action.QUICKBOOT_POWERON"
2. Configuración Manifest

Añadir permisos:

<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>

Registrar receiver dentro de <application>:

<receiver
    android:name=".BootReceiver"
    android:enabled="true"
    android:exported="true">

    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED"/>
        <action android:name="android.intent.action.LOCKED_BOOT_COMPLETED"/>
        <action android:name="android.intent.action.QUICKBOOT_POWERON"/>
    </intent-filter>

</receiver>

Verificar compatibilidad Android 12+ (exported=true obligatorio).

3. Sistema de Logging Completo

Crear helper:

BootLogger.kt

Objetivos:

logs visibles en Logcat
logs persistentes en archivo
timestamps
guardar últimos eventos de boot

Tags principales:

BOOT_RECEIVER
BOOT_NETWORK
BOOT_ACTIVITY
BOOT_SERVICE
BOOT_DEBUG

Implementar:

Log.d(...)
Log.e(...)

Y además persistencia en:

/files/boot_logs.txt
4. Loggear TODOS los eventos importantes

Registrar:

receiver recibido
action recibida
timestamp
estado de red
intento de lanzar Activity
éxito/error launch
excepciones completas
tiempo desde boot hasta launch
5. Delay Inteligente

NO abrir Activity inmediatamente.

Implementar delay configurable:

DEFAULT_BOOT_DELAY = 15000L

(15 segundos)

Usar:

Handler(Looper.getMainLooper()).postDelayed
6. Verificación de Red

Antes de abrir Activity:

Comprobar:

activeNetwork
internet capability
network validated

Usar:

ConnectivityManager
NetworkCapabilities

Loggear:

conectado/no conectado
tipo de red
tiempo de espera
7. Sistema Retry

Si no hay internet:

reintentar cada 10 segundos
máximo 6 intentos

Loggear cada retry.

8. Lanzamiento Seguro de Activity

Lanzar MainActivity usando:

Intent.FLAG_ACTIVITY_NEW_TASK

y:

context.packageManager
    .getLaunchIntentForPackage(context.packageName)

Capturar excepciones.

Loggear:

launch success
launch failure
exception stacktrace
9. Foreground Service Opcional

Implementar:

BootForegroundService

Objetivo:

mantener proceso vivo tras boot
evitar restricciones Android TV

Debe:

iniciarse desde receiver
poder lanzar Activity posteriormente
10. Pantalla Debug Interna

Crear pantalla:

Boot Debug Screen

Mostrando:

último boot detectado
última action recibida
estado de red
tiempo boot → launch
logs recientes
errores
11. Compatibilidad Android TV

Añadir detección:

Android TV
Google TV
AOSP TV box

Loggear fabricante/modelo:

Build.MANUFACTURER
Build.MODEL
Build.VERSION.SDK_INT

12. Herramientas Debug ADB

Documentar comandos:

Simular boot broadcast
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
Ver logs
adb logcat | grep BOOT
Ver receiver registrado
adb shell dumpsys package <package_name>
13. Diagnóstico Automático

Crear diagnóstico que detecte:

receiver jamás ejecutado
broadcast no emitido por TV
Activity bloqueada
app en stopped state
red no disponible
Android background restriction

Mostrar resultado en UI debug.

14. Compatibilidad con Deep Sleep TVs

Investigar comportamiento:

TVs que suspenden en vez de reboot
quick boot
fake shutdown

Implementar listeners adicionales si es necesario.

15. Objetivo Final

Conseguir comportamiento:

Encender TV
→ esperar sistema
→ verificar red
→ lanzar app automáticamente
→ registrar todo el proceso

Con capacidad completa de debugging y diagnóstico.
