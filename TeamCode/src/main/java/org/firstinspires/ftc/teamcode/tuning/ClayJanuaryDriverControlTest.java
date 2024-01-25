package org.firstinspires.ftc.teamcode.tuning;

import static org.firstinspires.ftc.teamcode.MacrosKt.clamp;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.MecanumDrive;


import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


@TeleOp(name = "# Clay January Driver Control (testing)")
public class ClayJanuaryDriverControlTest extends LinearOpMode {
    Pose2d poseEstimate = new Pose2d(0, 0, 0);

    private DcMotorEx hang, intake, slideR, slideL;
    private Servo trussL, trussR, armR, armL, clawR, clawL, drone, inlift;

    private IMU imu;

    private DistanceSensor distance;
    private boolean useBotRelative = true;
    private boolean armDown = true;
    private boolean clawOpen = true;
    private  boolean movingUp = false;
    private int liftPos = 0;

    private int hangMode = 0;
    private boolean  pressed = false;

    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    public void armDown(){
        armR.setPosition(0.05);
        armL.setPosition(0.95);
        armDown = true;
    }

    public void armUp(){
        armL.setPosition(0.65);
        armR.setPosition(0.35);
        armDown = false;
    }


    public void clawOpen(){
        clawR.setPosition(0.36);
        clawL.setPosition(0);
        clawOpen = true;
    }

    public void clawClose(){
        clawR.setPosition(0.07);
        clawL.setPosition(0.29);
        clawOpen = false;
    }

    @Override
    public void runOpMode() throws InterruptedException {
        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        hang = hardwareMap.get(DcMotorEx.class, "hang");
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


        slideR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        slideL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hang.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        clawOpen();
        armDown();

        trussL.setPosition(0.32);
        trussR.setPosition(0.3);
        drone.setPosition(0.36);
        inlift.setPosition(0);
        waitForStart();



        while (!isStopRequested()) {
            // counter-clockwise
            double gyroYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

            // +X = forward
            // +Y = left
            Vector2d inputVector = new Vector2d(
                gamepad1.left_stick_y,
                -gamepad1.left_stick_x
            );

            // angle of the stick
            double inputTheta = atan2(inputVector.y, inputVector.x);
            // evaluated theta
            double driveTheta = inputTheta - gyroYaw; // + PI
            // magnitude of inputVector clamped to [0, 1]
            double inputPower = clamp(
                sqrt(
                    (inputVector.x * inputVector.x) +
                    (inputVector.y * inputVector.y)
                ),
                0.0,
                1.0
            );

            double driveRelativeX = cos(driveTheta) * inputPower;
            double driveRelativeY = sin(driveTheta) * inputPower;

            // \frac{1}{1+\sqrt{2\left(1-\frac{\operatorname{abs}\left(\operatorname{mod}\left(a,90\right)-45\right)}{45}\right)\ }}
//        powerModifier = 1.0 / (1.0 + sqrt(2.0 * (1.0 - abs((gyroYaw % (PI / 2)) - (PI / 4)) / (PI / 4))))

            PoseVelocity2d pv = new PoseVelocity2d(
                ((useBotRelative) ? new Vector2d(
                    driveRelativeX,
                    driveRelativeY
                ) : inputVector),
                -gamepad1.right_stick_x
            );
            drive.setDrivePowers(pv);

            //Intake
            if (gamepad1.left_trigger>0.1|| gamepad2.left_trigger>0.1){
                intake.setPower(0.65);
            } else {
                intake.setPower(0);
            }


            //Placement

            if (gamepad1.dpad_up || gamepad2.dpad_up) {
                if (clawOpen){
                    clawClose();
                    wait(300);
                }
                movingUp = true;
                liftPos = 1565;
                slideR.setTargetPosition(-liftPos);
                slideL.setTargetPosition(liftPos);

                slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);

                slideR.setPower(1);
                slideL.setPower(1);

            } else if (gamepad1.dpad_down || gamepad2.dpad_down){
                if (!armDown){
                    armDown();
                    wait(100);
                }

                liftPos = 0;
                slideR.setTargetPosition(-liftPos);
                slideL.setTargetPosition(liftPos);

                slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);

                slideR.setPower(1);
                slideL.setPower(1);




            } else if (gamepad1.dpad_left && liftPos > 199 || gamepad2.dpad_left&& liftPos > 99){

                if (liftPos == 600){
                    if (!armDown){
                        armDown();
                        wait(100);
                    }

                    liftPos = 0;
                } else {
                    liftPos -= 200;

                }
                slideR.setTargetPosition(-liftPos);
                slideL.setTargetPosition(liftPos);

                slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);

