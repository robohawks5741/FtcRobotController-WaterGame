package org.firstinspires.ftc.teamcode.component

import android.util.Size
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.Alliance
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.SpikeMark
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.VisionPortal.CameraState
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.openftc.easyopencv.OpenCvPipeline
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Vision portal component
 */
class Vision(manager: ComponentManager) : Component(manager) {

    enum class VisionMode {
        APRILTAG,
        OPENCV
    }

    var mode: VisionMode = VisionMode.OPENCV
        set(desiredMode) {
            // delta
            if (field != desiredMode) when (desiredMode) {
                VisionMode.APRILTAG -> {

                }
                VisionMode.OPENCV -> {
                    visionPortal = null
                }
            }
            field = desiredMode
        }

    private val aprilTag: AprilTagProcessor = AprilTagProcessor.Builder()
//                    The following default settings are available to un-comment and edit as needed.
        .setDrawAxes(false)
//                        .setDrawCubeProjection(false)
        .setDrawTagOutline(true)
        .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
//                        .setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
        .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
//                    == CAMERA CALIBRATION ==
//                    If you do not manually specify calibration parameters, the SDK will attempt
//                    to load a predefined calibration for your camera.
//                            .setLensIntrinsics(578.272, 578.272, 402.145, 221.506)
//                    ... these parameters are fx, fy, cx, cy.
        .build()
//    @JvmField val tfod: TfodProcessor?
    private val openCvPipeline: OpenCvPipeline = object : OpenCvPipeline() {
        var elementColor: List<Int> = mutableListOf(0, 0, 255) //(red, green, blue)

        var toggleShow = true
        var original: Mat = Mat()
        var zone1: Mat = Mat()
        var zone2: Mat = Mat()
        var zone3: Mat = Mat()
        var avgColor1: Scalar? = null
        var avgColor2: Scalar? = null
        var avgColor3: Scalar? = null
        var distance1 = 1.0
        var distance2 = 1.0
        var distance3 = 1.0
        var maxDistance = 0.0

        override fun init(mat: Mat) {
            //Defining Zones
            //Rect(top left x, top left y, bottom right x, bottom right y)
            // TODO: change these constants
            if (BotShared.autoShouldParkRight) {
                zone1 = mat.submat(Rect(0, 0, 639, 600))
                zone2 = mat.submat(Rect(641, 0, 639, 600))
                zone3 = mat.submat(Rect(1281, 0, 639, 600))
            } else {
                zone1 = mat.submat(Rect(0, 0, 639, 600))
                zone2 = mat.submat(Rect(641, 0, 639, 600))
                zone3 = mat.submat(Rect(1281, 0, 639, 600))
            }
        }

        override fun processFrame(input: Mat): Mat {

            //Creating duplicate of original frame with no edits
            original = input.clone()

    //        input = input.submat(new Rect(0));
            // TODO: use OKLab/Lch for color delta and improve algorithm
    //        Imgproc.cvtColor(input, grey, Imgproc.COLOR_RGBALab);

            //Averaging the colors in the zones
            avgColor1 = Core.mean(zone1)
            avgColor2 = Core.mean(zone2)
            avgColor3 = Core.mean(zone3)

            //Putting averaged colors on zones (we can see on camera now)
            zone1.setTo(avgColor1)
            zone2.setTo(avgColor2)
            zone3.setTo(avgColor3)

            distance1 = colorDistance(avgColor1, elementColor)
            distance2 = colorDistance(avgColor2, elementColor)
            distance3 = colorDistance(avgColor3, elementColor)

            maxDistance = min(distance1, distance2)
            maxDistance = min(maxDistance, distance3)

            elementSpikeMark = when (maxDistance) {
                distance1 -> SpikeMark.LEFT
                distance2 -> SpikeMark.CENTER
                distance3 -> SpikeMark.RIGHT
                else -> SpikeMark.CENTER
            }

            // Allowing for the showing of the averages on the stream
            return if (toggleShow) {
                input
            } else {
                original
            }
        }

        private fun colorDistance(color1: Scalar?, color2: List<Int>): Double {
            val r1 = color1!!.`val`[0]
            val g1 = color1.`val`[1]
            val b1 = color1.`val`[2]
            val r2 = color2[0]
            val g2 = color2[1]
            val b2 = color2[2]

    //        deltaL = L1 - L2
    //        C1 = √(a1² + b1²)
    //        C2 = √(a2² + b2²)
    //        ΔC = C1 - C2
    //        Δa = a1 - a2
    //        Δb = b1 - b2
    //        ΔH = √(Δa² + Δb² - ΔC²)
    //        ΔEOK = √(ΔL² + ΔC² + ΔH²)
            return sqrt((r1 - r2).pow(2.0) + (g1 - g2).pow(2.0) + (b1 - b2).pow(2.0))
        }

        fun setAlliancePipe(alliance: Alliance?) {
            elementColor = when (alliance) {
                Alliance.RED -> mutableListOf(255, 0, 0)
                Alliance.BLUE -> mutableListOf(0, 0, 255)
                null -> TODO()
            }
        }

        fun toggleShowAverageZone() {
            toggleShow = !toggleShow
        }

        // TODO: document
        var elementSpikeMark = SpikeMark.RIGHT
    }

    @JvmField val camera = shared.camera

    override val status: Status

    /**
     * The variable to store our instance of the vision portal.
     */
    private var visionPortal: VisionPortal?

