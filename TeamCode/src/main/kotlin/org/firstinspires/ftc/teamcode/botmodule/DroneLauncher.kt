package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.idc

class DroneLauncher(cfg: ModuleConfig) : BotModule(cfg) {
    private val launchServo: Servo?  = idc { hardwareMap[Servo   ::class.java,   "drone" ] }

    override fun modStart() {
        launchServo?.position = 0.0
    }

    fun launch() {
        launchServo?.position = 1.0
    }

//    public fun launchAsync(launchWithoutArming: Boolean?): Boolean { }
}