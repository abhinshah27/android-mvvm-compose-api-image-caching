package com.example.images.loader.utils


// Constants object for storing constants used throughout the application.
object Constants {
    const val BASE_URL = "https://acharyaprashant.org/"
    const val CALL_TIMEOUT = 30L
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}

// EndPoints object for storing API endpoints used in the application.
object EndPoints {
    const val GET_IMAGES = "api/v2/content/misc/media-coverages?limit=100"
}