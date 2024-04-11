package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode

@Autonomous(name = "#ClayAuto", group = "Auto")
class ClayAuto : LinearOpMode() {
    //    public SpikeMark elementSpikeMark = SpikeMark.RIGHT;
    var teamAlliance = Alliance.RED
    var beginPose = Pose2d(0.0, 0.0, 0.0)
    var drive: MecanumDrive? = null
    var autoSub: CvSubsystem? = null
    var togglePreview = true
    @Throws(InterruptedException::class)
    override fun runOpMode() {
        drive = MecanumDrive(hardwareMap, beginPose)
        autoSub = CvSubsystem(teamAlliance, this)
        waitForStart()
        while (opModeIsActive()) {
            autoSub!!.detectElement()
            //            elementSpikeMark = autoSub.spikeMark;
//            telemetry.addData("getMaxDistance", autoSub.pipeline.maxDistance);
            if (togglePreview && gamepad2.a) {
                togglePreview = false
                autoSub!!.pipeline.showDefault = true
            } else if (!gamepad2.a) {
                togglePreview = true
            }
            if (gamepad1.x) {
                teamAlliance = Alliance.BLUE
            } else if (gamepad1.b) {
                teamAlliance = Alliance.RED
            }
            autoSub!!.pipeline.alliance = teamAlliance
            telemetry.addLine("Select Alliance (Gamepad1 X = Blue, Gamepad1 B = Red)")
            telemetry.addData("Current Alliance Selected", teamAlliance.toString())
            telemetry.update()
        }
    }
}
