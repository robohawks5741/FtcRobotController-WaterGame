package org.firstinspires.ftc.teamcode

import android.util.Log
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.ActionBind
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
import org.firstinspires.ftc.teamcode.botmodule.BotModule
import org.firstinspires.ftc.teamcode.botmodule.LSD
import org.firstinspires.ftc.teamcode.botmodule.ModuleHandler
import kotlin.math.roundToInt

typealias GamepadynRH = Gamepadyn<ActionDigital, ActionAnalog1, ActionAnalog2>
typealias ConfigurationRH = Configuration<ActionDigital, ActionAnalog1, ActionAnalog2>
typealias PlayerRH = Player<ActionDigital, ActionAnalog1, ActionAnalog2>
// TODO: fix this up

@TeleOp(name = "# Addie February Driver Control")
class AddieFebruaryDriverControl : LinearOpMode() {

    private val gamepadyn = GamepadynRH.create(
        ActionDigital::class,
        ActionAnalog1::class,
        ActionAnalog2::class,
        InputBackendFtc(this),
        strict = true,
    )
    private lateinit var shared: BotShared
    private lateinit var moduleHandler: ModuleHandler

    private var drone: Servo? = null
    private lateinit var imu: IMU

    private var driverRelative = true

    /**
     * Both players must be holding down the drone launch button in order to launch the drone.
     */
    private var droneTurnKey0 = false
    private var droneTurnKey1 = false


    private var slideAdjustHeight = 0
    private var wantSlideUp = false

    override fun runOpMode() {
        drone   = hardwareMap.search("drone")
        imu     = hardwareMap.search("imu")!!

        shared = BotShared(this)

        // initial drone position
        drone?.position = 1.0

        moduleHandler = ModuleHandler(this, shared, isTeleOp = true, gamepadyn = gamepadyn)

        // Configuration
        val p0 = gamepadyn.players[0]
        val p1 = gamepadyn.players[1]
        p0.configuration = GamepadConfig.player0
        p1.configuration = GamepadConfig.player1

        // MOD INIT
        moduleHandler.init()

        // MAKE SURE THAT SHARED IS INITIALIZED BEFORE THIS!!!
        shared.rr = if (moduleHandler.drive.status.status == BotModule.StatusEnum.OK) MecanumDrive(hardwareMap, BotShared.storedPose) else null

        val claw = moduleHandler.claw
        val drive = moduleHandler.drive
        val intake = moduleHandler.intake
        val lsd = moduleHandler.lsd
//        val opticon = moduleHandler.opticon
        val trussle = moduleHandler.trussle
        val rr = shared.rr

        imu.resetYaw()

        claw.leftOpen = true
        claw.rightOpen = true

        trussle.position = TrussPosition.DOWN
        drone?.position = 0.36
        intake.raised = false

        telemetry.update()

        waitForStart()

        // IMU orientation/calibration
        imu.initialize(IMU.Parameters(RevHubOrientationOnRobot(
            LogoFacingDirection.LEFT,
            UsbFacingDirection.FORWARD
        )))
        if (!BotShared.wasLastOpModeAutonomous) imu.resetYaw()

        gamepadyn.addListener(CLAW_LEFT_OPEN)   { if (it.data()) claw.leftOpen = true }
        gamepadyn.addListener(CLAW_LEFT_CLOSE)  { if (it.data()) claw.leftOpen = false }
        gamepadyn.addListener(CLAW_RIGHT_OPEN)  { if (it.data()) claw.rightOpen = true }
        gamepadyn.addListener(CLAW_RIGHT_CLOSE) { if (it.data()) claw.rightOpen = false }

        // cycle the truss hanger positions when the button is pressed
        gamepadyn.addListener(TRUSS_CYCLE) {
            if (it.data()) {
                trussle.position = when (trussle.position) {
                    TrussPosition.UP -> TrussPosition.DOWN
                    TrussPosition.DOWN -> TrussPosition.UP
                }
            }
        }

        // pull in the motors if the button is being held down
        gamepadyn.addListener(TRUSS_PULL) {
            trussle.pullPower = it.data.x.toDouble().stickCurve()
        }

        p0.addListener(TOGGLE_DRIVER_RELATIVITY) { if (it.data()) drive.useDriverRelative = !drive.useDriverRelative }

        gamepadyn.addListener(INTAKE_SPIN) { intake.power = it.data.x.toDouble() }

        gamepadyn.addListener(MACRO_SLIDE_UP) {
            if (it.data()) {
                // TODO: reduce the use of sleep
                if (claw.leftOpen || claw.rightOpen) {
                    claw.leftOpen = false
                    claw.rightOpen = false
                    // wait for claws to move
                    sleep(300)
                }

                // move up now
                if (slideAdjustHeight == 0) slideAdjustHeight = 6
                wantSlideUp = true
            }
        }

        gamepadyn.addListener(MACRO_SLIDE_DOWN) {
            if (it.data()) {
                // reset teleOp adjustments
                slideAdjustHeight = 0
                wantSlideUp = false
                lsd.targetHeight = LSD.HEIGHT_MIN
            }
        }

        p0.addListener(DRONE_LAUNCH) {
            droneTurnKey0 = it.data()
        }

        p1.addListener(DRONE_LAUNCH) {
            droneTurnKey1 = it.data()
        }

        p0.addListener(MACRO_PLACE_PIXEL) {
            if (it.data()) {
                // Open the claws
                if (!claw.leftOpen || !claw.leftOpen) {
                    claw.leftOpen = true
                    claw.rightOpen = true
                    sleep(400)
                }
                // reset teleOp adjustments
                slideAdjustHeight = 0
                wantSlideUp = false
                lsd.targetHeight = LSD.HEIGHT_MIN
            }
        }

        gamepadyn.addListener(SLIDE_ADJUST_UP) {
            // D-Pad left -> raise slides
            // pos = target * 200 + 300
            if (it.data() && slideAdjustHeight < 6) slideAdjustHeight++
        }

        gamepadyn.addListener(SLIDE_ADJUST_DOWN) {
            if (it.data() && slideAdjustHeight > 0) slideAdjustHeight--
        }

        // MOD START
        moduleHandler.start()

        telemetry.update()

        while (opModeIsActive()) {
            BotShared.wasLastOpModeAutonomous = false
            gamepadyn.update()

            drive.movement = p0.getState(MOVEMENT)
            drive.rotation = p0.getState(ROTATION)

            // MOD UPDATE
            moduleHandler.update()

            // TODO: patch this in
//            // Driver 2 Override
//            if (abs(gamepad2.left_stick_y) > 0.1) {
//                intake.power = gamepad2.left_stick_y.toDouble().stickCurve()
//            }

            // drone launch turnkey
            if (droneTurnKey0 && droneTurnKey1) drone?.position = 0.0

            slideAdjustHeight = slideAdjustHeight.coerceIn(0..6)
            if (wantSlideUp) moduleHandler.lsd.targetHeight = (slideAdjustHeight.toDouble() * ((LSD.HEIGHT_MAX - LSD.HEIGHT_ARM_SAFE).toDouble() / 6.0) + LSD.HEIGHT_ARM_SAFE.toDouble()).roundToInt()

            rr?.updatePoseEstimate()
            telemetry.addData("RR pos x", rr?.pose?.position?.x)
            telemetry.addData("RR pos y", rr?.pose?.position?.y)
            telemetry.addData("RR heading", rr?.pose?.heading?.log())
//            telemetry.addData("Distance", distance.getDistance(DistanceUnit.CM))
            telemetry.update()
        }

        moduleHandler.stop()
    }

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
}