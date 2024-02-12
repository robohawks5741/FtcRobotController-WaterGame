package org.firstinspires.ftc.teamcode

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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@TeleOp(name = "## Clay January Driver Control")
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
    private var isSlideMovingUp = false
    private var hasMovedSlide = false
    private var slideAdjustmentPressed = false

    private var hasCycledTrussHang = false
    private var driverRelative = false
    private var hasToggledDriverRelativity = false
    private var runToHeight = 0
    private var dpadUpPressed = false

    // automatically updates the truss servos when the value is changed
    private var trussPos = TrussPosition.DOWN
        set(pos) {
            field = pos
            trussL.position = trussPos.leftPos
            trussR.position = trussPos.rightPos
        }

    private var isArmDown = true
        set(status) {
            //              if    [down pos] else [up pos]
            armR.position = if (status) 0.05 else 0.35
            armL.position = if (status) 0.98 else 0.65
            field = status
        }

    private var isLeftClawOpen: Boolean = true
        set(status) {
            //               if   [open pos] else [closed pos]
            clawL.position = if (status) 0.0 else 0.29
            field = status
        }
    private var isRightClawOpen: Boolean = true
        set(status) {
            //               if    [open pos] else [closed pos]
            clawR.position = if (status) 0.36 else 0.07
            field = status
        }

    private fun updateDrive() {
        // Driver Relative Toggle
        if (gamepad1.back) if (!hasToggledDriverRelativity) {
            hasToggledDriverRelativity = true
            driverRelative = !driverRelative
            imu.resetYaw()
        } else hasToggledDriverRelativity = false

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
                    ), -gamepad1.right_stick_x.toDouble().stickCurve(),
                )
            )
        }
    }

    private var slidePos = 0
        set(pos) {
            field = pos.coerceIn(0..1500)
            slideR.targetPosition = -slidePos
            slideL.targetPosition = slidePos
            slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideR.power = 1.0
            slideL.power = 1.0
        }


    override fun runOpMode() {
        drive = MecanumDrive(hardwareMap, poseEstimate)

        hang = hardwareMap[DcMotorEx::class.java, "hang"]
        intake = hardwareMap[DcMotorEx::class.java, "intake"]
        slideR = hardwareMap[DcMotorEx::class.java, "slideR"]
        slideL = hardwareMap[DcMotorEx::class.java, "slideL"]
        drone = hardwareMap[Servo::class.java, "drone"]
        trussR = hardwareMap[Servo::class.java, "trussR"]
        trussL = hardwareMap[Servo::class.java, "trussL"]
        armR = hardwareMap[Servo::class.java, "armR"]
        armL = hardwareMap[Servo::class.java, "armL"]
        clawR = hardwareMap[Servo::class.java, "clawR"]
        clawL = hardwareMap[Servo::class.java, "clawL"]
        inlift = hardwareMap[Servo::class.java, "inlift"]
        imu = hardwareMap[IMU::class.java, "imu"]

//        data class Timeout(val calltime: Long, val callback: () -> Unit)
//        val waitList: ArrayList<Timeout> = arrayListOf();
//        fun wait(calltime: Long, callback: () -> Unit) = waitList.add(Timeout((time * 1000).toLong() + calltime, callback))

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
        drone.position = 1.0
        inlift.position = 0.0

        waitForStart()

        while (opModeIsActive()) {
            BotShared.wasLastOpModeAutonomous = false
            updateDrive()

            // Left trigger -> Spin intake inwards
            intake.power = if (gamepad1.left_trigger > 0.2 || gamepad2.left_trigger > 0.2) 0.8 else 0.0

            // D-Pad up -> Close claws, move slides up to a safe height, rotate arm up, move slide up fully (or to runToHeight)
            if ((gamepad1.dpad_up || gamepad2.dpad_up) && !dpadUpPressed) {
                dpadUpPressed = true
                if (isRightClawOpen || isLeftClawOpen) {
                    isRightClawOpen = false
                    isLeftClawOpen = false
                    sleep(300)
                }
                isSlideMovingUp = true
                if (runToHeight == 0 || slidePos > 0){
                    slidePos = 1500
                    runToHeight = 6
                } else {
                    slidePos = runToHeight * 200 + 300
                }

            } else dpadUpPressed = gamepad1.dpad_up || gamepad2.dpad_up

            // D-Pad down -> lower slides
            if ((gamepad1.dpad_down || gamepad2.dpad_down) || runToHeight == 0 && slidePos > 0) {
                runToHeight = 0
                isArmDown = true
                // if the slide is too low, wait 450ms in order to give the arm time to retract
                if (slidePos in 1..502) sleep(450)
                slidePos = 0
            }

            // make sure that the running height is
            runToHeight.coerceIn(0..6)

            // D-Pad left -> raise slides
            if ((gamepad1.dpad_left || gamepad2.dpad_left) && runToHeight < 6 && !slideAdjustmentPressed) {
                slideAdjustmentPressed = true
                runToHeight++

                // move slide up 200 ticks
                if (slidePos < 1500) slidePos += 200
            // D-Pad right -> lower slides
            } else if ((gamepad1.dpad_right || gamepad2.dpad_right) && runToHeight > 0 && !slideAdjustmentPressed) {
                slideAdjustmentPressed = true
                runToHeight--
                // move slide down 200 ticks
                if (slidePos > 0) slidePos -= 200
            }
            // if no slide adjustment buttons are pressed
            slideAdjustmentPressed = (!gamepad1.dpad_right && !gamepad1.dpad_left && !gamepad2.dpad_right && !gamepad2.dpad_left)

            /*
             * These are backwards. This is sorta intentional.
             */

            // Left bumper -> Open right claw
            if (gamepad1.left_bumper || gamepad2.left_bumper)
                isRightClawOpen = true

            // Right bumper -> Open left claw
            if (gamepad1.right_bumper || gamepad2.right_bumper)
                isLeftClawOpen = true

            if (slideL.currentPosition > 500 && isSlideMovingUp) {
                isArmDown = false
                isSlideMovingUp = false
            }

            // Claw
            if (gamepad1.x || gamepad2.x) {
                // Close
                isLeftClawOpen = false
                isRightClawOpen = false
            } else if (gamepad1.right_trigger > 0.1 || gamepad2.right_trigger > 0.1) {
                runToHeight = 0
                if (!isRightClawOpen || !isLeftClawOpen) {
                    isLeftClawOpen = true
                    isRightClawOpen = true
                    sleep(200)
                }

                if (runToHeight == 0 && (slidePos in 1..501)) {
                    isArmDown = true
                    sleep(450)
                    slidePos = 0
                } else {
                    isArmDown = true
                    slidePos = 0
                }
            }

            // Truss Hang
            if ((gamepad1.y || gamepad2.y) && !hasCycledTrussHang) {
                hasCycledTrussHang = true
                // swap (this used to be a cycle, so it's an enum)
                trussPos = when (trussPos) {
                    TrussPosition.DOWN -> TrussPosition.UP
                    TrussPosition.UP -> TrussPosition.DOWN
                }
            }
            hasCycledTrussHang = gamepad1.y || gamepad2.y

            // Face down / A -> Pull in truss hang
            hang.power = (gamepad1.a || gamepad2.a).toDouble()

            // Driver 2 Override
            // Manual intake controls
            if (abs(gamepad2.left_stick_y) > 0.1) {
                intake.power = gamepad2.left_stick_y.toDouble().stickCurve()
            }

            // Gamepad 2 truss controls
            if (abs(gamepad2.right_stick_y) > 0.1) {
                hang.power = gamepad2.right_stick_y.toDouble().stickCurve()
            }

            // Drone Launch ( !!! BOTH PLAYERS MUST HOLD B !!! )
            if (gamepad1.b || gamepad2.b) drone.position = 0.0

            drive.updatePoseEstimate()
            telemetry.addData("DriverRelative", driverRelative)
            telemetry.addData("x", drive.pose.position.x)
            telemetry.addData("y", drive.pose.position.y)
            telemetry.addData("heading", drive.pose.heading.log())
            telemetry.addData("rightSlide", slideR.currentPosition)
            telemetry.addData("leftSlide", slideL.currentPosition)
            telemetry.addData("drone", drone.position)
            telemetry.addData("inlift", inlift.position)
            telemetry.addData("hangMode", trussPos)
            telemetry.addData("RunToHeight", runToHeight)
            telemetry.addData("Slide Pos", slidePos)
            telemetry.update()
        }
    }
}