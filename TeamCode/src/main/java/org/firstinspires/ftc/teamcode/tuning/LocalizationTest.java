package org.firstinspires.ftc.teamcode.tuning;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import com.qualcomm.robotcore.hardware. Servo;

@TeleOp
public class LocalizationTest extends LinearOpMode {

    private DcMotorEx hang, intake, slideR, slideL;
    private Servo trussL, trussR, armR, armL, clawR, clawL, drone;

    private int liftPos = 0;
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



        slideR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        slideL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hang.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


        trussR.setPosition(0.32);
        trussL.setPosition(0.3);
        armR.setPosition(0.3);
        armL.setPosition(1);
        clawR.setPosition(0.07);
        clawL.setPosition(0.29);
        drone.setPosition(0.36);
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
            if (gamepad1.left_trigger>0.1|| gamepad2.left_trigger>0.1){
                intake.setPower(0.5);
            } else {
                intake.setPower(0);
            }


            //Placement

            if(gamepad1.dpad_up || gamepad2.dpad_up){

                if(liftPos == 0){

                    clawR.setPosition(0.36);
                    clawL.setPosition(0);

                    armL.setPosition(0.63);
                    armR.setPosition(0.67);
                }
                liftPos = 1600;
                slideR.setTargetPosition(liftPos);
                slideL.setTargetPosition(-liftPos);
                slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);

                slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                slideR.setPower(1);

                slideL.setPower(1);

            } else if (gamepad1.dpad_down || gamepad2.dpad_down){
                liftPos = 0;
                slideR.setTargetPosition(liftPos);
                slideL.setTargetPosition(-liftPos);

                slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);

                slideR.setPower(1);
                slideL.setPower(1);
                //Swing arm down
                armL.setPosition(1); //0.93 - 1
                armR .setPosition(0.3);

                //Open Claws
                clawR.setPosition(0.07);
                clawL.setPosition(0.29);



            } else if (gamepad1.dpad_left&& liftPos > 199 || gamepad2.dpad_left&& liftPos > 99){
                liftPos-=100;
                slideR.setTargetPosition(liftPos);
                slideL.setTargetPosition(-liftPos);

                slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);

                slideR.setPower(1);

                slideL.setPower(1);
            } else if (gamepad1.dpad_right && liftPos<1501 || gamepad2.dpad_right && liftPos<1501){
                if(liftPos == 0){

                    clawR.setPosition(0.36);
                    clawL.setPosition(0);

                    armL.setPosition(0.63);
                    armR.setPosition(0.67);
                }
                liftPos+=100;
                slideR.setTargetPosition(liftPos);
                slideL.setTargetPosition(-liftPos);

                slideR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                slideL.setMode(DcMotor.RunMode.RUN_TO_POSITION);

                slideR.setPower(1);

                slideL.setPower(1);
            }

            //Arm Rotation
            if (gamepad1.left_bumper || gamepad2.left_bumper){ //place
                armL.setPosition(0.63);
                armR.setPosition(0.67);
            } else if (gamepad1.right_bumper || gamepad2.right_bumper){ //Pickup
                armL.setPosition(1); //0.93 - 1
                armR .setPosition(0.3);
            }




            //Claw
            //Close
            if(gamepad1.x||gamepad2.x){
                clawR.setPosition(0.36);
                clawL.setPosition(0);
            } else if(gamepad1.right_trigger > 0.1||gamepad2.right_trigger > 0.1){ //open
                clawR.setPosition(0.07);
                clawL.setPosition(0.29);
            }



            //Truss hang
            if (gamepad1.y ||gamepad2.y){
                trussR.setPosition(0);
                trussL.setPosition(0.65);
            }
            if (gamepad1.a || gamepad2.a){
                hang.setPower(1);
            } else {
                hang.setPower(0);
            }


            //Drone Launch
            if (gamepad1.b|| gamepad2.b){
                drone.setPosition(0.8);
            }


            drive.updatePoseEstimate();

            telemetry.addData("x", drive.pose.position.x);
            telemetry.addData("y", drive.pose.position.y);
            telemetry.addData("heading (deg)", Math.toDegrees(drive.pose.heading.toDouble()));
            telemetry.addData("rightSlide", slideR.getCurrentPosition());
            telemetry.addData("leftSlide", slideL.getCurrentPosition());
            telemetry.addData("right servo", clawR.getPosition());
            telemetry.addData("left servo", clawL .getPosition());
            telemetry.update();
        }
    }
}
