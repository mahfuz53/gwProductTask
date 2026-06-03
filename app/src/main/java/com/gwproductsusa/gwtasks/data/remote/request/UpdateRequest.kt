package com.gwproductsusa.gwtasks.data.remote.request

import com.google.gson.Gson
import com.google.gson.JsonElement

class UpdateRequest(
    gson: Gson,
    requestId: Int,
    private val database: String,
    private val userId: Int,
    private val password: String,
    private val model: String,
    private val recordIds: List<Int>,
    private val values: Map<String, Any?>
) : JsonRpcRequestBuilder(gson, requestId) {

    override val service: String = "object"
    override val method: String = "execute_kw"

    override fun buildArgs(): List<JsonElement> = listOf(
        stringArg(database),
        intArg(userId),
        stringArg(password),
        stringArg(model),
        stringArg("write"),
        toJsonElement(listOf(recordIds, values))
    )

    companion object {
        fun forTask(
            gson: Gson,
            database: String,
            userId: Int,
            password: String,
            taskId: Int,
            values: Map<String, Any?>,
            requestId: Int = 4
        ): UpdateRequest = UpdateRequest(
            gson = gson,
            requestId = requestId,
            database = database,
            userId = userId,
            password = password,
            model = "project.task",
            recordIds = listOf(taskId),
            values = values
        )

        fun forUser(
            gson: Gson,
            database: String,
            userId: Int,
            password: String,
            targetUserId: Int,
            values: Map<String, Any?>,
            requestId: Int = 2
        ): UpdateRequest = UpdateRequest(
            gson = gson,
            requestId = requestId,
            database = database,
            userId = userId,
            password = password,
            model = "res.users",
            recordIds = listOf(targetUserId),
            values = values
        )
    }
}
