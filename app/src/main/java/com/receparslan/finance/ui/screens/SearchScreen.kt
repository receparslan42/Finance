package com.receparslan.finance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.receparslan.finance.R
import com.receparslan.finance.ui.components.CryptocurrencyRow
import com.receparslan.finance.ui.components.ErrorDialog
import com.receparslan.finance.ui.components.ScreenHolder
import com.receparslan.finance.util.States.SearchUIState
import com.receparslan.finance.viewmodel.SearchViewModel

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

// Composable function for the search bar
@Composable
private fun SearchBar(
    viewModel: SearchViewModel,
    state: SearchUIState
) {
    val focusManager = LocalFocusManager.current

    TextField(
        value = state.query,
        onValueChange = { viewModel.updateQuery(it) },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
            viewModel.searchCryptocurrencies()
        }),
        placeholder = {
            Text(
                text = "Search Cryptocurrency",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Outlined.Clear,
                contentDescription = "Clear",
                modifier = Modifier.clickable {
                    viewModel.updateQuery("")
                }
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                modifier = Modifier.padding(start = 10.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 9.dp, 20.dp, 0.dp)
            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), CircleShape),
        textStyle = TextStyle(
            fontFamily = FontFamily(Font(R.font.poppins)),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        ),
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            cursorColor = Color.Black,
            focusedTextColor = Color.White,
            unfocusedTextColor = MaterialTheme.colorScheme.tertiary,
        )
    )
}