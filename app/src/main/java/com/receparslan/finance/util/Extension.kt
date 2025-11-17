package com.receparslan.finance.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf

// This function is used to check if the user has scrolled to the end of the list.
fun LazyListState.reachedEnd(): Boolean {
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
    val state = mutableStateOf(false)

    lastVisibleItem?.let { state.value = it.index >= layoutInfo.totalItemsCount - 25 }

    return state.value
}