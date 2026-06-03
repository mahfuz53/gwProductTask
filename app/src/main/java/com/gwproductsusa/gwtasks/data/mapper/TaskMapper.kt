package com.gwproductsusa.gwtasks.data.mapper

import com.gwproductsusa.gwtasks.data.remote.dto.TaskDto
import com.gwproductsusa.gwtasks.domain.model.Task
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun TaskDto.toDomain(): Task {
    val (stageIdValue, stageName) = extractStage(stageId)
    return Task(
        id = id,
        name = name,
        stageId = stageIdValue,
        stageName = stageName,
        description = extractDescription(description),
        dueDate = formatDueDate(dateDeadline)
    )
}

fun List<TaskDto>.toDomainTasks(): List<Task> = map { it.toDomain() }

private fun extractStage(stageId: List<com.google.gson.JsonElement>?): Pair<Int, String> {
    if (stageId == null || stageId.isEmpty()) return 0 to "Unknown"
    val id = if (stageId[0].isJsonPrimitive && stageId[0].asJsonPrimitive.isNumber) {
        stageId[0].asInt
    } else {
        0
    }
    val name = if (stageId.size >= 2 && stageId[1].isJsonPrimitive) {
        stageId[1].asString
    } else {
        "Unknown"
    }
    return id to name
}

private fun extractDescription(description: com.google.gson.JsonElement?): String {
    if (description == null || description.isJsonNull) return ""
    if (description.isJsonPrimitive) {
        val primitive = description.asJsonPrimitive
        if (primitive.isBoolean && !primitive.asBoolean) return ""
        if (!primitive.isString) return ""
    } else {
        return ""
    }
    val raw = description.asString
    if (raw.isBlank()) return ""
    return raw
        .replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun formatDueDate(dateDeadline: String?): String {
    if (dateDeadline.isNullOrBlank()) return "No due date"
    return try {
        val parsed = LocalDateTime.parse(
            dateDeadline,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
        parsed.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
    } catch (_: Exception) {
        dateDeadline.substringBefore(" ")
    }
}
