package org.firstinspires.ftc.teamcode.botmodule

import android.util.Size
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.VisionPortal.CameraState
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor
import org.firstinspires.ftc.vision.tfod.TfodProcessor

/**
 * AprilTag detection class
 */
class Opticon(cfg: ModuleConfig) : BotModule(cfg) {

//    /**
//     * The variable to store our instance of the AprilTag processor.
//     */
//    @JvmField val aprilTag: AprilTagProcessor = AprilTagProcessor.Builder()
        // The following default settings are available to un-comment and edit as needed.

        //.setDrawAxes(false)
        //.setDrawCubeProjection(false)
        //.setDrawTagOutline(true)
        //.setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
        //.setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
        //.setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
        // == CAMERA CALIBRATION ==
        // If you do not manually specify calibration parameters, the SDK will attempt
        // to load a predefined calibration for your camera.
        //.setLensIntrinsics(578.272, 578.272, 402.145, 221.506)
        // ... these parameters are fx, fy, cx, cy.

//        .build()

    /**
     * The variable to store our instance of the TensorFlow Object Detection processor.
     */
    @JvmField val tfod: TfodProcessor?
    @JvmField val aprilTag: AprilTagProcessor?

    /**
     * The variable to store our instance of the vision portal.
     */
    private val visionPortal: VisionPortal?

//    val detections: ArrayList<AprilTagDetection>
//        get() = aprilTag.detections

    override fun modStart() {
        if (camera == null) {
            status = Status(StatusEnum.BAD, null, hardwareMissing = setOf("Webcam 1"))
        }
        // Wait for the DS start button to be touched.
        telemetry.addData("DS preview on/off", "3 dots, Camera Stream")
        telemetry.addData(">", "Touch Play to start OpMode")
    }

    override fun modUpdate() {
        if (visionPortal != null) {
            opticonTelemetry()
            when (val camState = visionPortal.cameraState) {
                CameraState.CAMERA_DEVICE_CLOSED,
                CameraState.CLOSING_CAMERA_DEVICE,
                CameraState.ERROR,
                CameraState.STARTING_STREAM,
                CameraState.STOPPING_STREAM,
                CameraState.OPENING_CAMERA_DEVICE, -> { /* do nothing, these states are all dangerous */ }
                // this throws an exception if you don't check for state. i love the ftc sdk ;)
                else -> when (camState) {
                    CameraState.CAMERA_DEVICE_READY -> if (isStreaming) visionPortal.resumeStreaming()
                    CameraState.STREAMING -> if (!isStreaming) visionPortal.stopStreaming()
                    else -> { /* shouldn't ever get here, but whatever */  }
                }
            }
            // Share the CPU.
            Thread.sleep(20)
        }
    }

    var isStreaming: Boolean = true

    override fun modStop() {
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

        val detections = aprilTag!!.detections
        telemetry.addData("AprilTag Count", detections.size)
        // Step through the list of recognitions and display info for each one.
        for (detection in detections) {
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
        if (camera == null) {
            visionPortal = null
            tfod = null
            aprilTag = null
            status = Status(StatusEnum.BAD, hardwareMissing = setOf("Webcam 1"))
        } else {
            // Create the TensorFlow processor by using a builder.
            tfod = TfodProcessor.Builder()
                //   Use setModelAssetName() if the custom TF Model is built in as an asset (AS only).
                //   Use setModelFileName() if you have downloaded a custom team model to the Robot Controller.
                .setModelAssetName(TFOD_MODEL_ASSET)
//                .setModelFileName(TFOD_MODEL_FILE)
                // The following default settings are available to un-comment and edit as needed to
                // set parameters for custom models.
                .setModelLabels(LABELS)
                .setIsModelTensorFlow2(true)
//                .setIsModelQuantized(true)
                .setModelInputSize(640)
                .setModelAspectRatio(16.0 / 9.0)
                .build()

            aprilTag = AprilTagProcessor.Builder()
                // The following default settings are available to un-comment and edit as needed.

                //.setDrawAxes(false)
                //.setDrawCubeProjection(false)
                //.setDrawTagOutline(true)
                //.setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                //.setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
                //.setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                // == CAMERA CALIBRATION ==
                // If you do not manually specify calibration parameters, the SDK will attempt
                // to load a predefined calibration for your camera.
                //.setLensIntrinsics(578.272, 578.272, 402.145, 221.506)
                // ... these parameters are fx, fy, cx, cy.

                .build()

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

    companion object {
        private const val USE_WEBCAM = true // true for webcam, false for phone camera

        // TFOD_MODEL_ASSET points to a model file stored in the project Asset location,
        // this is only used for Android Studio when using models in Assets.
        private const val TFOD_MODEL_ASSET = "TeamElements.tflite"

        // Define the labels recognized in the model for TFOD (must be in training order!)
        private val LABELS = arrayOf(
            "Red Element",
            "Blue Element",
        )
    }
}