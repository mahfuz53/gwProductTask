package com.gwproductsusa.gwtasks.domain.usecase

import com.gwproductsusa.gwtasks.core.session.SessionManager
import javax.inject.Inject

class CheckSessionUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Boolean = sessionManager.isLoggedInSync()

    suspend fun getUserId(): Int = sessionManager.getUserId()
}
