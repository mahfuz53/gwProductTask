package com.gwproductsusa.gwtasks.core.network

import com.gwproductsusa.gwtasks.core.logging.AppLogger
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkLoggingInterceptor @Inject constructor(
    private val appLogger: AppLogger
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val (request, requestBody) = cloneRequestWithReadableBody(originalRequest)
        val operation = resolveJsonRpcOperation(requestBody)

        appLogger.logNetworkRequest(
            url = request.url.toString(),
            method = request.method,
            headers = request.headers.toMap(),
            body = requestBody
        )
        appLogger.logJsonRpcRequest(operation, requestBody)

        val startNs = System.nanoTime()
        return try {
            val response = chain.proceed(request)
            val durationMs = (System.nanoTime() - startNs) / 1_000_000
            logResponse(request, response, durationMs, operation)
            response
        } catch (throwable: Throwable) {
            val durationMs = (System.nanoTime() - startNs) / 1_000_000
            appLogger.logNetworkFailure(
                url = request.url.toString(),
                method = request.method,
                throwable = throwable,
                durationMs = durationMs
            )
            throw throwable
        }
    }

    private fun logResponse(
        request: Request,
        response: Response,
        durationMs: Long,
        operation: String
    ) {
        val responseBody = peekResponseBody(response)
        val headers = response.headers.toMap()

        appLogger.logNetworkResponse(
            url = request.url.toString(),
            method = request.method,
            statusCode = response.code,
            headers = headers,
            body = responseBody,
            durationMs = durationMs
        )
        appLogger.logJsonRpcResponse(operation, responseBody)

        if (!response.isSuccessful) {
            appLogger.logJsonRpcError(
                operation = "$operation (HTTP ${response.code})",
                errorPayload = responseBody
            )
        }
    }

    /**
     * Reads the request body for logging and returns a new request with a fresh body instance,
     * so the outgoing request is not sent with an consumed body.
     */
    private fun cloneRequestWithReadableBody(request: Request): Pair<Request, String?> {
        val body = request.body ?: return request to null
        return try {
            val buffer = Buffer()
            body.writeTo(buffer)
            val bodyString = buffer.readUtf8()
            val clonedRequest = request.newBuilder()
                .method(request.method, bodyString.toRequestBody(body.contentType()))
                .build()
            clonedRequest to bodyString
        } catch (_: Exception) {
            request to "(unable to read request body)"
        }
    }

    private fun peekResponseBody(response: Response): String? {
        return try {
            val peeked = response.peekBody(MAX_PEEK_BYTES)
            peeked.string()
        } catch (_: Exception) {
            "(unable to read response body)"
        }
    }

    private fun resolveJsonRpcOperation(requestBody: String?): String {
        if (requestBody.isNullOrBlank()) return "unknown"
        return try {
            when {
                requestBody.contains("\"method\":\"login\"") ||
                    requestBody.contains("\"method\": \"login\"") -> "login"
                requestBody.contains("\"search_read\"") -> {
                    when {
                        requestBody.contains("res.users") -> "search_read_users"
                        requestBody.contains("project.task") -> "search_read_tasks"
                        else -> "search_read"
                    }
                }
                requestBody.contains("\"create\"") -> "create"
                requestBody.contains("\"write\"") -> "write"
                requestBody.contains("\"unlink\"") -> "unlink"
                requestBody.contains("execute_kw") -> "execute_kw"
                else -> "jsonrpc"
            }
        } catch (_: Exception) {
            "unknown"
        }
    }

    companion object {
        private const val MAX_PEEK_BYTES = 1024 * 1024L
    }
}
