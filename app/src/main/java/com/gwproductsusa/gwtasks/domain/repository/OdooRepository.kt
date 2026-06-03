package com.gwproductsusa.gwtasks.domain.repository

import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.domain.model.Task
import com.gwproductsusa.gwtasks.domain.model.User

interface OdooRepository {
    suspend fun login(email: String, password: String): Result<Int>
    suspend fun fetchUser(userId: Int): Result<User>
    suspend fun fetchTasks(): Result<List<Task>>
    suspend fun fetchDashboardData(userId: Int): Result<Pair<User, List<Task>>>
    suspend fun logout()
}
