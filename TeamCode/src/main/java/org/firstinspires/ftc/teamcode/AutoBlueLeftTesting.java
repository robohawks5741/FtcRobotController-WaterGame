package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.TankDrive;
import com.acmerobotics.roadrunner.SequentialAction;

@Autonomous(name = "#ClayAutoBlueLeftTest", group = "Auto")
public final class AutoBlueLeftTesting extends LinearOpMode {
    private DcMotorEx hang, intake, slideR, slideL;
    private Servo trussL, trussR, armR, armL, clawR, clawL, drone, inlift;

    public int placementZone = 1;
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

        Pose2d beginPose = new Pose2d(0, 0, 0);
        MecanumDrive drive = new MecanumDrive(hardwareMap, beginPose);
        waitForStart();

        Actions.runBlocking(
                drive.actionBuilder(beginPose)
                        .splineTo(new Vector2d(26.386, 0), 0)
                        .splineToConstantHeading(new Vector2d(23.386, 0), 0)
                        .turnTo(Math.toRadians(270))
                        .splineToConstantHeading(new Vector2d(25.682, 30.831), Math.toRadians(270))
                        .build()
        );
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
                        .splineTo(new Vector2d(53.80, 20), Math.toRadians(270))
                        .turnTo(Math.toRadians(270))
                        .build()
        );
        armR.setPosition(0.05);
        armL.setPosition(0.95);
        liftPos=0;
        slideR.setTargetPosition(-liftPos);
        slideL.setTargetPosition(liftPos);

        slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        slideR.setPower(1);
        slideL.setPower(1);

        Actions.runBlocking(
                drive.actionBuilder(drive.pose)
                        .splineTo(new Vector2d(53.80, -71.15), Math.toRadians(270))
                        .build()
        );




    }
}
