---
name: Bug Report
about: Create a report to help us improve
title: '[BUG] '
labels: bug
assignees: ''
---

## Bug Description

A clear and concise description of what the bug is.

## Reproduction Steps

1. **Setup Environment**
 > Environment Configuration

1. **Code To Reproduce**

```kotlin
 val rotation = windowManager.defaultDisplay.rotation
        when (rotation) {
            Surface.ROTATION_90 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            Surface.ROTATION_180 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            Surface.ROTATION_270 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            else -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        preset?.let { p ->
            try {
                rtmpCamera.prepareVideo(
                    p.width,
                    p.height,
                    p.frameRate,
                    p.bitrate,
                    4,
                    CameraHelper.getCameraOrientation(this)
                )

                rtmpCamera.prepareAudio(
                    128 * 1024,
                    48000,
                    true
                )

                val streamUrl = "$rtmpEndpoint/${streamKey ?: ""}"
                rtmpCamera.startStream(streamUrl)
                liveDesired = true
                goLiveButton.text = "Connecting... (Cancel)"

                Log.i(TAG, "Stream started")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start stream: ${e.message}")
                showToast("Failed to start streaming")
            }
        }
```

3. **Expected Behavior**
```
<!-- A clear and concise description of what you expected to happen.  -->
```

4. **Actual Behavior**
```
<!-- A clear and concise description of what actually happened. -->
```

5. **Environment**

- **SDK Version**: [e.g., 1.2.2]
- **Android Version**: [e.g., Android 12]
- **Min SDK Version**: [e.g., 24]
- **Target SDK Version**: [e.g., 35]
- **Device/Emulator**: [e.g., Pixel 5, Android Emulator]
- **Player**: [e.g., ExoPlayer 2.19.0, VideoView, etc.]
- **Kotlin Version**: [e.g., 2.0.21]

## Code Sample

```kotlin
// Please provide a minimal code sample that reproduces the issue
 val rotation = windowManager.defaultDisplay.rotation
        when (rotation) {
            Surface.ROTATION_90 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            Surface.ROTATION_180 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            Surface.ROTATION_270 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            else -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        preset?.let { p ->
            try {
                rtmpCamera.prepareVideo(
                    p.width,
                    p.height,
                    p.frameRate,
                    p.bitrate,
                    4,
                    CameraHelper.getCameraOrientation(this)
                )

                rtmpCamera.prepareAudio(
                    128 * 1024,
                    48000,
                    true
                )

                val streamUrl = "$rtmpEndpoint/${streamKey ?: ""}"
                rtmpCamera.startStream(streamUrl)
                liveDesired = true
                goLiveButton.text = "Connecting... (Cancel)"

                Log.i(TAG, "Stream started")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start stream: ${e.message}")
                showToast("Failed to start streaming")
            }
        }
```

## Logs/Stack Trace

```
Paste relevant logs or stack traces here
```

## Additional Context

Add any other context about the problem here.

## Screenshots

If applicable, add screenshots to help explain your problem.

