package com.receparslan.finance.util

// Sealed class to represent the state of a resource (Success, Error) with generic type T for data handling
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}