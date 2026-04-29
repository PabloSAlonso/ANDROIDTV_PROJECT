---
trigger: model_decision
description: Tareas del proyecto en android tv
---

Todo en orden, nueevas tareas pendientes:

Features:

1. Implementar un aviso en pantalla durante el Slide de imagenes cuando se pulsa una vez el boton hacia atras del mando, para indicar que debe mantenerse pulsado unos segundos para ir hacia atrás.

2. En la pantalla de inicio, implementemos un menú desplegable mediante la pulsación de un icono, de momento con 2 opciones (en un futuro habrá más): salir de la app (dejandola abierta en segundo plano) y cerrar la app del todo

Fixes:

1. En nuestra red hemos configurado la mac del dispositivo TV y al iniciar la app dice que no detecta MAC real y usa la predeterminada, aun estando la TV conectada al ordenador, debemos revisar la lógica de conexión ya que la MAC que buscamos es la del dispositivo tv a la q estamos conectados, y simplemente deberemos hacer la llamada a la URL de tegestiona/sync con la MAC, si el texto recibido es Null (sucede con MACS que no existen) podemos simplemente poner un mensaje en pantalla de que la instancia podría ser incorrecta y que se debe contactar a soporte 