@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)

package com.example.accompanistIssue1544Demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.*
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.accompanistIssue1544Demo.ui.theme.AccompanistIssue1544DemoTheme
import com.google.accompanist.navigation.animation.*
import com.google.accompanist.navigation.animation.composable

/// TO RECREATE THE ISSUE
// Launch the app and press on "settings" tab within the BottomNavigation twice.

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccompanistIssue1544DemoTheme {
                AdaptiveMainNavigation()
            }
        }
    }

}

@Composable
private fun MainBottomNav(
    currentDestination: NavBackStackEntry?,
    onRouteSelected: (String) -> Unit,
) {
    NavigationBar {
        Screen.bottomNavDestinations.forEach { screen ->
            val isSelected = currentDestination.isSelected(screen)
            NavigationBarItem(
                icon = {
                    screen.icon?.let { Icon(it, contentDescription = screen.translatedString) }
                },
                label = {
                    Text(
                        text = screen.translatedString,
                        maxLines = 1,
                    )
                },
                selected = isSelected,
                onClick = { onRouteSelected(screen.route) }
            )
        }
    }
}

@Composable
internal fun AdaptiveMainNavigation() {
    val navController = rememberAnimatedNavController()
    val currentDestination by navController.currentBackStackEntryAsState()

    // Interestingly enough, the following code does not crash if i use a Scaffold with bottomBar here.
    // I don't use a scaffold as my app is adapting to large screen devices by using a navigation rail
    // and in this case i use a Row instead.

    Column {
        MainNavigation(
            modifier = Modifier.weight(1f),
            currentDestination = currentDestination,
            navController = navController,
        )
        MainBottomNav(
            currentDestination = currentDestination,
            onRouteSelected = { route: String ->
                navController.navigate(route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                }
            },
        )
    }
}

@Composable
internal fun MainNavigation(
    modifier: Modifier = Modifier,
    currentDestination: NavBackStackEntry?,
    navController: NavHostController,
) {
    val secondNavController = rememberAnimatedNavController()
    Scaffold(
        modifier = modifier,
        topBar = {

            // The reason we nest two NavHosts is that we want to share a single toolbar with
            // all the composable destinations.

            CenterAlignedTopAppBar(
                title = { Text("Test") },
                actions = {
                    if (currentDestination?.destination?.route == Screen.Second.route)
                        IconButton(onClick = { secondNavController.navigate(0) }) {
                            Icon(Icons.Default.Call, null)
                        }
                }
            )
        },
    ) { innerPadding ->
        AnimatedNavHost(
            navController = navController,
            startDestination = Screen.First.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            firstScreen()
            secondScreen(secondNavController)
        }
    }
}

internal fun NavGraphBuilder.firstScreen(
) {
    composable(Screen.First.route) {
        ScreenPlaceholder(text = "First")
    }
}

internal fun NavGraphBuilder.secondScreen(secondNavController: NavHostController) {
    composable(Screen.Second.route) {
        SecondNavHost(secondNavController)
    }
}

private val Screen.translatedString: String
    @Composable
    get() = when(route) {
        Screen.First.route -> "good"
        Screen.Second.route -> "problem"
        else -> ""
    }

private val Screen.icon: ImageVector?
    @Composable
    get() = when (route) {
        Screen.First.route -> Icons.Default.CheckCircle
        Screen.Second.route -> Icons.Default.Warning
        else -> null
    }

private fun NavBackStackEntry?.isSelected(screen: Screen): Boolean {
    if (this == null)
        return false
    return destination.route == screen.route
}

internal sealed class Screen(val route: String) {
    object First : Screen("first")
    object Second : Screen("second")
    companion object {
        val bottomNavDestinations by lazy(LazyThreadSafetyMode.NONE) {
            arrayOf(First, Second)
        }
    }
}

@Composable
internal fun SecondNavHost(
    settingsNavController: NavHostController,
    modifier: Modifier = Modifier,
) {
    AnimatedNavHost(
        navController = settingsNavController,
        startDestination = Settings.Settings.route,
        modifier = modifier,
    ) {
        composable(Settings.Settings.route) {
            ScreenPlaceholder("SettingsScreen")
        }
    }
}

internal object Settings : Screen("settings") {
    object Settings : Screen("settings/settings")
}

@Composable
fun ScreenPlaceholder(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Text(text)
    }
}