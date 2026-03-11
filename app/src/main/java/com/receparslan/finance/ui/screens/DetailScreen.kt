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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.receparslan.finance.R
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.ui.charts.LineChart
import com.receparslan.finance.ui.components.ErrorDialog
import com.receparslan.finance.ui.components.ScreenHolder
import com.receparslan.finance.util.Constants
import com.receparslan.finance.util.States.DetailUIState
import com.receparslan.finance.viewmodel.DetailViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(Unit) {
        snapshotFlow { uiState.klineDataHistory }
            .distinctUntilChanged()
            .collect { data ->
                val x = data.map { it.openTime }
                val y = data.map { it.close.toDouble() }
                val map = data.associateBy { it.openTime }

                modelProducer.runTransaction {
                    if (data.isNotEmpty()) {
                        lineSeries { series(x, y) }

                        extras { it[Constants.ExtraKeys.klineDataMap] = map }
                    }
                }
            }
    }

    PullToRefreshBox(
        state = rememberPullToRefreshState(),
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshDetailScreen() },
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.isLoading) {
            ScreenHolder()
            return@PullToRefreshBox
        }

        if (uiState.cryptocurrency != null)
            Scaffold(
                topBar = {
                    AppBar(
                        state = uiState,
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            ) { innerPadding ->
                LazyColumn {
                    item {
                        Column(
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            CryptocurrencyInfoRow(
                                cryptocurrency = uiState.cryptocurrency ?: return@Column,
                                viewModel = viewModel
                            )

                            LineChart(
                                modelProducer = modelProducer,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                lineColor = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.onBackground,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(21.dp)
                            ) {
                                TimeButton("24H", Modifier.weight(1f), viewModel, uiState)
                                TimeButton("1W", Modifier.weight(1f), viewModel, uiState)
                                TimeButton("1M", Modifier.weight(1f), viewModel, uiState)
                                TimeButton("6M", Modifier.weight(1f), viewModel, uiState)
                                TimeButton("1Y", Modifier.weight(1f), viewModel, uiState)
                                TimeButton("5Y", Modifier.weight(1f), viewModel, uiState)
                            }

                            CryptocurrencyCalculationRow(
                                cryptocurrency = uiState.cryptocurrency ?: return@Column,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }

        if (uiState.errorMessage.isNotEmpty())
            ErrorDialog(
                message = uiState.errorMessage,
                onDismiss = { viewModel.clearErrorMessage() },
                onRetry = { viewModel.refreshDetailScreen() }
            )
    }
}

// This function creates a button for selecting the time period
@Composable
private fun TimeButton(
    time: String,
    modifier: Modifier,
    viewModel: DetailViewModel,
    state: DetailUIState
) {
    val isSelected = time == state.timePeriod

    Button(
        enabled = !isSelected,
        onClick = { viewModel.updateTimePeriod(time) },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.background
        ),
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
    state: DetailUIState,
    navController: NavController,
    viewModel: DetailViewModel
) {
    if (state.cryptocurrency == null) return

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
                    navController.popBackStack()
                },
            tint = Color.White
        )

        // Display the cryptocurrency logo
        Image(
            painter = rememberAsyncImagePainter(state.cryptocurrency.image),
            contentDescription = state.cryptocurrency.name,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
        )

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
                    text = state.cryptocurrency.name,
                    modifier = if (state.cryptocurrency.name.length > 10) Modifier.weight(1f) else Modifier,
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = Color.White,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "(${state.cryptocurrency.symbol.uppercase()})",
                    modifier = Modifier
                        .padding(start = 4.dp),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                )
            }

            // Display the save icon, which changes based on whether the cryptocurrency is saved or not
            Icon(
                imageVector = ImageVector.vectorResource(id = if (state.isSaved) R.drawable.star_filled_icon else R.drawable.star_icon),
                contentDescription = "Add to favorites",
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        if (state.isSaved) viewModel.deleteCryptocurrency() else viewModel.saveCryptocurrency()
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
    viewModel: DetailViewModel
) {
    val prevPrice =
        cryptocurrency.currentPrice / (1 + (cryptocurrency.priceChangePercentage24h / 100))
    val priceChange = cryptocurrency.currentPrice - prevPrice

    val currentPriceFormatted = "$${
        viewModel.decimalFormatter(
            "#,###.################",
            cryptocurrency.currentPrice
        )
    }"

    val priceChangeFormatted = (if (priceChange >= 0) "+" else "-") +
            viewModel.decimalFormatter("#,###.###", priceChange.absoluteValue)

    val priceChangePercentageFormatted = ("( " + if (priceChange > 0) "+" else "-") + "${
        viewModel.decimalFormatter(
            "#,###.###",
            cryptocurrency.priceChangePercentage24h.absoluteValue
        )
    }% )"

    val lastUpdatedTimeFormatted =
        DateTimeFormatter.ofPattern("dd.MM.yyyy\n      HH:mm", Locale.getDefault())
            .format(
                ZonedDateTime.parse(cryptocurrency.lastUpdated)
                    .withZoneSameInstant(ZoneId.systemDefault())
            )

    val priceChangeColor = if (priceChange > 0) Color.Green else Color.Red

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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
    viewModel: DetailViewModel
) {
    var amount by remember { mutableDoubleStateOf(0.0) }

    val prevPrice =
        cryptocurrency.currentPrice / (1 + (cryptocurrency.priceChangePercentage24h / 100))

    val priceChange = cryptocurrency.currentPrice - prevPrice

    val totalValue by remember(amount, cryptocurrency.currentPrice) {
        derivedStateOf {
            "$${viewModel.decimalFormatter("#,###.#####", cryptocurrency.currentPrice * amount)}"
        }
    }

    val totalPriceChange by remember(amount, priceChange) {
        derivedStateOf {
            (if (priceChange * amount > 0) "+" else if (priceChange * amount < 0) "-" else "") +
                    viewModel.decimalFormatter("#,###.###", priceChange.absoluteValue * amount)
        }
    }

    Row(
        modifier = modifier
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
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
                Text(
                    text = cryptocurrency.name,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Input field for the amount
                    AmountTextField(onAmountChange = { amount = it })

                    // Display the cryptocurrency symbol
                    Text(
                        text = cryptocurrency.symbol.uppercase(),
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.poppins)),
                        fontWeight = FontWeight(450),
                        color = MaterialTheme.colorScheme.tertiary,
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

// This function creates a text field for the user to input the amount of cryptocurrency they want to calculate with
@Composable
private fun AmountTextField(
    onAmountChange: (Double) -> Unit
) {
    var input by remember { mutableStateOf("") }

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
            input = newValue

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
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(15.dp)
            )
            .windowInsetsPadding(
                WindowInsets(
                    8.dp,
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