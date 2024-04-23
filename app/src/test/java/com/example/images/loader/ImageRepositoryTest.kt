package com.example.images.loader

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.images.loader.data.common.Resource
import com.example.images.loader.data.model.BackupDetails
import com.example.images.loader.data.model.ImagesResponse
import com.example.images.loader.data.model.Thumbnail
import com.example.images.loader.data.repository.ImageRepository
import com.example.images.loader.data.source.ApiServices
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ImageRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var apiService: ApiServices

    @Before
    fun setup() {
        context = mockk()
        apiService = mockk()
        MockKAnnotations.init(this)
    }

    @Test
    fun `test getImages with internet available`() {
        // Mock NetworkCapabilities to return true for hasTransport method
        val networkCapabilities = mockk<NetworkCapabilities>()
        every { networkCapabilities.hasTransport(any()) } returns true

        // Mock ConnectivityManager and Network
        val connectivityManager = mockk<ConnectivityManager>()
        val network = mockk<Network>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities

        // Mock context to return ConnectivityManager
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager

        // Mock successful API response
        val expectedImages = getMockData()
        coEvery { apiService.getImages() } returns expectedImages

        // Initialize ImageRepository with mocks
        val imageRepository = ImageRepository(context, apiService)

        // Call the method under test
        val result = runBlocking { imageRepository.getImages() }

        // Assert the result
        assertEquals(Resource.Success(expectedImages), result)
    }

    private fun getMockData(): List<ImagesResponse> {
        return listOf(
            ImagesResponse(
                id = "1",
                title = "Image 1",
                language = "English",
                thumbnail = Thumbnail(
                    id = "thumbnail_id",
                    version = 1,
                    domain = "example.com",
                    basePath = "thumbnails",
                    key = "thumbnail_key",
                    qualities = listOf(1, 2, 3),
                    aspectRatio = 16
                ),
                mediaType = 1,
                coverageURL = "https://example.com/image1.jpg",
                publishedAt = "2022-01-01",
                publishedBy = "Publisher",
                backupDetails = BackupDetails(
                    pdfLink = "https://example.com/pdf",
                    screenshotURL = "https://example.com/screenshot"
                )
            ), ImagesResponse(
                id = "2",
                title = "Image 2",
                language = "English",
                thumbnail = Thumbnail(
                    id = "thumbnail_id",
                    version = 1,
                    domain = "example.com",
                    basePath = "thumbnails",
                    key = "thumbnail_key",
                    qualities = listOf(1, 2, 3),
                    aspectRatio = 16
                ),
                mediaType = 1,
                coverageURL = "https://example.com/image2.jpg",
                publishedAt = "2022-01-02",
                publishedBy = "Publisher",
                backupDetails = BackupDetails(
                    pdfLink = "https://example.com/pdf",
                    screenshotURL = "https://example.com/screenshot"
                )
            )
        )
    }
}

