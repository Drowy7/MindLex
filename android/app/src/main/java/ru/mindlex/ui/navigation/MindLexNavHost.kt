package ru.mindlex.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.mindlex.data.local.TokenState
import ru.mindlex.feature.auth.AuthScreen
import ru.mindlex.feature.dashboard.DashboardScreen
import ru.mindlex.feature.practice.PracticeScreen
import ru.mindlex.feature.settings.SettingsScreen
import ru.mindlex.feature.words.WordsScreen
import ru.mindlex.ui.components.LoadingView
import ru.mindlex.ui.components.MindLexBackground

/**
 * Корневой граф навигации.
 *
 * <p>Экран входа показывается, пока в хранилище нет токена. Как только
 * пользователь вошёл (или токен найден при запуске), происходит переход на главный экран.</p>
 */
@Composable
fun MindLexNavHost(
    navController: NavHostController = rememberNavController(),
    rootViewModel: RootViewModel = hiltViewModel(),
) {
    val authState by rootViewModel.authState.collectAsStateWithLifecycle()

    if (authState == TokenState.UNKNOWN) {
        MindLexBackground {
            Box(modifier = Modifier.fillMaxSize()) {
                LoadingView(message = "Проверяем вход…")
            }
        }
        return
    }

    LaunchedEffect(authState) {
        if (authState == TokenState.LOGGED_IN && navController.currentDestination?.route == Routes.AUTH) {
            navController.navigate(Routes.DASHBOARD) {
                popUpTo(Routes.AUTH) { inclusive = true }
            }
        }
        if (authState == TokenState.LOGGED_OUT && navController.currentDestination?.route != Routes.AUTH) {
            navController.navigate(Routes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.AUTH,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(320),
            ) + fadeIn(tween(320))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(320),
            ) + fadeOut(tween(220))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(320),
            ) + fadeIn(tween(320))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(320),
            ) + fadeOut(tween(220))
        },
    ) {
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthenticated = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onMechanicClick = { mechanic -> navController.navigate(Routes.practice(mechanic)) },
                onOpenWords = { navController.navigate(Routes.WORDS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.WORDS) {
            WordsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.PRACTICE,
            arguments = listOf(navArgument(Routes.ARG_MECHANIC) { type = NavType.StringType }),
        ) { entry ->
            PracticeScreen(
                mechanic = entry.arguments?.getString(Routes.ARG_MECHANIC).orEmpty(),
                onExit = { navController.popBackStack() },
            )
        }
    }
}
