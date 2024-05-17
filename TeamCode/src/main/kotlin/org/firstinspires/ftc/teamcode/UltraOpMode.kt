package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.component.Component
import org.firstinspires.ftc.teamcode.component.Component.ComponentManager
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * This is a custom [OpMode] class that removes many annoying parts of the SDK's original class.
 * Many of the SDK class's problems are architectural, so this class does NOT extend [OpMode]!
 *
 * A blank example of how to use it:
 *
 * ```kotlin
 * class MyUltraOpMode(host: Host<MyUltraOpMode>) : UltraOpMode(host) {
 *     @TeleOp(name = "MyUltraOpMode")
 *     class Host : OpModeHost<MyUltraOpMode>(MyUltraOpMode::class)
 *     // ... implement the loop/start/stop functions here
 * }
 * ```
 *
 * @constructor You should never need to use this constructor manually; see the example above.
 *
 * @author Addie
 */
abstract class UltraOpMode(private val host: OpModeHost<*>) {
    val shared = BotShared(host)

    /**
     * A pre-made [ComponentManager] to handle using Addie's Component System.
     * @see [Component]
     */
    val componentManager = ComponentManager(this.host, shared)
    protected val telemetry: Telemetry = host.telemetry

    /**
     * Attempts to find a hardware device connected to the robot.
     * @return a matching hardware device, or `null` if none could be found.
     */
    protected fun <T> getHardware(deviceName: String, deviceType: Class<T>): T? = host.hardwareMap.tryGet(deviceType, deviceName)
    /**
     * Attempts to find a hardware device connected to the robot.
     * @return a matching hardware device, or `null` if none could be found.
     */
    protected inline fun <reified T> getHardware(deviceName: String): T? = getHardware(deviceName, T::class.java)

    /**
     * User-defined loop method.
     * This method will be called repeatedly during the period between when the
     * play button is pressed and when the OpMode is stopped.
     *
     * There is some delay between iterations of the loop in order to share the CPU.
     */
    protected open fun loop() {}

    /**
     * User-defined start method.
     * This method will be called ONCE, when the play button is pressed.
     *
     * Example usage: Starting another thread.
     */
    protected open fun start() { }

    /**
     * User-defined stop method.
     * This method will be called ONCE, when this OpMode is stopped.
     *
     * Your ability to control hardware from this method will be limited.
     */
    protected open fun stop() { }

    /**
     * A host of an [UltraOpMode]. You MUST create a subclass of OpModeHost for every [UltraOpMode]!
     * And don't forget to annotate the OpModeHost!
     * This is similar in concept to the [DemoSystem]'s OpMode "emulation."
     */
    abstract class OpModeHost<T: UltraOpMode>(private val clazz: KClass<T>) : OpMode() {
        // "hostee" doesn't really make much sense, but it gets the job done
        @Suppress("SpellCheckingInspection")
        private lateinit var hostee: T

        override fun init() {
            val hostedClazz = clazz//this@UltraOpMode::class
            // reflection silliness
            hostee = hostedClazz.primaryConstructor!!.call(this@OpModeHost)
        }

        override fun stop() {
            hostee.stop()
            hostee.componentManager.stop()
            BotShared.wasLastOpModeAutonomous = isOpModeTeleOp(this)
        }

        override fun loop() {
            hostee.componentManager.update()
            hostee.loop()
        }

        override fun start() {
            hostee.componentManager.start()
            hostee.start()
        }
    }
}