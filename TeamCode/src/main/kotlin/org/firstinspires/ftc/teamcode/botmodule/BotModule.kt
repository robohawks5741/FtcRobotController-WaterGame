package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.teamcode.Action
import org.firstinspires.ftc.teamcode.BotShared

abstract class BotModule(
    protected val shared: BotShared,
    protected val isTeleOp: Boolean
) {
    abstract fun modInit() {}
    abstract fun modStart() {}
    abstract fun modUpdate() {}
}
