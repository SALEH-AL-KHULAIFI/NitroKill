package com.isx3i.nitrokill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.isx3i.nitrokill.ui.screens.AboutScreen
import com.isx3i.nitrokill.ui.screens.AppManagerScreen
import com.isx3i.nitrokill.ui.screens.SpeedScreen
import com.isx3i.nitrokill.ui.theme.DarkBackground
import com.isx3i.nitrokill.ui.theme.NitroBlue
import com.isx3i.nitrokill.ui.theme.NitroKillTheme
import com.isx3i.nitrokill.ui.theme.NitroPurple

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            NitroKillTheme {
                NitroKillContent()
            }
        }
    }
}

private sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Speed : Screen(
        "speed",
        "السرعة",
        Icons.Filled.Speed
    )

    data object Apps : Screen(
        "apps",
        "التطبيقات",
        Icons.Filled.Bolt
    )

    data object About : Screen(
        "about",
        "حول",
        Icons.Filled.Info
    )
}

@Composable
private fun NitroKillContent() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Speed,
        Screen.Apps,
        Screen.About
    )

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF14141F)
            ) {
                val backStackEntry by navController
                    .currentBackStackEntryAsState()

                val currentRoute =
                    backStackEntry?.destination?.route

                items.forEach { screen ->

                    NavigationBarItem(
                        selected = currentRoute == screen.route,

                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(
                                    navController.graph.startDestinationId
                                ) {
                                    saveState = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        },

                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label
                            )
                        },

                        label = {
                            Text(screen.label)
                        },

                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NitroBlue,
                            selectedTextColor = NitroBlue,
                            indicatorColor = NitroPurple.copy(
                                alpha = 0.25f
                            ),
                            unselectedIconColor = Color(0xFF8888A0),
                            unselectedTextColor = Color(0xFF8888A0)
                        )
                    )
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkBackground,
                            Color(0xFF15152A)
                        )
                    )
                )
        ) {

            NavHost(
                navController = navController,
                startDestination = Screen.Speed.route
            ) {

                composable(Screen.Speed.route) {
                    SpeedScreen()
                }

                composable(Screen.Apps.route) {
                    AppManagerScreen()
                }

                composable(Screen.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}
