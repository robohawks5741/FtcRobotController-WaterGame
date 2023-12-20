package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.teamcode.Action

class DroneLauncher(private var launchServo: Servo, opMode: OpMode, isTeleOp: Boolean, gamepadyn: Gamepadyn<Action>?) : BotModule(opMode, isTeleOp, gamepadyn) {

    init {
        launchServo.position = 0.0
    }

    fun launch() {
        launchServo.position = 1.0
    }

//    public fun launchAsync(launchWithoutArming: Boolean?): Boolean { }
}