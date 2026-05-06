---
trigger: model_decision
description: Tareas del proyecto en android tv
---

Vamos a cambiar un poco la lógica de refresh: descargamos localmente el json, y comprobaremos cada 15 mins mediante una nueva llamada al json web si este es igual o ha cambiado, en caso de haber cambiado guardaremos el nuevo detectado, debe ser un método robusto que si falla por no haber conexión no 'cuelgue' la app ni moleste, aunque si debería haber algún tipo de aviso de falta de conexión para hacer un posible refresh necesario, por supuesto eliminamos el otro refresh de cada 6 horas.