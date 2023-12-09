package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@TeleOp
class ResetServos : OpMode() {

    private lateinit var shared: BotShared

    override fun init() {
        shared = BotShared(this)
    }

    override fun start() {
        shared.servoTrussLeft?.position = 0.0
        shared.servoTrussRight?.position = 0.0
        shared.servoArmLeft?.position = 0.0
    }
    override fun loop() { }

}
