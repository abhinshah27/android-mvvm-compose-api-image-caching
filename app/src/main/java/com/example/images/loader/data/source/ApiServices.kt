package com.example.images.loader.data.source

import com.example.images.loader.data.model.ImagesResponse
import com.example.images.loader.utils.EndPoints.GET_IMAGES
import retrofit2.http.GET
import retrofit2.http.Headers


/**
 * Interface for defining API services.
 */
interface ApiServices {

    /**
     * Fetches images from the API.
     *
     * @return A list of ImagesResponse objects.
     */
    @GET(GET_IMAGES)
    suspend fun getImages(): List<ImagesResponse>

    /**
     * Fetches images from the API cache.
     *
     * @return A list of ImagesResponse objects.
     */
    @GET(GET_IMAGES)
    @Headers("Cache-Control: only-if-cached, max-stale=604800") // Cache for 7 days
    suspend fun getImagesFromCache(): List<ImagesResponse>
}
