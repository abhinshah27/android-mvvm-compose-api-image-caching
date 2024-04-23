
---

# android-mvvm-compose-api-image-caching

An Android app demonstrating advanced image and API caching using MVVM and Jetpack Compose. It features offline image loading and efficient caching with Retrofit.

## Features

- **Retrofit Integration**: Utilizes Retrofit for making API calls and retrieving data from the server, with support for caching.
- **Offline Loading**: Supports offline loading of images using Retrofit cache. Images are retrieved from the cache when internet connectivity is unavailable.
- **Image Caching**: Develops caching mechanism to store images retrieved from the API in both memory and disk cache for efficient retrieval. Disk cache is utilized if an image is missing in the memory cache. When an image is read from the disk, the memory cache is updated.

## Technologies Used

- **Kotlin**: Programming language used for developing the application.
- **Jetpack Compose**: UI toolkit for building native Android UIs.
- **Dagger Hilt**: Dependency injection library for Android development.
- **LiveData**: Part of Android Architecture Components, used for observing changes in data.
- **MVVM Architecture**: Architectural pattern used for organizing code and separating concerns.

## Testing

### Unit Tests

Unit tests validate the functionality units of code in isolation. Mockito-Kotlin and JUnit 4 are utilized for mocking dependencies and verifying behavior.

### UI Tests

UI tests verify the behavior and appearance of the application's user interface. Jetpack Compose UI testing framework, along with the `compose-test` library, is utilized for writing and running UI tests.

## Demo Video

For a demonstration of the application's features, please refer to the following gif:

![ImageCachingDemo](img_cache_demo.gif)

---
