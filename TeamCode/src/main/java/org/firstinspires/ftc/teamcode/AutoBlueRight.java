package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

@Autonomous(name = "#ClayAutoBlueRight", group = "Auto")
public final class AutoBlueRight extends LinearOpMode {
    private DcMotorEx hang, intake, slideR, slideL;
    private Servo trussL, trussR, armR, armL, clawR, clawL, drone, inlift;

    public int placementZone = 2;
    private IMU imu;

    private DistanceSensor distance;
    int liftPos = 0;
    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }


    @Override
    public void runOpMode() throws InterruptedException {

        intake = hardwareMap.get(DcMotorEx.class, "intake");
        slideR = hardwareMap.get(DcMotorEx.class, "slideR");
        slideL = hardwareMap.get(DcMotorEx.class, "slideL");
        drone = hardwareMap.get(Servo.class, "drone");
        trussR = hardwareMap.get(Servo.class, "trussR");
        trussL = hardwareMap.get(Servo.class, "trussL");
        armR = hardwareMap.get(Servo.class, "armR");
        armL = hardwareMap.get(Servo.class, "armL");
        clawR = hardwareMap.get(Servo.class, "clawR");
        clawL = hardwareMap.get(Servo.class, "clawL");
        inlift = hardwareMap.get(Servo.class, "inlift");
        distance = hardwareMap.get(DistanceSensor.class, "distance");
        imu = hardwareMap.get(IMU.class, "imu");
        clawR.setPosition(0.07);
        clawL.setPosition(0.29);

        inlift.setPosition(0.34);
        armR.setPosition(0.05);
        armL.setPosition(0.95);
        slideR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        slideL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hang.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        Pose2d beginPose = new Pose2d(0, 0, 0);
            MecanumDrive drive = new MecanumDrive(hardwareMap, beginPose);
        waitForStart();
if (placementZone == 1) {
    Actions.runBlocking(
             drive.actionBuilder(beginPose)
                    .splineTo(new Vector2d(20.12, -6.98), Math.toRadians(-31.68))
                  .splineToConstantHeading(new Vector2d(7, 3), Math.toRadians(31.68))
                 .turnTo(Math.toRadians(0))
               .splineToConstantHeading(new Vector2d(49.28, 4.45), Math.toRadians(0))
                 .turnTo(Math.toRadians(90))
                     .splineToConstantHeading(new Vector2d(50.11, 76.62), Math.toRadians(90))
                     .turnTo(Math.toRadians(270))
                     .splineToConstantHeading(new Vector2d(19.38, 87.4), Math.toRadians(270))
                     .build()
    );

} else if (placementZone == 2) {
    Actions.runBlocking(
            drive.actionBuilder(beginPose)
                    .splineTo(new Vector2d(26.32, 2.04), 0)
                    .splineToConstantHeading(new Vector2d(16.8, -7.89), 0)
                    .splineToConstantHeading(new Vector2d(48.94, -8.83), 0)

                    .turnTo(Math.toRadians(90))
                    .splineToConstantHeading(new Vector2d(52.55, 77.19), Math.toRadians(90))
                    .turnTo(Math.toRadians(270))
                    .splineToConstantHeading(new Vector2d(28.21, 89.29), Math.toRadians(90))

                    .build()
    );
} else if (placementZone ==3){

                Actions.runBlocking(
                        drive.actionBuilder(beginPose)
                                .splineTo(new Vector2d(21.83, -5.68), Math.toRadians(-25))
                               .splineToConstantHeading(new Vector2d(25.96, 4.070), Math.toRadians(157.81))
                                .turnTo(Math.toRadians(270))
                                .splineToConstantHeading(new Vector2d(31.3, 31), Math.toRadians(270))
                                .build()
                );
        }

        liftPos=600;
        slideR.setTargetPosition(-liftPos);
        slideL.setTargetPosition(liftPos);

        slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        slideR.setPower(1);
        slideL.setPower(1);

        wait(300);
        armL.setPosition(0.65);
        armR.setPosition(0.35);
        wait(300);
        clawL.setPosition(0);
        clawR.setPosition(0.36);
        wait(300);

        Actions.runBlocking(
                drive.actionBuilder(drive.pose)
                        .lineToY(83)
                        .build()
        );
        armR.setPosition(0.05);
        armL.setPosition(0.95);
        wait(100);
        liftPos=0;
        slideR.setTargetPosition(-liftPos);
        slideL.setTargetPosition(liftPos);

        slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        slideR.setPower(1);
        slideL.setPower(1);
        inlift.setPosition(0);
        wait(200);
       Actions.runBlocking(
                drive.actionBuilder(drive.pose)
                        .strafeToConstantHeading(new Vector2d(50, 84.72))
                        .splineToConstantHeading(new Vector2d(50, 101.92), Math.toRadians(270))
                        .build()
        );


    }
}
