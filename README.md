# Video Player App

## 1. About

This is a video player application for Android built with modern Android development practices. It allows users to play videos from both their local device storage and from direct web URLs. The app is designed with a clean, user-friendly interface using Jetpack Compose.

## 2. Features

- **Dual Mode Playback**: Seamlessly switch between playing local files and streaming from a URL via a tabbed interface.
- **Local Media Browsing**: Scans and displays video files from the user's device storage.
- **Play from URL**: Allows users to input a direct link to a video file for streaming.
- **Advanced Player UI**:
    - Full-screen playback for an immersive experience.
    - Displays buffering status with real-time network speed (e.g., "1.5 Mbps").
    - Shows clear error messages if playback fails.
- **Automatic Orientation**: Intelligently switches the screen to landscape or portrait based on the video's aspect ratio.
- **Robust Navigation**: Built with Jetpack Navigation Compose, ensuring a stable and predictable user flow.
- **Runtime Permissions**: Properly handles storage access permissions before showing local files.

## 3. Architecture

The application follows Google's recommended architecture for modern Android apps:

- **UI Layer**: Built entirely with **Jetpack Compose**.
- **MVVM (Model-View-ViewModel)**: The UI state is managed by `ViewModel`s, providing data to the composables and separating logic from UI.
- **Repository Pattern**: A `VideoRepository` abstracts the data source (local video content), making the app scalable and easier to maintain.
- **Multi-Module Architecture**: The project is split into modules for better separation of concerns and build times.
    - `:app`: The main application module.
    - `:feature_play_from_url`: A self-contained feature module.
- **Dependency Injection**: Utilized to provide dependencies like `ExoPlayer` and `BandwidthMeter`.

## 4. Libraries

This project leverages a variety of modern libraries from the AndroidX and Kotlin ecosystems:

- **Jetpack Compose**:
    - `androidx.compose.ui`: Core UI components.
    - `androidx.compose.material3`: Material Design 3 components.
    - `androidx.navigation:navigation-compose`: For navigation between screens.
    - `androidx.lifecycle:lifecycle-viewmodel-compose`: To connect ViewModels to Composables.
- **Media Playback**:
    - `androidx.media3:media3-exoplayer`: The core component for video playback (ExoPlayer).
    - `androidx.media3:media3-ui`: Provides the `PlayerView` for displaying video.
- **Core & Coroutines**:
    - `androidx.core:core-ktx`: Kotlin extensions for core Android libraries.
    - `kotlinx.coroutines`: For managing background threads and asynchronous operations.
- **Image Loading**:
    - `io.coil-kt:coil-compose`: For asynchronously loading video thumbnails into the UI.

## 5. Components

- **`AppNavHost`**: The central navigation hub that controls the flow between different screens.
- **`PlayerScreen`**: A sophisticated, stateful composable that manages the entire video playback experience, including UI controls, state display (buffering/error), and orientation logic.
- **`VideosInFolderScreen`**: Displays a grid of video thumbnails fetched from local storage.
- **`PlayFromUrlScreen`**: A simple screen with a text field for users to input a video URL.
- **`PermissionGatedContent`**: A reusable composable that ensures necessary permissions are granted before displaying content.
- **`PlayerState`**: A sealed class that represents the various states of the video player (`Buffering`, `Playing`, `Error`, etc.), enabling a reactive UI.

## 6. Overall Structure

The project is organized into a multi-module structure to promote scalability and separation of concerns.

```
VIdeoPlayer/
├── app/                  # Main application module
│   └── src/main/java/
│       └── com/example/videoplayer/
│           ├── data/           # Repository and data models (VideoRepository)
│           ├── navigation/     # Navigation graph and screen definitions (AppNavHost)
│           ├── ui/             # Composable screens and UI components
│           │   ├── player/     # PlayerScreen, PlayerState
│           │   ├── videolist/  # VideosInFolderScreen
│           │   └── permissions/ # PermissionGatedContent
│           └── viewmodel/      # ViewModels and UI state classes
│
└── feature_play_from_url/ # Feature module for playing from a URL
    └── src/main/java/
        └── com/example/videoplayer/feature_play_from_url/
            └── PlayFromUrlScreen.kt
```

This structure clearly separates data, UI, and navigation logic, making the codebase clean, understandable, and easy to extend.
