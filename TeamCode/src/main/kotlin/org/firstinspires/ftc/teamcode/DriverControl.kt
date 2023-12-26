package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.MecanumKinematics
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.PoseVelocity2dDual
import com.acmerobotics.roadrunner.Time
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.clamp
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.ActionBind
import computer.living.gamepadyn.Configuration
import computer.living.gamepadyn.GDesc
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.InputType.ANALOG
import computer.living.gamepadyn.InputType.DIGITAL
import computer.living.gamepadyn.RawInput
import computer.living.gamepadyn.ftc.InputBackendFtc
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import org.firstinspires.ftc.teamcode.Action.CLAW
import org.firstinspires.ftc.teamcode.Action.MOVEMENT
import org.firstinspires.ftc.teamcode.Action.ROTATION
import org.firstinspires.ftc.teamcode.Action.SPIN_INTAKE
import org.firstinspires.ftc.teamcode.Action.TOGGLE_DRIVER_RELATIVITY
import org.firstinspires.ftc.teamcode.Action.TOGGLE_INTAKE_HEIGHT
import org.firstinspires.ftc.teamcode.Action.TRUSS_HANG
import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * CURRENT CONTROLS:
 *
 *
 * Gamepad 1: Movement
 *  - Left Stick X/Y: Movement
 *  - Right Stick X: Rotation
 *  - X (face left): Toggle driver-relative controls (ON by default)
 *  - D-Pad Up: Spin intake outwards
 *  - D-Pad Down: Spin intake inwards
 *
 * Gamepad 2: Objective
 *  - Left Stick Y: Manual slide
 *  - Left Trigger: Close(?) claw
 *  - Right Trigger: Open(?) claw
 *  - Right Bumper: Retract truss pulley
 *  - Left Bumper: Extend truss pulley
 *  - A (face down): Toggle intake height
 */

/*
 * TODO:
 *      - Store the orientation at the end of autonomous and reload it for DC
 */

@TeleOp(name = "# Driver Control (Standalone)", group = "# Sub-Mode")
class StandaloneDriverControl : DriverControlBase(Pose2d(0.0, 0.0, 0.0))
@TeleOp(name = "# Driver Control", group = "# Sub-Mode")
class DriverControl : DriverControlBase(BotShared.storedPose)

open class DriverControlBase(private val initialPose: Pose2d) : OpMode() {

    private lateinit var shared: BotShared

    /**
     * The time (in ms, relative to the OpMode's start time) that the robot last ran the `loop()` function.
     */
    private var lastLoopTime = 0.0
//    TODO: fix these delta time calculations!
    private var deltaTime = 60.0 / 1000.0

    // these variables can be deleted when Gamepadyn is finished (state transitions cause headaches)
    /** true for lowered, false for raised */
    private var lastIntakeStatus = false
    private var isIntakeLiftRaised = true

    private lateinit var gamepadyn: Gamepadyn<Action>

    /**
     * Set up the robot
     */
    override fun init() {
//        val setter = DriverControl::tagCamera.setter
        shared = BotShared(this)
        shared.drive = MecanumDrive(hardwareMap, initialPose)
        gamepadyn = Gamepadyn(InputBackendFtc(this), true, Action.actionMap)

        // Configuration
        gamepadyn.players[0].configuration = Configuration(
            ActionBind(RawInput.FACE_X, TOGGLE_DRIVER_RELATIVITY),
            ActionBind(RawInput.FACE_A, TOGGLE_INTAKE_HEIGHT),
            ActionBind(RawInput.FACE_Y, TRUSS_HANG),
        )

        // toggle driver-relative controls
        gamepadyn.players[0].getEventDigital(TRUSS_HANG)!!.addListener {
            if (it.digitalData) {
                shared.servoTrussLeft?.position = 0.5
                shared.servoTrussRight?.position = 0.5
                shared.servoTrussLeft?.position = 0.0
                shared.servoTrussRight?.position = 0.0
            }
        }

        val intake = shared.intake
        if (intake != null) {
            // toggle intake height
            gamepadyn.players[0].getEventDigital(TOGGLE_INTAKE_HEIGHT)!!.addListener { if (intake.raised) intake.lower() else intake.raise() }
        } else {
            telemetry.addLine("WARNING: Safeguard triggered (intake not present)");
        }

        shared.motorSlideLeft!!.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        shared.motorSlideRight!!.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    }

    override fun start() {
        lastLoopTime = time
    }

