package com.receparslan.finance.feature.detail.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.receparslan.finance.core.ui.charts.LineChart
import com.receparslan.finance.core.ui.components.ErrorDialog
import com.receparslan.finance.core.ui.components.ScreenHolder
import com.receparslan.finance.core.common.Constants
import com.receparslan.finance.feature.detail.presentation.components.AppBar
import com.receparslan.finance.feature.detail.presentation.components.CryptocurrencyCalculationRow
import com.receparslan.finance.feature.detail.presentation.components.CryptocurrencyInfoRow
import com.receparslan.finance.feature.detail.presentation.components.TimeButton
import com.receparslan.finance.feature.detail.presentation.viewmodel.DetailViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

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
                        lineModel { series(x, y) }

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