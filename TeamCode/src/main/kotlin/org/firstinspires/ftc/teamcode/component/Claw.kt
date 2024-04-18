package org.firstinspires.ftc.teamcode.component

import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo

class Claw(manager: ComponentManager) : Component(manager) {
    private val claw: Servo? = getHardware("claw")

    override val status: Status

    var isOpen: Boolean = true
        set(wantsOpen) {
            field = wantsOpen
            claw?.position = if (field) 1.0 else 0.0
        }

    init {
        isOpen = true

        val functionality = when {
            claw == null -> Functionality.NONE
            else -> Functionality.FULL
        }
        val hardwareSet: Set<HardwareUsage> = setOf(
            HardwareUsage("claw", Servo::class, claw != null)
        )
        status = Status(
            functionality,
            hardwareSet
        )
    }
}