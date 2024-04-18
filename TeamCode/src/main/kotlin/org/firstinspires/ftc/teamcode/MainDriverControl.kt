package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.clamp
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.Axis
import computer.living.gamepadyn.Configuration
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.RawInputAnalog1
import computer.living.gamepadyn.RawInputAnalog2
import computer.living.gamepadyn.RawInputDigital
import computer.living.gamepadyn.ftc.InputBackendFtc
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.teamcode.component.Claw
import org.firstinspires.ftc.teamcode.component.Drive
import org.firstinspires.ftc.teamcode.component.HorizontalSlide
import org.firstinspires.ftc.teamcode.component.VerticalSlide
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

class MainDriverControl(host: OpModeHost<MainDriverControl>) : UltraOpMode(host) {
    @TeleOp(name = "MainDriverControl")
    class Host : OpModeHost<MainDriverControl>(MainDriverControl::class)

    private val gamepadyn = Gamepadyn.create(
        ActionDigital::class,
        ActionAnalog1::class,
        ActionAnalog2::class,
        InputBackendFtc(host),
        strict = true
    )

    private val p0 = gamepadyn.getPlayer(0)!!
    private val p1 = gamepadyn.getPlayer(1)!!

    private val claw = Claw(componentManager)
    private val drive = Drive(componentManager)
//    private val horizontalSlide = HorizontalSlide(componentManager)
    private val verticalSlide = VerticalSlide(componentManager)
    private val leftFrontDrive: DcMotorEx   = getHardware("leftFront")!!
    private val leftBackDrive: DcMotorEx    = getHardware("leftBack")!!
    private val rightFrontDrive: DcMotorEx  = getHardware("rightFront")!!
    private val rightBackDrive: DcMotorEx   = getHardware("rightBack")!!

    private val imu: IMU                    = getHardware("imu")!!

    private val droneLaunch: Servo?         = getHardware("drone")
    private val arm: Servo?                 = getHardware("arm")

    private var useDriverRelative = true
    private var hasToggledDriverRelative = false
    private val orientationOnRobot = RevHubOrientationOnRobot(
        LogoFacingDirection.UP,
        UsbFacingDirection.RIGHT
    )

    private var hasLaunchedDrone = false

