package com.gwproductsusa.gwtasks.core.util

import com.gwproductsusa.gwtasks.core.error.AppError

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: AppError) : Result<Nothing>()
}