                slideR.setPower(1);
                slideL.setPower(1);
            } else if (gamepad1.dpad_right && liftPos<1501 || gamepad2.dpad_right && liftPos<1501){
                if (clawOpen) {
                    clawClose();
                    sleep(300);
                }

                if (liftPos == 0) {
                    liftPos=600;
                    movingUp = true;

                } else {
                    liftPos+=200;
                }
                slideR.setTargetPosition(-liftPos);
                slideL.setTargetPosition(liftPos);

                slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);

                slideR.setPower(1);

                slideL.setPower(1);
            }

            //Arm Rotation
            if (gamepad1.left_bumper || gamepad2.left_bumper){ //place
                armUp();
            } else if (gamepad1.right_bumper || gamepad2.right_bumper){ //Pickup
                armDown();
            }


            if (slideL.getCurrentPosition() > 500 &&movingUp){
                armUp();
                movingUp = false;
            }






            //Claw
            //Close
            if(gamepad1.x||gamepad2.x){
                clawClose();

            } else if(gamepad1.right_trigger > 0.1||gamepad2.right_trigger > 0.1){ //open

                if (!clawOpen){
                    clawOpen();
                    wait(200);
                }

                if (!armDown){
                    armDown();
                    wait(100);
                }
                liftPos = 0;
                slideR.setTargetPosition(-liftPos);
                slideL.setTargetPosition(liftPos);

                slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);

                slideR.setPower(1);
                slideL.setPower(1);


            }



            //Truss hang
            if (hangMode % 3 == 0){
                trussL.setPosition(0.32);
                trussR.setPosition(0.3);
            } else if (hangMode%3 == 1){
                trussL.setPosition(0.16);
                trussR.setPosition(0.45);
            }
            else if (hangMode%3 == 2){

                trussR.setPosition(0.65);
                trussL.setPosition(0);
            }


            if (gamepad1.y && !pressed || gamepad2.y && !pressed) {
                pressed = true;

                hangMode++;
            } else if (!gamepad1.y && !gamepad2.y){
                pressed = false;
            }


            if (gamepad1.a || gamepad2.a){
                hang.setPower(1);
            } else {
                hang.setPower(0);
            }


            if (gamepad1.back){
                trussL.setPosition(0.32);
                trussR.setPosition(0.3);
            }

            if (Math.abs(gamepad2.left_stick_y) > 0.1){
                intake.setPower(gamepad2.left_stick_y);
            }

            if(Math.abs(gamepad2.right_stick_y) > 0.1){
                hang.setPower(gamepad2.right_stick_y);
            }


            //Drone Launch
            if (gamepad1.b|| gamepad2.b){
                drone.setPosition(0.8);
            }




            drive.updatePoseEstimate();

            telemetry.addData("x", drive.pose.position.x);
            telemetry.addData("y", drive.pose.position.y);
            telemetry.addData("heading", drive.pose.heading.log());
            telemetry.addData("rightSlide", slideR.getCurrentPosition());
            telemetry.addData("leftSlide", slideL.getCurrentPosition());
            telemetry.addData("right servo", clawR.getPosition());
            telemetry.addData("left servo", clawL .getPosition());
            telemetry.addData("inlift", inlift.getPosition());
            telemetry.addData("hangMode", hangMode);
            telemetry.addData("Distance", distance.getDistance(DistanceUnit.CM));
            telemetry.update();
        }
    }
}