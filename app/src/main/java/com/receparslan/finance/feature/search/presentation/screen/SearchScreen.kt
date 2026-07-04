package com.receparslan.finance.feature.search.presentation.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.receparslan.finance.core.ui.components.CryptocurrencyRow
import com.receparslan.finance.core.ui.components.ErrorDialog
import com.receparslan.finance.core.ui.components.ScreenHolder
import com.receparslan.finance.feature.search.presentation.components.SearchBar
import com.receparslan.finance.feature.search.presentation.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // This is the search bar at the top of the screen where users can type their search queries.
    SearchBar(
        viewModel = viewModel,
        state = uiState
    )

    if (uiState.isLoading) {
        ScreenHolder()
        return
    }

    if (uiState.query.isEmpty() && uiState.searchResults.isEmpty())
        ScreenHolder(message = "Type something to search for a cryptocurrency")
    else if (uiState.isNotFound && uiState.searchResults.isEmpty())
        ScreenHolder(message = "No results found for \"${uiState.query}\"")
    else if (uiState.searchResults.isEmpty())
        ScreenHolder(message = "Please press the search button to see results")
    else
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp, 70.dp, 20.dp, 16.dp),
            contentPadding = PaddingValues(top = 7.dp),
        ) {
            items(uiState.searchResults) {
                CryptocurrencyRow(
                    cryptocurrency = it,
                    navController = navController
                )
            }

            item {
                Spacer(Modifier.height(100.dp))
            }
        }


    if (uiState.errorMessage.isNotEmpty())
        ErrorDialog(
            message = uiState.errorMessage,
            onDismiss = { viewModel.clearErrorMessage() },
            onRetry = { viewModel.searchCryptocurrencies() }
        )
}