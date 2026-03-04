package com.souschef.util

import com.souschef.util.ResponseError

/**
 * A generic wrapper class that represents the state of a data operation.
 * Use this to communicate Loading, Success, and Failure states from
 * repositories/use-cases to ViewModels and UI.
 */
sealed class Resource<out T> {

    /** Indicates an in-progress operation. */
    data object Loading : Resource<Nothing>()

    /** Indicates a successful operation with result data. */
    data class Success<T>(val data: T) : Resource<T>()

    /** Indicates a failed operation with an optional error and message. */
    data class Failure(
        val error: ResponseError = ResponseError.UNKNOWN,
        val message: String? = null
    ) : Resource<Nothing>()

    companion object {
        fun <T> loading(): Resource<T> = Loading
        fun <T> success(data: T): Resource<T> = Success(data)
        fun <T> failure(
            error: ResponseError = ResponseError.UNKNOWN,
            message: String? = null
        ): Resource<T> = Failure(error, message)
    }

    val isLoading get() = this is Loading
    val isSuccess get() = this is Success
    val isFailure get() = this is Failure
}

