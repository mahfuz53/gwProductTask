package com.gwproductsusa.gwtasks.domain.usecase

import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.domain.model.Task
import com.gwproductsusa.gwtasks.domain.model.User
import com.gwproductsusa.gwtasks.domain.repository.OdooRepository
import javax.inject.Inject

class LoadDashboardUseCase @Inject constructor(
    private val repository: OdooRepository
) {
    suspend operator fun invoke(userId: Int): Result<Pair<User, List<Task>>> =
        repository.fetchDashboardData(userId)
}
