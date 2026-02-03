package com.receparslan.finance.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.receparslan.finance.LoadingScreenHolder
import com.receparslan.finance.R
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.ui.charts.LineChart
import com.receparslan.finance.util.Constants.ExtraKeys
import com.receparslan.finance.viewmodel.DetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    cryptocurrencyParam: Cryptocurrency,
    navController: NavController,
    viewModel: DetailViewModel = hiltViewModel()
) {
    // This is the state that holds the selected cryptocurrency details
    val cryptocurrency by viewModel.cryptocurrency

    // Set the cryptocurrency state to the passed parameter for the first time
    if (cryptocurrency.id.isEmpty()) viewModel.cryptocurrency.value = cryptocurrencyParam

    // This is the state that holds the list of historical data for the cryptocurrency
    val historyList = viewModel.klineDataHistoryList

    // This is the state that holds the model producer for the chart
    val modelProducer = remember { CartesianChartModelProducer() }

    // State to manage the refreshing state
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    val isLoading by viewModel.isLoading // Loading state to show/hide loading indicators

    // Fetch the cryptocurrency history when the composable is first launched
    LaunchedEffect(cryptocurrency) { viewModel.setCryptocurrencyHistory() }

    // Update the chart model producer whenever the history list changes
    LaunchedEffect(historyList.size) {
        if (historyList.isNotEmpty()) {
            val historySnapshot = historyList.toList() // Defensive copy

            withContext(Dispatchers.Main.immediate) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(
                            historySnapshot.map { it.openTime },
                            historySnapshot.map { it.close.toFloat() }
                        )
                    }

                    // Store the history data in the extras of the model producer
                    extras {
                        it[ExtraKeys.klineDataMap] =
                            historySnapshot.associateBy { klineData -> klineData.openTime }
                    }
                }
            }
        }
    }

    @Suppress("AssignedValueIsNeverRead") // isRefreshing is not used directly but is required for the PullToRefreshBox
    // Check if the data is loaded
    if (isLoading)
        LoadingScreenHolder()
    else {
        // Pull to refresh functionality
        @Suppress("AssignedValueIsNeverRead") // isRefreshing is not used directly but is required for the PullToRefreshBox
        PullToRefreshBox(
            state = refreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.refreshDetailScreen()
                    delay(1500)
                    isRefreshing = false
                }
            }
        ) {
            // Scaffold used for the top app bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { AppBar(cryptocurrency, navController) }
            ) { innerPadding ->
                // LazyColumn used for the pushing of the content
                LazyColumn {
                    item {
                        Column(
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // Display the cryptocurrency price, price change,change percentage, and last updated time
                            CryptocurrencyInfoRow(cryptocurrency)

                            // Display the line chart for the cryptocurrency historical data
                            LineChart(
                                modelProducer,
                                Modifier
                                    .fillMaxWidth(),
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF002FFE),
                                        Color(0xFF0834F4)
                                    )
                                ),
                            )

                            // Display the time buttons for selecting the historical data
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(21.dp)
                            ) {
                                TimeButton("24H", Modifier.weight(1f), viewModel)
                                TimeButton("1W", Modifier.weight(1f), viewModel)
                                TimeButton("1M", Modifier.weight(1f), viewModel)
                                TimeButton("6M", Modifier.weight(1f), viewModel)
                                TimeButton("1Y", Modifier.weight(1f), viewModel)
                                TimeButton("5Y", Modifier.weight(1f), viewModel)
                            }

                            // Display the cryptocurrency calculation row for user input
                            CryptocurrencyCalculationRow(cryptocurrency)
                        }
                    }
                }
            }
        }
    }
}

// This function creates a button for selecting the time period
@Composable
private fun TimeButton(
    time: String,
    modifier: Modifier,
    viewModel: DetailViewModel
) {
    Button(
        onClick = {
            viewModel.selectedTimePeriod.value = time
            viewModel.setCryptocurrencyHistory() // Update the historical data based on the selected time period
        },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF211E41)),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .border(width = 0.5.dp, color = Color.Gray, shape = RoundedCornerShape(size = 20.dp))
            .size(width = 60.dp, height = 40.dp)
    ) {
        Text(
            text = time,
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(R.font.poppins)),
            fontWeight = FontWeight(450),
            color = Color.White
        )
    }
}

