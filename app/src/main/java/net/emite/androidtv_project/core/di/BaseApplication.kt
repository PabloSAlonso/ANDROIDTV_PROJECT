package net.emite.androidtv_project.core.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase base de la aplicación que inicializa Hilt para la inyección de dependencias.
 */
@HiltAndroidApp
class BaseApplication : Application()

