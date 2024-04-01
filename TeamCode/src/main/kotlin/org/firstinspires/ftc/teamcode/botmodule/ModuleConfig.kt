package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.teamcode.ActionAnalog1
import org.firstinspires.ftc.teamcode.ActionAnalog2
import org.firstinspires.ftc.teamcode.ActionDigital
import org.firstinspires.ftc.teamcode.Alliance
import org.firstinspires.ftc.teamcode.AllianceSide
import org.firstinspires.ftc.teamcode.BotShared

data class ModuleConfig @JvmOverloads constructor(
    val alliance: Alliance? = null,
    val side: AllianceSide? = null,
    val opMode: OpMode,
    val shared: BotShared,
    val parent: ModuleHandler,
    val isTeleOp: Boolean,
    val gamepadyn: Gamepadyn<ActionDigital, ActionAnalog1, ActionAnalog2>? = null
)