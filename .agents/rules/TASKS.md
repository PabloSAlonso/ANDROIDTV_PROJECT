---
trigger: model_decision
description: Tareas del proyecto en android tv
---

Aqui están las tareas actuales a realizar

Plan de Implementación: Soporte de Vídeo con Reproducción Ininterrumpida
1. Gestión de Dependencias
Añadiremos las bibliotecas de Media3 (ExoPlayer), que es el estándar moderno para reproducción de medios en Android.

Archivo: gradle/libs.versions.toml
Añadir versión: media3 = "1.5.1"
Añadir librerías: androidx-media3-exoplayer y androidx-media3-ui.
Archivo: app/build.gradle.kts
Implementar las nuevas dependencias en el bloque dependencies.
2. Actualización del Modelo de Dominio
Modificaremos SlideshowItem para que la app sepa qué tipo de contenido está manejando.

Archivo: app/src/main/java/net/emite/androidtv_project/domain/model/SlideshowItem.kt
Añadir un enum MediaType { IMAGE, VIDEO }.
Añadir el campo type: MediaType a la clase SlideshowItem.
3. Detección Inteligente de Tipo de Media
Actualizaremos el repositorio para que identifique automáticamente si un archivo es vídeo o imagen basándose en su extensión.

Archivo: app/src/main/java/net/emite/androidtv_project/data/mapper/SlideshowMapper.kt (o donde se realice el mapeo).
Lógica: Si la extensión es .mp4, .mkv, .webm, etc., marcar como VIDEO. De lo contrario, IMAGE.
4. Refactorización del ViewModel (Lógica de Control)
Este es el punto clave. Cambiaremos el bucle de "espera fija" por uno de "espera inteligente".

Archivo: app/src/main/java/net/emite/androidtv_project/presentation/viewmodel/SlideshowViewModel.kt
Introducir un mecanismo de señalización (usando Channel<Unit>) para que la UI avise al ViewModel cuando un vídeo termina.
Nueva Lógica del Bucle:
Si es IMAGE: Esperar los segundos definidos en durationSeconds.
Si es VIDEO: Esperar a recibir la señal de finalización desde la UI (completionSignal.receive()).
5. Creación del Componente de Vídeo
Implementaremos un componente optimizado para TV que use ExoPlayer.

Nuevo Componente: app/src/main/java/net/emite/androidtv_project/presentation/components/VideoPlayer.kt
Uso de AndroidView para integrar PlayerView.
Configuración de ExoPlayer: Auto-reproducción, sin controles (o mínimos para TV), y modo de escalado ASPECT_RATIO_VIDEO_FILLED.
Listener de Eventos: Detectar STATE_ENDED para llamar al callback que notificará al ViewModel.
6. Actualización de la Pantalla de Slideshow
Modificaremos la UI para alternar entre AsyncImage y nuestro nuevo VideoPlayer.

Archivo: app/src/main/java/net/emite/androidtv_project/presentation/screens/SlideshowScreen.kt
Usar un bloque when(item.type) para decidir qué renderizar.
Pasar la función viewModel.onMediaVideoEnded() al componente VideoPlayer.

Próximos Pasos Sugeridos
Añadir las dependencias en los archivos Gradle.
Actualizar el modelo SlideshowItem para soportar el tipo de media.
Implementar el VideoPlayer con el listener de finalización.