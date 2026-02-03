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
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import com.receparslan.finance.EmptyScreenHolder
import com.receparslan.finance.LoadingScreenHolder
import com.receparslan.finance.R
import com.receparslan.finance.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    // This is the list of cryptocurrencies that match the search query
    val cryptocurrencySearchList = viewModel.cryptocurrencySearchList

    val query by viewModel.query // This is the current search query

    // This is the state that indicates whether the app is currently loading data
    val isLoading by viewModel.isLoading

    // This is the state that indicates whether no results were found
    val isNotFound by viewModel.isNotFound

    // Search bar at the top of the screen
    SearchBar()

    // Display different content based on the search results and loading state
    if (isNotFound)
        EmptyScreenHolder("No results found for \"$query\"")
    else if (isLoading)
        LoadingScreenHolder()
    else
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp, 70.dp, 20.dp, 16.dp),
            contentPadding = PaddingValues(top = 7.dp),
        ) {
            items(cryptocurrencySearchList) {
                CryptocurrencyRow(it, navController)

                // Show a spacer at the end of the list to show cryptocurrency on the bottom bar
                if (cryptocurrencySearchList.lastOrNull() == it)
                    Spacer(Modifier.height(100.dp))
            }
        }
}

// Composable function for the search bar
@Composable
private fun SearchBar(
    viewModel: SearchViewModel = hiltViewModel()
) {
    // This is the state that holds the current search query
    var searchQuery by viewModel.query

    // This is used to manage focus on the keyboard
    val focusManager = LocalFocusManager.current

    TextField(
        value = searchQuery,
        onValueChange = {
            @Suppress("AssignedValueIsNeverRead") // It is used in the ViewModel
            searchQuery = it
        },
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
                    @Suppress("AssignedValueIsNeverRead") // It is used in the ViewModel
                    searchQuery = ""
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
            .border(BorderStroke(2.dp, Color(0xFF211E41)), CircleShape),
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
            focusedContainerColor = Color(0xFF211E41),
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            cursorColor = Color(0xFF000000),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color(0xFFA7A7A7),
        )
    )
}