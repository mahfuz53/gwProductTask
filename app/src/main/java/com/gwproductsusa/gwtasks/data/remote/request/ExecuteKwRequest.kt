package com.gwproductsusa.gwtasks.data.remote.request

import com.google.gson.Gson
import com.google.gson.JsonElement

open class ExecuteKwRequest(
    gson: Gson,
    requestId: Int,
    protected val database: String,
    protected val userId: Int,
    protected val password: String,
    protected val model: String,
    protected val odooMethod: String,
    protected val domain: List<List<Any?>>,
    protected val kwargs: Map<String, Any?>
) : JsonRpcRequestBuilder(gson, requestId) {

    override val service: String = "object"
    override val method: String = "execute_kw"

    override fun buildArgs(): List<JsonElement> = listOf(
        stringArg(database),
        intArg(userId),
        stringArg(password),
        stringArg(model),
        stringArg(odooMethod),
        toJsonElement(domain),
        toJsonElement(kwargs)
    )
}