    private fun handleDrive() {
        // counter-clockwise
        val currentYaw = imu.robotYawPitchRollAngles?.getYaw(AngleUnit.RADIANS) ?: 0.0

        val rotation = p0.getState(ActionAnalog1.ROTATION).x

        val movementInput = p0.getState(ActionAnalog2.MOVEMENT)

        // +X = forward
        // +Y = left
        val inputVector = Vector2d(
            movementInput.y.toDouble().stickCurve(),
            -movementInput.x.toDouble().stickCurve()
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
        val axial = if (useDriverRelative) driveRelativeX else inputVector.x
        val lateral = if (useDriverRelative) -driveRelativeY else -inputVector.y
        val yaw = rotation.toDouble().stickCurve()

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
        telemetry.addLine("Rotation Input: $rotation")
        telemetry.addLine("Movement Input: (${inputVector.x}, ${inputVector.y})")
        telemetry.addLine("Input Yaw: ${if (inputVector.x > 0.05 && inputVector.y > 0.05) inputTheta * 180.0 / PI else 0.0}")
//        telemetry.addLine("Yaw Difference (bot - input): " + )    }
    }

    private fun handleIntake() {
        val armMove = p1.getState(ActionAnalog1.ARM_MOVE_MANUAL).x
//        if (armMove.absoluteValue > 0.1) {
//            armMove.
//        }
        val arm = arm
        if (arm != null) arm.position = (arm.position + armMove * 0.1f).coerceIn(0.0..1.0)

        telemetry.addLine("Claw Position: ${claw.isOpen}")
        telemetry.addLine("Arm Position: ${arm?.position}")
    }

    private fun handleDroneLaunch() {
//        if (runtime > 90) {
//
//        }

        telemetry.addLine("Drone launch servo pos: ${droneLaunch?.position}")
    }

    override fun start() {

        leftFrontDrive.direction = DcMotorSimple.Direction.REVERSE
        leftBackDrive.direction = DcMotorSimple.Direction.FORWARD
        rightFrontDrive.direction = DcMotorSimple.Direction.FORWARD
        rightBackDrive.direction = DcMotorSimple.Direction.REVERSE

        droneLaunch?.position = 1.0

        imu.initialize(IMU.Parameters(orientationOnRobot))
        if (!BotShared.wasLastOpModeAutonomous) imu.resetYaw()

        p0.configuration = Configuration {
            actionDigital(ActionDigital.TOGGLE_DRIVER_RELATIVITY) { input(RawInputDigital.SPECIAL_BACK) }
            actionAnalog2(ActionAnalog2.MOVEMENT) { input(RawInputAnalog2.STICK_LEFT) }
            actionAnalog1(ActionAnalog1.ROTATION) { split(input(RawInputAnalog2.STICK_RIGHT), Axis.X) }
            actionDigital(ActionDigital.H_SLIDE_EXTEND) { gt(input(RawInputAnalog1.TRIGGER_RIGHT), constant(0.5f)) }
            actionDigital(ActionDigital.H_SLIDE_RETRACT) { gt(input(RawInputAnalog1.TRIGGER_LEFT), constant(0.5f)) }
        }

        p1.configuration = Configuration {
            actionDigital(ActionDigital.LAUNCH_DRONE) { input(RawInputDigital.STICK_RIGHT_BUTTON) }
//            actionDigital(ActionDigital.H_SLIDE_EXTEND) { gt(input(RawInputAnalog1.TRIGGER_RIGHT), constant(0.5f)) }
//            actionDigital(ActionDigital.H_SLIDE_RETRACT) { gt(input(RawInputAnalog1.TRIGGER_LEFT), constant(0.5f)) }
            actionDigital(ActionDigital.V_SLIDE_EXTEND) {
                gt(
                    split(
                        input(RawInputAnalog2.STICK_RIGHT),
                        Axis.Y
                    ),
                    constant(0.5f)
                )
            }
            actionDigital(ActionDigital.V_SLIDE_RETRACT) {
                lt(
                    split(
                        input(RawInputAnalog2.STICK_RIGHT),
                        Axis.Y
                    ),
                    constant(-0.5f)
                )
            }
            actionDigital(ActionDigital.CLAW_CLOSE) { input(RawInputDigital.BUMPER_LEFT) }
            actionDigital(ActionDigital.CLAW_OPEN) { input(RawInputDigital.BUMPER_RIGHT) }
            actionAnalog1(ActionAnalog1.ARM_MOVE_MANUAL) {
                subtract(
                    input(RawInputAnalog1.TRIGGER_RIGHT),
                    input(RawInputAnalog1.TRIGGER_LEFT)
                )
            }
            actionDigital(ActionDigital.ARM_EXTEND) { input(RawInputDigital.FACE_DOWN) }
            actionDigital(ActionDigital.ARM_RETRACT) { input(RawInputDigital.FACE_RIGHT) }
        }

//        gamepadyn.addListener(ActionDigital.H_SLIDE_EXTEND) { if (it.data()) horizontalSlide.extend() }
//        gamepadyn.addListener(ActionDigital.H_SLIDE_RETRACT) {if (it.data()) horizontalSlide.retract() }

        gamepadyn.addListener(ActionDigital.V_SLIDE_EXTEND) { if (it.data()) verticalSlide.extend() }
        gamepadyn.addListener(ActionDigital.V_SLIDE_RETRACT) { if (it.data()) verticalSlide.retract() }

        gamepadyn.addListener(ActionDigital.LAUNCH_DRONE) {
            if (it.data() && !hasLaunchedDrone) {
                droneLaunch?.position = 0.0
            }
        }

        gamepadyn.addListener(ActionDigital.TOGGLE_DRIVER_RELATIVITY) {
            if (it.data()) {
                imu.resetYaw()
                useDriverRelative = !useDriverRelative
            }
        }

        gamepadyn.addListener(ActionDigital.CLAW_OPEN) { if (it.data()) claw.isOpen = true }
        gamepadyn.addListener(ActionDigital.CLAW_CLOSE) { if (it.data()) claw.isOpen = false }

        gamepadyn.addListener(ActionDigital.ARM_EXTEND) { if (it.data()) arm?.position = 1.0 }
        gamepadyn.addListener(ActionDigital.ARM_RETRACT) { if (it.data()) arm?.position = 0.0 }

    }

    override fun loop() {
        gamepadyn.update()
        handleDrive()
        handleDroneLaunch()
        handleIntake()

        telemetry.update()
    }

    companion object {
    }
}