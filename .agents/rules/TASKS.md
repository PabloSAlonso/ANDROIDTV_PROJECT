---
trigger: model_decision
description: Tareas del proyecto en android tv
---

Cancelamos tema de MAC, ya que esta capado por android studio, vamos a utilizar el siguiente método: 

import java.util.UUID

// Generar un UUID único aleatorio (Versión 4)
val uniqueID: String = UUID.randomUUID().toString()

println("UUID Generado: $uniqueID")
// Ejemplo de salida: "550e8400-e29b-41d4-a716-446655440000"

Generaremos el código solo la primera vez que se ejecute la app en cada dispositivo, se guardará en la base de datos local y utilizará la misma URL acordada anteriormente pero cambiando la MAC por el código, externamente a la app nos encargaremos del backend para que esté sincronizado y tenga sentido, tu encargate primero de enlazar este código aleatorio generado y guardado con la URL destino.