// This function creates the app bar for the DetailScreen
@Composable
private fun AppBar(
    cryptocurrency: Cryptocurrency,
    navController: NavController,
    viewModel: DetailViewModel = hiltViewModel()
) {
    // Get the list of saved cryptocurrency IDs from the ViewModel
    val savedCryptocurrencyIds = viewModel.savedCryptocurrencyIds

    // Determine if the cryptocurrency is saved
    val isSaved = cryptocurrency.id in savedCryptocurrencyIds

    // App bar layout with back button, logo, name, symbol, and save icon
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Display the back button
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.back_icon),
            contentDescription = "Go back",
            modifier = Modifier
                .padding(start = 18.dp)
                .size(28.dp)
                .clickable {
                    navController.popBackStack() // Navigate back to the previous screen
                },
            tint = Color.White
        )

        // Display the cryptocurrency logo
        Image(
            painter = rememberAsyncImagePainter(cryptocurrency.image),
            contentDescription = cryptocurrency.name,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
        )

        // Row for the name, symbol, and save icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 8.dp, end = 16.dp)
                .weight(1f)
        ) {
            // Row for the name and symbol
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = cryptocurrency.name,
                    modifier = if (cryptocurrency.name.length > 10) Modifier.weight(1f) else Modifier,
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = Color.White,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "(${cryptocurrency.symbol.uppercase()})",
                    modifier = Modifier
                        .padding(start = 4.dp),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = Color(0xFFA7A7A7),
                    maxLines = 1,
                )
            }

            // Display the icon for saving the cryptocurrency
            Icon(
                // Change icon based on whether the cryptocurrency is saved
                imageVector = ImageVector.vectorResource(id = if (isSaved) R.drawable.star_filled_icon else R.drawable.star_icon),
                contentDescription = "Add to favorites",
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        // Toggle the saved state of the cryptocurrency
                        if (isSaved) viewModel.deleteCryptocurrency() else viewModel.saveCryptocurrency()
                    },
                tint = Color.White
            )
        }
    }
}

// This function creates a row displaying cryptocurrency information
@Composable
private fun CryptocurrencyInfoRow(
    cryptocurrency: Cryptocurrency,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    // Calculate the previous price and price change
    val prevPrice =
        cryptocurrency.currentPrice / (1 + (cryptocurrency.priceChangePercentage24h / 100))
    val priceChange = cryptocurrency.currentPrice - prevPrice

    // Format the current price
    val currentPriceFormatted =
        "$${viewModel.decimalFormatter("#,###.################", cryptocurrency.currentPrice)}"

    // Format the price change
    val priceChangeFormatted = (if (priceChange >= 0) "+" else "-") +
            viewModel.decimalFormatter("#,###.###", priceChange.absoluteValue)

    // Format the price change percentage
    val priceChangePercentageFormatted = ("( " + if (priceChange > 0) "+" else "-") + "${
        viewModel.decimalFormatter(
            "#,###.###",
            cryptocurrency.priceChangePercentage24h.absoluteValue
        )
    }% )"

    // Format the last updated time
    val lastUpdatedTimeFormatted =
        DateTimeFormatter.ofPattern("dd.MM.yyyy\n      HH:mm", Locale.getDefault())
            .format(
                ZonedDateTime.parse(cryptocurrency.lastUpdated)
                    .withZoneSameInstant(ZoneId.systemDefault())
            )

    // Determine the color for the price change
    val priceChangeColor = if (priceChange > 0) Color.Green else Color.Red

    // Display the cryptocurrency price, change percentage, and last updated time
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Display the cryptocurrency price and change percentage
        Column {
            // Display the current price
            Text(
                text = currentPriceFormatted,
                fontSize = 28.sp,
                fontFamily = FontFamily(Font(R.font.poppins)),
                fontWeight = FontWeight(500),
                color = Color.White
            )

            // Display the price change
            Text(
                text = priceChangeFormatted,
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.poppins)),
                fontWeight = FontWeight(450),
                color = priceChangeColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Display the price change percentage
            Text(
                text = priceChangePercentageFormatted,
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.poppins)),
                fontWeight = FontWeight(450),
                color = priceChangeColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Display the last updated time of the cryptocurrency
        Text(
            text = lastUpdatedTimeFormatted,
            fontSize = 20.sp,
            fontFamily = FontFamily(Font(R.font.poppins)),
            fontWeight = FontWeight(500),
            color = Color.Gray,
        )
    }
}

