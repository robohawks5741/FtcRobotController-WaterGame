package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareMap
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.teamcode.ActionAnalog1
import org.firstinspires.ftc.teamcode.ActionAnalog2
import org.firstinspires.ftc.teamcode.ActionDigital

/**
 * An abstract representation of an individual feature set of the greater robot.
 * Provides an API for use with Autonomous or for complex macros, and handles TeleOp control.
 *
 * Note that there is no `modInit()` function, because modules are only ever constructed during the init phase of an OpMode.
 * Feel free to call [HardwareMap.get] in member initialization.
 */
abstract class BotModule protected constructor(val config: ModuleConfig) {
    protected val shared = config.shared

    /**
     * The parent OpMode.
     */
    protected val opMode: OpMode = config.opMode

    /**
     * The hardware map of the parent OpMode.
     */
    protected val hardwareMap: HardwareMap = config.opMode.hardwareMap!!

    /**
     * The telemetry instance of the parent OpMode.
     */
    protected val telemetry: Telemetry = config.opMode.telemetry

    /**
     * A gamepadyn instance, if present.
     */
    protected val gamepadyn: Gamepadyn<ActionDigital, ActionAnalog1, ActionAnalog2>? = config.gamepadyn

    /**
     * The internal Inertial Measurement Unit (IMU) of the control hub.
     */
    protected val imu = shared.imu

    /**
     * The webcam connected to the robot, if present.
     */
    protected val camera: WebcamName? = shared.camera

    enum class StatusEnum {
        OK,
        BAD
    }

    data class Status(
        val status: StatusEnum,
        val hardwareUsed: Set<HardwareDevice>? = setOf(),
        val hardwareMissing: Set<String>? = null
    )

    var status: Status = Status(StatusEnum.OK)
        @Suppress("EmptyMethod")
        protected set

    /**
     * Run once when the OpMode is started.
     */
    open fun modStart() {}

    /**
     * Run after [modStart], but only if the OpMode is TeleOp.
     */
    open fun modStartTeleOp() {}

    /**
     * Run every loop of the parent OpMode.
     */
    open fun modUpdate() {}

    /**
     * Run after [modUpdate], but only if the OpMode is TeleOp.
     */
    open fun modUpdateTeleOp() {}

    /**
     * Run when the OpMode is stopped.
     */
    open fun modStop() {}

}
