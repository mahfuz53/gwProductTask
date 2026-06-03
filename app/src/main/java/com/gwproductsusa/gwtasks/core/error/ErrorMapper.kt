package com.gwproductsusa.gwtasks.core.error

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.gwproductsusa.gwtasks.core.logging.AppLogger
import com.gwproductsusa.gwtasks.data.remote.dto.JsonRpcErrorDto
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorMapper @Inject constructor(
    private val appLogger: AppLogger,
    private val gson: Gson
) {

    fun mapThrowable(throwable: Throwable): AppError {
        appLogger.logException(AppLogger.TAG_ERROR, throwable, "Mapping throwable")
        return when (throwable) {
        is SocketTimeoutException -> {
            appLogger.logAppError(AppLogger.TAG_ERROR, "Timeout", throwable.message.orEmpty())
            AppError.Timeout
        }
        is UnknownHostException -> {
            appLogger.logAppError(AppLogger.TAG_ERROR, "NoInternet", throwable.message.orEmpty())
            AppError.NoInternet
        }
        is IOException -> {
            val isTimeout = throwable.message?.contains("timeout", ignoreCase = true) == true
            appLogger.logAppError(
                AppLogger.TAG_ERROR,
                if (isTimeout) "Timeout" else "NoInternet",
                throwable.message.orEmpty()
            )
            if (isTimeout) AppError.Timeout else AppError.NoInternet
        }
        is HttpException -> {
            appLogger.logAppError(
                AppLogger.TAG_ERROR,
                "HttpException",
                "code=${throwable.code()} message=${throwable.message()}"
            )
            AppError.OdooServerError(
                message = "Server error (${throwable.code()}). Please try again later."
            )
        }
        is JsonSyntaxException -> {
            appLogger.logAppError(AppLogger.TAG_ERROR, "ParsingError", throwable.message.orEmpty())
            AppError.ParsingError
        }
        else -> {
            appLogger.logAppError(AppLogger.TAG_ERROR, "Unknown", throwable.message.orEmpty())
            AppError.Unknown(throwable.message ?: AppError.Unknown().message)
        }
    }
    }

    fun mapJsonRpcError(error: JsonRpcErrorDto?): AppError {
        if (error == null) return AppError.Unknown()
        appLogger.logJsonRpcError(
            operation = "jsonrpc_error",
            errorPayload = gson.toJson(error)
        )
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
            else -> {
                appLogger.logAppError(AppLogger.TAG_ERROR, "OdooServerError", fullMessage)
                AppError.OdooServerError(fullMessage)
            }
        }
    }
}
