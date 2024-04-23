package com.example.images.loader.data.common
/**
 * A sealed class representing different states of a resource, such as success, loading, or error.
 *
 * @param T The type of data contained in the resource.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}