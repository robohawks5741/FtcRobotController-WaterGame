package org.firstinspires.ftc.teamcode.tuning;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.MecanumDrive;

@TeleOp
public class ServoTest1 extends LinearOpMode {

    private DcMotorEx hang, intake, slideR, slideL, leftFront, leftBack, rightBack, rightFront;
    private Servo trussL, trussR, armR, armL, clawR, clawL, drone;

    private int liftPos = 0;
    @Override
    public void runOpMode() throws InterruptedException {
        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        hang = hardwareMap.get(DcMotorEx.class, "hang");
        intake = hardwareMap.get(DcMotorEx.class, "intake");
        slideR = hardwareMap.get(DcMotorEx.class, "slideR");
        slideL = hardwareMap.get(DcMotorEx.class, "slideL");

        leftFront = hardwareMap.get(DcMotorEx.class, "frontL");
        leftBack = hardwareMap.get(DcMotorEx.class, "backL");
        rightBack = hardwareMap.get(DcMotorEx.class, "backR");
        rightFront = hardwareMap.get(DcMotorEx.class, "frontR");

        trussR = hardwareMap.get(Servo.class, "trussR");
        trussL = hardwareMap.get(Servo.class, "trussL");
        armR = hardwareMap.get(Servo.class, "armR");
        armL = hardwareMap.get(Servo.class, "armL");
        clawR = hardwareMap.get(Servo.class, "clawR");
        clawL = hardwareMap.get(Servo.class, "clawL");




        slideR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        slideL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hang.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


        waitForStart();

        while (!isStopRequested()) {
            drive.setDrivePowers(new PoseVelocity2d(
                    new Vector2d(
                            -gamepad1.left_stick_y,
                            -gamepad1.left_stick_x
                    ),
                    -gamepad1.right_stick_x
            ));

            //Intake
            if (gamepad1.left_trigger>0.1){
                hang.setPower(1);
            } else if (gamepad1.right_trigger>0.1){
                hang.setPower(-1);

            } else {
                hang.setPower(0);
            }


            //Placement



            if (gamepad1.dpad_left) {
                rightFront.setPower(1);


            } else if (gamepad1.dpad_right) {
                rightBack.setPower(1);
            } else if (gamepad1.dpad_down) {
                leftFront.setPower(1);
            } else if (gamepad1.dpad_up){
                leftBack.setPower(1);
            }
//0.36
            if (gamepad1.x) {
                trussR.setPosition(trussR.getPosition() + 0.01);

            } else if (gamepad1.b) {
                trussR.setPosition(trussR.getPosition() - 0.01);

            } else if (gamepad1.a) {
                trussR.setPosition(0);
            }

            drive.updatePoseEstimate();

            telemetry.addData("x", drive.pose.position.x);
            telemetry.addData("y", drive.pose.position.y);
            telemetry.addData("heading (deg)", Math.toDegrees(drive.pose.heading.toDouble()));
            telemetry.addData("rightSlide", slideR.getCurrentPosition());
            telemetry.addData("leftSlide", slideL.getCurrentPosition());
            telemetry.addData("left servo", trussL .getPosition());
            telemetry.addData("right servo", trussR .getPosition());

            telemetry.update();
        }
    }
}
