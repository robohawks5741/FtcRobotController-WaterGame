package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraOpenListener
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation

class CvSubsystem(private var alliance: Alliance, private var opMode: OpMode) {
    var camera: OpenCvCamera
    @JvmField
    var pipeline: CvTeamElementPipeline
    var resolutionWidth = 1920
    var resolutionHeight = 1080
    var spikeMark = SpikeMark.RIGHT

    val hardwareMap: HardwareMap
        get() = opMode.hardwareMap
    val telemetry: Telemetry
        get() = opMode.telemetry

    private inner class EpicCameraListener(
        var hardwareMap: HardwareMap,
        var telemetry: Telemetry
    ) : AsyncCameraOpenListener {
        override fun onOpened() {
            camera.startStreaming(resolutionWidth, resolutionHeight, OpenCvCameraRotation.UPRIGHT)
        }

        override fun onError(errorCode: Int) {
            telemetry.addLine("Camera open encountered an error! $errorCode")
        }
    }

    init {
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.search("Webcam 1")!!)
        pipeline = CvTeamElementPipeline(alliance, opMode)
        camera.setPipeline(pipeline)
        camera.openCameraDeviceAsync(EpicCameraListener(hardwareMap, telemetry))
    }

    fun detectElement() {
        spikeMark = pipeline.elementSpikeMark
        telemetry.addData("Element Zone", spikeMark)
        //        return spikeMark;
    }
}
