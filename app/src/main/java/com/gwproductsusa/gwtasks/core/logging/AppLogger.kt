package com.gwproductsusa.gwtasks.core.logging

import android.util.Log
import com.gwproductsusa.gwtasks.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor(
    private val logSanitizer: LogSanitizer
) {

    fun d(tag: String, message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (!BuildConfig.DEBUG) return
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    fun logNetworkRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?
    ) {
        if (!BuildConfig.DEBUG) return
        val sanitizedHeaders = logSanitizer.sanitizeHeaders(headers)
        val sanitizedBody = logSanitizer.sanitizeAndFormat(body)
        d(
            TAG_NETWORK,
            buildString {
                appendLine("────────── HTTP REQUEST ──────────")
                appendLine("URL: $method $url")
                appendLine("Headers:")
                sanitizedHeaders.forEach { (key, value) ->
                    appendLine("  $key: $value")
                }
                appendLine("Body:")
                append(sanitizedBody)
                appendLine()
                appendLine("──────────────────────────────────")
            }
        )
    }

    fun logNetworkResponse(
        url: String,
        method: String,
        statusCode: Int,
        headers: Map<String, String>,
        body: String?,
        durationMs: Long
    ) {
        if (!BuildConfig.DEBUG) return
        val sanitizedHeaders = logSanitizer.sanitizeHeaders(headers)
        val sanitizedBody = logSanitizer.sanitizeAndFormat(body)
        d(
            TAG_NETWORK,
            buildString {
                appendLine("────────── HTTP RESPONSE ──────────")
                appendLine("URL: $method $url")
                appendLine("Status: $statusCode")
                appendLine("Duration: ${durationMs}ms")
                appendLine("Headers:")
                sanitizedHeaders.forEach { (key, value) ->
                    appendLine("  $key: $value")
                }
                appendLine("Body:")
                append(sanitizedBody)
                appendLine()
                appendLine("───────────────────────────────────")
            }
        )
    }

    fun logNetworkFailure(
        url: String,
        method: String,
        throwable: Throwable,
        durationMs: Long? = null
    ) {
        if (!BuildConfig.DEBUG) return
        e(
            TAG_NETWORK,
            buildString {
                appendLine("────────── HTTP FAILURE ──────────")
                appendLine("URL: $method $url")
                durationMs?.let { appendLine("Duration: ${it}ms") }
                appendLine("Exception: ${throwable.javaClass.simpleName}")
                appendLine("Message: ${throwable.message ?: "No message"}")
                appendLine("─────────────────────────────────")
            },
            throwable
        )
    }

    fun logJsonRpcRequest(operation: String, payload: String?) {
        if (!BuildConfig.DEBUG) return
        d(
            TAG_JSON_RPC,
            buildString {
                appendLine("────── JSON-RPC REQUEST [$operation] ──────")
                append(logSanitizer.sanitizeAndFormat(payload))
                appendLine()
                appendLine("──────────────────────────────────────────")
            }
        )
    }

    fun logJsonRpcResponse(operation: String, payload: String?) {
        if (!BuildConfig.DEBUG) return
        d(
            TAG_JSON_RPC,
            buildString {
                appendLine("────── JSON-RPC RESPONSE [$operation] ──────")
                append(logSanitizer.sanitizeAndFormat(payload))
                appendLine()
                appendLine("───────────────────────────────────────────")
            }
        )
    }

    fun logJsonRpcError(operation: String, errorPayload: String?) {
        if (!BuildConfig.DEBUG) return
        e(
            TAG_JSON_RPC,
            buildString {
                appendLine("────── JSON-RPC ERROR [$operation] ──────")
                append(logSanitizer.sanitizeAndFormat(errorPayload))
                appendLine()
                appendLine("────────────────────────────────────────")
            }
        )
    }

    fun logAppError(tag: String, errorType: String, message: String) {
        if (!BuildConfig.DEBUG) return
        e(tag, "[$errorType] $message")
    }

    fun logException(tag: String, throwable: Throwable, context: String? = null) {
        if (!BuildConfig.DEBUG) return
        val prefix = context?.let { "$it: " }.orEmpty()
        e(
            tag,
            buildString {
                append(prefix)
                append("Exception: ${throwable.javaClass.simpleName}")
                append(" | Message: ${throwable.message ?: "No message"}")
            },
            throwable
        )
    }

    companion object {
        const val TAG_NETWORK = "GwTasks/Network"
        const val TAG_JSON_RPC = "GwTasks/JsonRpc"
        const val TAG_REPOSITORY = "GwTasks/Repository"
        const val TAG_ERROR = "GwTasks/Error"
    }
}
