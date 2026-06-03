package com.gwproductsusa.gwtasks.data.remote.request

import com.google.gson.Gson

class SearchReadRequest(
    gson: Gson,
    requestId: Int,
    database: String,
    userId: Int,
    password: String,
    model: String,
    domain: List<List<Any?>> = emptyList(),
    fields: List<String>,
    limit: Int? = null
) : ExecuteKwRequest(
    gson = gson,
    requestId = requestId,
    database = database,
    userId = userId,
    password = password,
    model = model,
    odooMethod = "search_read",
    domain = domain,
    kwargs = buildMap {
        put("fields", fields)
        limit?.let { put("limit", it) }
    }
) {
    companion object {
        fun forUser(
            gson: Gson,
            database: String,
            userId: Int,
            password: String,
            targetUserId: Int,
            requestId: Int = 1
        ): SearchReadRequest = SearchReadRequest(
            gson = gson,
            requestId = requestId,
            database = database,
            userId = userId,
            password = password,
            model = "res.users",
            domain = listOf(listOf("id", "=", targetUserId)),
            fields = listOf("id", "name", "email", "login")
        )

        fun forTasks(
            gson: Gson,
            database: String,
            userId: Int,
            password: String,
            limit: Int = 20,
            requestId: Int = 2
        ): SearchReadRequest = SearchReadRequest(
            gson = gson,
            requestId = requestId,
            database = database,
            userId = userId,
            password = password,
            model = "project.task",
            domain = emptyList(),
            fields = listOf("id", "name", "stage_id", "description", "date_deadline"),
            limit = limit
        )
    }
}
