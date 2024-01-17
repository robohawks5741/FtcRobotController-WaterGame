package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.ActionBind
import computer.living.gamepadyn.ActionBindAnalog2to1
import computer.living.gamepadyn.ActionMap
import computer.living.gamepadyn.Axis
import computer.living.gamepadyn.Axis.Y
import computer.living.gamepadyn.Configuration
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.InputData
import computer.living.gamepadyn.InputDataAnalog1
import computer.living.gamepadyn.InputDataAnalog2
import computer.living.gamepadyn.InputType.ANALOG1
import computer.living.gamepadyn.InputType.ANALOG2
import computer.living.gamepadyn.InputType.DIGITAL
import computer.living.gamepadyn.Player
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

/*
 * TODO:
 *      - Store the orientation at the end of autonomous and reload it for DC
 */

typealias GamepadynRH = Gamepadyn<ActionDigital, ActionAnalog1, ActionAnalog2>
typealias ConfigurationRH = Configuration<ActionDigital, ActionAnalog1, ActionAnalog2>
typealias PlayerRH = Player<ActionDigital, ActionAnalog1, ActionAnalog2>

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

    @Suppress("LeakingThis")
    private val gamepadyn = Gamepadyn(
        InputBackendFtc(this),
        true, ActionMap(
        ActionDigital.entries,
            ActionAnalog1.entries,
            ActionAnalog2.entries,
        )
    )

    object Config {
        /**
         * Pixel placement config
         */
        val pixelPlacement = ConfigurationRH(
            ActionBindAnalog2to1(SLIDE_MANUAL,              STICK_LEFT, Y),
            ActionBind(PIXEL_END,                           FACE_RIGHT),
            ActionBind(PIXEL_MOVE_UP,                       DPAD_UP),
            ActionBind(PIXEL_MOVE_DOWN,                     DPAD_DOWN),
            ActionBind(PIXEL_COMMIT_LEFT,                   BUMPER_LEFT),
            ActionBind(PIXEL_COMMIT_RIGHT,                  BUMPER_RIGHT),
        )

        /**
         * Player 1 (index 0) config
         */
        val player0 = ConfigurationRH(
            ActionBind(PIXEL_START,                         FACE_UP),
            ActionBind(MOVEMENT,                            STICK_LEFT),
            ActionBind(TOGGLE_DRIVER_RELATIVITY,            SPECIAL_BACK),

            // TODO: Replace with BindSingleAxis
            object : ActionBind<ActionAnalog1>(ROTATION,    STICK_RIGHT) {
                override fun transform(
                    inputState: InputData,
                    targetActionState: InputData,
                    delta: Double
                ): InputData {
                    if (inputState !is InputDataAnalog2) throw Exception("exception in the crappy garbage code !1!")
                    return InputDataAnalog1(inputState.x)
                }
            },
            ActionBind(TOGGLE_INTAKE_HEIGHT,                FACE_DOWN),
            object : ActionBind<ActionAnalog1>(CLAW,        TRIGGER_RIGHT) {
                override fun transform(
                    inputState: InputData,
                    targetActionState: InputData,
                    delta: Double
                ): InputData {
                    if (inputState !is InputDataAnalog1 || targetActionState !is InputDataAnalog1) throw Exception("claw is not analog 1 ??")
                    return InputDataAnalog1(targetActionState.x + (delta * 10.0).toFloat())
                }
            }
        )

        /**
         * Player 2 (index 1) config
         */
        val player1 = ConfigurationRH(
            ActionBindAnalog2to1(SLIDE_MANUAL,              STICK_LEFT, Y),
            ActionBind(PIXEL_START,                         FACE_UP),
//            ActionBind(DRONE_LAUNCH,                        FACE_RIGHT),
//            ActionBind(DRONE_LAUNCH,                        SPECIAL_BACK),
//            ActionBind(INTAKE_SPIN,                         TRIGGER_LEFT),
//            ActionBind(INTAKE_SPIN,                         TRIGGER_LEFT),
        )
    }

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
        val p0 = gamepadyn.players[0]
        val p1 = gamepadyn.players[1]
        p0.configuration = Config.player0
        p1.configuration = Config.player1

        moduleHandler.init()
    }

    override fun start() {
        lastLoopTime = time
        moduleHandler.start()

        val p0 = gamepadyn.players[0]
        val p1 = gamepadyn.players[1]

        p0.getEvent(PIXEL_START) {
            if (it()) p0.configuration = Config.pixelPlacement
        }
        p1.getEvent(PIXEL_START) {
            if (it()) p1.configuration = Config.pixelPlacement
        }

        p0.getEvent(PIXEL_END) {
            if (it()) p0.configuration = Config.player0
        }
        p1.getEvent(PIXEL_END) {
            if (it()) p1.configuration = Config.player1
        }

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

        if (gamepadyn.players[0].configuration == Config.pixelPlacement) {
            telemetry.addLine("p0 is placing a pixel")
        }
        if (gamepadyn.players[1].configuration == Config.pixelPlacement) {
            telemetry.addLine("p1 is placing a pixel")
        }

        telemetry.addLine("Left Stick X: ${gamepad1.left_stick_x}")
        telemetry.addLine("Left Stick Y: ${-gamepad1.left_stick_y}")
//        telemetry.addLine("Delta Time: $deltaTime")
        telemetry.update()
    }
}