package org.firstinspires.ftc.teamcode.component

import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.clamp
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.IMU
import computer.living.gamepadyn.InputDataAnalog1
import computer.living.gamepadyn.InputDataAnalog2
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.ActionAnalog2.*
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.stickCurve
import java.lang.Math.toDegrees
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

class Drive(manager: ComponentManager) : Component(manager) {
    @JvmField val motorRightFront: DcMotorEx?       = getHardware("frontR")
    @JvmField val motorLeftFront: DcMotorEx?        = getHardware("frontL")
    @JvmField val motorRightBack: DcMotorEx?        = getHardware("backR")
    @JvmField val motorLeftBack: DcMotorEx?         = getHardware("backL")
    private val indicatorRed: DigitalChannel?       = getHardware("led1")
    private val indicatorGreen: DigitalChannel?     = getHardware("led0")
    private val imu: IMU?                           = getHardware("imu")

    private var powerModifier = 1.0
    private var isSnapping = false
    private var lastUpdateTimeNs: Long = 0

    private var previousError = 0.0
    private var integral = 0.0
    var kP: Double = 0.2
    var kI: Double = 0.005
    var kD: Double = 0.2

    var snapToCardinal = false

    // see the init block for how this is implemented
    override val status: Status

    /**
     * Whether or not to use driver relativity.
     */
    var useDriverRelative = isTeleOp && imu != null
        set(status) {
            field = if (status && imu == null) {
                false
            } else {
                status
            }
            indicatorRed?.mode = DigitalChannel.Mode.OUTPUT
            indicatorGreen?.mode = DigitalChannel.Mode.OUTPUT
            // redundant logic for readability's sake
            if (field) {
                indicatorRed?.state = false
                indicatorGreen?.state = true
            } else {
                indicatorRed?.state = true
                indicatorGreen?.state = false
            }
        }

    var movementInput = InputDataAnalog2()
    var rotationInput = InputDataAnalog1()

    init {
        if (motorRightFront == null || motorLeftFront == null || motorRightBack == null || motorLeftBack == null) {
//            if (motorRightFront == null)
//            if (motorLeftFront == null)
//            if (motorRightBack == null)
//            if (motorLeftBack == null)
        } else {
            // Drive motor directions **(DO NOT CHANGE THESE!!!)**
            motorRightFront.direction = DcMotorSimple.Direction.FORWARD
            motorLeftFront.direction = DcMotorSimple.Direction.REVERSE
            motorRightBack.direction = DcMotorSimple.Direction.FORWARD
            motorLeftBack.direction = DcMotorSimple.Direction.REVERSE

            // Zero-power behavior
            motorLeftFront.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            motorLeftBack.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            motorRightFront.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            motorRightBack.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

            if (imu != null) useDriverRelative = true
        }
    }

    override fun start() {
        if (imu != null) {
            // IMU orientation/calibration
            val logo = RevHubOrientationOnRobot.LogoFacingDirection.LEFT
            val usb = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
            val orientationOnRobot = RevHubOrientationOnRobot(logo, usb)
            imu.initialize(IMU.Parameters(orientationOnRobot))
            imu.resetYaw()
        }
    }

    override fun update() {
         //        val drive = shared.drive!!

        // counter-clockwise
        val currentYaw = imu?.robotYawPitchRollAngles?.getYaw(AngleUnit.RADIANS) ?: 0.0

        val movement = movementInput
        val rotation = rotationInput

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
            log("Drive (kP, kI, kD) = ($kP, $kI, $kD)")
            log("Drive delta T: $deltaTime")
            log("Drive integral: $integral")
            log("Drive error: $error")
            log("Drive previous error: $previousError")
            log("Drive derivative: $derivative")
            log("Drive PID output: $turnPower")
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

        if (isTeleOp) {
            shared.rr?.setDrivePowers(pv)
        }

        log("Driver Relativity: ${if (useDriverRelative) "enabled" else "disable"}")
        log("Gyro Yaw: ${toDegrees(currentYaw)}")
        log("Rotation Input: ${rotation.x}")
        log("Movement Input: (${movement.x}, ${movement.y})")
        log("Input Yaw: ${if (inputVector.x > 0.05 && inputVector.y > 0.05) inputTheta * 180.0 / PI else 0.0}")
//        telemetry.addLine("Yaw Difference (bot - input): " + )
    }

    init {
        val functionality = when {
            motorRightFront == null || motorLeftFront == null || motorRightBack == null || motorLeftBack == null -> Functionality.NONE
            imu == null || indicatorGreen == null || indicatorRed == null -> Functionality.PARTIAL
            else -> Functionality.FULL
        }
        val hardwareSet: Set<HardwareUsage> = setOf(
            HardwareUsage("frontR", DcMotorEx::class, motorRightFront != null),
            HardwareUsage("frontL", DcMotorEx::class, motorLeftFront != null),
            HardwareUsage("backR", DcMotorEx::class, motorRightBack != null),
            HardwareUsage("backL", DcMotorEx::class, motorLeftBack != null),
            HardwareUsage("imu", IMU::class, motorRightFront != null, false),
            HardwareUsage("led0", DigitalChannel::class, indicatorGreen != null, false),
            HardwareUsage("led1", DigitalChannel::class, indicatorRed != null, false)
        )
        status = Status(
            functionality,
            hardwareSet
        )
    }

}