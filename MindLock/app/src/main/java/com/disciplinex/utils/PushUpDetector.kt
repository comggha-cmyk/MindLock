package com.disciplinex.utils

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.*

/**
 * PushUpDetector uses CameraX + ML Kit Pose Detection to count push-up reps.
 *
 * Algorithm:
 * - Tracks shoulder and elbow Y-coordinates relative to body
 * - Detects "DOWN" state: elbows bent, shoulders near floor
 * - Detects "UP" state: arms extended, shoulders raised
 * - A DOWN→UP transition = 1 rep
 */
@androidx.camera.core.ExperimentalGetImage
class PushUpDetector(private val context: Context) {

    enum class PushUpState { IDLE, DOWN, UP }

    private val _repCount = MutableStateFlow(0)
    val repCount: StateFlow<Int> = _repCount

    private val _currentState = MutableStateFlow(PushUpState.IDLE)
    val currentState: StateFlow<PushUpState> = _currentState

    private val _feedback = MutableStateFlow("Get into push-up position")
    val feedback: StateFlow<String> = _feedback

    private var lastState = PushUpState.IDLE

    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector = PoseDetection.getClient(options)

    private var cameraProvider: ProcessCameraProvider? = null

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onRepCounted: (Int) -> Unit
    ) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            cameraProvider = future.get()
            bindCamera(lifecycleOwner, previewView, onRepCounted)
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onRepCounted: (Int) -> Unit
    ) {
        val provider = cameraProvider ?: return

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                poseDetector.process(image)
                    .addOnSuccessListener { pose ->
                        val newReps = analyzePose(pose)
                        if (newReps > _repCount.value) {
                            _repCount.value = newReps
                            onRepCounted(newReps)
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            } else {
                imageProxy.close()
            }
        }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun analyzePose(pose: com.google.mlkit.vision.pose.Pose): Int {
        val leftShoulder  = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow     = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow    = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist     = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist    = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip       = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip      = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        if (listOf(leftShoulder, rightShoulder, leftElbow, rightElbow, leftWrist, rightWrist, leftHip, rightHip)
                .any { it == null }) {
            _feedback.value = "Position your full body in frame"
            return _repCount.value
        }

        // Calculate elbow angles
        val leftElbowAngle = calculateAngle(
            leftShoulder!!.position3D,
            leftElbow!!.position3D,
            leftWrist!!.position3D
        )
        val rightElbowAngle = calculateAngle(
            rightShoulder!!.position3D,
            rightElbow!!.position3D,
            rightWrist!!.position3D
        )
        val avgElbowAngle = (leftElbowAngle + rightElbowAngle) / 2

        // Shoulder height relative to hips (normalized)
        val shoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2
        val hipY = (leftHip!!.position.y + rightHip!!.position.y) / 2
        val shoulderToHipRatio = shoulderY / hipY

        // State machine
        val newState = when {
            avgElbowAngle < 90 && shoulderToHipRatio > 0.7f  -> PushUpState.DOWN
            avgElbowAngle > 160 && shoulderToHipRatio < 0.95f -> PushUpState.UP
            else -> lastState
        }

        _currentState.value = newState

        var reps = _repCount.value
        if (lastState == PushUpState.DOWN && newState == PushUpState.UP) {
            reps++
            _feedback.value = "Rep ${reps}! Keep going! 💪"
        } else {
            _feedback.value = when (newState) {
                PushUpState.DOWN -> "Good — now push up!"
                PushUpState.UP   -> "Lower yourself down..."
                PushUpState.IDLE -> "Get into push-up position"
            }
        }

        lastState = newState
        return reps
    }

    private fun calculateAngle(
        a: com.google.mlkit.vision.common.PointF3D,
        b: com.google.mlkit.vision.common.PointF3D,
        c: com.google.mlkit.vision.common.PointF3D
    ): Double {
        val radians = atan2((c.y - b.y).toDouble(), (c.x - b.x).toDouble()) -
                atan2((a.y - b.y).toDouble(), (a.x - b.x).toDouble())
        var angle = Math.toDegrees(abs(radians))
        if (angle > 180) angle = 360 - angle
        return angle
    }

    fun reset() {
        _repCount.value = 0
        _currentState.value = PushUpState.IDLE
        lastState = PushUpState.IDLE
    }

    fun release() {
        cameraProvider?.unbindAll()
        poseDetector.close()
    }
}
