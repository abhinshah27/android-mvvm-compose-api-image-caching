package com.example.images.loader

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.images.loader.data.common.Resource
import com.example.images.loader.data.model.BackupDetails
import com.example.images.loader.data.model.ImagesResponse
import com.example.images.loader.data.model.Thumbnail
import com.example.images.loader.ui.theme.ImageLoadDemoTheme
import com.example.images.loader.ui.viewmodel.MainViewModel
import com.example.images.loader.utils.fetchImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageLoadDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Get the MainViewModel instance using the viewModel() delegate.
                    val viewModel: MainViewModel = viewModel()

                    // Use the ImageScreen composable with the images StateFlow from the ViewModel.
                    ImageScreen(imagesStateFlow = viewModel.images, viewModel = viewModel)
                }
            }
        }
    }
}

/**
 * Composable function to display the image screen.
 *
 * @param imagesStateFlow The StateFlow of images data.
 * @param modifier The modifier for the composable.
 * @param viewModel The ViewModel instance.
 *
 * @return The composable function.
 */
@Composable
fun ImageScreen(
    imagesStateFlow: StateFlow<Resource<List<ImagesResponse>>>, // StateFlow of images data
    modifier: Modifier = Modifier, // Modifier for the composable
    viewModel: MainViewModel? // ViewModel instance
) {
    // Get the current context.
    val imagesState by imagesStateFlow.collectAsState() // Collect the latest images state

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imagesState !is Resource.Success && imagesState !is Resource.Loading) {
            // Display a message and a retry button if there is no internet connection
            Text(
                text = stringResource(R.string.no_internet_connection),
                textAlign = TextAlign.Center, // Center the text
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
            )
            Button(
                onClick = { viewModel?.loadImages() }, // Reload images on button click
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)
            ) {
                Text(text = stringResource(R.string.retry_with_internet))
            }
        }
        when (val state = imagesState) {
            is Resource.Loading -> {
                // Handle the loading state
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator() // Display a circular progress indicator
                }
            }

            is Resource.Success -> {
                // Handle the success state
                val images = state.data // Get the list of images
                if (images.isNotEmpty()) {
                    ImageGrid(images = images, viewModel = viewModel) // Display the images
                }
            }

            is Resource.Error -> {
                // Handle the error state
                Toast.makeText(
                    LocalContext.current, state.message ?: "Unknown error", Toast.LENGTH_SHORT
                ).show() // Display an error message
            }
        }
    }
}

/**
 * Composable function to display a grid of images.
 *
 * @param images The list of images to display.
 * @param viewModel The ViewModel instance.
 */
@Composable
fun ImageGrid(
    images: List<ImagesResponse>,
    viewModel: MainViewModel?
) {
    val context = LocalContext.current
    val state = rememberLazyGridState()

    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(3)
    ) {
        itemsIndexed(images) { _, image ->
            Surface( // Surface for each image
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 8.dp,
                modifier = Modifier.padding(4.dp)
            ) {
                Box( // Box to center the image
                    modifier = Modifier.fillMaxSize().testTag("box_contain"),
                    contentAlignment = Alignment.Center
                ) { // Display image using the ImageItem composable
                    ImageItem(image = image, context = context, viewModel = viewModel)
                }
            }
        }
    }
}

/**
 * Composable function to display an image item.
 *
 * @param image The image data.
 * @param context The context.
 * @param viewModel The ViewModel instance.
 */
@Composable
private fun ImageItem(
    image: ImagesResponse,
    context: Context,
    viewModel: MainViewModel?
) {
    // State variables to track the image loading status
    val bitmapState = remember { mutableStateOf<Bitmap?>(null) }
    val loadingState = remember { mutableStateOf(true) }
    val errorState = remember { mutableStateOf(false) }

    // Check if the image URL has previously failed to load
    val hasFailed = viewModel?.isFailed(image.coverageURL) ?: false

    // Load the image only if it hasn't previously failed to load
    if (!hasFailed) {
        LaunchedEffect(image.coverageURL) {
            try {
                // Load the image in a background thread
                val bitmap = withContext(Dispatchers.IO) {
                    fetchImage(context, image.coverageURL)
                }

                // Update the state variables
                bitmapState.value = bitmap
                loadingState.value = false
                errorState.value = bitmap == null

                // Mark the image as failed if it couldn't be loaded
                if (bitmap == null) {
                    viewModel?.markAsFailed(image.coverageURL)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadingState.value = false
                errorState.value = true
                viewModel?.markAsFailed(image.coverageURL)
            }
        }

        // Display the image or loading/error message
        Column {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (loadingState.value) {
                    // Display a circular progress indicator while loading
                    CircularProgressIndicator(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxSize()
                            .padding(26.dp)
                    )
                } else if (errorState.value) {
                    // Display a message if the image failed to load
                    FailedToLoadMessage()
                } else {
                    // Display the image
                    bitmapState.value?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "",
                            modifier = Modifier.aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    } else {
        // Display a message if the image has previously failed to load
        FailedToLoadMessage()
    }
}

/**
 * Composable function to display a message when an image fails to load.
 */
@Composable
fun FailedToLoadMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.failed_to_load_image),
            color = Color.Red,
            textAlign = TextAlign.Center,
            style = typography.labelMedium,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewImageScreen() {
    val dummyImages = List(9) {
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
    val imagesStateFlow = MutableStateFlow(Resource.Success(dummyImages)).asStateFlow()
    ImageScreen(imagesStateFlow = imagesStateFlow, viewModel = null)
}