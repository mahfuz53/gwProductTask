package com.gwproductsusa.gwtasks.data.remote.request

import com.google.gson.Gson
import com.google.gson.JsonElement

class CreateRequest(
    gson: Gson,
    requestId: Int,
    private val database: String,
    private val userId: Int,
    private val password: String,
    private val model: String,
    private val values: Map<String, Any?>
) : JsonRpcRequestBuilder(gson, requestId) {

    override val service: String = "object"
    override val method: String = "execute_kw"

    override fun buildArgs(): List<JsonElement> = listOf(
        stringArg(database),
        intArg(userId),
        stringArg(password),
        stringArg(model),
        stringArg("create"),
        toJsonElement(listOf(values)),
        toJsonElement(emptyMap<String, Any?>())
    )

    companion object {
        fun forTask(
            gson: Gson,
            database: String,
            userId: Int,
            password: String,
            values: Map<String, Any?>,
            requestId: Int = 3
        ): CreateRequest = CreateRequest(
            gson = gson,
            requestId = requestId,
            database = database,
            userId = userId,
            password = password,
            model = "project.task",
            values = values
        )
    }
}
