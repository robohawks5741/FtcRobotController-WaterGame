package org.firstinspires.ftc.teamcode.test

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.ftc.InputBackendFtc
import org.firstinspires.ftc.teamcode.ActionAnalog1
import org.firstinspires.ftc.teamcode.ActionAnalog2
import org.firstinspires.ftc.teamcode.ActionDigital
import org.firstinspires.ftc.teamcode.Alliance
import org.firstinspires.ftc.teamcode.AllianceSide
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.MecanumDrive
import org.firstinspires.ftc.teamcode.SpikeMark
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import org.firstinspires.ftc.teamcode.botmodule.ModuleHandler
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import java.lang.Thread.sleep

@Autonomous(name = "# Module Check (Autonomous)")
class ModuleCheckAutonomous : OpMode() {

    private lateinit var shared: BotShared
    private lateinit var moduleHandler: ModuleHandler

    override fun init() {
        shared = BotShared(this)
        shared.rr = MecanumDrive(hardwareMap, Pose2d(0.0, 0.0, 0.0))
        moduleHandler = ModuleHandler(this, shared, isTeleOp = false, gamepadyn = null)
        moduleHandler.init()
    }

    override fun loop() {
        shared.rr?.updatePoseEstimate()
        moduleHandler.update()
    }

    override fun start() {
        moduleHandler.start()
    }

    override fun stop() {
        BotShared.storedPose = shared.rr?.pose!!
        moduleHandler.stop()
    }
}

@TeleOp(name = "# Module Check (TeleOp)")
class ModuleCheckTeleop : OpMode() {

    private val gamepadyn = Gamepadyn.create(
        ActionDigital::class,
        ActionAnalog1::class,
        ActionAnalog2::class,
        InputBackendFtc(this),
        strict =true,
    )

    private lateinit var shared: BotShared
    private lateinit var moduleHandler: ModuleHandler

    override fun init() {
        shared = BotShared(this)
        shared.rr = MecanumDrive(hardwareMap, Pose2d(0.0, 0.0, 0.0))
        moduleHandler = ModuleHandler(this, shared, isTeleOp = true, gamepadyn = gamepadyn)
        moduleHandler.init()
        telemetry.update()
    }

    override fun loop() {
        shared.rr?.updatePoseEstimate()
        moduleHandler.update()
        telemetry.update()
    }

    override fun start() {
        moduleHandler.start()
        telemetry.update()
    }

    override fun stop() {
        BotShared.storedPose = shared.rr?.pose!!
        moduleHandler.stop()
        telemetry.update()
    }
}