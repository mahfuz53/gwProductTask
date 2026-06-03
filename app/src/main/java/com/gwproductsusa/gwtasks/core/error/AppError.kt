package com.gwproductsusa.gwtasks.core.error

sealed class AppError {
    abstract val message: String

    data object NoInternet : AppError() {
        override val message: String = "No internet connection. Please check your network and try again."
    }

    data object Timeout : AppError() {
        override val message: String = "Request timed out. Please try again."
    }

    data object AuthenticationFailure : AppError() {
        override val message: String = "Session expired. Please log in again."
    }

    data object InvalidCredentials : AppError() {
        override val message: String = "Invalid credentials. Please try again."
    }

    data class OdooServerError(override val message: String) : AppError()

    data object ParsingError : AppError() {
        override val message: String = "Unable to process server response. Please try again."
    }

    data class Unknown(override val message: String = "Something went wrong. Please try again.") : AppError()
}
