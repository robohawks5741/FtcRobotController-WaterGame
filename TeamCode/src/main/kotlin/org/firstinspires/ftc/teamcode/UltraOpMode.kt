package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.component.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

abstract class UltraOpMode /*private constructor*/(protected val host: OpModeHost<*>) {
    val shared = BotShared(host)
    val componentManager = Component.ComponentManager(this.host, shared)
    protected val telemetry: Telemetry = host.telemetry

    protected fun <T> getHardware(deviceName: String, deviceType: Class<T>): T? = host.hardwareMap.tryGet(deviceType, deviceName)
    protected inline fun <reified T> getHardware(deviceName: String): T? = getHardware(deviceName, T::class.java)

    protected abstract fun loop()
    protected open fun start() { }
    protected open fun stop() { }

    abstract class OpModeHost<T: UltraOpMode>(private val clazz: KClass<T>) : OpMode() {
        // "hostee" doesn't really make much sense, but it gets the job done
        @Suppress("SpellCheckingInspection")
        private lateinit var hostee: T

        override fun init() {
            val hostedClazz = clazz//this@UltraOpMode::class
            hostee = hostedClazz.primaryConstructor!!.call(this@OpModeHost)
        }

        override fun stop() {
            hostee.stop()
            BotShared.wasLastOpModeAutonomous = isOpModeTeleOp(this)
        }

        override fun loop() {
            hostee.componentManager.update()
            hostee.loop()
        }

        override fun start() {
            hostee.start()
        }
    }
}

//class MyHost : UltraOpMode.Host<MyUltraOpMode>(MyUltraOpMode::class)
//class MyUltraOpMode(host: Host<MyUltraOpMode>) : UltraOpMode(host)