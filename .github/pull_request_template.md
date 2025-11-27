# FastPix Resumable Uploads SDK - Documentation PR

## Documentation Changes

### What Changed
- [ ] New documentation added
- [ ] Existing documentation updated
- [ ] Documentation errors fixed
- [ ] Code examples updated
- [ ] Links and references updated

### Files Modified
- [ ] README.md
- [ ] docs/ files
- [ ] USAGE.md
- [ ] CONTRIBUTING.md
- [ ] Other: _______________

### Summary
**Brief description of changes:**

<!-- What documentation was added, updated, or fixed? -->

### Code Examples
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

### Testing
- [ ] All code examples tested
- [ ] Links verified
- [ ] Grammar checked
- [ ] Formatting consistent

### Review Checklist
- [ ] Content is accurate
- [ ] Code examples work
- [ ] Links are working
- [ ] Grammar is correct
- [ ] Formatting is consistent

---

**Ready for review!**
