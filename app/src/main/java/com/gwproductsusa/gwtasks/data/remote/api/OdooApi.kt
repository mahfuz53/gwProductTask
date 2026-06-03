package com.gwproductsusa.gwtasks.data.remote.api

import com.google.gson.JsonElement
import com.gwproductsusa.gwtasks.data.remote.dto.JsonRpcRequestDto
import com.gwproductsusa.gwtasks.data.remote.dto.JsonRpcResponseDto
import com.gwproductsusa.gwtasks.data.remote.dto.StageDto
import com.gwproductsusa.gwtasks.data.remote.dto.TaskDto
import com.gwproductsusa.gwtasks.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.POST

interface OdooApi {

    @POST("jsonrpc")
    suspend fun login(@Body request: JsonRpcRequestDto): JsonRpcResponseDto<JsonElement>

    @POST("jsonrpc")
    suspend fun searchReadUsers(@Body request: JsonRpcRequestDto): JsonRpcResponseDto<List<UserDto>>

    @POST("jsonrpc")
    suspend fun searchReadTasks(@Body request: JsonRpcRequestDto): JsonRpcResponseDto<List<TaskDto>>

    @POST("jsonrpc")
    suspend fun createTask(@Body request: JsonRpcRequestDto): JsonRpcResponseDto<Int>

    @POST("jsonrpc")
    suspend fun searchReadStages(@Body request: JsonRpcRequestDto): JsonRpcResponseDto<List<StageDto>>

    @POST("jsonrpc")
    suspend fun updateTask(@Body request: JsonRpcRequestDto): JsonRpcResponseDto<Boolean>

    @POST("jsonrpc")
    suspend fun updateUser(@Body request: JsonRpcRequestDto): JsonRpcResponseDto<Boolean>
}
