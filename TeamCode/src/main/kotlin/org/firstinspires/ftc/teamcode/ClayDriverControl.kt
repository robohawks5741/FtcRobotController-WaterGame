package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.clamp
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.teamcode.MecanumDrive
import java.lang.Math.pow
import java.lang.Math.toDegrees
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

@TeleOp(name = "# Clay Scrimmage Driver Control")
class ClayDriverControl : OpMode() {
    private lateinit var hang: DcMotorEx
    private lateinit var intake: DcMotorEx
    private lateinit var slideR: DcMotorEx
    private lateinit var slideL: DcMotorEx
    private lateinit var trussR: Servo
    private lateinit var trussL: Servo
    private lateinit var armR: Servo
    private lateinit var armL: Servo
    private lateinit var clawR: Servo
    private lateinit var clawL: Servo
    private lateinit var drone: Servo
    private lateinit var imu: IMU
    private lateinit var drive: MecanumDrive
    private var liftPos = 0

    override fun init() {
        drive = MecanumDrive(hardwareMap, Pose2d(0.0, 0.0, 0.0))
        hang = hardwareMap[DcMotorEx::class.java, "hang"]
        intake = hardwareMap[DcMotorEx::class.java, "intake"]
        slideR = hardwareMap[DcMotorEx::class.java, "slideR"]
        slideL = hardwareMap[DcMotorEx::class.java, "slideL"]
        trussR = hardwareMap[Servo::class.java, "trussR"]
        trussL = hardwareMap[Servo::class.java, "trussL"]
        armR = hardwareMap[Servo::class.java, "armR"]
        armL = hardwareMap[Servo::class.java, "armL"]
        clawR = hardwareMap[Servo::class.java, "clawR"]
        clawL = hardwareMap[Servo::class.java, "clawL"]
        drone = hardwareMap[Servo::class.java, "drone"]
        imu = hardwareMap[IMU::class.java, "imu"]

        slideR.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideR.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        slideL.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideL.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        hang.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
    }

    override fun start() {
        trussR.position = 0.32
        trussL.position = 0.3
        armR.position = 0.3
        armL.position = 1.0
        clawR.position = 0.07
        clawL.position = 0.29
        drone.position = 0.36

        // IMU orientation/calibration
        val logo = RevHubOrientationOnRobot.LogoFacingDirection.LEFT
        val usb = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        val orientationOnRobot = RevHubOrientationOnRobot(logo, usb)
        imu.initialize(IMU.Parameters(orientationOnRobot))
        imu.resetYaw()
    }

    private fun Double.stickCurve(): Double {
        val x = this.absoluteValue
        val s = this.sign
        val nI = 1.5
        val nO = 0.5
        return (x.pow(nI) * (1 - x)) + (x.pow(nO) * x) * s
    }

