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
import com.receparslan.finance.viewmodel.FavouritesViewModel

@Composable
fun FavouritesScreen(
    navController: NavController,
    viewModel: FavouritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CenterHeaderText("Favourites")

    if (uiState.isLoading) {
        ScreenHolder()
        return
    }

    if (uiState.savedCryptocurrencies.isEmpty())
        ScreenHolder(message = " No favourites added yet.")
    else
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            columns = GridCells.Fixed(2),
        ) {
            items(uiState.savedCryptocurrencies) { cryptocurrency ->
                GridItem(cryptocurrency, navController) {
                    val icon = if (cryptocurrency.priceChangePercentage24h < 0)
                        R.drawable.down_icon
                    else
                        R.drawable.up_icon

                    val color = if (cryptocurrency.priceChangePercentage24h < 0)
                        Color.Red
                    else Color.Green

                    Icon(
                        modifier = Modifier.size(28.dp),
                        imageVector = ImageVector.vectorResource(icon),
                        contentDescription = "Price Change",
                        tint = color
                    )
                }
            }

            // If the number of saved cryptocurrencies is odd, add extra space at the end to prevent the last item from being cut off by the bottom navigation bar.
            item {
                if (uiState.savedCryptocurrencies.size % 2 != 0)
                    Spacer(Modifier.height(272.dp))
                else
                    Spacer(Modifier.height(100.dp))
            }
        }

    if (uiState.errorMessage.isNotEmpty())
        ErrorDialog(
            message = uiState.errorMessage,
            onDismiss = { viewModel.clearErrorMessage() },
            onRetry = { viewModel.refreshFavouritesScreen() }
        )
}