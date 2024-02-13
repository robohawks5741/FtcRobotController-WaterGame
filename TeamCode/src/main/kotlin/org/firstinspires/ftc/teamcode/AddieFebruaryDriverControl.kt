package org.firstinspires.ftc.teamcode

import android.util.Log
import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.ActionBind
import computer.living.gamepadyn.ActionMap
import computer.living.gamepadyn.Configuration
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.InputData
import computer.living.gamepadyn.InputDataAnalog1
import computer.living.gamepadyn.InputDataAnalog2
import computer.living.gamepadyn.InputDataDigital
import computer.living.gamepadyn.Player
import computer.living.gamepadyn.RawInputAnalog1.*
import computer.living.gamepadyn.RawInputAnalog2.*
import computer.living.gamepadyn.RawInputDigital.*
import computer.living.gamepadyn.ftc.InputBackendFtc
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.ActionAnalog2.*
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.botmodule.LSD
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import org.firstinspires.ftc.teamcode.botmodule.ModuleHandler
import kotlin.math.abs

typealias GamepadynRH = Gamepadyn<ActionDigital, ActionAnalog1, ActionAnalog2>
typealias ConfigurationRH = Configuration<ActionDigital, ActionAnalog1, ActionAnalog2>
typealias PlayerRH = Player<ActionDigital, ActionAnalog1, ActionAnalog2>
// TODO: fix this up

@TeleOp(name = "# Addie February Driver Control")
class AddieFebruaryDriverControl : LinearOpMode() {

    private val gamepadyn = GamepadynRH(
        InputBackendFtc(this),
        strict = true,
        ActionMap(
            ActionDigital.entries,
            ActionAnalog1.entries,
            ActionAnalog2.entries,
        )
    )
    private lateinit var shared: BotShared
    private lateinit var moduleHandler: ModuleHandler

    private lateinit var drone: Servo
    private lateinit var imu: IMU

    private var driverRelative = true

    /**
     * Both players must be holding down the drone launch button in order to launch the drone.
     */
    private var droneTurnKey0 = false
    private var droneTurnKey1 = false

    object GamepadConfig {
        /**
         * Player 1 (index 0) config
         */
        val player0 = ConfigurationRH(
            ActionBind(TOGGLE_DRIVER_RELATIVITY,            SPECIAL_BACK),
            ActionBind(MOVEMENT,                            STICK_LEFT),
            ActionBind(TRUSS_CYCLE,                         FACE_UP),

            ActionBind(CLAW_LEFT_OPEN,                      BUMPER_LEFT),
            ActionBind(CLAW_RIGHT_OPEN,                     BUMPER_RIGHT),

            ActionBind(CLAW_LEFT_CLOSE,                     FACE_LEFT),
            ActionBind(CLAW_RIGHT_CLOSE,                    FACE_LEFT),
            // TODO: Replace with BindSingleAxis
            object : ActionBind<ActionAnalog1>(TRUSS_PULL, FACE_DOWN) {
                override fun transform(
                    inputState: InputData,
                    targetActionState: InputData,
                    delta: Double
                ): InputData {
                    if (inputState !is InputDataDigital) {
                        Log.e("Gamepadyn", "truss pull code broke :(")
                        return targetActionState
                    }
                    return InputDataAnalog1(if (inputState()) 1.0f else 0.0f)
                }
            },

            ActionBind(MACRO_SLIDE_UP,                      DPAD_UP),
            ActionBind(MACRO_SLIDE_DOWN,                    DPAD_DOWN),

            ActionBind(SLIDE_ADJUST_UP,                     DPAD_LEFT),
            ActionBind(SLIDE_ADJUST_DOWN,                   DPAD_RIGHT),

            ActionBindAnalog1Threshold(MACRO_PLACE_PIXEL,   TRIGGER_RIGHT, threshold = 0.2f),
            ActionBindAnalog1SnapToAnalog1(INTAKE_SPIN,     TRIGGER_LEFT, activeValue = 0.8f, inactiveValue = Float.NaN, threshold = 0.2f),
            // TODO: Replace with BindSingleAxis
            object : ActionBind<ActionAnalog1>(ROTATION, STICK_RIGHT) {
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
            }
        )

