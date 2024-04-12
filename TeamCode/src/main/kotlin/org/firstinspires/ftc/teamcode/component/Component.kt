package org.firstinspires.ftc.teamcode.component

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap

abstract class Component(manager: ComponentManager) {

    protected val opMode: OpMode
    protected val hardwareMap: HardwareMap

    class ComponentManager(val opMode: OpMode) {
    }

    // power mode (low power)
    // status (usable)



    init {
        opMode = manager.opMode
        hardwareMap = manager.opMode.hardwareMap
    }
}