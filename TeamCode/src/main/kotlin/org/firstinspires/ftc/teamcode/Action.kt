package org.firstinspires.ftc.teamcode

import computer.living.gamepadyn.ActionEnumDigital
import computer.living.gamepadyn.ActionEnumAnalog1
import computer.living.gamepadyn.ActionEnumAnalog2

enum class ActionDigital : ActionEnumDigital {
    TOGGLE_DRIVER_RELATIVITY,   // toggle
    TOGGLE_INTAKE_HEIGHT,       // toggle
    TRUSS_HANG                  // one-shot
}
enum class ActionAnalog1 : ActionEnumAnalog1 {
    INTAKE_SPIN,                // X = spin power (+inwards, -outwards)
    ROTATION,                   // X = clockwise yaw       
    CLAW                        // X = open..closed
}




enum class ActionAnalog2 : ActionEnumAnalog2 {
    MOVEMENT                    // Y = run, X = strafe
}