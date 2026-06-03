package com.gwproductsusa.gwtasks.domain.usecase

import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.domain.model.CreateTaskInput
import com.gwproductsusa.gwtasks.domain.repository.OdooRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val repository: OdooRepository
) {
    suspend operator fun invoke(input: CreateTaskInput): Result<Int> =
        repository.createTask(input)
}
