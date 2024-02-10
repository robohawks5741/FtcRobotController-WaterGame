package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name = "BlueLeftFixed", group = "Auto")
public class AutoBlueLeftFixed extends OpMode {
    Alliance alliance = Alliance.RED;

    Pose2d beginPose = new Pose2d(0, 0, 0);
    public MecanumDrive drive;
    boolean togglePreview = true;
    public AutoSubsystem autoSub = new AutoSubsystem(this);

    @Override
    public void init(){
        drive = new MecanumDrive(hardwareMap, beginPose);
    }

    @Override
    public void init_loop(){
        autoSub.detectElement();
        telemetry.addData("getMaxDistance", autoSub.pipeline.getMaxDistance());

        if (togglePreview && gamepad2.a) {
            togglePreview = false;
            autoSub.pipeline.toggleShowAverageZone();
        } else if (!gamepad2.a) {
            togglePreview = true;
        }

        if (gamepad1.x) {
            alliance = Alliance.BLUE;
        } else if (gamepad1.b) {
            alliance = Alliance.RED;
        }

        autoSub.setAlliance(alliance);
        telemetry.addLine("Select Alliance (Gamepad1 X = Blue, Gamepad1 B = Red)");
        telemetry.addData("Current Alliance Selected", alliance.toString());

        telemetry.update();
    }

    @Override
    public void loop() { }
}
