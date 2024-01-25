package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareMap
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.*

data class ModuleConfig(
    val opMode: OpMode,
    val shared: BotShared,
    val isTeleOp: Boolean,
    val gamepadyn: Gamepadyn<ActionDigital, ActionAnalog1, ActionAnalog2>? = null
)

abstract class BotModule protected constructor(config: ModuleConfig) {
    protected val opMode: OpMode = config.opMode
    protected val hardwareMap: HardwareMap = config.opMode.hardwareMap
    protected val telemetry: Telemetry = config.opMode.telemetry
    protected val isTeleOp = config.isTeleOp
    protected val gamepadyn = config.gamepadyn
    protected val shared = config.shared
    protected val imu = shared.imu
    protected val camera = shared.camera

    protected val gamepad1: Gamepad
        get() = opMode.gamepad1
    protected val gamepad2: Gamepad
        get() = opMode.gamepad2

    enum class StatusEnum {
        OK,
        MISSING_HARDWARE
    }

    data class Status(
        val status: StatusEnum,
        val hardwareUsed: Set<HardwareDevice>? = setOf(),
        val hardwareMissing: Set<String>? = null
    )

    var status: Status = Status(StatusEnum.OK)
        protected set

    open fun modStart() { }
    open fun modUpdate() { }

}
