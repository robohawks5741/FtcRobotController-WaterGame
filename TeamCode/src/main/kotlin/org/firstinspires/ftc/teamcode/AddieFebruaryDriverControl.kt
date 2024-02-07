package org.firstinspires.ftc.teamcode

import android.util.Log
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
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
import computer.living.gamepadyn.Player
import computer.living.gamepadyn.RawInputAnalog1.*
import computer.living.gamepadyn.RawInputAnalog2.*
import computer.living.gamepadyn.RawInputDigital.*
import computer.living.gamepadyn.ftc.InputBackendFtc
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.ActionAnalog2.*
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import org.firstinspires.ftc.teamcode.botmodule.ModuleHandler
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

typealias GamepadynRH = Gamepadyn<ActionDigital, ActionAnalog1, ActionAnalog2>
typealias ConfigurationRH = Configuration<ActionDigital, ActionAnalog1, ActionAnalog2>
typealias PlayerRH = Player<ActionDigital, ActionAnalog1, ActionAnalog2>

@TeleOp(name = "# Clay January Driver Control")
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
            ActionBind(CLAW_LEFT_OPEN,                      BUMPER_LEFT),
            ActionBind(CLAW_RIGHT_OPEN,                     BUMPER_RIGHT),
            ActionBind(TRUSS_CYCLE,                         FACE_UP),
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
            ActionBind(CLAW_LEFT_OPEN,                      BUMPER_LEFT),
        )
    }


    private fun updateTruss() {
        hang.power = if (gamepad1.a || gamepad2.a) 1.0 else 0.0

        // Gamepad 2 truss controls
        if (abs(gamepad2.right_stick_y) > 0.1) {
            hang.power = gamepad2.right_stick_y.toDouble().stickCurve()
        }
    }

    private var slidePos = 0
        set(pos) {
            field = pos
            slideR.targetPosition = -slidePos
            slideL.targetPosition = slidePos
            slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
            slideR.power = 1.0
            slideL.power = 1.0
        }


    override fun runOpMode() {
        shared.rr = MecanumDrive(hardwareMap, poseEstimate)
        shared = BotShared(this)
        moduleHandler = ModuleHandler(ModuleConfig(this, shared, true, gamepadyn))
s
        // Configuration
        val p0 = gamepadyn.players[0]
        val p1 = gamepadyn.players[1]
        p0.configuration = GamepadConfig.player0
        p1.configuration = GamepadConfig.player1

        moduleHandler.init()

        val claw = moduleHandler.claw
        val drive = moduleHandler.drive
        val droneLauncher = moduleHandler.droneLauncher
        val intake = moduleHandler.intake
        val lsd = moduleHandler.lsd
        val opticon = moduleHandler.opticon
        val trussel = moduleHandler.trussel
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

        waitForStart()

        while (opModeIsActive()) {
            // Intake
            // TODO: replace w/ gamepadyn (delay reason: I don't want to write another analog->digital transformer)
            if (gamepad1.left_trigger > 0.1 || gamepad2.left_trigger > 0.1) {
                intake.power = 0.65
            } else {
                intake.power = 0.0
            }

            // Move slides up
            if (gamepad1.dpad_up || gamepad2.dpad_up) {
                isSlideMovingUp = true
                if (isRightClawOpen || isLeftClawOpen) {
                    isRightClawOpen = false
                    isLeftClawOpen = false
                }
                sleep(300)
                slidePos = 400
                sleep(200)
                isArmDown = false
                slidePos = 1565
                isSlideMovingUp = false
            }
            // Move slides down
            if (gamepad1.dpad_down || gamepad2.dpad_down) {
                if (!isArmDown) {
                    isArmDown = true
                    // used to be 100
                    sleep(200)
                    slidePos = 0
                } else {
                    slidePos = 0
                }
            }
            // TODO:
            if ((gamepad1.dpad_left || gamepad2.dpad_left) && slidePos > 199) {
                if (slidePos == 600 && !isArmDown) {
                    isArmDown = true
                    // used to be 100
                    sleep(200)
                    slidePos = 0
                } else {
                    slidePos -= 200
                }
            }
            if ((gamepad1.dpad_right || gamepad2.dpad_right) && slidePos < 1501) {
                if (isRightClawOpen || isLeftClawOpen) {
                    isRightClawOpen = false
                    isLeftClawOpen = false
                    sleep(300)
                }
                if (slidePos == 0) {
                    slidePos = 600
                    isSlideMovingUp = true
                } else {
                    slidePos += 200
                }
            }

            // TODO: what does this code do?? why is it here
            if (slidePos > 500 && isSlideMovingUp) {
                isArmDown = false
                isSlideMovingUp = false
            }

            // Claw
            if (gamepad1.x || gamepad2.x) {
                // Close
                isLeftClawOpen = false
                isRightClawOpen = false
            } else if (gamepad1.right_trigger > 0.1 || gamepad2.right_trigger > 0.1) {
                // Open
                if (!isRightClawOpen || !isLeftClawOpen) {
                    isLeftClawOpen = true
                    isRightClawOpen = true
                    sleep(200)
                }
                if (!isArmDown) {
                    isArmDown = true
                    sleep(200)
                    slidePos = 0
                } else {
                    slidePos = 0
                }
            }

            updateTruss()


            // Driver 2 Override
            if (abs(gamepad2.left_stick_y) > 0.1) {
                intake.power = gamepad2.left_stick_y.toDouble().stickCurve()
            }

            //Drone Launch
            if (gamepad1.b || gamepad2.b) drone.position = 0.8

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
    }
}