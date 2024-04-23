package com.example.images.loader

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.images.loader.data.common.Resource
import com.example.images.loader.data.model.BackupDetails
import com.example.images.loader.data.model.ImagesResponse
import com.example.images.loader.data.model.Thumbnail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Rule
import org.junit.Test

class ImageScreenTest {

    @ExperimentalComposeUiApi
    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun imageScreen_UIRendering() {
        // Dummy images data for testing
        val dummyImages = List(1) {
            ImagesResponse(
                id = "123",
                title = "Sample Image ${it + 1}",
                language = "English",
                thumbnail = Thumbnail(
                    id = "456",
                    version = 1,
                    domain = "example.com",
                    basePath = "thumbnails",
                    key = "thumbnail_${it + 1}",
                    qualities = listOf(1, 2, 3),
                    aspectRatio = 16 / 9
                ),
                mediaType = 1,
                coverageURL = "https://google/",
                publishedAt = "2022-04-20",
                publishedBy = "Test",
                backupDetails = BackupDetails(
                    pdfLink = "https://google/", screenshotURL = "https://google/"
                )
            )
        }

        //MutableStateFlow with dummy images data
        val imagesStateFlow = MutableStateFlow(Resource.Success(dummyImages)).asStateFlow()

        // Set up the UI with the ImageScreen composable
        composeTestRule.setContent {
            ImageScreen(imagesStateFlow = imagesStateFlow, viewModel = null)
        }

        // Verify UI elements
        composeTestRule.onNodeWithTag("box_contain").assertExists()
    }
}