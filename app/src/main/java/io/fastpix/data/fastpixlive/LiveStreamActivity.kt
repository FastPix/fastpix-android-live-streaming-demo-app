package io.fastpix.data.fastpixlive

import android.content.pm.ActivityInfo
import android.media.MediaCodec
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.pedro.common.ConnectChecker
import com.pedro.encoder.CodecErrorCallback
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.encoder.utils.CodecUtil.CodecTypeError
import com.pedro.library.rtmp.RtmpCamera1
import com.pedro.library.util.FpsListener
import com.pedro.library.view.OpenGlView
import java.io.IOException

class LiveStreamActivity : AppCompatActivity(), SurfaceHolder.Callback, ConnectChecker, View.OnTouchListener {

    companion object {
        private const val TAG = "FastPixLive"
        private const val rtmpEndpoint = "rtmps://live.cloudflare.com:443/live"

        const val intentExtraStreamKey = "STREAMKEY"
        const val intentExtraPreset = "PRESET"
        private const val ZERO_KBPS = "0 kbps"
        private const val ZERO_FPS = "0 fps"
        private const val GO_LIVE_TEXT = "Go Live!"
        private const val CHANNEL_IS_CLOSED_FOR_WRITE = "Channel is closed for write"
        private const val STREAM_HEALTH_CHECK_DELAY_MS = 8000L

        fun sanitizeStreamKey(input: String): String {
            var key = input.trim()
            if (key.contains("://")) {
                key = key.substringAfterLast('/').trim()
            }
            return key.trim('/')
        }
    }

    enum class Preset(val bitrate: Int, val width: Int, val height: Int, val frameRate: Int) {
        hd_1080p_30fps_5mbps(5000000, 1920, 1080, 30),
        hd_720p_30fps_3mbps(3000000, 1280, 720, 30),
        sd_540p_30fps_2mbps(2000000, 640, 480, 30),
        sd_360p_30fps_1mbps(1000000, 640, 360, 30)
    }

    private lateinit var goLiveButton: Button
    private lateinit var bitrateLabel: TextView
    private lateinit var fpsLabel: TextView
    private lateinit var openGlView: OpenGlView
    private lateinit var connectionStatus: View
    private lateinit var backCameraButton: TextView
    private lateinit var frontCameraButton: TextView
    private lateinit var closeButton: ImageView

    private lateinit var rtmpCamera: RtmpCamera1
    private var liveDesired = false
    private var streamKey: String? = null
    private var preset: Preset? = null
    private var activePreset: Preset? = null
    private var isBackCamera = true
    private var isStoppingStream = false

