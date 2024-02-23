package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.teamcode.ActionAnalog1
import org.firstinspires.ftc.teamcode.ActionAnalog2
import org.firstinspires.ftc.teamcode.ActionDigital
import org.firstinspires.ftc.teamcode.BotShared

data class ModuleConfig(
    val opMode: OpMode,
    val shared: BotShared,
    val parent: ModuleHandler,
    val isTeleOp: Boolean,
    val gamepadyn: Gamepadyn<ActionDigital, ActionAnalog1, ActionAnalog2>? = null
)