package com.souschef.usecases.admin

import com.souschef.model.auth.UserProfile
import com.souschef.repository.auth.AuthRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Phase 8 — toggle a user's verified-chef status.
 *
 * Guards on the caller being an admin. Firestore security rules must enforce
 * the same on the server side; this is purely a client-side defence.
 */
class ChefVerificationUseCase(
    private val authRepository: AuthRepository
) {
    fun execute(
        admin: UserProfile?,
        targetUid: String,
        isVerified: Boolean
    ): Flow<Resource<Unit>> {
        if (admin == null || !admin.isAdmin) {
            return flowOf(Resource.failure(message = "Only admins can change verified-chef status"))
        }
        return authRepository.setVerifiedChef(targetUid, isVerified)
    }
}
