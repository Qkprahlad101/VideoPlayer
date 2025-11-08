# Video Player

A modern, feature-rich video player application for Android, built entirely with Jetpack Compose and other modern Android development practices.

## About

This application serves as a demonstration of modern Android app development, combining robust architecture with a clean, user-friendly interface. It allows users to play videos from both their local device storage and from web URLs, handling various playback scenarios gracefully.

## Features

- **Dual Playback Modes**: A tabbed interface allows users to seamlessly switch between playing local media files and streaming videos from a URL.
- **Local Media Browser**: Automatically scans the user's device for video files and displays them in a grid with thumbnails.
- **Play from URL**:
    - Users can paste a URL to stream a video directly from the web.
    - **Proactive Extractor Updates**: The app automatically updates its video extraction logic on startup to keep up with changes on source websites (like YouTube), significantly reducing playback errors.
    - **Note**: This feature is intended for videos from free online resources. It may not work with premium/DRM-protected content or on all websites due to server-side restrictions.
- **Advanced Player UI**:
    - A clean, stateful player screen built with `androidx.media3.ui`.
    - **Error Handling**: Displays clear error messages (e.g., "403 Forbidden") when playback fails.
    - **Graceful Fallback**: If direct playback fails, a button appears allowing the user to open the video's original webpage in their browser.
    - **Buffering Indicator**: Shows a loading spinner while the video is buffering.
- **Automatic Orientation**: The player screen automatically switches to landscape or portrait based on the video's aspect ratio for the best viewing experience.
- **Robust Navigation**: Uses Jetpack Navigation Compose for a stable and predictable navigation flow.
- **Runtime Permissions**: Properly requests storage permissions before accessing local video files.

## Architecture

The application is built following Google's recommended architecture for modern Android apps, emphasizing separation of concerns, scalability, and testability.

- **100% Kotlin & Jetpack Compose**: The entire UI is built with Jetpack Compose, Android's modern declarative UI toolkit.
- **MVVM (Model-View-ViewModel)**: UI state is managed by `ViewModel`s, providing data to the composables and separating business logic from the UI.
- **Multi-Module Architecture**: The project is logically separated into modules to improve build times and enforce separation of concerns.
    - `:app`: The main application module containing core infrastructure, local playback, and navigation.
    - `:feature_play_from_url`: A self-contained feature module for the "Play from URL" screen.
- **Dependency Injection**: Uses **Koin** for managing and providing dependencies like `ViewModel`s and services throughout the app.

## Key Libraries & Technologies

- **Jetpack Compose**: `ui`, `material3`, `navigation-compose`, `lifecycle-viewmodel-compose` for the entire UI layer.
- **Media3 ExoPlayer**: `media3-exoplayer`, `media3-exoplayer-hls`, and `media3-ui` for powerful and extensible video playback.
- **youtubedl-android**: A library to extract direct streaming links from various online video sources.
- **Kotlin Coroutines**: For managing asynchronous operations, from fetching videos to making network requests.
- **Koin**: For lightweight dependency injection.
- **Coil**: For efficiently loading and displaying video thumbnails.

## How to Build

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Let Gradle sync the dependencies.
4.  Build and run on an Android device or emulator.
