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
public class ServoTest extends LinearOpMode {

    private DcMotorEx hang, intake, slideR, slideL;
    private Servo trussL, trussR, armR, armL, clawR, clawL, drone, inLift;

    private int liftPos = 0;
    @Override
    public void runOpMode() throws InterruptedException {
        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        hang = hardwareMap.get(DcMotorEx.class, "hang");
        intake = hardwareMap.get(DcMotorEx.class, "intake");
        slideR = hardwareMap.get(DcMotorEx.class, "slideR");
        slideL = hardwareMap.get(DcMotorEx.class, "slideL");
        inLift = hardwareMap.get(Servo.class, "inlift");


        trussR = hardwareMap.get(Servo.class, "trussR");
        trussL = hardwareMap.get(Servo.class, "trussL");
        armR = hardwareMap.get(Servo.class, "armR");
        armL = hardwareMap.get(Servo.class, "armL");
        clawR = hardwareMap.get(Servo.class, "clawR");
        drone = hardwareMap.get(Servo.class, "drone");



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
                intake.setPower(0.5);
            } else if (gamepad1.right_trigger>0.1){
                intake.setPower(-0.5);

            } else {
                intake.setPower(0);
            }


            //Placement



            if (gamepad1.dpad_left) {
                drone.setPosition(drone.getPosition() + 0.01);


            } else if (gamepad1.dpad_right) {
                drone.setPosition(drone.getPosition() - 0.01);
            } else if (gamepad1.dpad_down) {
                drone.setPosition(0);
            }
//0.36
            if (gamepad1.x) {
                armR.setPosition(armR.getPosition() + 0.01);

            } else if (gamepad1.b) {
                armR.setPosition(armR.getPosition() - 0.01);

            } else if (gamepad1.a) {
                armR.setPosition(0);
            }


            drive.updatePoseEstimate();

            telemetry.addData("x", drive.pose.position.x);
            telemetry.addData("y", drive.pose.position.y);
            telemetry.addData("heading (deg)", Math.toDegrees(drive.pose.heading.toDouble()));
            telemetry.addData("rightSlide", slideR.getCurrentPosition());
            telemetry.addData("leftSlide", slideL.getCurrentPosition());
            telemetry.addData("drone", drone .getPosition());
            telemetry.addData("right servo", armR .getPosition());

            telemetry.update();
        }
    }
}
