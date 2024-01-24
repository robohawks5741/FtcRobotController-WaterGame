package org.firstinspires.ftc.teamcode.test

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.botmodule.BotModule
import org.firstinspires.ftc.teamcode.botmodule.BotModule.StatusEnum.OK
import org.firstinspires.ftc.teamcode.botmodule.LSD
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import kotlin.math.roundToInt

@TeleOp
class SlideCalibrator : OpMode() {
    private lateinit var shared: BotShared
    private lateinit var lsd: LSD


    override fun init() {
        shared = BotShared(this)
        lsd = LSD(ModuleConfig(this, shared, true, null))
    }

    override fun start() {
        lsd.modStart()
    }

    override fun loop() {
        if (lsd.status.status == OK) {
            lsd.targetHeight += (-gamepad1.left_stick_y * 200f).roundToInt()
            lsd.modUpdate()
            telemetry.addData("Slide Height", lsd.targetHeight)
        } else {
            telemetry.addLine("lsd is not ok :(");
            telemetry.addLine(lsd.status.hardwareMissing?.joinToString());
        }
        telemetry.update()
    }

}