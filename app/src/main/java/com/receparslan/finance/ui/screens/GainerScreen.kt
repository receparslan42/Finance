package com.receparslan.finance.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.receparslan.finance.R
import com.receparslan.finance.ui.components.CenterHeaderText
import com.receparslan.finance.ui.components.ErrorDialog
import com.receparslan.finance.ui.components.GridItem
import com.receparslan.finance.ui.components.ScreenHolder
import com.receparslan.finance.viewmodel.GainerAndLoserViewModel

@Composable
fun GainerScreen(
    navController: NavController,
    viewModel: GainerAndLoserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CenterHeaderText("Top Gainers")

    PullToRefreshBox(
        state = rememberPullToRefreshState(),
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshGainersAndLosers() },
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.isLoading) {
            ScreenHolder()
            return@PullToRefreshBox
        }

        if (uiState.isRefreshing) return@PullToRefreshBox

        if (uiState.gainers.isEmpty())
            ScreenHolder(message = "No gainers found.")
        else
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(top = 40.dp),
                contentPadding = PaddingValues(16.dp),
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.gainers) { cryptocurrency ->
                    GridItem(cryptocurrency, navController) {
                        Icon(
                            modifier = Modifier.size(15.dp),
                            imageVector = ImageVector.vectorResource(R.drawable.up_icon),
                            contentDescription = "Price Change",
                            tint = Color.Green
                        )
                    }
                }

                // If the number of gainers is odd, add a spacer to balance the grid.
                item {
                    if (uiState.gainers.size % 2 != 0)
                        Spacer(Modifier.height(272.dp))
                    else
                        Spacer(Modifier.height(100.dp))
                }
            }

        if (uiState.errorMessage.isNotEmpty())
            ErrorDialog(
                message = uiState.errorMessage,
                onDismiss = { viewModel.clearErrorMessage() },
                onRetry = { viewModel.refreshGainersAndLosers() }
            )
    }
}