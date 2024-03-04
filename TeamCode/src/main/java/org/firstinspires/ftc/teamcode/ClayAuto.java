package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name = "#ClayAuto", group = "Auto")
public final class ClayAuto extends LinearOpMode {
//    public SpikeMark elementSpikeMark = SpikeMark.RIGHT;
    Alliance teamAlliance = Alliance.RED;


    boolean togglePreview = true;

    @Override
    public void runOpMode() throws InterruptedException {
        Pose2d beginPose = new Pose2d(0, 0, 0);
        MecanumDrive drive = new MecanumDrive(hardwareMap, beginPose);
        ComputerVisionSubsystem autoSub = new ComputerVisionSubsystem(this);

        waitForStart();



        while (opModeIsActive()) {
            autoSub.detectElement();
//            elementSpikeMark = autoSub.spikeMark;
            telemetry.addData("getMaxDistance", autoSub.pipeline.maxDistance);

            if (togglePreview && gamepad2.a) {
                togglePreview = false;
                autoSub.pipeline.toggleShowAverageZone();
            } else if (!gamepad2.a) {
                togglePreview = true;
            }

            if (gamepad1.x) {
                teamAlliance = Alliance.BLUE;
            } else if (gamepad1.b) {
                teamAlliance = Alliance.RED;
            }

            autoSub.setAlliance(teamAlliance);
            telemetry.addLine("Select Alliance (Gamepad1 X = Blue, Gamepad1 B = Red)");
            telemetry.addData("Current Alliance Selected", teamAlliance.toString());

            telemetry.update();
        }


    }
}
