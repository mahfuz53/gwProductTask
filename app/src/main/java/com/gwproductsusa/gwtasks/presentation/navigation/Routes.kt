package com.gwproductsusa.gwtasks.presentation.navigation

import android.net.Uri

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val CREATE_TASK = "create_task"
    const val UPDATE_TASK = "update_task"
    const val UPDATE_ACCOUNT = "update_account"

    fun updateAccount(userId: Int, userName: String): String = buildString {
        append(UPDATE_ACCOUNT)
        append("/$userId")
        append("/${encodeNavArg(userName)}")
    }

    fun updateTask(
        taskId: Int,
        taskTitle: String,
        deadline: String,
        stageId: Int
    ): String = buildString {
        append(UPDATE_TASK)
        append("/$taskId")
        append("/${encodeNavArg(taskTitle)}")
        append("/${encodeNavArg(deadline)}")
        append("/$stageId")
    }

    private fun encodeNavArg(value: String): String = Uri.encode(value)
}
