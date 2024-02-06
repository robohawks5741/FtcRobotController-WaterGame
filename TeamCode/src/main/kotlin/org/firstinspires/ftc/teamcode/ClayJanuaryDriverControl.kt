package org.firstinspires.ftc.teamcode

import android.util.Log
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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@TeleOp(name = "# Clay January Driver Control")
class ClayJanuaryDriverControl : LinearOpMode() {
    private var poseEstimate = Pose2d(0.0, 0.0, 0.0)
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
    private var movingUp = false

    private var pressed = false
    private var driverRelative = true
    private var driveModePressed = false

    // automatically updates the truss servos when the value is changed
    private var trussPos = TrussPosition.DOWN
        set(pos) {
            field = pos
            trussL.position = trussPos.leftPos
            trussR.position = trussPos.rightPos
        }

    private var isArmDown = true
        set(status) {
            armR.position = if (status) 0.05 else 0.35
            armL.position = if (status) 0.95 else 0.65
            field = status
        }

    private var isLeftClawOpen: Boolean = true
        set(status) {
            clawL.position = if (status) 0.0 else 0.29
            field = status
        }
    private var isRightClawOpen: Boolean = true
        set(status) {
            clawR.position = if (status) 0.36 else 0.07
            field = status
        }

    private fun updateDrive() {
        //Driver Relative Toggle
        if (gamepad1.back && !driveModePressed) {
            driveModePressed = true
            driverRelative = !driverRelative
            imu.resetYaw()
        } else if (!gamepad1.back) {
            driveModePressed = false
        }
        if (driverRelative) {
            val gyroYaw = imu.robotYawPitchRollAngles.getYaw(AngleUnit.RADIANS)

            // +X = forward
            // +Y = left
            val x = -gamepad1.left_stick_y.toDouble().stickCurve()
            val y = -gamepad1.left_stick_x.toDouble().stickCurve()

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
                        -gamepad1.left_stick_y.toDouble().stickCurve(),
                        -gamepad1.left_stick_x.toDouble().stickCurve()
                    ), -gamepad1.right_stick_x.toDouble().stickCurve()
                )
            )
        }
    }

    private var liftPos = 0
        set(pos) {
            field = pos
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

        data class Timeout(val calltime: Long, val callback: () -> Unit)
        val waitList: ArrayList<Timeout> = arrayListOf();
        fun wait(calltime: Long, callback: () -> Unit) = waitList.add(Timeout((time * 1000).toLong() + calltime, callback))

        imu.resetYaw()
        slideR.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideR.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        slideL.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideL.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        hang.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        isRightClawOpen = true
        isLeftClawOpen = true
        isArmDown = true

        trussPos = TrussPosition.DOWN
        drone.position = 0.36
        inlift.position = 0.0
        waitForStart()

        while (opModeIsActive()) {

            for (timeout in waitList) if (time * 1000 >= timeout.calltime) {
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

            // Placement
            movingUp = true
            if ((gamepad1.dpad_up || gamepad2.dpad_up)/* && !movingUp*/) {
                if (isRightClawOpen || isLeftClawOpen) {
                    isRightClawOpen = false
                    isLeftClawOpen = false
                    movingUp = true
                    sleep(300)
                    liftPos = 1565
                    wait(300) {
                        liftPos = 1565
                        movingUp = false
                    }
                } else {
                    movingUp = true
                    liftPos = 1565
                }

            } else if (gamepad1.dpad_down || gamepad2.dpad_down) {
                if (!isArmDown) {
                    isArmDown = true
                    // used to be 100
                    wait(600) {
                        liftPos = 0
                    }
                } else {
                    liftPos = 0

                }
                // these numbers should be equal
            } else if (gamepad1.dpad_left && liftPos > 199 || gamepad2.dpad_left && liftPos > 199) {
                if (liftPos == 600) {
                    if (!isArmDown) {
                        isArmDown = true
                        // used to be 100
                        wait(600) {
                            liftPos = 0
                        }
                    }
                } else {
                    liftPos -= 200
                }
            } else if (gamepad1.dpad_right && liftPos < 1501 || gamepad2.dpad_right && liftPos < 1501) {
                if (isRightClawOpen || isLeftClawOpen) {
                    isRightClawOpen = false
                    isLeftClawOpen = false
                    wait(300) {
                        if (liftPos == 0) {
                            liftPos = 600
                            movingUp = true
                        } else {
                            liftPos += 200
                        }
                    }
                } else {
                    if (liftPos == 0) {
                        liftPos = 600
                        movingUp = true
                    } else {
                        liftPos += 200
                    }
                }
            }

            //Arm Rotation
            if (gamepad1.left_bumper || gamepad2.left_bumper) { //place
                isLeftClawOpen = true
            } else if (gamepad1.right_bumper || gamepad2.right_bumper) { //Pickup
                isRightClawOpen = true
            }
            if (slideL.currentPosition > 500 && movingUp) {
                isArmDown = false
                movingUp = false
            }


            //Claw
            //Close
            if (gamepad1.x || gamepad2.x) {
                isLeftClawOpen = false
                isRightClawOpen = false
            } else if (gamepad1.right_trigger > 0.1 || gamepad2.right_trigger > 0.1) { //open
                if (!isRightClawOpen || !isLeftClawOpen) {
                    isLeftClawOpen = true
                    isRightClawOpen = true
                    Log.i("CJDC", "!rco || !lco about to sleep 200ms")
                    // sleep is probably justified here
                    sleep(200)
                    Log.i("CJDC", "!rco || !lco slept 200ms")
                }
                if (!isArmDown) {
                    isArmDown = true
                    Log.i("CJDC", "!armDown about to wait 100ms")
                    wait(100) {
                        liftPos = 0
                        Log.i("CJDC", "!armDown waited 100ms")
                    }
                } else {
                    liftPos = 0
                    Log.i("CJDC", "armDown updated slide")
                }
            }

            // Truss Hang
            if (gamepad1.y && !pressed || gamepad2.y && !pressed) {
                pressed = true
                trussPos = when (trussPos) {
                    TrussPosition.DOWN -> TrussPosition.UP
                    TrussPosition.UP -> TrussPosition.DOWN
                }
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
                intake.power = gamepad2.left_stick_y.toDouble().stickCurve()
            }
            if (abs(gamepad2.right_stick_y) > 0.1) {
                hang.power = gamepad2.right_stick_y.toDouble().stickCurve()
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
            telemetry.addData("hangMode", trussPos)
            telemetry.addData("Distance", distance.getDistance(DistanceUnit.CM))
            telemetry.update()
        }
    }
}