package com.example.images.loader.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {

    /**
     * Checks if there is an active internet connection.
     *
     * @param context The application context.
     * @return True if there is an active internet connection, false otherwise.
     */
    fun isInternetAvailable(context: Context): Boolean {
        // Get the ConnectivityManager service.
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Get the active network.
        val network = connectivityManager.activeNetwork ?: return false

        // Get the network capabilities.
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        // Check if the network has Wi-Fi, cellular, or Ethernet transport.
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}