package org.firstinspires.ftc.teamcode.component

import com.qualcomm.robotcore.hardware.Servo

class Wrist(manager: ComponentManager) : Component(manager) {
    private val wrist: Servo? = getHardware("claw")

    override val status: Status

    var position: Double = 0.0
        set(pos) {
            field = pos.coerceIn(0.0..1.0)
            wrist?.position = field
        }

    fun extend() {
        position = 1.0
    }

    fun retract() {
        position = 0.0
    }

    override fun loop() {
        if (status.functionality != Functionality.NONE) {
            telemetry.addLine("Wrist Position: $position (0.0 retracted -> 1.0 extended)")
        }
    }

    init {
        position = 0.0

        val functionality = if (wrist == null) Functionality.NONE else Functionality.FULL
        val hardwareSet: Set<HardwareUsage> = setOf(
            HardwareUsage("wrist", Servo::class, wrist != null)
        )
        status = Status(
            functionality,
            hardwareSet
        )
    }
}