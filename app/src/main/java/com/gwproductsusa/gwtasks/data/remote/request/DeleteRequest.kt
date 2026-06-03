package com.gwproductsusa.gwtasks.data.remote.request

import com.google.gson.Gson
import com.google.gson.JsonElement

class DeleteRequest(
    gson: Gson,
    requestId: Int,
    private val database: String,
    private val userId: Int,
    private val password: String,
    private val model: String,
    private val recordIds: List<Int>
) : JsonRpcRequestBuilder(gson, requestId) {

    override val service: String = "object"
    override val method: String = "execute_kw"

    override fun buildArgs(): List<JsonElement> = listOf(
        stringArg(database),
        intArg(userId),
        stringArg(password),
        stringArg(model),
        stringArg("unlink"),
        toJsonElement(recordIds)
    )

    companion object {
        fun forTask(
            gson: Gson,
            database: String,
            userId: Int,
            password: String,
            taskId: Int,
            requestId: Int = 5
        ): DeleteRequest = DeleteRequest(
            gson = gson,
            requestId = requestId,
            database = database,
            userId = userId,
            password = password,
            model = "project.task",
            recordIds = listOf(taskId)
        )
    }
}