        /**
         * Player 2 (index 1) config
         */
        val player1 = ConfigurationRH(
            ActionBind(MACRO_SLIDE_UP,                      DPAD_UP),
            ActionBind(MACRO_SLIDE_DOWN,                    DPAD_DOWN),

            ActionBind(SLIDE_ADJUST_UP,                     DPAD_LEFT),
            ActionBind(SLIDE_ADJUST_DOWN,                   DPAD_RIGHT),

            /*
             * These are backwards. This is sorta intentional.
             */
            ActionBind(CLAW_LEFT_OPEN,                      BUMPER_RIGHT),
            ActionBind(CLAW_RIGHT_OPEN,                     BUMPER_LEFT),
            ActionBind(CLAW_LEFT_CLOSE,                     FACE_LEFT),
            ActionBind(CLAW_RIGHT_CLOSE,                    FACE_LEFT),

            ActionBind(DRONE_LAUNCH,                        FACE_RIGHT),
            ActionBindAnalog1Threshold(MACRO_PLACE_PIXEL,   TRIGGER_RIGHT, threshold = 0.2f),
            ActionBindAnalog1SnapToAnalog1(INTAKE_SPIN,     TRIGGER_LEFT, activeValue = 0.65f, threshold = 0.2f),
        )
    }

    override fun runOpMode() {
        drone   = hardwareMap!![Servo::class.java, "drone"]
        imu     = hardwareMap!![IMU::class.java, "imu"]

        shared = BotShared(this)

        // initial drone position
        drone.position = 1.0

        // MAKE SURE THAT SHARED IS INITIALIZED BEFORE THIS!!!
        shared.rr = MecanumDrive(hardwareMap, BotShared.storedPose)
        moduleHandler = ModuleHandler(ModuleConfig(this, shared, isTeleOp = true, gamepadyn))

        // Configuration
        val p0 = gamepadyn.players[0]
        val p1 = gamepadyn.players[1]
        p0.configuration = GamepadConfig.player0
        p1.configuration = GamepadConfig.player1

        // MOD INIT
        moduleHandler.init()

        val claw = moduleHandler.claw
        val drive = moduleHandler.drive
        val intake = moduleHandler.intake
        val lsd = moduleHandler.lsd
        val opticon = moduleHandler.opticon
        val trussle = moduleHandler.trussle
        val rr = shared.rr!!

        imu.resetYaw()

        claw.leftOpen = true
        claw.rightOpen = true

        trussle.position = TrussPosition.DOWN
        drone.position = 0.36
        intake.raised = false

        telemetry.update()

        waitForStart()

        // TODO: fix these macros

//        // MACRO
//
        val macroPlacePixel = fun(it: InputDataDigital) {
            if (it()) {
                // Open the claws
                if (!claw.leftOpen || !claw.leftOpen) {
                    claw.leftOpen = true
                    claw.rightOpen = true
                    sleep(400)
                }
                lsd.isArmDown = true
                lsd.targetHeight = LSD.HEIGHT_MIN
            }
        }

        val macroSlideUp = fun(it: InputDataDigital) {
            if (it()) {
                // TODO: reduce the use of sleep
                if (claw.leftOpen || claw.rightOpen) {
                    claw.leftOpen = false
                    claw.rightOpen = false
                    // wait for claws to move
                    sleep(300)
                }

                lsd.isArmDown = false

                // move up slightly as to move the arm up
                lsd.targetHeight = 400
                // wait for slide to move
                lsd.isArmDown = false
                // move up now
                lsd.targetHeight = LSD.HEIGHT_MAX
            }
        }

        val macroSlideDown = fun(it: InputDataDigital) {
            if (it()) {
                lsd.isArmDown = true
                lsd.targetHeight = LSD.HEIGHT_MIN
            }
        }

        p0.getEvent(MACRO_SLIDE_UP, macroSlideUp)
        p1.getEvent(MACRO_SLIDE_UP, macroSlideUp)

        p0.getEvent(MACRO_SLIDE_DOWN, macroSlideDown)
        p1.getEvent(MACRO_SLIDE_DOWN, macroSlideDown)

        p0.getEvent(DRONE_LAUNCH) {
            droneTurnKey0 = it()
        }
        p1.getEvent(DRONE_LAUNCH) {
            droneTurnKey1 = it()
        }

        p0.getEvent(MACRO_PLACE_PIXEL, macroPlacePixel)
        p1.getEvent(MACRO_PLACE_PIXEL, macroPlacePixel)

        // MOD START
        moduleHandler.start()

        telemetry.update()

        while (opModeIsActive()) {
            BotShared.wasLastOpModeAutonomous = false
            gamepadyn.update()

            // MOD UPDATE
            moduleHandler.update()

            // Driver 2 Override
            if (abs(gamepad2.left_stick_y) > 0.1) {
                intake.power = gamepad2.left_stick_y.toDouble().stickCurve()
            }

            // drone launch turnkey
            if (droneTurnKey0 && droneTurnKey1) drone.position = 0.0

            rr.updatePoseEstimate()
            telemetry.addData("RR pos x", rr.pose.position.x)
            telemetry.addData("RR pos y", rr.pose.position.y)
            telemetry.addData("RR heading", rr.pose.heading.log())
//            telemetry.addData("Distance", distance.getDistance(DistanceUnit.CM))
            telemetry.update()
        }

        moduleHandler.stop()
    }
}