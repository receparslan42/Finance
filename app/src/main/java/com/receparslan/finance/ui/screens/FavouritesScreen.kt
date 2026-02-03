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
import androidx.navigation.NavController
import com.receparslan.finance.CenterHeaderText
import com.receparslan.finance.EmptyScreenHolder
import com.receparslan.finance.GridItem
import com.receparslan.finance.LoadingScreenHolder
import com.receparslan.finance.R
import com.receparslan.finance.viewmodel.FavouritesViewModel

@Composable
fun FavouritesScreen(
    navController: NavController,
    viewModel: FavouritesViewModel = hiltViewModel()
) {
    // This is the list of saved cryptocurrencies from the ViewModel
    val savedCryptocurrencies = viewModel.savedCryptocurrencyList

    // This is the state that indicates whether the app is currently loading data
    val isLoading by viewModel.isLoading

    CenterHeaderText("Favourites") // Header text at the top of the screen

    // This is the loading indicator that is displayed when the app is loading data
    if (savedCryptocurrencies.isEmpty())
        EmptyScreenHolder(text = " No favourites added yet.")
    else if (isLoading)
        LoadingScreenHolder()
    else {
        // This is the grid that displays the saved cryptocurrencies
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            columns = GridCells.Fixed(2),
        ) {
            items(savedCryptocurrencies) { cryptocurrency ->
                GridItem(cryptocurrency, navController) {
                    // Determine the icon based on price change percentage
                    val icon =
                        if (cryptocurrency.priceChangePercentage24h < 0) R.drawable.down_icon else R.drawable.up_icon

                    // Determine the color based on price change percentage
                    val color =
                        if (cryptocurrency.priceChangePercentage24h < 0) Color.Red else Color.Green

                    // Display the price change icon with appropriate size and tint
                    Icon(
                        modifier = Modifier.size(28.dp),
                        imageVector = ImageVector.vectorResource(icon),
                        contentDescription = "Price Change",
                        tint = color
                    )

                }

                // Add extra space at the bottom of the list
                if (savedCryptocurrencies.indexOf(cryptocurrency) >= savedCryptocurrencies.size - 1)
                    Spacer(Modifier.height(300.dp))
            }
        }
    }
}