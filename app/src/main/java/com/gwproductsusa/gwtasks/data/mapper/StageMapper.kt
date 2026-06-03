package com.gwproductsusa.gwtasks.data.mapper

import com.gwproductsusa.gwtasks.data.remote.dto.StageDto
import com.gwproductsusa.gwtasks.domain.model.Stage

fun StageDto.toDomain(): Stage = Stage(id = id, name = name)

fun List<StageDto>.toDomainStages(): List<Stage> = map { it.toDomain() }
