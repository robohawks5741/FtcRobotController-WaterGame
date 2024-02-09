package org.firstinspires.ftc.teamcode

import android.util.Log
import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
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

    private var poseEstimate = Pose2d(0.0, 0.0, 0.0)
    private lateinit var hang: DcMotorEx
    private lateinit var intake: DcMotorEx
    private lateinit var slideR: DcMotorEx
    private lateinit var slideL: DcMotorEx
    private lateinit var trussL: Servo
    private lateinit var trussR: Servo
    private lateinit var armR: Servo
    private lateinit var armL: Servo
    private lateinit var clawR: Servo
    private lateinit var clawL: Servo
    private lateinit var drone: Servo
    private lateinit var inlift: Servo
    private lateinit var imu: IMU
//    private lateinit var distance: DistanceSensor

    private var driverRelative = true

    // automatically updates the truss servos when the value is changed
    private var trussPos = TrussPosition.DOWN
        set(pos) {
            field = pos
            trussL.position = trussPos.leftPos
            trussR.position = trussPos.rightPos
        }

    // THE ARM HAS NO MODULE !!
    private var isArmDown = true
        set(status) {
            armR.position = if (status) 0.05 else 0.35
            armL.position = if (status) 0.95 else 0.65
            field = status
        }

    object GamepadConfig {
        /**
         * Player 1 (index 0) config
         */
        val player0 = ConfigurationRH(
            ActionBind(TOGGLE_DRIVER_RELATIVITY,            SPECIAL_BACK),
            ActionBind(MOVEMENT,                            STICK_LEFT),
            ActionBind(INTAKE_SPIN,                         TRIGGER_LEFT),
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
            ActionBindAnalog1Threshold(MACRO_PLACE_PIXEL,   TRIGGER_RIGHT, threshold = 0.2f),
            ActionBindAnalog1Snap(INTAKE_SPIN,              TRIGGER_LEFT, activeValue = 0.65f, threshold = 0.2f),
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
            ActionBind(CLAW_LEFT_OPEN,                      BUMPER_LEFT),
            ActionBind(CLAW_RIGHT_OPEN,                     BUMPER_RIGHT),
            ActionBind(CLAW_LEFT_CLOSE,                     FACE_LEFT),
            ActionBind(CLAW_RIGHT_CLOSE,                    FACE_LEFT),
            ActionBind(DRONE_LAUNCH,                        FACE_RIGHT),
            ActionBindAnalog1Threshold(MACRO_PLACE_PIXEL,   TRIGGER_RIGHT, threshold = 0.2f),
            ActionBindAnalog1Snap(INTAKE_SPIN,              TRIGGER_LEFT, activeValue = 0.65f, threshold = 0.2f),
        )
    }

    override fun runOpMode() {
        hang = hardwareMap[DcMotorEx::class.java, "hang"]
        intake = hardwareMap[DcMotorEx::class.java, "intake"]
        slideR = hardwareMap[DcMotorEx::class.java, "slideR"]
        slideL = hardwareMap[DcMotorEx::class.java, "slideL"]
        drone = hardwareMap[Servo::class.java, "drone"]
        trussR = hardwareMap[Servo::class.java, "trussR"]
        trussL = hardwareMap[Servo::class.java, "trussL"]
        armR = hardwareMap[Servo::class.java, "armR"]
        armL = hardwareMap[Servo::class.java, "armL"]
        clawR = hardwareMap[Servo::class.java, "clawR"]
        clawL = hardwareMap[Servo::class.java, "clawL"]
        inlift = hardwareMap[Servo::class.java, "inlift"]
//        distance = hardwareMap[DistanceSensor::class.java, "distance"]
        imu = hardwareMap[IMU::class.java, "imu"]
        shared = BotShared(this)

        shared.rr = MecanumDrive(hardwareMap, poseEstimate)
        moduleHandler = ModuleHandler(ModuleConfig(this, shared, isTeleOp = true, gamepadyn))

        // Configuration
        val p0 = gamepadyn.players[0]
        val p1 = gamepadyn.players[1]
        p0.configuration = GamepadConfig.player0
        p1.configuration = GamepadConfig.player1

        moduleHandler.init()

        val claw = moduleHandler.claw
        val drive = moduleHandler.drive
        val intake = moduleHandler.intake
        val lsd = moduleHandler.lsd
        val opticon = moduleHandler.opticon
        val trussel = moduleHandler.trussle
        val rr = shared.rr!!

//        data class Timeout(val calltime: Long, val callback: () -> Unit)
//        val waitList: ArrayList<Timeout> = arrayListOf();
//        fun wait(calltime: Long, callback: () -> Unit) = waitList.add(Timeout((time * 1000).toLong() + calltime, callback))

        imu.resetYaw()
        slideR.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideR.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        slideL.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideL.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        hang.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        claw.leftOpen = true
        claw.rightOpen = true
        isArmDown = true

        trussPos = TrussPosition.DOWN
        drone.position = 0.36
        inlift.position = 0.0

        telemetry.update()

        waitForStart()

        //        lsd.currentHeight > LSD.SLIDE_HEIGHT_CLAW_SAFE

        // MACROS
        val macroSlideUp = { it: InputDataDigital ->
            if (it()) {
//                    isSlideMovingUp = true
                // TODO: reduce the use of sleep
                if (claw.leftOpen || claw.rightOpen) {
                    claw.leftOpen = false
                    claw.leftOpen = false
                    // wait for claws to move
                    sleep(200)
                }
                // move up slightly as to move the arm up
                lsd.targetHeight = 400
                // wait for slide to move
                sleep(1000)
//                while (lsd.currentHeight < LSD.HEIGHT_CLAW_SAFE) {
//                    sleep(20)
//                }
                isArmDown = false
                // move up now
                lsd.targetHeight = LSD.HEIGHT_MAX
//                    isSlideMovingUp = false
            }
        }

        val macroSlideDown = { it: InputDataDigital ->
            if (it()) {
                if (!isArmDown) {
                    isArmDown = true
                    sleep(100)
                }
                lsd.targetHeight = LSD.HEIGHT_MIN
            }
        }

        val droneLaunch = { it: InputDataDigital ->
            if (it()) {
                drone.position = 0.8
            }
        }

        val macroPlacePixel = { it: InputDataDigital ->
            if (it()) {
                // Open the claws
                if (!claw.leftOpen || !claw.leftOpen) {
                    claw.leftOpen = true
                    claw.rightOpen = true
                    sleep(200)
                }
                if (!isArmDown) {
                    isArmDown = true
                    sleep(200)
                }
                lsd.targetHeight = LSD.HEIGHT_MIN
            }
        }

        p0.getEvent(MACRO_SLIDE_UP, macroSlideUp)
        p1.getEvent(MACRO_SLIDE_UP, macroSlideUp)

        p0.getEvent(MACRO_SLIDE_DOWN, macroSlideDown)
        p1.getEvent(MACRO_SLIDE_DOWN, macroSlideDown)

        p0.getEvent(DRONE_LAUNCH, droneLaunch)
        p1.getEvent(DRONE_LAUNCH, droneLaunch)

        p0.getEvent(MACRO_PLACE_PIXEL, droneLaunch)
        p1.getEvent(MACRO_PLACE_PIXEL, droneLaunch)

        moduleHandler.start()

        telemetry.update()

        while (opModeIsActive()) {
            gamepadyn.update()

            // for
            moduleHandler.update()

            // TODO: port to gamepadyn
            // if dpad left is pressed (and the slide is up, mostly
            if ((gamepad1.dpad_left || gamepad2.dpad_left) && lsd.currentHeight > LSD.HEIGHT_CLAW_SAFE) {
                if (lsd.targetHeight == 600 && !isArmDown) {
                    isArmDown = true
                    // used to be 100
                    sleep(200)
                    lsd.targetHeight = 0
                } else {
                    lsd.targetHeight -= 200
                }
            }

            // TODO: port to gamepadyn
            if ((gamepad1.dpad_right || gamepad2.dpad_right) && lsd.currentHeight < LSD.HEIGHT_MAX - 64) {
                if (claw.leftOpen || claw.rightOpen) {
                    claw.rightOpen = false
                    claw.leftOpen = false
                    sleep(200)
                }
                if (lsd.targetHeight == 0) {
                    lsd.targetHeight = 600
//                    isSlideMovingUp = true
                } else {
                    lsd.targetHeight += 200
                }
            }

            // Driver 2 Override
            if (abs(gamepad2.left_stick_y) > 0.1) {
                intake.power = gamepad2.left_stick_y.toDouble().stickCurve()
            }


            rr.updatePoseEstimate()
            telemetry.addData("DriverRelative", driverRelative)
            telemetry.addData("x", rr.pose.position.x)
            telemetry.addData("y", rr.pose.position.y)
            telemetry.addData("heading", rr.pose.heading.log())
            telemetry.addData("rightSlide", slideR.currentPosition)
            telemetry.addData("leftSlide", slideL.currentPosition)
            telemetry.addData("right arm", armR.position)
            telemetry.addData("left arm", armL.position)
            telemetry.addData("inlift", inlift.position)
            telemetry.addData("hangMode", trussPos)
//            telemetry.addData("Distance", distance.getDistance(DistanceUnit.CM))
            telemetry.update()
        }

        moduleHandler.stop()
    }
}