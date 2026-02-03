package com.receparslan.finance.util

// Sealed class to represent the state of a resource (Success, Error, Loading, None) with generic type T for data handling
sealed class Resource<out T> {
    // Represents a successful resource state containing data of type T
    data class Success<out T>(val data: T) : Resource<T>()

    // Represents an error state with an error message
    data class Error(val message: String) : Resource<Nothing>()
}