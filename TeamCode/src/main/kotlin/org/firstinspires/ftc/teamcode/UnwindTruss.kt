package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.BotShared

@TeleOp(name = "Unwind Truss")
class UnwindTruss : OpMode() {

    private lateinit var shared: BotShared

    override fun init() {
        shared = BotShared(this)
    }

    override fun start() {
        super.start()
    }

    override fun loop() {
        shared.update()
    }
}