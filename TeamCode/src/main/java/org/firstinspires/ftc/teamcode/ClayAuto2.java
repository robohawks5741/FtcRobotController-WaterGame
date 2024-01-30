package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.TankDrive;

@Autonomous(name="ClayAuto", group="Auto")

public final class ClayAuto2 extends LinearOpMode {
    public int element_zone = 1;

    private AutoSubsystem autoSub=null;

    boolean togglePreview = true;
    @Override
    public void runOpMode() throws InterruptedException {
        Pose2d beginPose = new Pose2d(0, 0, 0);
            MecanumDrive drive = new MecanumDrive(hardwareMap, beginPose);

            waitForStart();

        autoSub = new AutoSubsystem(hardwareMap);

        String curAlliance = "red";

        while (!opModeIsActive() && !isStopRequested()){
            element_zone = autoSub.elementDetection(telemetry);
            telemetry.addData("getMaxDistance", autoSub.getMaxDistance());

            if (togglePreview && gamepad2.a){
                togglePreview = false;
                autoSub.toggleAverageZone();
            }else if (!gamepad2.a){
                togglePreview = true;
            }


            if (gamepad1.x){
                curAlliance = "blue";
            }else if (gamepad1.b){
                curAlliance = "red";
        }
            autoSub.setAlliance(curAlliance);
            telemetry.addData("Select Alliance (Gamepad1 X = Blue, Gamepad1 B = Red)", "");
            telemetry.addData("Current Alliance Selected : ", curAlliance.toUpperCase());


            telemetry.update();
        }


    }
}
