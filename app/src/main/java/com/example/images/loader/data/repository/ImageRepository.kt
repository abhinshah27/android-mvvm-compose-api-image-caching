package com.example.images.loader.data.repository


import android.content.Context
import com.example.images.loader.R
import com.example.images.loader.data.common.Resource
import com.example.images.loader.data.model.ImagesResponse
import com.example.images.loader.data.source.ApiServices
import com.example.images.loader.utils.NetworkUtils
import javax.inject.Inject

/**
 * Repository class for fetching images.
 *
 * @param context The application context.
 * @param apiService The API service for making network requests.
 */
class ImageRepository @Inject constructor(
    private val context: Context,
    private val apiService: ApiServices
) {

    /**
     * Fetches images from the API or cache.
     *
     * @return A Resource object containing the list of images or an error message.
     */
    suspend fun getImages(): Resource<List<ImagesResponse>> {
        // Check if there's internet connection
        if (NetworkUtils.isInternetAvailable(context)) {
            // Fetch images from the API
            return try {
                val images = apiService.getImages()
                Resource.Success(images)
            } catch (e: Exception) {
                Resource.Error(e.message)
            }
        } else {
            // If there's no internet, attempt to load data from cache
            return try {
                val cachedImages = apiService.getImagesFromCache()
                Resource.Success(cachedImages)
            } catch (cacheException: Exception) {
                Resource.Error(context.getString(R.string.internet_not_available))
            }
        }
    }
}