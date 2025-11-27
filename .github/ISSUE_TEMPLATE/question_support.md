---
name: Question/Support
about: Ask questions or get help with the FastPix Resumable Uploads SDK
title: '[QUESTION] '
labels: ['question', 'needs-triage']
assignees: ''
---

# Question/Support

Thank you for reaching out! We're here to help you with the FastPix Resumable Uploads SDK. Please provide the following information:

## Question Type
- [ ] How to use a specific feature
- [ ] Integration help
- [ ] Configuration question
- [ ] Performance question
- [ ] Troubleshooting help
- [ ] Other: _______________

## Question
**What would you like to know?**

<!-- Please provide a clear, specific question -->

## What You've Tried
**What have you already attempted to solve this?**

```kotlin
// Your attempted code here
```

## Current Setup
**Describe your current setup:**

## Environment
- **SDK Version**: [e.g., 1.2.2]
- **Android Version**: [e.g., Android 12]
- **Min SDK Version**: [e.g., 24]
- **Target SDK Version**: [e.g., 35]
- **Device/Emulator**: [e.g., Pixel 5, Android Emulator]
- **Player**: [e.g., ExoPlayer 2.19.0, VideoView, etc.]
- **Kotlin Version**: [e.g., 2.0.21]

## Configuration
```kotlin
// Your current SDK configuration (remove sensitive information)
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

## Expected Outcome
**What are you trying to achieve?**

<!-- Describe your end goal -->

## Error Messages (if any)
```
<!-- If you're getting errors, paste them here -->
```

## Additional Context

### Use Case
**What are you building?**

- [ ] Web application
- [ ] Mobile app (web-based)
- [ ] File upload service
- [ ] Media upload platform
- [ ] Other: _______________


### Timeline
**When do you need this resolved?**

- [ ] ASAP (blocking development)
- [ ] This week
- [ ] This month
- [ ] No rush

### Resources Checked
**What resources have you already checked?**

- [ ] README.md
- [ ] Documentation
- [ ] Examples
- [ ] Stack Overflow
- [ ] GitHub Issues
- [ ] Other: _______________

## Priority
Please indicate the urgency:

- [ ] Critical (Blocking production deployment)
- [ ] High (Blocking development)
- [ ] Medium (Would like to know soon)
- [ ] Low (Just curious)

## Checklist
Before submitting, please ensure:

- [ ] I have provided a clear question
- [ ] I have described what I've tried
- [ ] I have included my current setup
- [ ] I have checked existing documentation
- [ ] I have provided sufficient context

---

**We'll do our best to help you get unstuck! 🚀**

**For urgent issues, please also consider:**
- [FastPix Documentation](https://docs.fastpix.io/)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/fastpix)
- [GitHub Discussions](https://github.com/FastPix/web-uploads-sdk/discussions)