    override fun loop() {

        // counter-clockwise
        val gyroYaw = imu.robotYawPitchRollAngles.getYaw(AngleUnit.RADIANS)

        val inputY = (-gamepad1.left_stick_y.toDouble()).stickCurve()
        val inputX = (-gamepad1.left_stick_x.toDouble()).stickCurve()
        val inputT = (-gamepad1.right_stick_x.toDouble()).stickCurve()

        // +X = forward
        // +Y = left
        val inputVector = Vector2d(
            inputY,
            inputX
        )

        // angle of the stick
        val inputTheta = atan2(inputVector.y, inputVector.x)
        // evaluated theta
        val driveTheta = inputTheta - gyroYaw // + PI
        // magnitude of inputVector clamped to [0, 1]
        val inputPower = clamp(
            sqrt(
                (inputVector.x * inputVector.x) + (inputVector.y * inputVector.y)
            ), 0.0, 1.0
        )

        val driveRelativeX = cos(driveTheta) * inputPower
        val driveRelativeY = sin(driveTheta) * inputPower

        // \frac{1}{1+\sqrt{2\left(1-\frac{\operatorname{abs}\left(\operatorname{mod}\left(a,90\right)-45\right)}{45}\right)\ }}
//        powerModifier = 1.0 / (1.0 + sqrt(2.0 * (1.0 - abs((gyroYaw % (PI / 2)) - (PI / 4)) / (PI / 4))))

        val pv = PoseVelocity2d(
                Vector2d(
                    driveRelativeX,
                    driveRelativeY
                ),
//            Vector2d(
//                -gamepad1.left_stick_y.toDouble(),
//                -gamepad1.left_stick_x.toDouble()
//            ),
            inputT
        )

        drive.setDrivePowers(pv)

        // Intake
        intake.power = if (gamepad1.left_trigger > 0.1 || gamepad2.left_trigger > 0.1) {
            0.5
        } else {
            0.0
        }


        // Placement
        if (gamepad1.dpad_up || gamepad2.dpad_up) {
            if (liftPos == 0) {
                clawR.position = 0.36
                clawL.position = 0.0
                armL.position = 0.63
                armR.position = 0.67
            }
            liftPos = 1600
            slideR.targetPosition = liftPos
            slideL.targetPosition = -liftPos
            slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideR.power = 1.0
            slideL.power = 1.0
        } else if (gamepad1.dpad_down || gamepad2.dpad_down) {
            liftPos = 0
            slideR.targetPosition = liftPos
            slideL.targetPosition = -liftPos
            slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideR.power = 1.0
            slideL.power = 1.0
            // Swing arm down
            armL.position = 1.0 //0.93 - 1
            armR.position = 0.3

            // Open Claws
            clawR.position = 0.07
            clawL.position = 0.29
        } else if (gamepad1.dpad_left && liftPos > 199 || gamepad2.dpad_left && liftPos > 99) {
            liftPos -= 100
            slideR.targetPosition = liftPos
            slideL.targetPosition = -liftPos
            slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideR.power = 1.0
            slideL.power = 1.0
        } else if (gamepad1.dpad_right && liftPos < 1501 || gamepad2.dpad_right && liftPos < 1501) {
            if (liftPos == 0) {
                clawR.position = 0.36
                clawL.position = 0.0
                armL.position = 0.63
                armR.position = 0.67
            }
            liftPos += 100
            slideR.targetPosition = liftPos
            slideL.targetPosition = -liftPos
            slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideR.power = 1.0
            slideL.power = 1.0
        }

        // Arm Rotation
        if (gamepad1.left_bumper || gamepad2.left_bumper) { // place
            armL.position = 0.63
            armR.position = 0.67
        } else if (gamepad1.right_bumper || gamepad2.right_bumper) { // Pickup
            armL.position = 1.0 //0.93 - 1
            armR.position = 0.3
        }


        // Claw
        // Close
        if (gamepad1.x || gamepad2.x) {
            clawR.position = 0.36
            clawL.position = 0.0
        } else if (gamepad1.right_trigger > 0.1 || gamepad2.right_trigger > 0.1) { //open
            clawR.position = 0.07
            clawL.position = 0.29
        }

        // Truss hang
        if (gamepad1.y || gamepad2.y) {
            trussR.position = 0.0
            trussL.position = 0.65
        }
        if (gamepad1.a || gamepad2.a) {
            hang.power = 1.0
        } else {
            hang.power = 0.0
        }

        // Drone Launch
        if (gamepad1.b || gamepad2.b) {
            drone.position = 0.8
        }
        drive.updatePoseEstimate()
//            telemetry.addData("x", drive.pose.position.x)
//            telemetry.addData("y", drive.pose.position.y)

        telemetry.addLine("left stick: (${gamepad1.left_stick_x}, ${gamepad1.left_stick_y})")
        telemetry.addLine("right stick: (${gamepad1.right_stick_x}, ${gamepad1.right_stick_y})")
        telemetry.addLine("imu rotation (physical yaw): ${toDegrees(gyroYaw)} deg")
        telemetry.addLine("evaluated rotation (sorta meaningless): ${toDegrees(driveTheta)} deg")
        telemetry.addData("heading (deg)", toDegrees(drive.pose.heading.log()))
        telemetry.addData("rightSlide", slideR.currentPosition)
        telemetry.addData("leftSlide", slideL.currentPosition)
        telemetry.addData("right servo", clawR.position)
        telemetry.addData("left servo", clawL.position)
        telemetry.update()
    }
}