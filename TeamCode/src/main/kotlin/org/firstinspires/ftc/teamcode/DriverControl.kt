package org.firstinspires.ftc.teamcode

import android.util.Log
import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import computer.living.gamepadyn.ActionBind
import computer.living.gamepadyn.ActionBindAnalog2to1
import computer.living.gamepadyn.ActionMap
import computer.living.gamepadyn.Axis.Y
import computer.living.gamepadyn.Configuration
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.InputData
import computer.living.gamepadyn.InputDataAnalog1
import computer.living.gamepadyn.InputDataAnalog2
import computer.living.gamepadyn.Player
import computer.living.gamepadyn.RawInputDigital.*
import computer.living.gamepadyn.RawInputAnalog1.*
import computer.living.gamepadyn.RawInputAnalog2.*
import computer.living.gamepadyn.ftc.InputBackendFtc
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.ActionAnalog2.*
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import org.firstinspires.ftc.teamcode.botmodule.ModuleHandler

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

    object GamepadConfig {
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
            ActionBind(INTAKE_SPIN,                         TRIGGER_LEFT),
            // TODO: Replace with BindSingleAxis
            object : ActionBind<ActionAnalog1>(ROTATION,    STICK_RIGHT) {
                override fun transform(
                    inputState: InputData,
                    targetActionState: InputData,
                    delta: Double
                ): InputData {
                    if (inputState !is InputDataAnalog2) {
                        Log.e("Gamepadyn", "rotation code broke :(")
                        return targetActionState
                    }
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
                    if (inputState !is InputDataAnalog1 || targetActionState !is InputDataAnalog1) {
                        Log.e("Gamepadyn", "claw is not analog 1 ??")
                        return targetActionState
                    }
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
            ActionBind(INTAKE_SPIN,                         TRIGGER_LEFT),
            object : ActionBind<ActionAnalog1>(INTAKE_SPIN, TRIGGER_RIGHT) {
                override fun transform(
                    inputState: InputData,
                    targetActionState: InputData,
                    delta: Double
                ): InputData {
                    if (inputState !is InputDataAnalog1) {
                        Log.e("Gamepadyn", "exception in the crappy garbage code intake")
                        return targetActionState
                    }
                    return InputDataAnalog1(-inputState.x)
                }
            },
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
        shared = BotShared(this)
        moduleHandler = ModuleHandler(ModuleConfig(this, shared, true, gamepadyn))
        shared.rr = MecanumDrive(hardwareMap, initialPose)

        // Configuration
        val p0 = gamepadyn.players[0]
        val p1 = gamepadyn.players[1]
        p0.configuration = GamepadConfig.player0
        p1.configuration = GamepadConfig.player1

        moduleHandler.init()
    }

    override fun start() {
        lastLoopTime = time
        moduleHandler.start()

        val p0 = gamepadyn.players[0]
        val p1 = gamepadyn.players[1]

        p0.getEvent(PIXEL_START) {
            if (it()) p0.configuration = GamepadConfig.pixelPlacement
        }
        p1.getEvent(PIXEL_START) {
            if (it()) p1.configuration = GamepadConfig.pixelPlacement
        }

        p0.getEvent(PIXEL_END) {
            if (it()) p0.configuration = GamepadConfig.player0
        }
        p1.getEvent(PIXEL_END) {
            if (it()) p1.configuration = GamepadConfig.player1
        }

    }

    override fun stop() {
        moduleHandler.stop()
    }

    /**
     * The robot's game loop. Doesn't do much! Almost everything is delegated to other modules.
     */
    override fun loop() {
        deltaTime = (time - lastLoopTime)
        lastLoopTime = time

        gamepadyn.update()
        shared.update()

        moduleHandler.update()

        if (gamepadyn.players[0].configuration == GamepadConfig.pixelPlacement) {
            telemetry.addLine("p0 is placing a pixel")
        }
        if (gamepadyn.players[1].configuration == GamepadConfig.pixelPlacement) {
            telemetry.addLine("p1 is placing a pixel")
        }

        telemetry.addLine("Left Stick X: ${gamepad1.left_stick_x}")
        telemetry.addLine("Left Stick Y: ${-gamepad1.left_stick_y}")
//        telemetry.addLine("Delta Time: $deltaTime")
        telemetry.update()
    }
}