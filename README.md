# FastPix Live - Android Streaming App
 
An  Android live streaming application built with [RootEncoder RTMP library](https://github.com/pedroSG94/RootEncoder) that delivers high-quality broadcasting capabilities with robust network handling, adaptive bitrate streaming, and modern Material Design UI.
 
---
 
## Demo App
 


https://github.com/user-attachments/assets/a3e90666-99a1-491e-b652-3ac988bdc7e8






 
---
 
## Key Features
 
####  Key stream
- **RTMPS Broadcasting** — Secure streaming to FastPix Live platform
- **Real-time Camera Switching** — Seamless front/back camera toggle during live streams
- **Network Resilience** — Automatic reconnection with 3-second retry logic on network dropouts
- **Adaptive Bitrate** — Dynamic quality adjustment based on network conditions (API 19+)
- **Multi-Resolution Support** — Four broadcasting profiles from 360p to 1080p.
 
#### Advanced Features
- **Screen Wake Lock** — Prevents device from sleeping during broadcasts using FLAG_KEEP_SCREEN_ON
- **Hardware Encoding** — Efficient H264 hardware acceleration with MediaCodec
- **Real-time Stats** — Live FPS counter and bitrate monitoring in kbps
- **Connection Management** — Intelligent retry logic with automatic reconnection
- **Orientation Support** — Dynamic layout adaptation for portrait and landscape modes
 
#### User Experience
- **Material Design UI** — Clean interface with quality selector buttons and status indicators
- **Intuitive Controls** — One-tap streaming with clear visual feedback
- **Error Recovery** — Graceful handling of SSL/TLS cleanup and network failures
- **Toast Notifications** — Contextual feedback for connection status and errors
 
---
 
## Known Limitations
 
- frame drops, lowering frame rate to around 20fps on older devices
- SSL cleanup warnings during connection failures are expected and handled gracefully
- Rotation changes during active streaming should be avoided to prevent connection instability
 
---
 
## Quick Start
 
### Prerequisites
 
1. **FastPix Account** — Sign up at [FastPix Dashboard](https://dashboard.fastpix.io/signup)
2. **RTMP Stream Key** — Create a live stream and obtain your [unique stream key](https://docs.fastpix.io/docs/get-started-with-live-streaming). For more details about `streamKey`, see Step 3.
3. **Android Device** — Android API 16+ required (physical device recommended for optimal performance)
 
## Installation
 
1. **Clone the repository**
```bash
git clone https://github.com/FastPix/fastpix-android-live-streaming-demo-app.git
 
```
 
2. **Install dependencies**
```gradle
implementation("com.github.pedroSG94.RootEncoder:library:2.6.4")
implementation("com.github.pedroSG94.RootEncoder:extra-sources:2.6.4")
implementation("androidx.core:core-splashscreen:1.0.1")
```
 
 
4. **Device Testing**
   - Connect your physical device (simulator won't work for camera/streaming)
   - Build and run the project
   - Enter your FastPix stream key when prompted
 
## System Requirements
 
- **Android Version:** API 16+ (Android 4.1 Jelly Bean or higher)
- **Recommended:** API 21+ for optimal performance with Camera2 API
- **Device:** Physical Android device (emulator has limited camera support)
- **Network:** WiFi or cellular data connection with stable upload bandwidth
- **Permissions:** Camera (CAMERA) and Microphone (RECORD_AUDIO) access
 
---
 
## Architecture Overview
 
### Core Components
 
**ConfigurationActivity.kt**
- Quality selection interface
- Stream key input
- Permission management
- Button selection states
 
**LiveStreamActivity.kt**
- Camera management with safe switching
- Stream control for broadcasting
- Network handling and retry logic
- Real-time stats UI
 
---
 
### Key Classes
 
**RtmpCamera1**
- Hardware H264 encoding with MediaCodec
- AAC audio encoding
- FPS listener callback
- RTMPS connection with TLS
 
**ConnectChecker Interface**
- Connection lifecycle callbacks (started, success, failed, disconnect)
- Authentication handling
- Real-time bitrate reporting
 
**Preset Enum**
- hd_1080p_30fps_5mbps (1920x1080, 5Mbps)
- hd_720p_30fps_3mbps (1280x720, 3Mbps)
- sd_540p_30fps_2mbps (640x480, 2Mbps)
- sd_360p_30fps_1mbps (640x360, 1Mbps)
 
 
 
# Technical Implementation
 
## Stream Setup Process
 
1. **Permission Handling**
```kotlin
if (!hasPermissions(this, PERMISSIONS)) {
ActivityCompat.requestPermissions(this, PERMISSIONS, 1)
}
```
 
2. **RTMP Configuration**
```kotlin
rtmpCamera = RtmpCamera1(surfaceView, this)
rtmpCamera.prepareVideo(preset.width, preset.height, preset.frameRate, preset.bitrate, 4, CameraHelper.getCameraOrientation(this))
rtmpCamera.prepareAudio(128 * 1024, 48000, true)
val streamUrl = "rtmps://live.cloudflare.com:443/live/$streamKey"
rtmpCamera.startStream(streamUrl)
```
 
---
 
## Error Handling & Recovery
 
- **Automatic Reconnection** — 3-second delay on connection failures
- **SSL Cleanup** — "Channel is closed for write" exceptions handled, no crash
- **Thread Safety** — Camera operations on background thread, UI updates on main thread
 
---
 
 
## Monitoring & Analytics
 
### Real-time Metrics
- **FPS Monitoring**: Live frame rate display
- **Bitrate Tracking**: Current upload speed in kbps
- **Connection Status**: Visual indicators for stream health
- **Quality Adaptation**: Automatic bitrate adjustment notifications
```kotlin
rtmpCamera.setFpsListener { fps -> runOnUiThread { fpsLabel.text = "$fps fps" } }
override fun onNewBitrate(bitrate: Long) { runOnUiThread { bitrateLabel.text = "${bitrate / 1024} kbps" } }
```
 
 
---
 
## UI Features
 
- Material Design buttons/selectors
- Connection status dot (red/green)
- Camera toggle visual feedback
- Top-centered Toast notifications
 
---
 
## Platform Integration
 
- Uses Camera1 API for compatibility
- SurfaceView for live rendering
- FLAG_KEEP_SCREEN_ON on streaming
- Activity lifecycle-based cleanup
 
---
 
## Performance Optimizations
 
- Hardware encoding reduces CPU/memory use
- Complete resource cleanup in onDestroy()
- Adaptive bitrate for network optimization
 
---
 
## Support & Troubleshooting
 
### Common Issues
- Black screen? Check camera permissions, stream key, physical device.
- Connection failure? Verify network, RTMP endpoint, stream key.
- SSL/TLS cleanup warnings? Expected, handled in code.
- Frame drops issues
 
---
 
 
