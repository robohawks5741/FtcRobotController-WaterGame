package org.firstinspires.ftc.teamcode.botmodule

@Suppress("MemberVisibilityCanBePrivate")
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
            opticon,
            intake,
            droneLauncher
        )

    /**
     * STATUS: Working
     * TODO: replace IMU with RoadRunner
     */
    lateinit var drive: Drive private set

    /**
     * STATUS: UNKNOWN
     * TODO: test, implement with Gamepadyn
     */
    lateinit var lsd: LSD private set

    /**
     * STATUS: NOT WORKING
     * TODO:
     */
    lateinit var claw: Claw private set

    /**
     * STATUS: NOT WORKING
     * TODO:
     */
    lateinit var opticon: Opticon private set

    /**
     * STATUS: UNKNOWN
     * TODO: test
     */
    lateinit var intake: Intake private set

    /**
     * STATUS: NOT WORKING
     * TODO:
     */
    lateinit var droneLauncher: DroneLauncher private set

    fun init() {
        drive = Drive(config)
        lsd = LSD(config)
        claw = Claw(config)
        opticon = Opticon(config)
        intake = Intake(config)
        droneLauncher = DroneLauncher(config)
    }

    fun start() {
        if (!hasStarted) hasStarted = true else return
        for (module in modules) module.modStart()
    }

    fun update() {
        for (module in modules) module.modUpdate()
        for (module in modules) {
            if (module.status.status != BotModule.StatusEnum.OK) {
                val hmissing = module.status.hardwareMissing?.joinToString("\", \"", "[ \"", "\" ]")
                config.opMode.telemetry.addLine("MODULE NOT OK: \"${module::class.simpleName}\" is missing hardware: $hmissing")
            }
        }
    }

    fun stop() {
        for (module in modules) module.modStop()
    }
}