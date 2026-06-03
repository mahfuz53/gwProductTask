package com.gwproductsusa.gwtasks.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class JsonRpcRequestDto(
    @SerializedName("jsonrpc") val jsonrpc: String,
    @SerializedName("method") val method: String,
    @SerializedName("params") val params: JsonRpcParamsDto,
    @SerializedName("id") val id: Int
)

data class JsonRpcParamsDto(
    @SerializedName("service") val service: String,
    @SerializedName("method") val method: String,
    @SerializedName("args") val args: List<JsonElement>
)

data class JsonRpcResponseDto<T>(
    @SerializedName("jsonrpc") val jsonrpc: String?,
    @SerializedName("id") val id: Int?,
    @SerializedName("result") val result: T?,
    @SerializedName("error") val error: JsonRpcErrorDto?
)

data class JsonRpcErrorDto(
    @SerializedName("code") val code: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: JsonRpcErrorDataDto?
)

data class JsonRpcErrorDataDto(
    @SerializedName("name") val name: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("arguments") val arguments: List<String>?,
    @SerializedName("debug") val debug: String?
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String?,
    @SerializedName("login") val login: String?
)

data class TaskDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stage_id") val stageId: List<JsonElement>?,
    @SerializedName("description") val description: JsonElement?,
    @SerializedName("date_deadline") val dateDeadline: String?
)
