package com.receparslan.finance

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import com.receparslan.finance.model.Cryptocurrency
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
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.absoluteValue

//@Suppress("unused","RedundantSuppression")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceTheme {
                // Create a NavHostController for navigation
                val navController = rememberNavController()

                // Main screen layout with a bottom navigation bar
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

                        composable(route = Screen.Favourites.rout) { FavouritesScreen(navController = navController) }

                        composable(route = Screen.Search.rout) { SearchScreen(navController) }

                        composable(
                            route = "${Screen.Detail.rout}/{cryptoCurrency}",
                            arguments = listOf(
                                navArgument("cryptoCurrency") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->
                            // Get the arguments passed to this composable
                            val decodedString =
                                backStackEntry.arguments?.getString("cryptoCurrency")

                            // Decode the URL-encoded string and convert it to a Cryptocurrency object
                            val cryptoCurrency: Cryptocurrency =
                                Gson().fromJson(
                                    URLDecoder.decode(decodedString, "utf-8"),
                                    Cryptocurrency::class.java
                                )

                            // DetailScreen is a composable function that displays the details of a cryptocurrency.
                            DetailScreen(cryptoCurrency, navController)
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
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
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

// This function is used to display a centered header text with an optional icon on both sides.
@Composable
fun CenterHeaderText(
    text: String,
    modifier: Modifier = Modifier
) {
    // Determine the icon based on whether it's for "Top Gainers" or "Top Losers"
    val icon: Int? =
        if (text == "Top Gainers") R.drawable.up_icon else if (text == "Top Losers") R.drawable.down_icon else null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Display the icon on the left side
        if (icon != null)
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = "Price Change",
                tint = Color.Green
            )


        // Title text "Top Gainers" with shadow effect
        Text(
            text = text,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.White,
                    offset = Offset(0f, 2f),
                    blurRadius = 3f
                ),
                fontFamily = FontFamily(Font(R.font.poppins)),
                fontSize = 20.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            ),
            textAlign = TextAlign.Center
        )

        // Display the icon again on the right side
        if (icon != null)
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = "Price Change",
                tint = Color.Green
            )
    }
}

// This function is used to display a grid item for a cryptocurrency for the Gainer and Loser screens
@Composable
fun GridItem(
    cryptocurrency: Cryptocurrency,
    navController: NavController,
    arrow: @Composable () -> Unit = {}
) {
    // Format the cryptocurrency price with commas and four decimal places
    val cryptocurrencyPrice by remember {
        derivedStateOf {
            "$" + DecimalFormat("#,###.####", DecimalFormatSymbols(Locale.US)).format(
                cryptocurrency.currentPrice
            )
        }
    }

    // Get the absolute value of the price change percentage with a percentage sign
    val priceChangePercentage by remember {
        derivedStateOf {
            cryptocurrency.priceChangePercentage24h.absoluteValue.toString() + "%"
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(
                elevation = 3.dp,
                spotColor = Color.White,
                ambientColor = Color.White,
                shape = RoundedCornerShape(size = 15.dp)
            )
            .background(color = Color(0xFF211E41), shape = RoundedCornerShape(size = 15.dp))
            .clickable {
                // Encode the cryptocurrency object to a JSON string
                val encodedString = URLEncoder.encode(Gson().toJson(cryptocurrency), "utf-8")

                // Navigate to the detail screen with the encoded cryptocurrency object as an argument
                navController.navigate("detail_screen/$encodedString")
            },
    ) {
        Column {
            // Display the cryptocurrency icon
            Image(
                painter = rememberAsyncImagePainter(cryptocurrency.image),
                contentDescription = cryptocurrency.name,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            // Display the cryptocurrency name
            Text(
                text = cryptocurrency.name,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = TextStyle(
                    color = Color(0xFFA7A7A7),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(500),
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 1f),
                        blurRadius = 2f
                    )
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Display the cryptocurrency price
            Text(
                text = cryptocurrencyPrice,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(600),
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 1f),
                        blurRadius = 2f
                    )
                ),
                maxLines = 1,
            )

            // Display the price change percentage
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(0.5.dp, Color(0xFF211E41), RoundedCornerShape(16.dp))
                    .background(Color(0xFF1D1B32), RoundedCornerShape(16.dp))
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally)
                    .padding(4.dp)
            ) {
                arrow() // Display the arrow icon based on the price change percentage

                // Display the absolute value of the price change percentage
                Text(
                    text = priceChangePercentage,
                    style = TextStyle(
                        color = Color(0xFFA7A7A7),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.poppins)),
                        fontWeight = FontWeight(500),
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(0f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
            }
        }
    }
}

// This function is used to display a placeholder screen when there are no items to show
@Composable
fun EmptyScreenHolder(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins)),
                fontSize = 20.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            ),
            textAlign = TextAlign.Center
        )
    }
}

// This function is used to display a loading indicator while the cryptocurrency data is being loaded.
@Composable
fun LoadingScreenHolder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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