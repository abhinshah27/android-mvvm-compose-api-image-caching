package com.example.images.loader.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Object for storing images in memory cache.
 */
object ImageMemoryCache {
    /**
     * The underlying mutable map that stores the cached images.
     */
    private val cache = mutableMapOf<String, Bitmap>()

    /**
     * Gets the bitmap from memory cache for the given key.
     *
     * @param key The key to retrieve the bitmap for.
     * @return The bitmap associated with the key, or null if not found.
     */
    fun get(key: String): Bitmap? {
        return cache[key]
    }

    /**
     * Puts the bitmap into memory cache for the given key.
     *
     * @param key The key to store the bitmap under.
     * @param bitmap The bitmap to store.
     */
    fun put(key: String, bitmap: Bitmap) {
        cache[key] = bitmap
    }
}

/**
 * Object for storing images in disk cache.
 */
object ImageDiskCache {
    /**
     * The name of the directory where the cache is stored.
     */
    private const val CACHE_DIR_NAME = "image_cache"

    /**
     * Gets the bitmap from disk cache for the given URL.
     *
     * @param context The application context.
     * @param url The URL of the image.
     * @return The bitmap associated with the URL, or null if not found.
     */
    fun get(context: Context, url: String): Bitmap? {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
        val file = File(cacheDir, hashKeyForDisk(url))
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    /**
     * Puts the bitmap into disk cache for the given URL.
     *
     * @param context The application context.
     * @param url The URL of the image.
     * @param bitmap The bitmap to store.
     */
    fun put(context: Context, url: String, bitmap: Bitmap) {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val file = File(cacheDir, hashKeyForDisk(url))
        try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Generates a unique hash key for the given URL.
     *
     * @param url The URL to generate a hash key for.
     * @return The hash key as a string.
     */
    private fun hashKeyForDisk(url: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(url.toByteArray())
            val bytes = digest.digest()
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            url.hashCode().toString()
        }
    }
}
/**
 * Fetches an image from the network, memory cache, or disk cache.
 *
 * @param context The application context.
 * @param url The URL of the image.
 * @return The fetched bitmap, or null if an error occurred.
 */
suspend fun fetchImage(context: Context, url: String): Bitmap? {
    return try {
        // Check if image is available in memory cache
        val memoryCacheBitmap = ImageMemoryCache.get(url)
        if (memoryCacheBitmap != null) {
            return memoryCacheBitmap
        }
        // Check if image is available in disk cache
        val diskCacheBitmap = ImageDiskCache.get(context, url)
        if (diskCacheBitmap != null) {
            // Update memory cache with image from disk
            ImageMemoryCache.put(url, diskCacheBitmap)
            return diskCacheBitmap
        }
        // If image is not available in cache, fetch from network
        val fetchedBitmap = downloadImage(url)
        // Update memory and disk cache with fetched image
        fetchedBitmap?.let {
            ImageMemoryCache.put(url, it)
            ImageDiskCache.put(context, url, it)
        }
        fetchedBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Downloads an image from the given URL.
 *
 * @param url The URL of the image to download.
 * @return The downloaded bitmap, or null if an error occurred.
 */
suspend fun downloadImage(url: String): Bitmap? {
    // Use the IO dispatcher for network operations.
    return withContext(Dispatchers.IO) {
        try {
            // Open a connection to the URL.
            val connection = URL(url).openConnection() as HttpURLConnection

            // Set the doInput flag to true to enable reading from the connection.
            connection.doInput = true

            // Connect to the server.
            connection.connect()

            // Get the input stream from the connection.
            val input = connection.inputStream

            // Decode the input stream into a Bitmap object.
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}