// This function creates a row for cryptocurrency calculations based on user input
@Composable
private fun CryptocurrencyCalculationRow(
    cryptocurrency: Cryptocurrency,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    var amount by remember { mutableDoubleStateOf(0.0) } // State to hold the input amount

    // Calculate the previous price and price change
    val prevPrice =
        cryptocurrency.currentPrice / (1 + (cryptocurrency.priceChangePercentage24h / 100))
    val priceChange = cryptocurrency.currentPrice - prevPrice

    // Calculate the total value based on the input amount
    val totalValue by remember {
        derivedStateOf {
            "$${viewModel.decimalFormatter("#,###.#####", cryptocurrency.currentPrice * amount)}"
        }
    }

    // Calculate the total price change based on the input amount
    val totalPriceChange by remember {
        derivedStateOf {
            (if (priceChange * amount > 0) "+" else if (priceChange * amount < 0) "-" else "") +
                    viewModel.decimalFormatter("#,###.###", priceChange.absoluteValue * amount)
        }
    }

    Row(
        modifier = modifier
            .padding(16.dp)
            .background(
                color = Color(0xFF211E41),
                shape = RoundedCornerShape(size = 15.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Display the cryptocurrency logo
        Image(
            painter = rememberAsyncImagePainter(cryptocurrency.image),
            contentDescription = cryptocurrency.name,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Column for the name and row for amount input and symbol
            Column {
                // Display the cryptocurrency name
                Text(
                    text = cryptocurrency.name,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = Color.White
                )

                // Row for amount input and cryptocurrency symbol
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Input field for the amount
                    AmountTextField(onAmountChange = { amount = it })

                    // Display the cryptocurrency symbol
                    Text(
                        text = cryptocurrency.symbol.uppercase(),
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.poppins)),
                        fontWeight = FontWeight(450),
                        color = Color(0xFFA7A7A7),
                        maxLines = 1,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Display the total value based on the input amount
                Text(
                    text = totalValue,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = Color.White
                )

                // Display the total price change based on the input amount
                Text(
                    text = totalPriceChange,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = if (priceChange * amount > 0) Color(android.graphics.Color.GREEN) else if (priceChange * amount < 0) Color(
                        android.graphics.Color.RED
                    ) else Color.Gray,
                )
            }
        }
    }
}

@Composable
private fun AmountTextField(
    onAmountChange: (Double) -> Unit
) {
    var input by remember { mutableStateOf("") } // This is the state that holds the text input by the user

    // This is the state that holds the focus manager for the keyboard
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = input,
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.poppins)),
            fontWeight = FontWeight(450),
            color = Color.Gray
        ),
        onValueChange = { newValue ->
            input = newValue // Update the input state

            // Update the amount state based on the user input
            val parsed = input.toDoubleOrNull() ?: 0.0
            onAmountChange(parsed)
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        modifier = Modifier
            .widthIn(75.dp, 75.dp)
            .border(
                1.dp,
                Color.Gray,
                RoundedCornerShape(15.dp)
            )
            .background(
                Color(0xFF1D1B32),
                shape = RoundedCornerShape(15.dp)
            )
            .windowInsetsPadding(
                WindowInsets(
                    4.dp,
                    4.dp,
                    4.dp,
                    4.dp
                )
            ),
        cursorBrush = SolidColor(Color.Gray),
        singleLine = true,
    ) {
        // Placeholder
        if (input.isEmpty()) {
            Text(
                text = "00.00",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = Color.Gray
                )
            )
            it()
        } else
            it()
    }
}