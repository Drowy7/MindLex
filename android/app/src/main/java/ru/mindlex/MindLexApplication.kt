package ru.mindlex

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Точка входа приложения. Hilt создаёт граф зависимостей,
 * TokenStore сам поднимает сохранённый токен при создании.
 */
@HiltAndroidApp
class MindLexApplication : Application()