    override fun update() {
        if (visionPortal != null) {
            opticonTelemetry()
            when (val camState = visionPortal?.cameraState) {
                CameraState.CAMERA_DEVICE_CLOSED,
                CameraState.CLOSING_CAMERA_DEVICE,
                CameraState.ERROR,
                CameraState.STARTING_STREAM,
                CameraState.STOPPING_STREAM,
                CameraState.OPENING_CAMERA_DEVICE,
                -> { /* do nothing, these states are all dangerous */ }
                // this throws an exception if you don't check for state. i love the ftc sdk ;)
                CameraState.CAMERA_DEVICE_READY -> if (isStreaming) visionPortal?.resumeStreaming()
                CameraState.STREAMING -> if (!isStreaming) visionPortal?.stopStreaming()
                else -> { /* we don't really care */ }
            }
            // Share the CPU.
            Thread.sleep(20)
        }
    }

    val detections: ArrayList<AprilTagDetection>?
        get() = if (visionPortal == null) aprilTag.detections else null

    var isStreaming: Boolean = true

    override fun stop() {
        // Save more CPU resources when camera is no longer needed.
        visionPortal?.close()

    }

    private fun opticonTelemetry() {
//        val currentRecognitions = tfod!!.recognitions
//        telemetry.addData("TFOD Object Count", currentRecognitions.size)
//
//        // Step through the list of recognitions and display info for each one.
//        for (recognition in currentRecognitions) {
//            val x = ((recognition.left + recognition.right) / 2).toDouble()
//            val y = ((recognition.top + recognition.bottom) / 2).toDouble()
//            telemetry.addData("", " ")
//            telemetry.addData(
//                "Image",
//                "%s (%.0f %% Conf.)",
//                recognition.label,
//                recognition.confidence * 100
//            )
//            telemetry.addData("- Position", "%.0f / %.0f", x, y)
//            telemetry.addData("- Size", "%.0f x %.0f", recognition.width, recognition.height)
//        }

        val aprilTagDetections = aprilTag.detections
        log("AprilTag Count: ${aprilTagDetections.size}")
        // Step through the list of recognitions and display info for each one.
        for (detection in aprilTagDetections) {
            if (detection.metadata != null) {
                telemetry.addLine("\n==== (ID ${detection.id}) ${detection.metadata.name}")
                telemetry.addLine(
                    String.format(
                        "XYZ %6.1f %6.1f %6.1f  (inch)",
                        detection.ftcPose.x,
                        detection.ftcPose.y,
                        detection.ftcPose.z
                    )
                )
                telemetry.addLine(
                    String.format(
                        "PRY %6.1f %6.1f %6.1f  (deg)",
                        detection.ftcPose.pitch,
                        detection.ftcPose.roll,
                        detection.ftcPose.yaw
                    )
                )
                telemetry.addLine(
                    String.format(
                        "RBE %6.1f %6.1f %6.1f  (inch, deg, deg)",
                        detection.ftcPose.range,
                        detection.ftcPose.bearing,
                        detection.ftcPose.elevation
                    )
                )
            } else {
                telemetry.addLine("\n==== (ID ${detection.id}) Unknown")
                telemetry.addLine(
                    String.format(
                        "Center %6.0f %6.0f   (pixels)",
                        detection.center.x,
                        detection.center.y
                    )
                )
            }
        }
    }

    init {
        val functionality = if (camera == null) Functionality.FULL else Functionality.NONE
        val hardwareSet: Set<HardwareUsage> = setOf(
            HardwareUsage("Webcam 1", WebcamName::class, camera != null),
        )
        status = Status(
            functionality,
            hardwareSet
        )
        if (camera == null) {
//            tfod = null
            visionPortal = null
        } else {
//            // Create the TensorFlow processor by using a builder.
//            tfod = TfodProcessor.Builder()
//                //   Use setModelAssetName() if the custom TF Model is built in as an asset (AS only).
//                //   Use setModelFileName() if you have downloaded a custom team model to the Robot Controller.
//                .setModelAssetName(TFOD_MODEL_ASSET)
////                .setModelFileName(TFOD_MODEL_FILE)
//                // The following default settings are available to un-comment and edit as needed to
//                // set parameters for custom models.
//                .setModelLabels(LABELS)
//                .setIsModelTensorFlow2(true)
////                .setIsModelQuantized(true)
//                .setModelInputSize(640)
//                .setModelAspectRatio(16.0 / 9.0)
//                .build()

            // or AprilTagProcessor.easyCreateWithDefaults()
            val portalBuilder = VisionPortal.Builder()

            // We only use a webcam in practice, so we don't have any code for builtin cameras.
            portalBuilder.setCamera(camera)

            // Choose a camera resolution. Not all cameras support all resolutions.
            portalBuilder.setCameraResolution(Size(1280, 720))

            // Enable the RC preview (LiveView).  Set "false" to omit camera monitoring.
            portalBuilder.enableLiveView(true)

            // Set the stream format; MJPEG uses less bandwidth than default YUY2.
            // builder.setStreamFormat(VisionPortal.StreamFormat.YUY2);

            // Choose whether or not LiveView stops if no processors are enabled.
            // If set "true", monitor shows solid orange screen if no processors enabled.
            // If set "false", monitor shows camera view without annotations.
            //builder.setAutoStopLiveView(false);

            // Set and enable the processor.
            portalBuilder.addProcessor(aprilTag)
//            portalBuilder.addProcessor(tfod)

            // Build the Vision Portal, using the above settings.
            visionPortal = portalBuilder.build()

            // Disable or re-enable the aprilTag processor at any time.
            //visionPortal.setProcessorEnabled(aprilTag, true);
            isStreaming = true
        }
    }
}