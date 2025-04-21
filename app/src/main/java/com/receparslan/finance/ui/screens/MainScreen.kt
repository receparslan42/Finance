package com.receparslan.finance.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.receparslan.finance.R
import com.receparslan.finance.ui.Screen
import com.receparslan.finance.ui.navigationItems
import com.receparslan.finance.viewmodel.CryptocurrencyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CryptocurrencyViewModel) {
    val navController = rememberNavController()

    // State to manage the refreshing state
    val isRefreshing = rememberSaveable { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        PullToRefreshBox(
            state = refreshState,
            isRefreshing = isRefreshing.value,
            onRefresh = {
                isRefreshing.value = true
                coroutineScope.launch {
                    viewModel.refreshCryptocurrencies()
                    delay(1500)
                    isRefreshing.value = false
                }
            }
        ) {
            // Set up the navigation graph
            val graph = navController.createGraph(startDestination = Screen.Home.rout) {
                composable(route = Screen.Home.rout) {
                    HomeScreen(viewModel)
                }
                composable(route = Screen.Gainer.rout) {
                    GainerScreen(viewModel)
                }
                composable(route = Screen.Loser.rout) {
                    LoserScreen(viewModel)
                }
                composable(route = Screen.Favourites.rout) {
                    FavouritesScreen()
                }
                composable(route = Screen.Search.rout) {
                    SearchScreen(viewModel)
                }
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

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // State to keep track of the selected navigation index
    val selectedNavigationIndex = rememberSaveable { mutableIntStateOf(2) }

    // Bottom navigation bar with a background image
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Background image for the navigation bar
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.navigation_bar),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentDescription = "Bottom Navigation Bar",
            contentScale = ContentScale.FillWidth
        )

        // Background circle image
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
                    selected = selectedNavigationIndex.intValue == index,
                    onClick = {
                        selectedNavigationIndex.intValue = index
                        navController.navigate(item.route) {
                            // Clear the back stack to prevent going back to the previous screen
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(if (selectedNavigationIndex.intValue == index) item.filledIcon else item.icon),
                            contentDescription = item.title,
                            modifier = Modifier.size(35.dp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color(0xFFD3D3D3),
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

// This function is used to display a loading indicator while the cryptocurrency data is being loaded.
@Composable
fun ScreenHolder() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading...",
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins)),
                fontSize = 20.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        )
    }
}