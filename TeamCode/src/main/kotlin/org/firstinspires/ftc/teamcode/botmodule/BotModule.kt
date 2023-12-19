package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode

abstract class BotModule(
    protected val opMode: OpMode,
) {
	  abstract fun modInit()
    abstract fun modStart()
    abstract fun modUpdate()
    open fun <T: Enum<T>> bindTeleOp(gamepadyn: Gamepadyn<T>) {}
}
