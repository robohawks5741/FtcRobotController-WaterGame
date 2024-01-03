package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode

class ModuleHandler(
    private val opMode: OpMode,
    vararg mods: BotModule
) {
    private val modules = mods.toSet()
    private var hasStarted = false

    fun start() {
        if (!hasStarted) hasStarted = true else return
        for (module in modules) module.modStart()
    }

    fun update() {
        for (module in modules) module.modUpdate()
    }
}