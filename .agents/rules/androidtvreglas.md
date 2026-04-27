---
trigger: model_decision
description: Reglas y estructura del proyecto android tv
---

1. Lenguaje y stack base

Te recomendaría sin dudar:

Lenguaje: Kotlin
UI: Jetpack Compose (también funciona en Android TV)
Arquitectura: MVVM o incluso mejor, Clean Architecture ligera
Networking: Retrofit + OkHttp
Parsing JSON: Kotlinx Serialization o Moshi
Imágenes: Coil (muy optimizado y simple en Compose)
Persistencia opcional: Room o DataStore (para cachear JSON o configuración)
🧱 2. Estructura de proyecto (clave)

Evita el típico proyecto “todo en activities”. Mejor separarlo en capas:

com.tuapp.tv
│
├── data
│   ├── remote
│   │   ├── api (Retrofit interfaces)
│   │   ├── dto (modelos del JSON)
│   │   └── repository_impl
│   │
│   ├── local (cache si lo necesitas)
│   │
│   └── mapper (DTO -> dominio)
│
├── domain
│   ├── model (modelos limpios)
│   ├── repository (interfaces)
│   └── usecase (casos de uso)
│
├── presentation
│   ├── ui
│   │   ├── screens
│   │   ├── components
│   │   └── theme
│   │
│   ├── viewmodel
│   └── state
│
└── core
    ├── utils
    ├── constants
    └── di (Hilt o Koin)
🔄 3. Flujo de datos (muy importante)

Tu caso encaja perfecto en este flujo:

App arranca
ViewModel llama a un UseCase
UseCase pide datos al Repository
Repository:
Descarga JSON desde URL
Lo transforma a modelos de dominio
ViewModel expone estado (Loading / Success / Error)
UI muestra imágenes en bucle
📦 4. Ejemplo de JSON (simplificado)
{
  "interval": 10,
  "items": [
    {
      "type": "image",
      "url": "https://..."
    },
    {
      "type": "image",
      "url": "https://..."
    }
  ]
}
🎞️ 5. Lógica de reproducción (clave del proyecto)

Esto es lo importante:

Usar un timer controlado desde ViewModel
O mejor: coroutines + Flow

Ejemplo conceptual:

while(true) {
    mostrarSiguienteImagen()
    delay(intervalo)
}

Pero bien hecho con StateFlow.

📺 6. Consideraciones específicas Android TV

Android TV NO es un móvil:

Navegación con mando (focus-based)
Usa androidx.tv (TV Compose)
Evita interacciones táctiles
UI minimalista (pantalla completa)

Muy útil:

TvLazyRow, TvLazyColumn
Focusable
🖼️ 7. Renderizado de imágenes

Con Coil:

AsyncImage(
    model = imageUrl,
    contentDescription = null,
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop
)
🌐 8. Gestión del JSON remoto

Cosas que debes contemplar:

Reintentos si falla la red
Cache local (por si no hay internet)
Actualización periódica (WorkManager opcional)
🧩 9. Casos de uso típicos

Crea use cases claros:

GetSlideshowConfigUseCase
GetNextItemUseCase
RefreshContentUseCase
⚠️ 10. Problemas típicos (y cómo evitarlos)
❌ Bloquear UI → usa coroutines
❌ Repetir lógica en UI → usa ViewModel
❌ No cachear → mala experiencia offline
❌ No controlar errores → app congelada
🧭 11. Versión simple vs escalable
MVP (rápido)
Una Activity
Un ViewModel
Retrofit + Coil
Timer simple
Escalable (recomendado)
Clean Architecture
DI (Hilt)
Flows
Cache + retry
Separación clara
💡 12. Bonus (muy útil en este tipo de apps)

Podrías añadir:

Soporte para vídeos (ExoPlayer)
Programación por horarios
Config remota avanzada
Logs remotos