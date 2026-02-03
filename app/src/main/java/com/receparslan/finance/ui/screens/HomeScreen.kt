package com.receparslan.finance.ui.screens

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.receparslan.finance.LoadingScreenHolder
import com.receparslan.finance.R
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.util.reachedEnd
import com.receparslan.finance.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel() // Get the HomeViewModel instance using Hilt

    val cryptocurrencyList = viewModel.cryptocurrencyList // List of cryptocurrencies to display

    val isLoading by viewModel.isLoading // Loading state to show/hide loading indicators

    val listState = rememberLazyListState() // State for the LazyColumn

    // State to manage the refreshing state
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    // This derived state is used to check if the user has scrolled to the end of the list.
    val reachedEnd by remember {
        derivedStateOf {
            listState.reachedEnd()
        }
    }

    // This LaunchedEffect is triggered when the user reaches the end of the list.
    LaunchedEffect(reachedEnd) { if (reachedEnd) viewModel.loadMore() }

    LeftHeaderText() // Display the header text for the Home Screen

    // Show a loading indicator if the cryptocurrency list is empty and data is being loaded
    if (cryptocurrencyList.isEmpty() && isLoading)
        LoadingScreenHolder()
    else {
        // Pull to refresh functionality for the list
        @Suppress("AssignedValueIsNeverRead") // isRefreshing is not used directly but is required for the PullToRefreshBox
        PullToRefreshBox(
            state = refreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.refreshHomeScreen()
                    delay(1500)
                    isRefreshing = false
                }
            }
        ) {
            // LazyColumn to display the list of cryptocurrencies
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp, 40.dp, 20.dp, 16.dp),
                contentPadding = PaddingValues(top = 7.dp),
                state = listState
            ) {
                items(cryptocurrencyList) {
                    CryptocurrencyRow(it, navController) // Display each cryptocurrency in a row

                    // Show a loading placeholder at the end of the list if data is being loaded for pagination
                    if (cryptocurrencyList.lastOrNull() == it) {
                        if (isLoading) CryptocurrencyPlaceholder()
                        Spacer(Modifier.height(100.dp)) // Extra space at the bottom of the list because of bottom navigation bar
                    }
                }
            }
        }
    }
}

// This function is used to display each cryptocurrency in a row.
@Composable
fun CryptocurrencyRow(
    cryptocurrency: Cryptocurrency,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .shadow(
                elevation = 3.dp,
                spotColor = Color.White,
                ambientColor = Color.White,
                shape = RoundedCornerShape(size = 15.dp)
            )
            .background(color = Color(0xFF211E41), shape = RoundedCornerShape(size = 15.dp))
            .clickable {
                // URLEncoder is used to encode the cryptocurrency object as a JSON string
                val encodedString = viewModel.encodeToString(cryptocurrency)

                // Navigate to the detail screen with the encoded cryptocurrency object as an argument
                navController.navigate("detail_screen/$encodedString")
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display the cryptocurrency icon
            Image(
                painter = rememberAsyncImagePainter(cryptocurrency.image),
                contentDescription = cryptocurrency.name,
                modifier = Modifier
                    .padding(16.dp, 16.dp, 12.dp, 16.dp)
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.FillBounds
            )

            // Display the cryptocurrency name and symbol
            Column {
                // Display the cryptocurrency name with dynamic font size based on its length
                Text(
                    text = cryptocurrency.name,
                    style = TextStyle(
                        fontSize = if (cryptocurrency.name.length > 40) 10.sp else if (cryptocurrency.name.length > 30) 12.sp else if (cryptocurrency.name.length > 20) 14.sp else 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily(Font(R.font.poppins)),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Small spacer between name and symbol
                Spacer(Modifier.size(3.dp))

                // Display the cryptocurrency symbol in uppercase
                Text(
                    text = cryptocurrency.symbol.uppercase(),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFFA7A7A7),
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily(Font(R.font.poppins)),
                    )
                )
            }
        }

        // Display the cryptocurrency price and price change percentage
        Column {
            // Display the current price formatted with commas and up to 4 decimal places
            Text(
                text = "$" + DecimalFormat("#,###.####", DecimalFormatSymbols(Locale.US)).format(
                    cryptocurrency.currentPrice
                ),
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                ),
                modifier = Modifier
                    .padding(end = 18.dp)
                    .align(Alignment.End)
            )

            // Small spacer between price and percentage change
            Spacer(Modifier.size(5.dp))

            // Display the price change percentage with color coding for positive/negative changes
            Text(
                text = cryptocurrency.priceChangePercentage24h.toString() + "%",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = if ((cryptocurrency.priceChangePercentage24h) > 0) Color(0xFF21BF73) else Color(
                        0xFFD90429
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                ),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .align(Alignment.End)
            )
        }
    }
}

// This function is used to display the header text for the Home Screen
@Composable
private fun LeftHeaderText(
    modifier: Modifier = Modifier
) {
    Text(
        text = "Trending Coins",
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 7.dp),
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
        )
    )
}

// This function is used to display a loading indicator for each cryptocurrency row while the data is being loaded.
@Composable
private fun CryptocurrencyPlaceholder(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) { CircularProgressIndicator() }
}