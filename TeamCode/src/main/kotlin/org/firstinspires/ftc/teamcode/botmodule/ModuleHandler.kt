package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.GamepadynRH

@Suppress("MemberVisibilityCanBePrivate")
class ModuleHandler(
    opMode: OpMode,
    shared: BotShared,
    isTeleOp: Boolean,
    gamepadyn: GamepadynRH? = null
) {
    private val config: ModuleConfig = ModuleConfig(
        opMode,
        shared,
        this,
        isTeleOp,
        gamepadyn
    )

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
     * STATUS: UNKNOWN
     * TODO: test
     */
    lateinit var trussle: Trussle private set

    lateinit var samFin: SamFin private set

    val modules
        get() = setOf(
            drive,
            lsd,
            claw,
            opticon,
            intake,
            trussle,
            samFin
        )

    fun init() {
        drive = Drive(config)
        lsd = LSD(config)
        claw = Claw(config)
        opticon = Opticon(config)
        intake = Intake(config)
        trussle = Trussle(config)
        samFin = SamFin(config)
    }

    fun start() {
        if (!hasStarted) hasStarted = true
        else return
        for (module in modules) {
            module.modStart()
        }
    }

    fun update() {
        for (module in modules) {
            module.modUpdate()
        }
        for (module in modules) {
            if (module.status.status != BotModule.StatusEnum.OK) {
                val missingString = module.status.hardwareMissing?.joinToString("\", \"", "[ \"", "\" ]")
                config.opMode.telemetry.addLine("MODULE NOT OK: \"${module::class.simpleName}\" is missing hardware: $missingString")
            }
        }

        val voltageSensor = config.opMode.hardwareMap.voltageSensor
        for (sensor in voltageSensor) {
            config.opMode.telemetry.addData("(voltage sensor ${sensor.deviceName}) voltage", sensor.voltage)
        }

    }

    fun stop() {
        for (module in modules) {
            module.modStop()
//            if (config.isTeleOp) module.modStopTeleOp()
        }
    }
}