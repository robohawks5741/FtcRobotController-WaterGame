package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.teamcode.Action

abstract class BotModule(
    protected val opMode: OpMode,
    protected val isTeleOp: Boolean,
    protected val gamepadyn: Gamepadyn<Action>? = null
) {
    open fun modStart() {}
    open fun modUpdate() {}
}
