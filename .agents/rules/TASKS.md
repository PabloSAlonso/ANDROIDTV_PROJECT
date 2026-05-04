---
trigger: model_decision
description: Tareas del proyecto en android tv
---

Añadir que la app no se 'duerma' en modo slide

Kotlin: window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

Dar aspecto ideal a la app en base a la foto de drawable: wappa_banner_tv.png (tonos rojizos y blancos) y utilizarla para decoración en la herramienta.

Gestionar sincronización de varios dispositivos, debe haber un contador de segundos global que determine despues de cuantos segundos totales del dia se ha empezado a ejecutar los slides, que sume cuantos segundos tarda segun las imagenes que muestra y la duracion de cada una y que haga esperar a las televisiones posteriores a las q esten ejecutandose para ejecutarse a la vez justo en el inicio del bucle de nuevo, tener en cuenta que no se van a mostrar todas las imagenes siempre, tenemos la URL: https://+instancia+.tegestiona.es/pantallas/sync/+codigo_unico que generamos en sharedpreferences, bien pues de ahi debemos tener en cuenta más campos, no solo los archivos de fotos y su id, sino los dias que se enseñan las fotos, el orden, y la duración, además de las horas:

recuerda el ejemplo_datos.txt de la raíz del proyecto, que es un ejemplo real de uno de esos archivos.