    private val healthCheckHandler = Handler(Looper.getMainLooper())
    private var healthCheckRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_livestream)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Log.i(TAG, "Device: ${Build.MANUFACTURER} ${Build.MODEL}, API ${Build.VERSION.SDK_INT}")

        initializeViews()
        setupCamera()
        setupClickListeners()
        handleIntent()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun initializeViews() {
        openGlView = findViewById(R.id.surfaceView)
        openGlView.holder.addCallback(this)
        openGlView.setOnTouchListener(this)

        goLiveButton = findViewById(R.id.goLiveButton)
        closeButton = findViewById(R.id.closeButton)
        backCameraButton = findViewById(R.id.backCameraButton)
        frontCameraButton = findViewById(R.id.frontCameraButton)

        bitrateLabel = findViewById(R.id.bitrateLabel)
        fpsLabel = findViewById(R.id.fpslabel)
        connectionStatus = findViewById(R.id.connectionStatus)

        connectionStatus.setBackgroundResource(R.drawable.connection_dot_red)
        bitrateLabel.text = ZERO_KBPS
        fpsLabel.text = ZERO_FPS

        updateButtonColorsInstant()
    }

    private fun setupCamera() {
        try {
            rtmpCamera = RtmpCamera1(openGlView, this)

            rtmpCamera.setFpsListener(object : FpsListener.Callback {
                override fun onFps(fps: Int) {
                    runOnUiThread {
                        fpsLabel.text = "$fps fps"
                    }
                }
            })

            rtmpCamera.setEncoderErrorCallback(object : CodecErrorCallback {
                override fun onCodecError(type: CodecTypeError, e: MediaCodec.CodecException) {
                    Log.e(TAG, "Codec error [$type]: ${e.diagnosticInfo} - ${e.message}")
                    runOnUiThread {
                        showToast("Encoder error: ${e.message}")
                        connectionStatus.setBackgroundResource(R.drawable.connection_dot_red)
                    }
                }

                override fun onEncodeError(type: CodecTypeError, e: IllegalStateException): Boolean {
                    Log.e(TAG, "Encode error [$type]: ${e.message}")
                    return true
                }
            })
        } catch (e: RuntimeException) {
            Log.e(TAG, "Camera setup failed: ${e.message}")
            showToast("Camera setup failed on this device")
        }
    }

    private fun setupClickListeners() {
        closeButton.setOnClickListener {
            finish()
        }

        goLiveButton.setOnClickListener {
            goLiveClicked()
        }

        backCameraButton.setOnClickListener {
            if (!isBackCamera) {
                isBackCamera = true
                updateButtonColorsInstant()

                Thread {
                    try {
                        rtmpCamera.switchCamera()
                    } catch (e: Exception) {
                        Log.e(TAG, "Camera switch failed: ${e.message}")
                    }
                }.start()
            }
        }

        frontCameraButton.setOnClickListener {
            if (isBackCamera) {
                isBackCamera = false
                updateButtonColorsInstant()

                Thread {
                    try {
                        rtmpCamera.switchCamera()
                    } catch (e: Exception) {
                        Log.e(TAG, "Camera switch failed: ${e.message}")
                    }
                }.start()
            }
        }
    }

    private fun updateButtonColorsInstant() {
        if (isBackCamera) {
            backCameraButton.setBackgroundResource(R.drawable.quality_button_selected)
            backCameraButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))

            frontCameraButton.setBackgroundResource(R.drawable.quality_button_unselected)
            frontCameraButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            frontCameraButton.setBackgroundResource(R.drawable.quality_button_selected)
            frontCameraButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))

            backCameraButton.setBackgroundResource(R.drawable.quality_button_unselected)
            backCameraButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        }
    }

    private fun handleIntent() {
        intent.extras?.let { extras ->
            streamKey = sanitizeStreamKey(extras.getString(intentExtraStreamKey).orEmpty())
            preset = extras.getSerializable(intentExtraPreset) as? Preset
            Log.i(TAG, "Preset: ${preset?.name}, stream key length: ${streamKey?.length ?: 0}")
        }
    }

    fun goLiveClicked() {
        if (liveDesired) {
            stopStreaming()
        } else {
            startStreaming()
        }
    }

    private fun getPreviewPreset(): Preset {
        return preset ?: Preset.sd_540p_30fps_2mbps
    }

    private fun startPreviewIfNeeded() {
        if (!::rtmpCamera.isInitialized || rtmpCamera.isStreaming) return

        val previewPreset = getPreviewPreset()
        try {
            rtmpCamera.startPreview(previewPreset.width, previewPreset.height)
            Log.i(TAG, "Preview started at ${previewPreset.width}x${previewPreset.height}")
        } catch (e: Exception) {
            Log.e(TAG, "Preview failed: ${e.message}")
        }
    }

    private fun tryPrepareEncoders(targetPreset: Preset): Boolean {
        val rotation = CameraHelper.getCameraOrientation(this)
        val videoReady = rtmpCamera.prepareVideo(
            targetPreset.width,
            targetPreset.height,
            targetPreset.frameRate,
            targetPreset.bitrate,
            2,
            rotation
        )
        val audioReady = rtmpCamera.prepareAudio(128 * 1024, 48000, true)

        Log.i(
            TAG,
            "Encoder prep ${targetPreset.name} (${targetPreset.width}x${targetPreset.height}): " +
                "video=$videoReady audio=$audioReady"
        )
        return videoReady && audioReady
    }

    private fun prepareEncodersWithFallback(primary: Preset): Preset? {
        val fallbackOrder = listOf(primary) + Preset.values()
            .filter { it != primary }
            .sortedBy { it.bitrate }

        for (candidate in fallbackOrder) {
            if (tryPrepareEncoders(candidate)) {
                if (candidate != primary) {
                    Log.w(TAG, "Fell back from ${primary.name} to ${candidate.name}")
                    runOnUiThread {
                        showToast("Using ${candidate.width}x${candidate.height} fallback for this device")
                    }
                }
                return candidate
            }
        }
        return null
    }

    private fun startStreaming() {
        val rotation = windowManager.defaultDisplay.rotation
        when (rotation) {
            Surface.ROTATION_90 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            Surface.ROTATION_180 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            Surface.ROTATION_270 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            else -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        val selectedPreset = preset
        if (selectedPreset == null) {
            showToast("No quality preset selected")
            return
        }

        val key = streamKey.orEmpty()
        if (key.isEmpty()) {
            showToast("Stream key is missing")
            return
        }

        try {
            if (rtmpCamera.isStreaming) {
                rtmpCamera.stopStream()
            }

            val preparedPreset = prepareEncodersWithFallback(selectedPreset)
            if (preparedPreset == null) {
                Log.e(TAG, "All encoder configurations failed on ${Build.MANUFACTURER} ${Build.MODEL}")
                showToast("This device cannot initialize video/audio encoders")
                return
            }
            activePreset = preparedPreset

            val streamUrl = buildStreamUrl(key)
            Log.i(TAG, "Publishing to $streamUrl at ${preparedPreset.width}x${preparedPreset.height}")
            rtmpCamera.startStream(streamUrl)
            liveDesired = true
            goLiveButton.text = "Connecting... (Cancel)"
            Log.i(TAG, "Stream started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start stream: ${e.message}")
            showToast("Failed to start streaming")
        }
    }

    private fun stopStreaming() {
        if (isStoppingStream) return

        isStoppingStream = true
        cancelStreamHealthCheck()
        goLiveButton.text = "Stopping..."
        liveDesired = false

        Thread {
            try {
                if (rtmpCamera.isStreaming) {
                    rtmpCamera.stopStream()
                }
            } catch (e: IOException) {
                if (e.message?.contains(CHANNEL_IS_CLOSED_FOR_WRITE) == true) {
                    Log.w(TAG, "SSL cleanup race condition (expected): ${e.message}")
                } else {
                    Log.e(TAG, "Stream stop error: ${e.message}")
                }
            } finally {
                isStoppingStream = false
                activePreset = null
                runOnUiThread {
                    goLiveButton.text = GO_LIVE_TEXT
                    connectionStatus.setBackgroundResource(R.drawable.connection_dot_red)
                    bitrateLabel.text = ZERO_KBPS
                    fpsLabel.text = ZERO_FPS
                    startPreviewIfNeeded()
                }
            }
        }.start()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun buildStreamUrl(streamKey: String): String {
        val key = sanitizeStreamKey(streamKey)
        return "${rtmpEndpoint.trimEnd('/')}/$key"
    }

    private fun scheduleStreamHealthCheck() {
        cancelStreamHealthCheck()
        healthCheckRunnable = Runnable {
            if (!liveDesired || !rtmpCamera.isStreaming) return@Runnable

            val noBitrate = bitrateLabel.text == ZERO_KBPS
            val noFps = fpsLabel.text == ZERO_FPS
            if (noBitrate && noFps) {
                Log.e(
                    TAG,
                    "Stream health check failed on ${Build.MANUFACTURER} ${Build.MODEL}: " +
                        "connected but sending 0 fps / 0 kbps"
                )
                showToast("Connected but no video is being sent. Try 360p quality.")
            }
        }
        healthCheckHandler.postDelayed(healthCheckRunnable!!, STREAM_HEALTH_CHECK_DELAY_MS)
    }

    private fun cancelStreamHealthCheck() {
        healthCheckRunnable?.let { healthCheckHandler.removeCallbacks(it) }
        healthCheckRunnable = null
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.show()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val layoutParams = openGlView.layoutParams as ConstraintLayout.LayoutParams
        val displayRotation = windowManager.defaultDisplay.rotation

        when (displayRotation) {
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                layoutParams.dimensionRatio = "w,16:9"
            }
            else -> {
                layoutParams.dimensionRatio = "h,9:16"
            }
        }
        openGlView.layoutParams = layoutParams

        if (!rtmpCamera.isStreaming) {
            startPreviewIfNeeded()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (::rtmpCamera.isInitialized && !rtmpCamera.isStreaming) {
            rtmpCamera.stopPreview()
        }
    }

    override fun onConnectionSuccess() {
        runOnUiThread {
            goLiveButton.text = "Stop Streaming!"
            connectionStatus.setBackgroundResource(R.drawable.connection_dot_green)
            showToast("RTMP Connection Successful!")
        }
        Log.i(TAG, "RTMP connection successful")
        scheduleStreamHealthCheck()
    }

    override fun onConnectionStarted(url: String) {
        runOnUiThread {
            goLiveButton.text = "Connecting... (Cancel)"
        }
        Log.i(TAG, "RTMP connection started: $url")
    }

    override fun onConnectionFailed(reason: String) {
        Log.w(TAG, "RTMP connection failed: $reason")
        cancelStreamHealthCheck()
        runOnUiThread {
            goLiveButton.text = "Connection Failed"
            connectionStatus.setBackgroundResource(R.drawable.connection_dot_red)
            showToast("Connection failed: $reason")
        }

        if (liveDesired) {
            Thread {
                try {
                    if (rtmpCamera.isStreaming) {
                        rtmpCamera.stopStream()
                    }
                    Thread.sleep(1000)
                } catch (e: IOException) {
                    if (e.message?.contains(CHANNEL_IS_CLOSED_FOR_WRITE) == true) {
                        Log.w(TAG, "SSL cleanup during retry (expected)")
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    if (liveDesired && !isStoppingStream) {
                        runOnUiThread {
                            goLiveButton.text = "Reconnecting..."
                        }
                        startStreaming()
                    }
                }, 3000)
            }.start()
        }
    }

    override fun onNewBitrate(bitrate: Long) {
        runOnUiThread {
            bitrateLabel.text = "${bitrate / 1024} kbps"
        }
    }

    override fun onDisconnect() {
        Log.i(TAG, "RTMP disconnected")
        cancelStreamHealthCheck()
        runOnUiThread {
            bitrateLabel.text = ZERO_KBPS
            fpsLabel.text = ZERO_FPS
            goLiveButton.text = GO_LIVE_TEXT
            connectionStatus.setBackgroundResource(R.drawable.connection_dot_red)
            showToast("Disconnected")
        }
        liveDesired = false
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onAuthError() {
        Log.w(TAG, "RTMP auth error")
        cancelStreamHealthCheck()
        runOnUiThread {
            goLiveButton.text = GO_LIVE_TEXT
            connectionStatus.setBackgroundResource(R.drawable.connection_dot_red)
            showToast("Authentication Error - check stream key")
        }
        liveDesired = false
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onAuthSuccess() {
        Log.i(TAG, "RTMP auth success")
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean = false

    override fun onDestroy() {
        super.onDestroy()
        liveDesired = false
        cancelStreamHealthCheck()

        if (::rtmpCamera.isInitialized) {
            try {
                if (rtmpCamera.isStreaming) {
                    rtmpCamera.stopStream()
                }
            } catch (e: IOException) {
                if (e.message?.contains(CHANNEL_IS_CLOSED_FOR_WRITE) == true) {
                    Log.w(TAG, "SSL cleanup in onDestroy (expected)")
                }
            }
        }
    }

    override fun onBackPressed() {
        if (liveDesired && !isStoppingStream) {
            stopStreaming()

            Handler(Looper.getMainLooper()).postDelayed({
                super.onBackPressed()
                finish()
            }, 1500)
        } else {
            super.onBackPressed()
            finish()
        }
    }
}
