package org.firstinspires.ftc.teamcode.test

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.botmodule.LSD
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import kotlin.math.roundToInt

@TeleOp
class SlideCalibrator : OpMode() {
    val shared = BotShared(this)
    private lateinit var lsd: LSD

    override fun start() {
        lsd.modStart()
    }
    override fun init() {
        lsd = LSD(ModuleConfig(this, shared, true, null))
    }

    override fun loop() {
        lsd.targetHeight += (-gamepad1.left_stick_y * 200f).roundToInt()
        lsd.modUpdate()
        telemetry.addData("Slide Height", lsd.targetHeight)
        telemetry.update()
    }

}