    /**
     * The robot's game loop. Handles input, updates the motors, basically calls everything.
     */
    override fun loop() {
        deltaTime = (time - lastLoopTime)
        lastLoopTime = time


        /**
         * Run the various update functions
         */
        updateDrive()
        updateSlide()
        updateIntake()
        updateTrussHang()

        shared.motorTrussPull?.power = 1.0 * ((if (gamepad1.b) 1.0 else 0.0) + (if (gamepad1.a) -1.0 else 0.0))
////        shared.motorSlide!!.mode = RUN_WITHOUT_ENCODER
////        shared.motorSlide!!.power = (gamepad1.left_trigger - gamepad2.right_trigger).toDouble().coerceAtLeast(0.0).coerceAtMost(1.0)
//        shared.motorSlide!!.targetPosition = (shared.motorSlide!!.targetPosition + (10 * ((if (gamepad1.left_trigger > 0.5) 1 else 0) + (if (gamepad1.right_trigger > 0.5) -1 else 0)))).coerceAtLeast(0).coerceAtMost(1086)
//        shared.motorSlide!!.mode = RUN_TO_POSITION
//        shared.motorSlide!!.power = 1.0
//        telemetry.addLine(
//            """
//        |==================================================
//        |Slide target position: ${shared.motorSlide!!.targetPosition}
//        |Slide current position: ${shared.motorSlide!!.currentPosition}
//        |Slide mode: ${shared.motorSlide!!.mode}
//        |Slide ZPB: ${shared.motorSlide!!.zeroPowerBehavior}"
//        |Slide is enabled: ${shared.motorSlide!!.isMotorEnabled}"
//        |Slide power: ${shared.motorSlide!!.power}"
//        |Slide current: ${shared.motorSlide!!.getCurrent(CurrentUnit.AMPS)}"
//        |Slide velocity: ${shared.motorSlide!!.velocity}
//        |==================================================
//        """.trimMargin()
//        )
        val sarml = shared.servoArmLeft
        if (sarml != null) {
            sarml.position = (sarml.position + (0.01 * ((if (gamepad1.right_bumper) 1.0 else 0.0) + (if (gamepad1.left_bumper) -1.0 else 0.0)))) % 1.0
        }
        if (gamepad1.y) {
            shared.servoTrussLeft?.position = 1.0
            shared.servoTrussRight?.position = 0.0
        }


        // Most input values are [-1.0, 1.0]

        telemetry.addLine("Left Stick X: ${gamepad1.left_stick_x}")
        telemetry.addLine("Left Stick Y: ${gamepad1.left_stick_y}")
        telemetry.addLine("Delta Time: $deltaTime")
        telemetry.update()

        gamepadyn.update()

        shared.update()
    }

    /**
     * Handle controls for the truss pulley
     */
    private fun updateTrussHang() {
        // TODO: should this be locked until endgame?
        //       we could use (timer > xx.xx) or something

    }

    /**
     * Update bot movement (drive motors)
     */
    private fun updateDrive() {
        shared.drive
    }

    /**
     * Update the linear slide
     */
    private fun updateSlide() {
        val lsd = shared.lsd!!
//        // TODO: replace with Linear Slide Driver
//        val slide = shared.motorSlide
//        //        val lsd = shared.lsd!!
//        if (slide != null) {
//            // lift/slide
//            slide.mode = RUN_WITHOUT_ENCODER
//            slide.power = if (abs(gamepad2.left_stick_y) > 0.1) -gamepad2.left_stick_y.toDouble() else 0.0
//        } else {
//            telemetry.addLine("WARNING: Safeguard triggered (slide not present)");
//        }
//        shared.motorSlide!!.mode = RUN_WITHOUT_ENCODER
//        shared.motorSlide!!.power = (gamepad1.left_trigger - gamepad2.right_trigger).toDouble().coerceAtLeast(0.0).coerceAtMost(1.0)
        lsd.targetHeight += 10 * ((if (gamepad1.left_trigger > 0.5) 1 else 0) + (if (gamepad1.right_trigger > 0.5) -1 else 0))
//        telemetry.addLine("Slide target position: ${shared.motorSlide!!.targetPosition}")
//        telemetry.addLine("Slide current position: ${shared.motorSlide!!.currentPosition}")
//        telemetry.addLine("Slide mode: ${shared.motorSlide!!.mode}")
//        telemetry.addLine("Slide ZPB: ${shared.motorSlide!!.zeroPowerBehavior}")
//        telemetry.addLine("Slide is enabled: ${shared.motorSlide!!.isMotorEnabled}")
//        telemetry.addLine("Slide power: ${shared.motorSlide!!.power}")
//        telemetry.addLine("Slide current: ${shared.motorSlide!!.getCurrent(CurrentUnit.AMPS)}")
//        telemetry.addLine("Slide velocity: ${shared.motorSlide!!.velocity}")
    }

    /**
     * Update the intake mechanisms (spinner, claw, arm)
     */
    private fun updateIntake() {
        val intake = shared.intake
        val armLeft = shared.servoArmLeft
//        val armRight = shared.servoArmRight
        val claw = shared.claw

        // Claw
        if (claw != null && shared.servoClawLeft != null && shared.servoClawRight != null) {
            // TODO: need to make claw work
            claw.state += 0.5 * 0.01 * (gamepad2.right_trigger - gamepad2.left_trigger)

            //        claw.state +=
            //        clawLeft.position +=    Servo.MAX_POSITION * 0.5 * deltaTime * (gamepad2.right_trigger - gamepad2.left_trigger)
            //        clawRight.position +=   Servo.MAX_POSITION * 0.5 * deltaTime * (gamepad2.right_trigger - gamepad2.left_trigger)
            telemetry.addLine("Claw Positions: L=${shared.servoClawLeft?.position} R=${shared.servoClawRight?.position}")
            telemetry.addLine("Claw Module: ${claw.state}")
        } else {
            telemetry.addLine("WARNING: Safeguard triggered (claw not present)");
        }

        // Arm
        if (armLeft != null /* && armRight != null*/) {
            val armPos = (armLeft.position + Servo.MAX_POSITION * /*deltaTime*/ 0.01 * -gamepad2.right_stick_y)
            armLeft.position = armPos.clamp(Servo.MIN_POSITION, Servo.MAX_POSITION)

            telemetry.addLine("Arm Position: ${armLeft.position}")
        } else {
            telemetry.addLine("WARNING: Safeguard triggered (arm not present)");
        }

        // spinner
        if (intake == null) telemetry.addLine("WARNING: Safeguard triggered (intake not present)");
        intake?.active =
            if (gamepad1.dpad_down) 1.0     // inwards
            else if (gamepad1.dpad_up) -1.0 // outwards
            else 0.0                        // off
        telemetry.addLine("Intake State: ${intake?.active}")
    }
}