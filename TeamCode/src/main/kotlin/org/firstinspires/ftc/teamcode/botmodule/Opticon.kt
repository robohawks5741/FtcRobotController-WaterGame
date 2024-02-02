package org.firstinspires.ftc.teamcode.botmodule

import android.util.Size
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor
import org.firstinspires.ftc.vision.tfod.TfodProcessor

/**
 * AprilTag detection class
 */
class Opticon(cfg: ModuleConfig) : BotModule(cfg) {

    /**
     * The variable to store our instance of the AprilTag processor.
     */
    @JvmField val aprilTag: AprilTagProcessor = AprilTagProcessor.Builder()
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

    /**
     * The variable to store our instance of the TensorFlow Object Detection processor.
     */
    private val tfod: TfodProcessor?

    /**
     * The variable to store our instance of the vision portal.
     */
    private val visionPortal: VisionPortal?

    val detections: ArrayList<AprilTagDetection>
        get() = aprilTag.detections

    override fun modStart() {
        // Wait for the DS start button to be touched.
        telemetry.addData("DS preview on/off", "3 dots, Camera Stream")
        telemetry.addData(">", "Touch Play to start OpMode")
    }

    override fun modUpdate() {
        opticonTelemetry()

        // Push telemetry to the Driver Station.
        telemetry.update()

        // Share the CPU.
        Thread.sleep(20)
    }

    public fun setStreaming(status: Boolean): Boolean? {
        if (visionPortal == null) return null
        if (status) visionPortal.stopStreaming()
        else visionPortal.resumeStreaming()
        return status
    }

    override fun modStop() {
        // Save more CPU resources when camera is no longer needed.
        visionPortal!!.close()
    }

    private fun opticonTelemetry() {
        val currentRecognitions = tfod!!.recognitions
        telemetry.addData("# Objects Detected", currentRecognitions.size)

        // Step through the list of recognitions and display info for each one.
        for (recognition in currentRecognitions) {
            val x = ((recognition.left + recognition.right) / 2).toDouble()
            val y = ((recognition.top + recognition.bottom) / 2).toDouble()
            telemetry.addData("", " ")
            telemetry.addData(
                "Image",
                "%s (%.0f %% Conf.)",
                recognition.label,
                recognition.confidence * 100
            )
            telemetry.addData("- Position", "%.0f / %.0f", x, y)
            telemetry.addData("- Size", "%.0f x %.0f", recognition.width, recognition.height)
        } // end for() loop
    }

    init {
        if (camera == null) {
            visionPortal = null
            tfod = null
            status = Status(StatusEnum.MISSING_HARDWARE, hardwareMissing = setOf("Webcam 1"))
        } else {
            // Create the TensorFlow processor by using a builder.
            tfod = TfodProcessor.Builder()
                //   Use setModelAssetName() if the custom TF Model is built in as an asset (AS only).
                //   Use setModelFileName() if you have downloaded a custom team model to the Robot Controller.
                .setModelAssetName(TFOD_MODEL_ASSET)
                //.setModelFileName(TFOD_MODEL_FILE)
                // The following default settings are available to un-comment and edit as needed to
                // set parameters for custom models.
                .setModelLabels(LABELS)
                .setIsModelTensorFlow2(true)
                .setIsModelQuantized(true)
                .setModelInputSize(300)
                .setModelAspectRatio(16.0 / 9.0)
                .build()

            // or AprilTagProcessor.easyCreateWithDefaults()
            val portalBuilder = VisionPortal.Builder()

            // We only use a webcam in practice, so we don't have any code for builtin cameras.
            portalBuilder.setCamera(camera)

            // Choose a camera resolution. Not all cameras support all resolutions.
            portalBuilder.setCameraResolution(Size(640, 480))

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
            portalBuilder.addProcessor(tfod)

            // Build the Vision Portal, using the above settings.
            visionPortal = portalBuilder.build()

            // Disable or re-enable the aprilTag processor at any time.
            //visionPortal.setProcessorEnabled(aprilTag, true);
        }
    }

    companion object {
        private const val USE_WEBCAM = true // true for webcam, false for phone camera

        // TFOD_MODEL_ASSET points to a model file stored in the project Asset location,
        // this is only used for Android Studio when using models in Assets.
        private const val TFOD_MODEL_ASSET = "CenterStage.tflite"

        // Define the labels recognized in the model for TFOD (must be in training order!)
        private val LABELS = arrayOf(
            "Pixel",
            "TeamElement"
        )
    }
}