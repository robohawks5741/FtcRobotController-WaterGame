package org.firstinspires.ftc.teamcode.component

import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.clamp
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.InputDataAnalog1
import computer.living.gamepadyn.InputDataAnalog2
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.stickCurve
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

class Drive(manager: ComponentManager) : Component(manager) {
    override val status: Status

    private val leftFrontDrive: DcMotorEx? = getHardware("leftFront")
    private val leftBackDrive: DcMotorEx? = getHardware("leftBack")
    private val rightFrontDrive: DcMotorEx? = getHardware("rightFront")
    private val rightBackDrive: DcMotorEx? = getHardware("rightBack")

    var rotationInput = 0f
    var movementInput = InputDataAnalog2()


    var useDriverRelative = shared.imu != null
        set(wantEnabled) {
            field = if (shared.imu == null) false else wantEnabled
        }

    init {
        if (leftFrontDrive != null && leftBackDrive != null && rightFrontDrive != null && rightBackDrive != null) {
            leftFrontDrive.direction = DcMotorSimple.Direction.REVERSE
            leftBackDrive.direction = DcMotorSimple.Direction.FORWARD
            rightFrontDrive.direction = DcMotorSimple.Direction.FORWARD
            rightBackDrive.direction = DcMotorSimple.Direction.REVERSE
        }

        val functionality = when {
            leftFrontDrive == null || leftBackDrive == null || rightFrontDrive == null || rightBackDrive == null -> Functionality.NONE
            shared.imu == null -> Functionality.PARTIAL
            else -> Functionality.FULL
        }
        val hardwareSet: Set<HardwareUsage> = setOf(
            HardwareUsage("leftFront", DcMotorEx::class, leftFrontDrive != null),
            HardwareUsage("leftBack", DcMotorEx::class, leftBackDrive != null),
            HardwareUsage("rightFront", DcMotorEx::class, rightFrontDrive != null),
            HardwareUsage("rightBack", DcMotorEx::class, rightBackDrive != null),
            HardwareUsage("imu", IMU::class, shared.imu != null, false)
        )
        status = Status(
            functionality,
            hardwareSet
        )
    }
    
    override fun start() {

    }

    override fun loop() {
        if (leftFrontDrive != null && leftBackDrive != null && rightFrontDrive != null && rightBackDrive != null) {

            // counter-clockwise
            val currentYaw = shared.imu?.robotYawPitchRollAngles?.getYaw(AngleUnit.RADIANS) ?: 0.0

            // +X = forward
            // +Y = left
            val inputVector = Vector2d(
                -movementInput.y.toDouble().stickCurve(),
                movementInput.x.toDouble().stickCurve()
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
                ), 0.0, 1.0
            )

            val driveRelativeX = cos(driveTheta) * inputPower
            val driveRelativeY = sin(driveTheta) * inputPower

            var max: Double

            // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
            val axial = if (useDriverRelative) driveRelativeX else -inputVector.x
            val lateral = if (useDriverRelative) -driveRelativeY else inputVector.y
            val yaw = rotationInput.toDouble().stickCurve()

            // Combine the joystick requests for each axis-motion to determine each wheel's power.
            // Set up a variable for each drive wheel to save the power level for telemetry.
            var leftFrontPower = axial + lateral + yaw
            var rightFrontPower = axial - lateral - yaw
            var leftBackPower = axial - lateral + yaw
            var rightBackPower = axial + lateral - yaw

            // Normalize the values so no wheel power exceeds 100%
            // This ensures that the robot maintains the desired motion.
            max = max(abs(leftFrontPower), abs(rightFrontPower))
            max = max(max, abs(leftBackPower))
            max = max(max, abs(rightBackPower))

            if (max > 1.0) {
                leftFrontPower /= max
                rightFrontPower /= max
                leftBackPower /= max
                rightBackPower /= max
            }

            leftFrontDrive.power = leftFrontPower
            rightFrontDrive.power = rightFrontPower
            leftBackDrive.power = leftBackPower
            rightBackDrive.power = rightBackPower

            telemetry.addLine("Driver Relativity: ${if (useDriverRelative) "enabled" else "disabled"}")
            telemetry.addLine("Gyro Yaw: ${Math.toDegrees(currentYaw)}")
            telemetry.addLine("Rotation Input: $rotationInput")
            telemetry.addLine("Movement Input: (${inputVector.x}, ${inputVector.y})")
            telemetry.addLine("Input Yaw: ${if (inputVector.x > 0.05 && inputVector.y > 0.05) inputTheta * 180.0 / PI else 0.0}")
//        telemetry.addLine("Yaw Difference (bot - input): " + )    }
        }
    }
}