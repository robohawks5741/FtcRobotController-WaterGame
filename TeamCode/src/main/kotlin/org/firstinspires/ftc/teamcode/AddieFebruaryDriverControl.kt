package org.firstinspires.ftc.teamcode

import android.util.Log
import com.acmerobotics.roadrunner.Pose2d
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
import org.firstinspires.ftc.teamcode.botmodule.LSD
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import org.firstinspires.ftc.teamcode.botmodule.ModuleHandler
import kotlin.math.abs

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

    private lateinit var drone: Servo
    private lateinit var imu: IMU

    private var driverRelative = true

    /**
     * Both players must be holding down the drone launch button in order to launch the drone.
     */
    private var droneTurnKey0 = false
    private var droneTurnKey1 = false

    override fun runOpMode() {
        drone   = hardwareMap!![Servo::class.java, "drone"]
        imu     = hardwareMap!![IMU::class.java, "imu"]

        shared = BotShared(this)

        // initial drone position
        drone.position = 1.0

        // MAKE SURE THAT SHARED IS INITIALIZED BEFORE THIS!!!
        shared.rr = MecanumDrive(hardwareMap, BotShared.storedPose)
        moduleHandler = ModuleHandler(this, shared, isTeleOp = true, gamepadyn)

        // Configuration
        val p0 = gamepadyn.getPlayer(0)!!
        val p1 = gamepadyn.getPlayer(1)!!
        p0.configuration =
        p1.configuration =

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
                lsd.targetHeight = LSD.HEIGHT_MAX
            }
        }
        gamepadyn.addListener(MACRO_SLIDE_DOWN) {
            if (it.data()) {
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
                lsd.targetHeight = LSD.HEIGHT_MIN
            }
        }

        // MOD START
        moduleHandler.start()

        telemetry.update()

        while (opModeIsActive()) {
            BotShared.wasLastOpModeAutonomous = false
            gamepadyn.update()

            // MOD UPDATE
            moduleHandler.update()

            // TODO: patch this in
//            // Driver 2 Override
//            if (abs(gamepad2.left_stick_y) > 0.1) {
//                intake.power = gamepad2.left_stick_y.toDouble().stickCurve()
//            }

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