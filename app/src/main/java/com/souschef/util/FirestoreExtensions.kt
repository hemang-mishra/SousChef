package com.souschef.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Safely wraps a Firestore/Firebase suspend call in a [Resource].
 * Catches all known Firebase exceptions and maps them to [ResponseError].
 *
 * Usage:
 * ```
 * val result = safeFirestoreCall { firestore.collection("users").document(uid).get().await() }
 * ```
 */
suspend fun <T> safeFirestoreCall(call: suspend () -> T): Resource<T> {
    return try {
        Resource.success(call())
    } catch (e: FirebaseFirestoreException) {
        val error = when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> ResponseError.PERMISSION_DENIED
            FirebaseFirestoreException.Code.NOT_FOUND         -> ResponseError.NOT_FOUND
            FirebaseFirestoreException.Code.ALREADY_EXISTS    -> ResponseError.ALREADY_EXISTS
            FirebaseFirestoreException.Code.UNAUTHENTICATED   -> ResponseError.AUTH_ERROR
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> ResponseError.TIMEOUT
            else                                               -> ResponseError.UNKNOWN
        }
        Resource.failure(error = error, message = e.message)
    } catch (e: FirebaseAuthException) {
        Resource.failure(error = ResponseError.AUTH_ERROR, message = e.message)
    } catch (e: FirebaseNetworkException) {
        Resource.failure(error = ResponseError.NETWORK_ERROR, message = e.message)
    } catch (e: Exception) {
        Resource.failure(error = ResponseError.UNKNOWN, message = e.message)
    }
}

