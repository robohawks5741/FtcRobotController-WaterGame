package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
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

    protected abstract fun start()
    protected abstract fun update()
    protected abstract fun stop()

    abstract class OpModeHost<T: UltraOpMode>(private val clazz: KClass<T>) : LinearOpMode() {
        // "hostee" doesn't really make much sense, but it gets the job done
        @Suppress("SpellCheckingInspection")
        private lateinit var hostee: T

        override fun runOpMode() {
            val hostedClazz = clazz//this@UltraOpMode::class
            hostee = hostedClazz.primaryConstructor!!.call(this@OpModeHost)
            waitForStart()
            if (isStarted) {
                hostee.start()
                while (opModeIsActive()) {
                    hostee.componentManager.update()
                    hostee.update()
                }
                hostee.stop()
            }
            BotShared.wasLastOpModeAutonomous = isOpModeTeleOp(this)
        }
    }
}

//class MyHost : UltraOpMode.Host<MyUltraOpMode>(MyUltraOpMode::class)
//class MyUltraOpMode(host: Host<MyUltraOpMode>) : UltraOpMode(host)