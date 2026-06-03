package com.gwproductsusa.gwtasks.domain.usecase

import com.gwproductsusa.gwtasks.core.util.Result
import com.gwproductsusa.gwtasks.domain.model.Stage
import com.gwproductsusa.gwtasks.domain.repository.OdooRepository
import javax.inject.Inject

class LoadStagesUseCase @Inject constructor(
    private val repository: OdooRepository
) {
    suspend operator fun invoke(): Result<List<Stage>> = repository.fetchStages()
}
