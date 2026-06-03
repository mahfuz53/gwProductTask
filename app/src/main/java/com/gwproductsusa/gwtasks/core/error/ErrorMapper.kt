package com.gwproductsusa.gwtasks.core.error

import com.gwproductsusa.gwtasks.data.remote.dto.JsonRpcErrorDto
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorMapper @Inject constructor() {

    fun mapThrowable(throwable: Throwable): AppError = when (throwable) {
        is SocketTimeoutException -> AppError.Timeout
        is UnknownHostException -> AppError.NoInternet
        is IOException -> {
            if (throwable.message?.contains("timeout", ignoreCase = true) == true) {
                AppError.Timeout
            } else {
                AppError.NoInternet
            }
        }
        is HttpException -> {
            AppError.OdooServerError(
                message = "Server error (${throwable.code()}). Please try again later."
            )
        }
        is JsonSyntaxException -> AppError.ParsingError
        else -> AppError.Unknown(throwable.message ?: AppError.Unknown().message)
    }

    fun mapJsonRpcError(error: JsonRpcErrorDto?): AppError {
        if (error == null) return AppError.Unknown()
        val dataMessage = error.data?.message?.toString()
        val fullMessage = listOfNotNull(error.message, dataMessage)
            .joinToString(": ")
            .ifBlank { "Server error occurred." }

        return when {
            fullMessage.contains("Access Denied", ignoreCase = true) ||
                fullMessage.contains("Invalid", ignoreCase = true) ||
                error.code == 200 && fullMessage.contains("login", ignoreCase = true) ->
                AppError.InvalidCredentials
            fullMessage.contains("Session", ignoreCase = true) ||
                fullMessage.contains("Authentication", ignoreCase = true) ->
                AppError.AuthenticationFailure
            else -> AppError.OdooServerError(fullMessage)
        }
    }
}
