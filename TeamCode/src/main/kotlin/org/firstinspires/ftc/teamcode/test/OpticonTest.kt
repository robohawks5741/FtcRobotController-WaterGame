package org.firstinspires.ftc.teamcode.test

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.acmerobotics.roadrunner.Pose2d
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import org.firstinspires.ftc.teamcode.botmodule.ModuleHandler
import org.firstinspires.ftc.teamcode.botmodule.Opticon
import kotlin.math.roundToInt

@TeleOp
class OpticonTest : OpMode() {
    private lateinit var shared: BotShared
    private lateinit var moduleHandler: ModuleHandler

    val beginPose = Pose2d(0.0, 0.0, 0.0)

    override fun init() {
        shared = BotShared(this)
        moduleHandler = ModuleHandler(this, shared, true)
    }

    override fun start() {
        moduleHandler.start()
    }

    override fun loop() {
        moduleHandler.update()

        val tfod = moduleHandler.opticon.tfod!!

        telemetry.addLine("recognition count: ${tfod.recognitions.size}")
        for (element in tfod.recognitions) {
            telemetry.addLine("element \"${element.label}\" (confidence ${element.confidence * 100.0}%")
        }
        telemetry.addLine("searching for recognition (${runtime.roundToInt()} seconds elapsed)")
        telemetry.update()

        Thread.yield()
    }
}