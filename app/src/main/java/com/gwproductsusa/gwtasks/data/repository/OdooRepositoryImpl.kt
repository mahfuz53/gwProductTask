package com.gwproductsusa.gwtasks.data.repository

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.gwproductsusa.gwtasks.core.error.AppError
import com.gwproductsusa.gwtasks.core.error.ErrorMapper
import com.gwproductsusa.gwtasks.core.logging.AppLogger
import com.gwproductsusa.gwtasks.core.session.SessionManager
import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.core.util.OdooConstants
import com.gwproductsusa.gwtasks.data.mapper.toDomainStages
import com.gwproductsusa.gwtasks.data.mapper.toDomainTasks
import com.gwproductsusa.gwtasks.data.mapper.toDomainUsers
import com.gwproductsusa.gwtasks.data.remote.api.OdooApi
import com.gwproductsusa.gwtasks.data.remote.request.CreateRequest
import com.gwproductsusa.gwtasks.data.remote.request.LoginRequest
import com.gwproductsusa.gwtasks.data.remote.request.SearchReadRequest
import com.gwproductsusa.gwtasks.data.remote.request.UpdateRequest
import com.gwproductsusa.gwtasks.domain.model.CreateTaskInput
import com.gwproductsusa.gwtasks.domain.model.Stage
import com.gwproductsusa.gwtasks.domain.model.Task
import com.gwproductsusa.gwtasks.domain.model.User
import com.gwproductsusa.gwtasks.domain.repository.OdooRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OdooRepositoryImpl @Inject constructor(
    private val api: OdooApi,
    private val gson: Gson,
    private val sessionManager: SessionManager,
    private val errorMapper: ErrorMapper,
    private val appLogger: AppLogger
) : OdooRepository {

    override suspend fun login(email: String, password: String): Result<Int> = try {
        appLogger.d(AppLogger.TAG_REPOSITORY, "login() started for email=${email.trim()}")
        val request = LoginRequest.create(gson, email, password).build()
        val response = api.login(request)

        response.error?.let { error ->
            appLogger.logJsonRpcError("login", gson.toJson(error))
            return Result.Error(errorMapper.mapJsonRpcError(error))
        }

        val userId = parseLoginResult(response.result)
        if (userId <= 0) {
            appLogger.logAppError(
                AppLogger.TAG_REPOSITORY,
                "InvalidCredentials",
                "Login returned invalid user id"
            )
            Result.Error(AppError.InvalidCredentials)
        } else {
            appLogger.d(AppLogger.TAG_REPOSITORY, "login() success userId=$userId")
            sessionManager.saveSession(
                userId = userId,
                email = email,
                password = password
            )
            Result.Success(userId)
        }
    } catch (e: Exception) {
        appLogger.logException(AppLogger.TAG_REPOSITORY, e, "login() failed")
        Result.Error(errorMapper.mapThrowable(e))
    }

    override suspend fun fetchUser(userId: Int): Result<User> = executeAuthenticated { uid, password, database ->
        appLogger.d(AppLogger.TAG_REPOSITORY, "fetchUser() targetUserId=$userId")
        val request = SearchReadRequest.forUser(
            gson = gson,
            database = database,
            userId = uid,
            password = password,
            targetUserId = userId
        ).build()
        val response = api.searchReadUsers(request)

        when {
            response.error != null ->
                Result.Error(errorMapper.mapJsonRpcError(response.error))
            response.result.isNullOrEmpty() ->
                Result.Error(AppError.Unknown("User information not found."))
            else -> {
                val user = response.result!!.toDomainUsers(userId)
                if (user == null) {
                    Result.Error(AppError.Unknown("User information not found."))
                } else {
                    Result.Success(user)
                }
            }
        }
    }

    override suspend fun fetchTasks(): Result<List<Task>> = executeAuthenticated { uid, password, database ->
        appLogger.d(AppLogger.TAG_REPOSITORY, "fetchTasks() started")
        val request = SearchReadRequest.forTasks(
            gson = gson,
            database = database,
            userId = uid,
            password = password
        ).build()
        val response = api.searchReadTasks(request)

        when {
            response.error != null ->
                Result.Error(errorMapper.mapJsonRpcError(response.error))
            else -> Result.Success(response.result?.toDomainTasks().orEmpty())
        }
    }

    override suspend fun fetchDashboardData(userId: Int): Result<Pair<User, List<Task>>> =
        coroutineScope {
            try {
                val password = sessionManager.getPassword()
                    ?: return@coroutineScope Result.Error(AppError.AuthenticationFailure)
                val database = sessionManager.getDatabaseName()
                val sessionUserId = sessionManager.getUserId()

                val userDeferred = async {
                    fetchUserInternal(sessionUserId, password, database, userId)
                }
                val tasksDeferred = async {
                    fetchTasksInternal(sessionUserId, password, database)
                }

                val userResult = userDeferred.await()
                val tasksResult = tasksDeferred.await()

                when {
                    userResult is Result.Error -> userResult
                    tasksResult is Result.Error -> tasksResult
                    userResult is Result.Success && tasksResult is Result.Success ->
                        Result.Success(userResult.data to tasksResult.data)
                    else -> Result.Error(AppError.Unknown())
                }
            } catch (e: Exception) {
                appLogger.logException(AppLogger.TAG_REPOSITORY, e, "fetchDashboardData() failed")
                Result.Error(errorMapper.mapThrowable(e))
            }
        }

    override suspend fun createTask(input: CreateTaskInput): Result<Int> =
        executeAuthenticated { uid, password, database ->
            try {
                appLogger.d(AppLogger.TAG_REPOSITORY, "createTask() name=${input.name}")
                val values = buildMap<String, Any?> {
                    put("name", input.name)
                    put("project_id", OdooConstants.DEFAULT_PROJECT_ID)
                    put("stage_id", OdooConstants.DEFAULT_STAGE_ID)
                    put("description", input.description.ifBlank { false })
                    put("date_deadline", input.dateDeadline)
                }
                val request = CreateRequest.forTask(
                    gson = gson,
                    database = database,
                    userId = uid,
                    password = password,
                    values = values,
                    requestId = 12
                ).build()
                val response = api.createTask(request)
                response.error?.let {
                    appLogger.logJsonRpcError("create_task", gson.toJson(it))
                    return@executeAuthenticated Result.Error(errorMapper.mapJsonRpcError(it))
                }
                val taskId = response.result
                if (taskId == null || taskId <= 0) {
                    Result.Error(AppError.Unknown("Failed to create task."))
                } else {
                    appLogger.d(AppLogger.TAG_REPOSITORY, "createTask() success id=$taskId")
                    Result.Success(taskId)
                }
            } catch (e: Exception) {
                appLogger.logException(AppLogger.TAG_REPOSITORY, e, "createTask() failed")
                Result.Error(errorMapper.mapThrowable(e))
            }
        }

    override suspend fun fetchStages(): Result<List<Stage>> =
        executeAuthenticated { uid, password, database ->
            try {
                val request = SearchReadRequest.forStages(
                    gson = gson,
                    database = database,
                    userId = uid,
                    password = password
                ).build()
                val response = api.searchReadStages(request)
                response.error?.let { return@executeAuthenticated Result.Error(errorMapper.mapJsonRpcError(it)) }
                Result.Success(response.result?.toDomainStages().orEmpty())
            } catch (e: Exception) {
                Result.Error(errorMapper.mapThrowable(e))
            }
        }

    override suspend fun updateTaskStage(taskId: Int, stageId: Int): Result<Boolean> =
        executeAuthenticated { uid, password, database ->
            try {
                val request = UpdateRequest.forTask(
                    gson = gson,
                    database = database,
                    userId = uid,
                    password = password,
                    taskId = taskId,
                    values = mapOf("stage_id" to stageId),
                    requestId = 14
                ).build()
                val response = api.updateTask(request)
                response.error?.let { return@executeAuthenticated Result.Error(errorMapper.mapJsonRpcError(it)) }
                Result.Success(response.result == true)
            } catch (e: Exception) {
                Result.Error(errorMapper.mapThrowable(e))
            }
        }

    override suspend fun updateUserName(userId: Int, name: String): Result<Boolean> =
        executeAuthenticated { uid, password, database ->
            try {
                val request = UpdateRequest.forUser(
                    gson = gson,
                    database = database,
                    userId = uid,
                    password = password,
                    targetUserId = userId,
                    values = mapOf("name" to name),
                    requestId = 2
                ).build()
                val response = api.updateUser(request)
                response.error?.let { return@executeAuthenticated Result.Error(errorMapper.mapJsonRpcError(it)) }
                Result.Success(response.result == true)
            } catch (e: Exception) {
                Result.Error(errorMapper.mapThrowable(e))
            }
        }

    override suspend fun logout() {
        appLogger.d(AppLogger.TAG_REPOSITORY, "logout() clearing session")
        sessionManager.clearSession()
    }

    private suspend fun fetchUserInternal(
        uid: Int,
        password: String,
        database: String,
        targetUserId: Int
    ): Result<User> = try {
        val request = SearchReadRequest.forUser(
            gson = gson,
            database = database,
            userId = uid,
            password = password,
            targetUserId = targetUserId
        ).build()
        val response = api.searchReadUsers(request)
        response.error?.let { return Result.Error(errorMapper.mapJsonRpcError(it)) }
        val user = response.result?.toDomainUsers(targetUserId)
        if (user == null) Result.Error(AppError.Unknown("User information not found."))
        else Result.Success(user)
    } catch (e: Exception) {
        appLogger.logException(AppLogger.TAG_REPOSITORY, e, "fetchUserInternal() failed")
        Result.Error(errorMapper.mapThrowable(e))
    }

    private suspend fun fetchTasksInternal(
        uid: Int,
        password: String,
        database: String
    ): Result<List<Task>> = try {
        val request = SearchReadRequest.forTasks(
            gson = gson,
            database = database,
            userId = uid,
            password = password
        ).build()
        val response = api.searchReadTasks(request)
        response.error?.let { return Result.Error(errorMapper.mapJsonRpcError(it)) }
        Result.Success(response.result?.toDomainTasks().orEmpty())
    } catch (e: Exception) {
        appLogger.logException(AppLogger.TAG_REPOSITORY, e, "fetchTasksInternal() failed")
        Result.Error(errorMapper.mapThrowable(e))
    }

    private suspend fun <T> executeAuthenticated(
        block: suspend (uid: Int, password: String, database: String) -> Result<T>
    ): Result<T> {
        return try {
            val password = sessionManager.getPassword()
                ?: return Result.Error(AppError.AuthenticationFailure)
            val uid = sessionManager.getUserId()
            if (uid <= 0) return Result.Error(AppError.AuthenticationFailure)
            val database = sessionManager.getDatabaseName()
            block(uid, password, database)
        } catch (e: Exception) {
            appLogger.logException(AppLogger.TAG_REPOSITORY, e, "executeAuthenticated() failed")
            Result.Error(errorMapper.mapThrowable(e))
        }
    }

    private fun parseLoginResult(result: JsonElement?): Int {
        if (result == null || result.isJsonNull) return -1
        return when {
            result.isJsonPrimitive && result.asJsonPrimitive.isNumber ->
                result.asInt
            result.isJsonPrimitive && result.asJsonPrimitive.isBoolean &&
                !result.asBoolean -> -1
            else -> -1
        }
    }
}
