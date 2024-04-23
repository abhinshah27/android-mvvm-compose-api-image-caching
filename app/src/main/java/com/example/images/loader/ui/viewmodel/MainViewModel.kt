package com.example.images.loader.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.images.loader.data.common.Resource
import com.example.images.loader.data.model.ImagesResponse
import com.example.images.loader.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the main screen.
 *
 * @param repository The ImageRepository instance for fetching images.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    // StateFlow to hold the list of images or error message
    private val _images = MutableStateFlow<Resource<List<ImagesResponse>>>(Resource.Loading)
    val images: StateFlow<Resource<List<ImagesResponse>>> = _images.asStateFlow()

    // MutableState to hold the set of failed image URLs
    private val failedUrls = mutableStateOf<Set<String>>(emptySet())


    init {
        // Load images when the ViewModel is initialized
        loadImages()
    }

    /**
     * Marks an image URL as failed.
     *
     * @param url The URL of the failed image.
     */
    fun markAsFailed(url: String) {
        failedUrls.value += url
    }

    /**
     * Checks if an image URL has failed.
     *
     * @param url The URL of the image.
     * @return True if the image URL has failed, false otherwise.
     */
    fun isFailed(url: String): Boolean {
        return url in failedUrls.value
    }

    /**
     * Loads images from the repository.
     */
    fun loadImages() {
        viewModelScope.launch {
            try {
                val result = repository.getImages()
                _images.value = result
            } catch (e: Exception) {
                _images.value = Resource.Error(e.message)
            }
        }
    }
}