package com.gwproductsusa.gwtasks.data.remote.request

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.gwproductsusa.gwtasks.core.util.OdooConstants
import com.gwproductsusa.gwtasks.data.remote.dto.JsonRpcParamsDto
import com.gwproductsusa.gwtasks.data.remote.dto.JsonRpcRequestDto

abstract class JsonRpcRequestBuilder(
    protected val gson: Gson,
    protected val requestId: Int
) {
    protected abstract val service: String
    protected abstract val method: String
    protected abstract fun buildArgs(): List<JsonElement>

    fun build(): JsonRpcRequestDto = JsonRpcRequestDto(
        jsonrpc = OdooConstants.JSONRPC_VERSION,
        method = OdooConstants.JSONRPC_METHOD,
        params = JsonRpcParamsDto(
            service = service,
            method = method,
            args = buildArgs()
        ),
        id = requestId
    )

    protected fun stringArg(value: String): JsonElement = JsonPrimitive(value)
    protected fun intArg(value: Int): JsonElement = JsonPrimitive(value)
    protected fun toJsonElement(value: Any?): JsonElement = gson.toJsonTree(value)
}
