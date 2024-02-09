package org.firstinspires.ftc.teamcode.botmodule

@Suppress("MemberVisibilityCanBePrivate")
class ModuleHandler(
    private val config: ModuleConfig
) {
//    private lateinit var modules: Set<BotModule>
    private var hasStarted = false

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
     * STATUS: UNKOWN
     * TODO: test
     */
    lateinit var trussle: Trussle private set

    /**
     * STATUS: NOT WORKING
     * TODO:
     */

    val modules
        get() = setOf(
            drive,
            lsd,
            claw,
            opticon,
            intake,
            trussle
        )

    fun init() {
        drive = Drive(config)
        lsd = LSD(config)
        claw = Claw(config)
        opticon = Opticon(config)
        intake = Intake(config)
        trussle = Trussle(config)
    }

    fun start() {
        if (!hasStarted) hasStarted = true
        else return
        for (module in modules) {
//            config.opMode.telemetry.addLine("start module ${module::class.simpleName}")
            module.modStart()
            if (config.isTeleOp) {
                module.modStartTeleOp()
//                config.opMode.telemetry.addLine("TELEOP start module ${module::class.simpleName}")
            }
        }
    }

    fun update() {
        for (module in modules) {
            module.modUpdate()
//            config.opMode.telemetry.addLine("update module ${module::class.simpleName}")
            if (config.isTeleOp) {
//                config.opMode.telemetry.addLine("TELEOP update module ${module::class.simpleName}")
                module.modUpdateTeleOp()
            }
        }
        for (module in modules) {
            if (module.status.status != BotModule.StatusEnum.OK) {
                val hmissing = module.status.hardwareMissing?.joinToString("\", \"", "[ \"", "\" ]")
//                config.opMode.telemetry.addLine("MODULE NOT OK: \"${module::class.simpleName}\" is missing hardware: $hmissing")
            }
        }
    }

    fun stop() {
        for (module in modules) {
            module.modStop()
//            if (config.isTeleOp) module.modStopTeleOp()
        }
    }
}