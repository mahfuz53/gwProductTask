package com.gwproductsusa.gwtasks.data.remote.request

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.gwproductsusa.gwtasks.core.util.OdooConstants

class LoginRequest(
    gson: Gson,
    requestId: Int,
    private val database: String,
    private val email: String,
    private val password: String
) : JsonRpcRequestBuilder(gson, requestId) {

    override val service: String = "common"
    override val method: String = "login"

    override fun buildArgs(): List<JsonElement> = listOf(
        stringArg(database),
        stringArg(email),
        stringArg(password)
    )

    companion object {
        fun create(
            gson: Gson,
            email: String,
            password: String,
            database: String = OdooConstants.DATABASE_NAME,
            requestId: Int = 1
        ): LoginRequest = LoginRequest(gson, requestId, database, email, password)
    }
}
