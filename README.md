# Android TV Media Player

Una aplicación para Android TV diseñada para reproducir presentaciones multimedia (diapositivas) de imágenes y vídeos de forma dinámica y continua. La aplicación está optimizada para pantallas grandes y ofrece transiciones suaves y una experiencia de usuario ininterrumpida.

## Funcionamiento de la App

La aplicación funciona como un reproductor de cartelería digital o presentaciones (slideshow) específico para dispositivos Android TV:

1. **Configuración Inicial (Setup):** El sistema utiliza una configuración basada en instancias (sin necesidad de autenticación de usuario tradicional). Al iniciar, la aplicación obtiene la configuración del dispositivo desde una API y la almacena localmente.
2. **Ciclo de Presentación (Slideshow):** Reproduce en bucle el contenido multimedia (imágenes y vídeos) asignado a la pantalla o dispositivo.
3. **Transiciones Suaves:** Cuenta con transiciones visuales optimizadas (como fundidos en negro o cross-fade) entre diapositivas para eliminar parpadeos molestos o pantallas blancas, mejorando la calidad visual.
4. **Reproducción de Vídeo Integrada:** Los vídeos se reproducen en su totalidad sin ser interrumpidos por el temporizador general del pase de diapositivas, integrando el ciclo de vida del vídeo con la orquestación del reproductor.
5. **Funcionamiento Offline-First:** Los datos y la configuración principal (y el contenido cacheado) se apoyan en una base de datos local para mantener la estabilidad frente a cortes de red.

## 🛠 Tecnologías Utilizadas

El proyecto está desarrollado con las últimas tecnologías y patrones recomendados para el desarrollo moderno en Android:

* **Lenguaje:** [Kotlin](https://kotlinlang.org/)
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) y **Android TV Compose** (`androidx.tv`) para una interfaz declarativa y adaptada a la navegación con mando a distancia (D-Pad).
* **Arquitectura:** MVVM (Model-View-ViewModel) + Clean Architecture (Capas de Presentación, Dominio y Datos).
* **Inyección de Dependencias:** [Dagger Hilt](https://dagger.dev/hilt/).
* **Programación Asíncrona:** Kotlin Coroutines y Flow.
* **Red y API:** 
  * [Retrofit](https://square.github.io/retrofit/) y OkHttp para peticiones HTTP.
  * [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) para el parseo de JSON.
* **Base de Datos Local:** [Room](https://developer.android.com/training/data-storage/room) para la persistencia de datos (configuraciones, pantallas, etc.).
* **Carga de Imágenes:** [Coil](https://coil-kt.github.io/coil/compose/) (optimizado para Compose).
* **Reproducción de Vídeo:** [Media3 / ExoPlayer](https://developer.android.com/media/media3) para un control preciso y robusto del playback de vídeo.

## Pruebas Realizadas

Durante el desarrollo de esta aplicación se ha puesto especial foco en la experiencia visual y la estabilidad de la reproducción:

* **Pruebas de Transición Visual:** Se verificó rigurosamente la eliminación del "efecto flash" (pantallazo blanco) durante el cambio dinámico entre distintos tipos de media (de imagen a imagen, de imagen a vídeo, etc.), confirmando que el *fade-through-black* y *cross-fade* se ejecutan sin artefactos visuales.
* **Pruebas de Ciclo de Vida de Vídeo:** Se han realizado pruebas de integración para asegurar que la duración dinámica de los vídeos se respeta. El temporizador de la diapositiva se detiene/adapta para permitir que ExoPlayer termine la reproducción completa del vídeo antes de avanzar al siguiente recurso, evitando cortes abruptos.
* **Pruebas de Orquestación y Estado:** Validación del flujo de configuración inicial (Setup), persistencia de datos en Room y manejo de estados de UI mediante ViewModels en la capa de presentación (SlideshowViewModel).
* **Pruebas de Sincronización:** Comprobación del correcto parseo de la configuración remota (sin autenticación tradicional) y la formación dinámica de las URLs de los recursos (ej. `/files/{folder}/t_pantallas_media/{id}_{filename}`).

## Estructura del Proyecto

El proyecto sigue una estructura modular orientada a funcionalidades dentro del paquete `net.emite.androidtv_project`:
- `data`: Implementación de repositorios, base de datos (Room `entity`, `dao`) y fuentes de datos (API).
- `domain`: Modelos de negocio (ej. `Config.kt`) y casos de uso.
- `presentation`: Componentes de UI (Compose `screens`), `ViewModels` (ej. `SlideshowViewModel`) y estados.

---
*Desarrollado para proporcionar una experiencia multimedia fluida y profesional en entornos Android TV.*
