package org.firstinspires.ftc.teamcode.component

import android.util.Log
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.teamcode.Alliance
import org.firstinspires.ftc.teamcode.SpikeMark
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraOpenListener
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation
import org.openftc.easyopencv.OpenCvInternalCamera.CameraDirection
import org.openftc.easyopencv.OpenCvPipeline


class Vision(
    manager: ComponentManager,
    private val alliance: Alliance
) : Component(manager) {
    override val status: Status

    private val camera: WebcamName? = getHardware("webcam")
    private val cvCamera: OpenCvCamera?
    private val pipeline: OpenCvPipeline?

    private val width = 320
    private val height = 240

    private class TeamElementPipeline(
        private val alliance: Alliance,
        private val width: Int,
        private val height: Int
    ) : OpenCvPipeline() {
        private var zoneLeft: Mat = Mat()
        private var zoneCenter: Mat = Mat()
        private var zoneRight: Mat = Mat()

        private val rectLeft = Rect(0, 0, 80, 240)
        private val rectCenter = Rect(120, 0, 80, 240)
        private val rectRight = Rect(240, 0, 80, 240)
        @Volatile @JvmField var currentZone = SpikeMark.CENTER

        val hueRed = 0.0
        val hueBlue = 230.0

        override fun init(input: Mat) {
            zoneLeft = input.submat(rectLeft)
            zoneCenter = input.submat(rectCenter)
            zoneRight = input.submat(rectRight)
        }

        override fun processFrame(input: Mat): Mat {
            // convert to HSV
            Imgproc.cvtColor(input, input, Imgproc.COLOR_BGR2HSV_FULL)
            val meanLeft = Core.mean(zoneLeft)
            val meanCenter = Core.mean(zoneCenter)
            val meanRight = Core.mean(zoneRight)
//            Imgproc.rectangle()
            return input
        }

    }

    init {
        val functionality = when {
            camera == null -> Functionality.NONE
            shared.imu == null -> Functionality.PARTIAL
            else -> Functionality.FULL
        }
        val hardwareSet: Set<HardwareUsage> = setOf(
            HardwareUsage("webcam", WebcamName::class, camera != null)
        )
        status = Status(
            functionality,
            hardwareSet
        )

        if (camera != null) {
            cvCamera = OpenCvCameraFactory.getInstance().createWebcam(camera)
            pipeline = TeamElementPipeline(alliance, width, height)
            cvCamera.setPipeline(pipeline)

            cvCamera.openCameraDeviceAsync(object : AsyncCameraOpenListener {
                override fun onOpened() {
                    cvCamera.startStreaming(width, height, OpenCvCameraRotation.SIDEWAYS_LEFT)
                }

                override fun onError(errorCode: Int) {
                    telemetry.addLine("ADDIE CV ERROR: code $errorCode")
                    Log.e("ADDIE CV ERROR", "code $errorCode")
                }
            })
        } else {
            pipeline = null
            cvCamera = null
        }
    }
}