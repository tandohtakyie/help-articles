package com.aimhigh.shared.domain

import kotlinx.serialization.Serializable

@Serializable
data class BackendErrorResponse(
    val errorCode: String,
    val errorTitle: String,
    val errorMessage: String,
)

sealed class DataError : Exception() {

    data class NetworkError(val errorCause: Throwable? = null) : DataError() {
        override val message: String =
            "Network connection failed. Please check your internet connection."
    }

    data class Timeout(val errorCause: Throwable? = null) : DataError() {
        override val message: String = "Request timed out. Please try again."
    }

    data class ServerError(val code: Int, val errorCause: Throwable? = null) : DataError() {
        override val message: String = "Server error ($code). Please try again later."
    }

    // Backend errors
    data class BackendError(
        val errorCode: String,
        val errorTitle: String,
        val errorMessage: String
    ) : DataError() {
        override val message: String = "$errorTitle: $errorMessage"
    }

    // Parse/Unknown errors
    data class ParseError(val errorCause: Throwable? = null) : DataError() {
        override val message: String = "Failed to parse response. Please try again."
    }

    data class Unknown(val errorCause: Throwable? = null) : DataError() {
        override val message: String = cause?.message ?: "An unknown error occurred."
    }
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: DataError) : Result<Nothing>()
}
