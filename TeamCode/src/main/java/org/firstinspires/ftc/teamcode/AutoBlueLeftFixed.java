package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.AutoSubsystem;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.SpikeMark;
@Autonomous(name = "BlueLeftFixed", group = "Auto")
public class AutoBlueLeftFixed extends OpMode {
    public SpikeMark elementSpikeMark = SpikeMark.RIGHT;
    Alliance teamAlliance = Alliance.RED;

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
        elementSpikeMark = autoSub.elementDetection();
        telemetry.addData("getMaxDistance", autoSub.pipeline.getMaxDistance());

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

    @Override
    public void loop() { }
}
