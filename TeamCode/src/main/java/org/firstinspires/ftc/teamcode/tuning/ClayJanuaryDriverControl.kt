package org.firstinspires.ftc.teamcode.tuning

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DistanceSensor
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.MecanumDrive
import org.firstinspires.ftc.teamcode.clamp
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@TeleOp(name = "# Clay January Driver Control")
class ClayJanuaryDriverControl : LinearOpMode() {
    var poseEstimate = Pose2d(0.0, 0.0, 0.0)
    private lateinit var hang: DcMotorEx
    private lateinit var intake: DcMotorEx
    private lateinit var slideR: DcMotorEx
    private lateinit var slideL: DcMotorEx
    private lateinit var trussL: Servo
    private lateinit var trussR: Servo
    private lateinit var armR: Servo
    private lateinit var armL: Servo
    private lateinit var clawR: Servo
    private lateinit var clawL: Servo
    private lateinit var drone: Servo
    private lateinit var inlift: Servo
    private lateinit var imu: IMU
    private lateinit var distance: DistanceSensor
    private lateinit var drive: MecanumDrive
    private var armDown = true
    private var leftClawOpen = true
    private var rightClawOpen = true
    private var movingUp = false
    private var liftPos = 0
    private var hangMode = 0
    private var pressed = false
    private var driverRelative = true
    private var driveModePressed = false

    private data class Timeout(val calltime: Long, val callback: () -> Unit)
    private val waitList: MutableSet<Timeout> = mutableSetOf()

    private fun wait(calltime: Long, callback: () -> Unit) = waitList.add(Timeout(calltime, callback))

    private fun armDown() {
        armR.position = 0.05
        armL.position = 0.95
        armDown = true
    }

    private fun armUp() {
        armL.position = 0.65
        armR.position = 0.35
        armDown = false
    }

    private fun leftClawOpen() {
        clawL.position = 0.0
        leftClawOpen = true
    }

    private fun rightClawOpen() {
        clawR.position = 0.36
        rightClawOpen = true
    }

    private fun leftClawClose() {
        clawL.position = 0.29
        leftClawOpen = false
    }

    private fun rightClawClose() {
        clawR.position = 0.07
        rightClawOpen = false
    }

    private fun updateDrive() {
        //Driver Relative Toggle
        if (gamepad1.back && !driveModePressed) {
            driveModePressed = true
            driverRelative = !driverRelative
        } else if (!gamepad1.back) {
            driveModePressed = false
        }
        if (driverRelative) {
            val gyroYaw = imu.robotYawPitchRollAngles.getYaw(AngleUnit.RADIANS)
            val rotation = gamepad1.right_stick_x

            // +X = forward
            // +Y = left
            val (x, y) = Vector2d(
                gamepad1.left_stick_y.toDouble(),
                -gamepad1.left_stick_x.toDouble()
            )

            // angle of the stick
            val inputTheta = atan2(y, x)
            // evaluated theta
            val driveTheta = inputTheta - gyroYaw // + PI
            // magnitude of inputVector clamped to [0, 1]
            val inputPower = sqrt(x * x + y * y).clamp(0.0, 1.0)
            val driveRelativeX = cos(driveTheta) * inputPower
            val driveRelativeY = sin(driveTheta) * inputPower
            val pv = PoseVelocity2d(
                Vector2d(driveRelativeX, driveRelativeY),
                -gamepad1.right_stick_x.toDouble()
            )
            drive.setDrivePowers(pv)
        } else {
            drive.setDrivePowers(
                PoseVelocity2d(
                    Vector2d(
                        -gamepad1.left_stick_y.toDouble(),
                        -gamepad1.left_stick_x.toDouble()
                    ), -gamepad1.right_stick_x.toDouble()
                )
            )
        }
    }

    private fun updateSlide() {
        slideR.targetPosition = -liftPos
        slideL.targetPosition = liftPos
        slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideR.power = 1.0
        slideL.power = 1.0
    }

