---
trigger: model_decision
description: Tareas del proyecto en android tv
---

Aqui están las tareas actuales a realizar

1. ruta real de imagenes: https:// + instancia + .tegestiona.es/files/demo/t_pantallas_media/ + indice_foto + _ + nombre_foto

Implementar la selección correcta de imagenes mediante la URL especificada.

2. Eliminar LOGIN de la app, querremos simplemente determinar la instancia como parametro de la app y que esta haga las peticiones a Tegestiona que sean necesarias, he comprobado que las URLs de imagenes y de sync son accesibles sin credenciales, eliminar tanto el email como la contraseña de base de datos, solo necesitamos la instancia como parametro para determinar las URLs visitadas.