package org.firstinspires.ftc.teamcode.component

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.flip
import org.firstinspires.ftc.teamcode.isOpModeTeleOp
import org.firstinspires.ftc.teamcode.isOpModeTeleOp
import kotlin.reflect.KClass

/**
 * A discrete code module, a part of the robot.
 * This system allows for spreading out the code across files,
 * and re-using features.
 *
 * To use a component, create an instance of it, provide a reference to a [ComponentManager]
 * (of which should be shared by all components that you use).
 *
 * @author Addie
 */
abstract class Component(
    /**
     * The [ComponentManager] that is responsible for handling this component.
     */
    private val manager: ComponentManager
) {

    /**
     * The [OpMode] that this component is being used with.
     */
    protected val opMode: OpMode = manager.opMode

    /**
     * The [HardwareMap] associated with [opMode], available here for convenience.
     */
    protected val hardwareMap: HardwareMap = opMode.hardwareMap

    /**
     * The [Telemetry] instance associated with [opMode], available here for convenience.
     */
    protected val telemetry: Telemetry = opMode.telemetry

    /**
     * Whether or not the [OpMode] that is using this component is running as [TeleOp] or [Autonomous].
     * This is determined by sloppy heuristics; see [isOpModeTeleOp]
     */
    protected val isTeleOp = manager.isTeleOp

    /**
     * The shared data used and provided by [opMode]
     */
    protected val shared = manager.shared

    /**
     * @param deviceName The name of the device, as passed to [HardwareMap.get]
     * @param deviceType The class of the device, as passed to [HardwareMap.get]
     * @param isPresent Whether or not the device was actually found
     * @param isRequired Whether or not the device is required for the component to function
     */
    data class HardwareUsage(
        val deviceName: String,
        val deviceType: KClass<*>,
        val isPresent: Boolean,
        val isRequired: Boolean = true
    )

    enum class Functionality {
        FULL,
        PARTIAL,
        NONE
    }

    data class Status(
        val functionality: Functionality,
        val hardware: Set<HardwareUsage>,
        val description: String? = null
    )

    /**
     * The current status of this component.
     * The [ComponentManager] uses this to provide debug information.
     */
    abstract val status: Status

    /**
     * Attempts to find a hardware device connected to the robot.
     * @return a matching hardware device, or `null` if none could be found.
     */
    protected fun <T> getHardware(deviceName: String, deviceType: Class<T>): T? = hardwareMap.tryGet(deviceType, deviceName)
    /**
     * Attempts to find a hardware device connected to the robot.
     * @return a matching hardware device, or `null` if none could be found.
     */
    protected inline fun <reified T> getHardware(deviceName: String): T? = getHardware(deviceName, T::class.java)

    protected fun log(line: String) {
        opMode.telemetry.addLine("[${this::class.simpleName}] $line")
    }

    /**
     * @see [OpMode.loop]
     */
    open fun loop() {}

    /**
     * @see [OpMode.start]
     */
    open fun start() {}

    /**
     * @see [OpMode.stop]
     */
    open fun stop() {}

    /**
     * Used to manage
     * @author Addie
     */
    class ComponentManager(val opMode: OpMode, val shared: BotShared) {
        @JvmSynthetic
        internal val components = mutableSetOf<Component>()

        // reflection to try and figure out if we're autonomous or teleop.
        // if we can't figure it out, assume we're in teleop.
        val isTeleOp: Boolean = isOpModeTeleOp(opMode)

        fun start() {
            for (component in components) {
                component.start()
            }
        }

        fun update() {
            for (component in components) {
                if (component.status.functionality != Functionality.FULL) {
                    opMode.telemetry.addLine(
                        "[${if (component.status.functionality == Functionality.NONE) "ERROR" else "WARNING"}] Component \"${component::class.simpleName}\" is not fully functional!" +
                        "\nDetails: ${component.status.hardware.joinToString() }}"
                    )
                }
                component.loop()
            }
        }

        fun stop() {
            for (component in components) {
                component.stop()
            }
        }

    }

    init {
        // it's probably fine :)
        @Suppress("LeakingThis")
        manager.components.add(this)
    }

    // power mode (low power)
    // status (usable)


}