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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.receparslan.finance.CenterHeaderText
import com.receparslan.finance.GridItem
import com.receparslan.finance.LoadingScreenHolder
import com.receparslan.finance.R
import com.receparslan.finance.viewmodel.GainerAndLoserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoserScreen(
    navController: NavController,
    viewModel: GainerAndLoserViewModel = hiltViewModel()
) {
    // This is the list of cryptocurrencies that are currently losing value
    val cryptocurrencyLosers = viewModel.cryptocurrencyLoserList

    // This is the state that indicates whether the app is currently loading data
    val isLoading by viewModel.isLoading

    // State to manage the refreshing state
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    CenterHeaderText("Top Losers") // Header text at the top of the screen

    // Show a loading indicator if the cryptocurrency list is empty and data is being loaded
    if (cryptocurrencyLosers.isEmpty() || isLoading) {
        LoadingScreenHolder()
    } else {
        // Pull to refresh functionality
        @Suppress("AssignedValueIsNeverRead") // isRefreshing is not used directly but is required for the PullToRefreshBox
        PullToRefreshBox(
            state = refreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.setGainersAndLosersList()
                    delay(1500)
                    isRefreshing = false
                }
            }
        ) {
            // Grid to display the list of cryptocurrencies
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                columns = GridCells.Fixed(2),
            ) {
                items(cryptocurrencyLosers) { cryptocurrency ->
                    GridItem(cryptocurrency, navController) {
                        Icon(
                            modifier = Modifier
                                .size(28.dp),
                            imageVector = ImageVector.vectorResource(R.drawable.down_icon),
                            contentDescription = "Price Change",
                            tint = Color.Red
                        )
                    }

                    // Add extra space at the bottom of the grid
                    if (cryptocurrencyLosers.indexOf(cryptocurrency) >= cryptocurrencyLosers.size - 2)
                        Spacer(Modifier.height(300.dp))
                }
            }
        }
    }
}
