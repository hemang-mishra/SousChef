package com.souschef.util

/**
 * Represents the categories of errors that can occur in network/data operations.
 */
enum class ResponseError(val message: String) {
    NETWORK_ERROR("A network error occurred. Please check your connection."),
    AUTH_ERROR("Authentication failed. Please sign in again."),
    PERMISSION_DENIED("You don't have permission to perform this action."),
    NOT_FOUND("The requested resource was not found."),
    ALREADY_EXISTS("This item already exists."),
    INVALID_DATA("Invalid data provided."),
    TIMEOUT("The request timed out. Please try again."),
    UNKNOWN("An unexpected error occurred. Please try again.")
}

