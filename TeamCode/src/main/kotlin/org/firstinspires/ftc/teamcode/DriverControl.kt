package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.ActionBind
import computer.living.gamepadyn.ActionMap
import computer.living.gamepadyn.Configuration
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.InputData
import computer.living.gamepadyn.InputDataAnalog1
import computer.living.gamepadyn.InputDataAnalog2
import computer.living.gamepadyn.InputType.ANALOG1
import computer.living.gamepadyn.InputType.ANALOG2
import computer.living.gamepadyn.InputType.DIGITAL
import computer.living.gamepadyn.RawInputDigital.*
import computer.living.gamepadyn.RawInputAnalog1.*
import computer.living.gamepadyn.RawInputAnalog2.*
import computer.living.gamepadyn.ftc.InputBackendFtc
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.ActionAnalog2.*
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import org.firstinspires.ftc.teamcode.botmodule.ModuleHandler
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
 *  - Y (face up): Truss hang activate
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

    private val gamepadyn = Gamepadyn(
        InputBackendFtc(this),
        true, ActionMap(
        ActionDigital.entries,
            ActionAnalog1.entries,
            ActionAnalog2.entries,
        )
    )

    private lateinit var moduleHandler: ModuleHandler

    /**
     * Set up the robot
     */
    override fun init() {
//        val setter = DriverControl::tagCamera.setter
        shared = BotShared(this)
        moduleHandler = ModuleHandler(ModuleConfig(this, shared, true, gamepadyn))
        shared.rr = MecanumDrive(hardwareMap, initialPose)

        // Configuration
        gamepadyn.players[0].configuration = Configuration(
            ActionBind(MOVEMENT,                            STICK_LEFT),
            ActionBind(TOGGLE_DRIVER_RELATIVITY,            FACE_X),
            // TODO: Replace with BindSingleAxis
            object : ActionBind<ActionAnalog1>(ROTATION,    STICK_RIGHT) {
                override fun transform(
                    inputState: InputData,
                    targetActionState: InputData
                ): InputData {
                    if (inputState !is InputDataAnalog2) throw Exception("exception in the crappy garbage code !1!")
                    return InputDataAnalog1(inputState.x)
                }
            },
            ActionBind(TOGGLE_INTAKE_HEIGHT,                FACE_A),
//            ActionBind(TRUSS_,                          FACE_Y),
        )

        moduleHandler.init()
    }

    override fun start() {
        lastLoopTime = time
        moduleHandler.start()
    }

    /**
     * The robot's game loop. Handles input, updates the motors, basically calls everything.
     */
    override fun loop() {
        deltaTime = (time - lastLoopTime)
        lastLoopTime = time

// TODO: un-break this code

//        val intake = shared.intake
//        val armLeft = shared.servoArmLeft
////        val armRight = shared.servoArmRight
//        val claw = shared.claw
//
//        // Claw
//        if (claw != null && shared.servoClawLeft != null && shared.servoClawRight != null) {
//            claw.state += 0.5 * 0.01 * (gamepad2.right_trigger - gamepad2.left_trigger)
//
//            //        claw.state +=
//            //        clawLeft.position +=    Servo.MAX_POSITION * 0.5 * deltaTime * (gamepad2.right_trigger - gamepad2.left_trigger)
//            //        clawRight.position +=   Servo.MAX_POSITION * 0.5 * deltaTime * (gamepad2.right_trigger - gamepad2.left_trigger)
//            telemetry.addLine("Claw Positions: L=${shared.servoClawLeft?.position} R=${shared.servoClawRight?.position}")
//            telemetry.addLine("Claw Module: ${claw.state}")
//        } else {
//            telemetry.addLine("WARNING: Safeguard triggered (claw not present)");
//        }
//
//        // Arm
//        if (armLeft != null /* && armRight != null*/) {
//            val armPos = (armLeft.position + Servo.MAX_POSITION * /*deltaTime*/ 0.01 * -gamepad2.right_stick_y)
//            armLeft.position = armPos.clamp(Servo.MIN_POSITION, Servo.MAX_POSITION)
//
//            telemetry.addLine("Arm Position: ${armLeft.position}")
//        } else {
//            telemetry.addLine("WARNING: Safeguard triggered (arm not present)");
//        }
//
//        val sarml = shared.servoArmLeft
//        if (sarml != null) {
//            sarml.position = (sarml.position + (0.01 * ((if (gamepad1.right_bumper) 1.0 else 0.0) + (if (gamepad1.left_bumper) -1.0 else 0.0)))) % 1.0
//        }


        // Most input values are [-1.0, 1.0]

        gamepadyn.update()
        shared.update()

        moduleHandler.update()

        telemetry.addLine("Left Stick X: ${gamepad1.left_stick_x}")
        telemetry.addLine("Left Stick Y: ${-gamepad1.left_stick_y}")
//        telemetry.addLine("Delta Time: $deltaTime")
        telemetry.update()
    }
}