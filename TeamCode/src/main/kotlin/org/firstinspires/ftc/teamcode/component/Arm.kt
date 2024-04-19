package org.firstinspires.ftc.teamcode.component

import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.ActionAnalog1

class Arm(manager: ComponentManager) : Component(manager) {
    private val arm: Servo? = getHardware("arm")

    override val status: Status

    var position: Double = 0.0
        set(pos) {
            field = pos.coerceIn(0.0..1.0)
            arm?.position = field
        }

    fun extend() {
        position = 1.0
    }

    fun retract() {
        position = 0.0
    }

    override fun loop() {
        if (status.functionality != Functionality.NONE) {
            telemetry.addLine("Arm Position: $position (0.0 retracted -> 1.0 extended)")
        }
    }

    init {
        position = 0.0

        val functionality = when {
            arm == null -> Functionality.NONE
            else -> Functionality.FULL
        }
        val hardwareSet: Set<HardwareUsage> = setOf(
            HardwareUsage("arm", Servo::class, arm != null)
        )
        status = Status(
            functionality,
            hardwareSet
        )
    }
}