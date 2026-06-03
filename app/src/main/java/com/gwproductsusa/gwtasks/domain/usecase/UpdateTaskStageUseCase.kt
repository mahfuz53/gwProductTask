package com.gwproductsusa.gwtasks.domain.usecase

import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.domain.repository.OdooRepository
import javax.inject.Inject

class UpdateTaskStageUseCase @Inject constructor(
    private val repository: OdooRepository
) {
    suspend operator fun invoke(taskId: Int, stageId: Int): Result<Boolean> =
        repository.updateTaskStage(taskId, stageId)
}
