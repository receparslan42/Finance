package com.receparslan.finance.util

import androidx.compose.foundation.lazy.LazyListState

// This extension function checks if the user has scrolled close to the end of the list (within 25 items) to trigger loading more data.
fun LazyListState.reachedEnd(): Boolean {
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
    return lastVisibleItem.index >= layoutInfo.totalItemsCount - 25
}