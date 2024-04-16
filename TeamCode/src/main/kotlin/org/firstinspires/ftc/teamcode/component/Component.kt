package org.firstinspires.ftc.teamcode.component

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.BotShared
import org.firstinspires.ftc.teamcode.isOpModeTeleOp
import kotlin.reflect.KClass

abstract class Component(private val manager: ComponentManager) {

    protected val opMode: OpMode = manager.opMode
    protected val hardwareMap: HardwareMap = manager.opMode.hardwareMap
    protected val telemetry: Telemetry = manager.opMode.telemetry
    protected val isTeleOp = manager.isTeleOp
    protected val shared = manager.shared

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

    abstract val status: Status

    protected fun <T> getHardware(deviceName: String, deviceType: Class<T>): T? = hardwareMap.tryGet(deviceType, deviceName)
    protected inline fun <reified T> getHardware(deviceName: String): T? = getHardware(deviceName, T::class.java)

    protected fun log(line: String) {
        opMode.telemetry.addLine("[${this::class.simpleName}] $line");
    }

    open fun update() {}
    open fun start() {}
    open fun stop() {}

    class ComponentManager(val opMode: OpMode, val shared: BotShared) {
        @JvmSynthetic
        internal val components = mutableSetOf<Component>()

        // reflection to try and figure out if we're autonomous or teleop.
        // if we can't figure it out, assume we're in teleop.
        val isTeleOp: Boolean = isOpModeTeleOp(opMode)

        fun update() {
            for (component in components) {
                component.update()
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