package com.receparslan.finance.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.receparslan.finance.R
import com.receparslan.finance.ui.components.CryptocurrencyRow
import com.receparslan.finance.ui.components.ErrorDialog
import com.receparslan.finance.ui.components.ScreenHolder
import com.receparslan.finance.util.reachedEnd
import com.receparslan.finance.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // This derived state is used to check if the user has scrolled to the end of the list.
    val reachedEnd by remember {
        derivedStateOf {
            listState.reachedEnd()
        }
    }

    // This LaunchedEffect is triggered when the user reaches the end of the list.
    LaunchedEffect(reachedEnd) { if (reachedEnd) viewModel.loadMore() }

    LeftHeaderText()

    PullToRefreshBox(
        state = rememberPullToRefreshState(),
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshHomeScreen() },
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.isLoading) {
            ScreenHolder()
            return@PullToRefreshBox
        }

        if (uiState.isRefreshing) return@PullToRefreshBox

        if (uiState.cryptoList.isEmpty())
            ScreenHolder(message = "No cryptocurrencies found. Please try again later.")
        else
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp, 40.dp, 20.dp, 16.dp),
                contentPadding = PaddingValues(top = 7.dp),
                state = listState
            ) {
                items(items = uiState.cryptoList, key = { it.id }) {
                    CryptocurrencyRow(
                        cryptocurrency = it,
                        navController = navController
                    )
                }

                item {
                    if (uiState.isLoadingMore && uiState.cryptoList.isNotEmpty())
                        CryptocurrencyPlaceholder()

                    Spacer(Modifier.height(100.dp))
                }
            }

        if (uiState.errorMessage.isNotEmpty())
            ErrorDialog(
                message = uiState.errorMessage,
                onDismiss = { viewModel.clearErrorMessage() },
                onRetry = { viewModel.refreshHomeScreen() }
            )
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

// This function is used to display a placeholder while the cryptocurrency data is being loaded.
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