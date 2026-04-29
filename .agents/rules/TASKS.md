---
trigger: model_decision
description: Tareas del proyecto en android tv
---

Añadir que la app no se 'duerma' en modo slide

Kotlin: window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

Dar aspecto ideal a la app en base a la foto de drawable: wappa_banner_tv.png (tonos rojizos y blancos) y utilizarla para decoración en la herramienta.

Gestionar sincronización de varios dispositivos, debe haber un contador de segundos global que determine despues de cuantos segundos totales del dia se ha empezado a ejecutar los slides, que sume cuantos segundos tarda segun las imagenes que muestra y la duracion de cada una y que haga esperar a las televisiones posteriores a las q esten ejecutandose para ejecutarse a la vez justo en el inicio del bucle de nuevo.