    override fun runOpMode() {
        drive = MecanumDrive(hardwareMap, poseEstimate)
        hang = hardwareMap.get(DcMotorEx::class.java, "hang")
        intake = hardwareMap.get(DcMotorEx::class.java, "intake")
        slideR = hardwareMap.get(DcMotorEx::class.java, "slideR")
        slideL = hardwareMap.get(DcMotorEx::class.java, "slideL")
        drone = hardwareMap.get(Servo::class.java, "drone")
        trussR = hardwareMap.get(Servo::class.java, "trussR")
        trussL = hardwareMap.get(Servo::class.java, "trussL")
        armR = hardwareMap.get(Servo::class.java, "armR")
        armL = hardwareMap.get(Servo::class.java, "armL")
        clawR = hardwareMap.get(Servo::class.java, "clawR")
        clawL = hardwareMap.get(Servo::class.java, "clawL")
        inlift = hardwareMap.get(Servo::class.java, "inlift")
        distance = hardwareMap.get(DistanceSensor::class.java, "distance")
        imu = hardwareMap.get(IMU::class.java, "imu")

        slideR.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideR.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        slideL.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideL.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        hang.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        rightClawOpen()
        leftClawOpen()
        armDown()
        trussL.position = 0.32
        trussR.position = 0.3
        drone.position = 0.36
        inlift.position = 0.0
        waitForStart()

        while (opModeIsActive()) {

            for (timeout in waitList) if (time >= timeout.calltime) {
                timeout.callback()
                waitList.remove(timeout)
            }

            updateDrive()

            //Intake
            if (gamepad1.left_trigger > 0.1 || gamepad2.left_trigger > 0.1) {
                intake.power = 0.65
            } else {
                intake.power = 0.0
            }

            //Placement
            if (gamepad1.dpad_up || gamepad2.dpad_up) {
                if (rightClawOpen || leftClawOpen) {
                    rightClawClose()
                    leftClawClose()
                    wait(300) {
                        movingUp = true
                        liftPos = 1565
                        updateSlide()
                    }
                }
            } else if (gamepad1.dpad_down || gamepad2.dpad_down) {
                if (!armDown) {
                    armDown()
                    wait(100) {
                        liftPos = 0
                        updateSlide()
                    }
                } else {
                    liftPos = 0
                    updateSlide()
                }
            } else if (gamepad1.dpad_left && liftPos > 199 || gamepad2.dpad_left && liftPos > 99) {
                if (liftPos == 600) {
                    if (!armDown) {
                        armDown()
                        wait(100) {
                            liftPos = 0
                            updateSlide()
                        }
                    }
                } else {
                    liftPos -= 200
                    updateSlide()
                }
            } else if (gamepad1.dpad_right && liftPos < 1501 || gamepad2.dpad_right && liftPos < 1501) {
                if (rightClawOpen || leftClawOpen) {
                    rightClawClose()
                    leftClawClose()
                    wait(300) {
                        if (liftPos == 0) {
                            liftPos = 600
                            movingUp = true
                        } else {
                            liftPos += 200
                        }
                        updateSlide()
                    }
                } else {
                    if (liftPos == 0) {
                        liftPos = 600
                        movingUp = true
                    } else {
                        liftPos += 200
                    }
                    updateSlide()
                }
            }

            //Arm Rotation
            if (gamepad1.left_bumper || gamepad2.left_bumper) { //place
                leftClawOpen()
            } else if (gamepad1.right_bumper || gamepad2.right_bumper) { //Pickup
                rightClawOpen()
            }
            if (slideL.currentPosition > 500 && movingUp) {
                armUp()
                movingUp = false
            }


            //Claw
            //Close
            if (gamepad1.x || gamepad2.x) {
                rightClawClose()
                leftClawClose()
            } else if (gamepad1.right_trigger > 0.1 || gamepad2.right_trigger > 0.1) { //open
                if (!rightClawOpen || !leftClawOpen) {
                    rightClawOpen()
                    leftClawOpen()
                    // sleep is probably justified here
                    sleep(200)
                }
                if (!armDown) {
                    armDown()
                    wait(100) {
                        liftPos = 0
                        updateSlide()
                    }
                } else updateSlide()
            }


            //Truss hang
            if (hangMode % 3 == 0) {
                trussL.position = 0.32
                trussR.position = 0.3
            } else if (hangMode % 3 == 1) {
                trussL.position = 0.16
                trussR.position = 0.45
            } else if (hangMode % 3 == 2) {
                trussR.position = 0.65
                trussL.position = 0.0
            }

            //Truss Hang
            if (gamepad1.y && !pressed || gamepad2.y && !pressed) {
                pressed = true
                hangMode++
            } else if (!gamepad1.y && !gamepad2.y) {
                pressed = false
            }
            if (gamepad1.a || gamepad2.a) {
                hang.power = 1.0
            } else {
                hang.power = 0.0
            }


            // Driver 2 Override
            if (abs(gamepad2.left_stick_y) > 0.1) {
                intake.power = gamepad2.left_stick_y.toDouble()
            }
            if (abs(gamepad2.right_stick_y) > 0.1) {
                hang.power = gamepad2.right_stick_y.toDouble()
            }


            //Drone Launch
            if (gamepad1.b || gamepad2.b) {
                drone.position = 0.8
            }
            drive.updatePoseEstimate()
            telemetry.addData("DriverRelative", driverRelative)
            telemetry.addData("x", drive.pose.position.x)
            telemetry.addData("y", drive.pose.position.y)
            telemetry.addData("heading", drive.pose.heading.log())
            telemetry.addData("rightSlide", slideR.currentPosition)
            telemetry.addData("leftSlide", slideL.currentPosition)
            telemetry.addData("right arm", armR.position)
            telemetry.addData("left arm", armL.position)
            telemetry.addData("inlift", inlift.position)
            telemetry.addData("hangMode", hangMode)
            telemetry.addData("Distance", distance.getDistance(DistanceUnit.CM))
            telemetry.update()
        }
    }
}