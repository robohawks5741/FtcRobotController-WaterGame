package org.firstinspires.ftc.teamcode.botmodule

import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.clamp
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.IMU
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.ActionAnalog2.*
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.search
import org.firstinspires.ftc.teamcode.stickCurve
import java.lang.Math.toDegrees
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

class Drive(config: ModuleConfig) : BotModule(config) {
    @JvmField val motorRightFront: DcMotorEx        = hardwareMap.search("frontR")!!
    @JvmField val motorLeftFront: DcMotorEx         = hardwareMap.search("frontL")!!
    @JvmField val motorRightBack: DcMotorEx         = hardwareMap.search("backR")!!
    @JvmField val motorLeftBack: DcMotorEx          = hardwareMap.search("backL")!!
    @JvmField val indicatorRed: DigitalChannel?     = hardwareMap.search("led1")
    @JvmField val indicatorGreen: DigitalChannel?   = hardwareMap.search("led0")

    private var powerModifier = 1.0
    var snapToCardinal = true
    var isSnapping = false
    var lastUpdateTimeNs: Long = 0


    var previousError = 0.0
    var integral = 0.0
    var kP: Double = 0.2
    var kI: Double = 0.005
    var kD: Double = 0.2

    var useDriverRelative = true
        set(status) {
            indicatorRed?.mode = DigitalChannel.Mode.OUTPUT
            indicatorGreen?.mode = DigitalChannel.Mode.OUTPUT
            // redundant logic for readability's sake
            if (status) {
                indicatorRed?.state = false
                indicatorGreen?.state = true
            } else {
                indicatorRed?.state = true
                indicatorGreen?.state = false
            }
            field = status
        }

    init {
        // Drive motor directions **(DO NOT CHANGE THESE!!!)**
        motorRightFront.direction = DcMotorSimple.Direction.FORWARD
        motorLeftFront. direction = DcMotorSimple.Direction.REVERSE
        motorRightBack. direction = DcMotorSimple.Direction.FORWARD
        motorLeftBack.  direction = DcMotorSimple.Direction.REVERSE

        // Zero-power behavior
        motorLeftFront.     zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        motorLeftBack.      zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        motorRightFront.    zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        motorRightBack.     zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        useDriverRelative = true
    }

    override fun modStartTeleOp() {
        if (gamepadyn == null) {
            telemetry.addLine("(Drive Module) TeleOp was enabled but Gamepadyn was null!")
            return
        }
        gamepadyn.players[0].getEvent(TOGGLE_DRIVER_RELATIVITY) { if (it()) useDriverRelative = !useDriverRelative }
        // IMU orientation/calibration
        val logo = RevHubOrientationOnRobot.LogoFacingDirection.LEFT
        val usb = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        val orientationOnRobot = RevHubOrientationOnRobot(logo, usb)
        imu.initialize(IMU.Parameters(orientationOnRobot))
        imu.resetYaw()
    }

    override fun modUpdateTeleOp() {
        if (gamepadyn == null) {
            telemetry.addLine("(Drive Module) TeleOp was enabled but Gamepadyn was null!")
            return
        }

        //        val drive = shared.drive!!

        // counter-clockwise
        val currentYaw = imu.robotYawPitchRollAngles.getYaw(AngleUnit.RADIANS)

        val movement = gamepadyn.players[0].getState(MOVEMENT)
        val rotation = gamepadyn.players[0].getState(ROTATION)

        // +X = forward
        // +Y = left
        val inputVector = Vector2d(
            movement.y.toDouble().stickCurve(),
            -movement.x.toDouble().stickCurve()
        )

        // angle of the stick
        val inputTheta = atan2(inputVector.y, inputVector.x)
        // evaluated theta
        val driveTheta = inputTheta - currentYaw // + PI
        // magnitude of inputVector clamped to [0, 1]
        val inputPower = clamp(
            sqrt(
                (inputVector.x * inputVector.x) +
                (inputVector.y * inputVector.y)
            ), 0.0, 1.0)

        val driveRelativeX = cos(driveTheta) * inputPower
        val driveRelativeY = sin(driveTheta) * inputPower

        // \frac{1}{1+\sqrt{2\left(1-\frac{\operatorname{abs}\left(\operatorname{mod}\left(a,90\right)-45\right)}{45}\right)\ }}
//        powerModifier = 1.0 / (1.0 + sqrt(2.0 * (1.0 - abs((gyroYaw % (PI / 2)) - (PI / 4)) / (PI / 4))))

        val deltaTime = lastUpdateTimeNs - System.nanoTime()
        lastUpdateTimeNs = System.nanoTime()

        val turnPower: Double
        if (snapToCardinal && abs(rotation.x) < 0.1) {
            // current yaw snapped to 45deg increments
            val targetYaw = round(currentYaw * (PI / 2)) / (PI / 2)

            // terrible PID algorithm

            val error = targetYaw - currentYaw
            integral += error * deltaTime
            val derivative = (error - previousError) / deltaTime
            turnPower = (kP * error) + (kI * integral) + (kD * derivative)
            previousError = error
        } else {
            turnPower = -rotation.x.toDouble().stickCurve()
        }

        val pv = PoseVelocity2d(
            if (useDriverRelative) Vector2d(
                driveRelativeX,
                driveRelativeY
            ) else inputVector,
            turnPower
        )

        shared.rr?.setDrivePowers(pv)

        telemetry.addLine("Driver Relativity: ${if (useDriverRelative) "enabled" else "disable" }")
        telemetry.addLine("Gyro Yaw: ${toDegrees(currentYaw)}")
        telemetry.addLine("Rotation Input: ${rotation.x}")
        telemetry.addLine("Movement Input: (${movement.x}, ${movement.y})")
        telemetry.addLine("Input Yaw: " + if (inputVector.x > 0.05 && inputVector.y > 0.05) inputTheta * 180.0 / PI else 0.0)
//        telemetry.addLine("Yaw Difference (bot - input): " + )

    }

}