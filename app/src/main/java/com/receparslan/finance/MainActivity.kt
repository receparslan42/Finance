package com.receparslan.finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import com.receparslan.finance.ui.Screen
import com.receparslan.finance.ui.navigationItems
import com.receparslan.finance.ui.screens.DetailScreen
import com.receparslan.finance.ui.screens.FavouritesScreen
import com.receparslan.finance.ui.screens.GainerScreen
import com.receparslan.finance.ui.screens.HomeScreen
import com.receparslan.finance.ui.screens.LoserScreen
import com.receparslan.finance.ui.screens.SearchScreen
import com.receparslan.finance.ui.theme.FinanceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    // Set up the navigation graph
                    val graph = navController.createGraph(startDestination = Screen.Home.rout) {
                        composable(route = Screen.Home.rout) { HomeScreen(navController) }

                        composable(route = Screen.Gainer.rout) { GainerScreen(navController) }

                        composable(route = Screen.Loser.rout) { LoserScreen(navController) }

                        composable(route = Screen.Favourites.rout) { FavouritesScreen(navController) }

                        composable(route = Screen.Search.rout) { SearchScreen(navController) }

                        composable(
                            route = "${Screen.Detail.rout}/{cryptoId}",
                            arguments = listOf(
                                navArgument("cryptoId") {
                                    type = NavType.StringType
                                }
                            )
                        ) { DetailScreen(navController) }
                    }

                    // Set up the NavHost with the graph
                    NavHost(
                        navController = navController,
                        graph = graph,
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                    )
                }
            }
        }
    }
}

// This composable function defines the bottom navigation bar with a background image and navigation items.
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom navigation bar with a background image
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.navigation_bar),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentDescription = "Bottom Navigation Bar",
            contentScale = ContentScale.FillWidth
        )

        Image(
            painter = painterResource(R.drawable.home_icon_circle),
            contentDescription = "Home_screen",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 7.dp, y = 14.dp)
                .size(200.dp)
        )

        // Bottom navigation bar items
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier
                .offset(x = 6.dp, y = 50.dp)
                .matchParentSize()
        ) {
            navigationItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    modifier = Modifier.offset(y = if (index == 2) (-36).dp else 0.dp),
                    selected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = false
                            restoreState = false
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(if (currentRoute == item.route) item.filledIcon else item.icon),
                            contentDescription = item.title,
                            modifier = Modifier.size(35.dp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = MaterialTheme.colorScheme.onTertiary,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}