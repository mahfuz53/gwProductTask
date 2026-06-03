package com.gwproductsusa.gwtasks.core.logging

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogSanitizer @Inject constructor(
    private val gson: Gson
) {

    private val prettyGson: Gson by lazy {
        GsonBuilder().setPrettyPrinting().serializeNulls().create()
    }

    fun sanitizeAndFormat(raw: String?): String {
        if (raw.isNullOrBlank()) return "(empty)"
        return try {
            val element = gson.fromJson(raw, JsonElement::class.java)
            prettyGson.toJson(redactJsonElement(element))
        } catch (_: Exception) {
            redactPlainText(raw)
        }
    }

    fun sanitizeHeaders(headers: Map<String, String>): Map<String, String> =
        headers.mapValues { (key, value) ->
            if (SENSITIVE_HEADER_KEYS.any { key.equals(it, ignoreCase = true) }) {
                REDACTED
            } else {
                value
            }
        }

    private fun redactJsonElement(element: JsonElement): JsonElement = when {
        element.isJsonObject -> {
            val result = JsonObject()
            element.asJsonObject.entrySet().forEach { (key, value) ->
                result.add(
                    key,
                    when {
                        SENSITIVE_JSON_KEYS.any { key.equals(it, ignoreCase = true) } ->
                            JsonPrimitive(REDACTED)
                        key == "args" && value.isJsonArray ->
                            redactOdooArgsArray(value.asJsonArray)
                        else -> redactJsonElement(value)
                    }
                )
            }
            result
        }
        element.isJsonArray -> {
            val result = JsonArray()
            element.asJsonArray.forEach { item ->
                result.add(redactJsonElement(item))
            }
            result
        }
        else -> element
    }

    /**
     * Odoo JSON-RPC login and execute_kw pass credentials as the 3rd argument (index 2).
     */
    private fun redactOdooArgsArray(args: JsonArray): JsonArray {
        val result = JsonArray()
        args.forEachIndexed { index, element ->
            val sanitized = when {
                index == PASSWORD_ARG_INDEX &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isString -> JsonPrimitive(REDACTED)
                else -> redactJsonElement(element)
            }
            result.add(sanitized)
        }
        return result
    }

    private fun redactPlainText(text: String): String {
        var result = text
        SENSITIVE_JSON_KEYS.forEach { key ->
            result = result.replace(
                Regex("""("${Regex.escape(key)}"\s*:\s*)"[^"]*"""", RegexOption.IGNORE_CASE),
                "$1\"$REDACTED\""
            )
        }
        return result
    }

    companion object {
        private const val REDACTED = "***REDACTED***"
        private const val PASSWORD_ARG_INDEX = 2

        private val SENSITIVE_JSON_KEYS = setOf(
            "password",
            "passwd",
            "token",
            "access_token",
            "refresh_token",
            "session_id",
            "api_key",
            "secret",
            "authorization"
        )

        private val SENSITIVE_HEADER_KEYS = setOf(
            "Authorization",
            "Cookie",
            "Set-Cookie",
            "X-Api-Key",
            "X-Auth-Token"
        )
    }
}
