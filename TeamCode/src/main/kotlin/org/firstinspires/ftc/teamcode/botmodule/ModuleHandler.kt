package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode

class ModuleHandler(
    private val config: ModuleConfig
) {
//    private lateinit var modules: Set<BotModule>
    private var hasStarted = false

    val modules
        get() = setOf(
            drive,
            lsd,
            claw,
            march,
            intake,
            droneLauncher
        )

    lateinit var drive: Drive private set
    lateinit var lsd: LSD private set
    lateinit var claw: Claw private set
    lateinit var march: March private set
    lateinit var intake: Intake private set
    lateinit var droneLauncher: DroneLauncher private set

    fun init() {
        drive = Drive(config)
        lsd = LSD(config)
        claw = Claw(config)
        march = March(config)
        intake = Intake(config)
        droneLauncher = DroneLauncher(config)
    }

    fun start() {
        if (!hasStarted) hasStarted = true else return
        for (module in modules) module.modStart()
    }

    fun update() {
        for (module in modules) module.modUpdate()
    }
}