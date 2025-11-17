package com.receparslan.finance.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.receparslan.finance.R
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.model.KlineData
import com.receparslan.finance.ui.charts.LineChart
import com.receparslan.finance.viewmodel.DetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

object ExtraKeys {
    val klineDataMap = ExtraStore.Key<Map<Long, KlineData>>()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    cryptocurrencyParam: Cryptocurrency,
    viewModel: DetailViewModel,
    navController: NavController
) {
    viewModel.cryptocurrency = remember { mutableStateOf(cryptocurrencyParam) }

    val cryptocurrency by remember { viewModel.cryptocurrency }

    // This is the state that holds the list of historical data for the cryptocurrency
    val historyList by remember { viewModel.klineDataHistoryList }

    // This is the state that holds the model producer for the chart
    val modelProducer = remember { CartesianChartModelProducer() }

    // This is the state that holds the focus manager for the keyboard
    val focusManager = LocalFocusManager.current

    // State to manage the refreshing state
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    // Variables to calculate the price of cryptocurrency
    var amount by remember { mutableDoubleStateOf(0.0) }
    val prevPrice =
        cryptocurrency.currentPrice / (1 + (cryptocurrency.priceChangePercentage24h / 100))
    val priceChange = cryptocurrency.currentPrice - prevPrice

    val isLoading by remember { viewModel.isLoading }

    // Trigger the initial data load when the screen is first displayed
    LaunchedEffect(cryptocurrency) {
        viewModel.setCryptocurrencyHistory()
    }

    // This is the state that holds the list of historical data for the cryptocurrency
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
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }
    } else {
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
            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                topBar = {
                    AppBar(cryptocurrency, viewModel, navController)
                }
            ) { innerPadding ->
                // LazyColumn used for the pushing of the content
                LazyColumn {
                    item {
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Display the cryptocurrency price and change percentage
                                Column {
                                    Text(
                                        text = "$${
                                            DecimalFormat(
                                                "#,###.################",
                                                DecimalFormatSymbols(Locale.US)
                                            ).format(cryptocurrency.currentPrice)
                                        }",
                                        fontSize = 28.sp,
                                        fontFamily = FontFamily(Font(R.font.poppins)),
                                        fontWeight = FontWeight(500),
                                        color = Color.White
                                    )

                                    Text(
                                        text = (if (priceChange > 0) "+" else "-") + "${
                                            DecimalFormat(
                                                "#,###.###",
                                                DecimalFormatSymbols(Locale.US)
                                            ).format(priceChange.absoluteValue)
                                        }",
                                        fontSize = 24.sp,
                                        fontFamily = FontFamily(Font(R.font.poppins)),
                                        fontWeight = FontWeight(450),
                                        color = if (priceChange > 0) Color(android.graphics.Color.GREEN) else Color(
                                            android.graphics.Color.RED
                                        ),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )

                                    Text(
                                        text = ("( " + if (priceChange > 0) "+" else "-")
                                                + "${
                                            DecimalFormat(
                                                "#,###.###",
                                                DecimalFormatSymbols(Locale.US)
                                            ).format(cryptocurrency.priceChangePercentage24h.absoluteValue)
                                        }% )",
                                        fontSize = 24.sp,
                                        fontFamily = FontFamily(Font(R.font.poppins)),
                                        fontWeight = FontWeight(450),
                                        color = if (priceChange > 0) Color(android.graphics.Color.GREEN) else Color(
                                            android.graphics.Color.RED
                                        ),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                // Display the last updated time of the cryptocurrency
                                Text(
                                    text = DateTimeFormatter.ofPattern(
                                        "dd.MM.yyyy\n      HH:mm",
                                        Locale.getDefault()
                                    ).format(
                                        ZonedDateTime.parse(cryptocurrency.lastUpdated)
                                            .withZoneSameInstant(ZoneId.systemDefault())
                                    ),
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily(Font(R.font.poppins)),
                                    fontWeight = FontWeight(500),
                                    color = Color.Gray,
                                )
                            }

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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(21.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TimeButton("24H", Modifier.weight(1f), viewModel)
                                TimeButton("1W", Modifier.weight(1f), viewModel)
                                TimeButton("1M", Modifier.weight(1f), viewModel)
                                TimeButton("6M", Modifier.weight(1f), viewModel)
                                TimeButton("1Y", Modifier.weight(1f), viewModel)
                                TimeButton("5Y", Modifier.weight(1f), viewModel)
                            }

                            Row(
                                modifier = Modifier
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
                                    contentDescription = "Logo",
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
                                    Column {
                                        Text(
                                            text = cryptocurrency.name,
                                            fontSize = 20.sp,
                                            fontFamily = FontFamily(Font(R.font.poppins)),
                                            fontWeight = FontWeight(450),
                                            color = Color.White
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            var input by remember { mutableStateOf("") }

                                            BasicTextField(
                                                value = input,
                                                textStyle = TextStyle(
                                                    fontSize = 16.sp,
                                                    fontFamily = FontFamily(Font(R.font.poppins)),
                                                    fontWeight = FontWeight(450),
                                                    color = Color.Gray
                                                ),
                                                onValueChange = {
                                                    input = it

                                                    amount = if (input.isNotEmpty())
                                                        DecimalFormat.getInstance(Locale.US)
                                                            .parse(input)?.toDouble() ?: 0.0
                                                    else
                                                        0.0
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
                                        Text(
                                            text = "$${
                                                DecimalFormat(
                                                    "#,###.#####",
                                                    DecimalFormatSymbols(Locale.US)
                                                ).format(cryptocurrency.currentPrice * amount)
                                            }",
                                            fontSize = 16.sp,
                                            fontFamily = FontFamily(Font(R.font.poppins)),
                                            fontWeight = FontWeight(450),
                                            color = Color.White
                                        )

                                        Text(
                                            text = (if (priceChange * amount > 0) "+" else if (priceChange * amount < 0) "-" else "")
                                                    + "${
                                                DecimalFormat(
                                                    "#,###.###",
                                                    DecimalFormatSymbols(Locale.US)
                                                ).format(priceChange.absoluteValue * amount)
                                            }",
                                            fontSize = 16.sp,
                                            fontFamily = FontFamily(Font(R.font.poppins)),
                                            fontWeight = FontWeight(450),
                                            color = if (priceChange * amount > 0) Color(android.graphics.Color.GREEN) else if (priceChange * amount < 0) Color(
                                                android.graphics.Color.RED
                                            ) else Color.Gray,
                                        )

                                        Text(
                                            text = (if (priceChange * amount > 0) "+" else if (priceChange * amount < 0) "-" else "")
                                                    + "${
                                                DecimalFormat(
                                                    "#,###.###",
                                                    DecimalFormatSymbols(Locale.US)
                                                ).format(cryptocurrency.priceChangePercentage24h.absoluteValue * amount)
                                            }%",
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
                    }
                }
            }
        }
    }
}

// This function creates a button for selecting the time period
@Composable
private fun TimeButton(time: String, modifier: Modifier, viewModel: DetailViewModel) {
    Button(
        onClick = { viewModel.selectedTimePeriod.value = time },
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
    viewModel: DetailViewModel,
    navController: NavController
) {
    val savedCryptocurrencyIds by remember { viewModel.savedCryptocurrencyIds }

    // This is the state that indicates whether the cryptocurrency is saved
    var isSaved by remember { mutableStateOf(cryptocurrency.id in savedCryptocurrencyIds) }

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
            contentDescription = "Logo",
            modifier = Modifier
                .padding(start = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
        )

        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 16.dp)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Display the name and symbol of the cryptocurrency
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
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
                        if (isSaved) {
                            viewModel.deleteCryptocurrency()
                            isSaved = false
                        } else {
                            viewModel.saveCryptocurrency()
                            isSaved = true
                        }
                    },
                tint = Color.White
            )
        }
    }
}