package org.firstinspires.ftc.teamcode

import computer.living.gamepadyn.ActionEnumAnalog1
import computer.living.gamepadyn.ActionEnumAnalog2
import computer.living.gamepadyn.ActionEnumDigital

enum class ActionDigital : ActionEnumDigital {
    TOGGLE_DRIVER_RELATIVITY,
    LAUNCH_DRONE,
    H_SLIDE_EXTEND,
    H_SLIDE_RETRACT,
    V_SLIDE_EXTEND,
    V_SLIDE_RETRACT,
}

enum class ActionAnalog1 : ActionEnumAnalog1 {
    ROTATION,
    H_SLIDE_EXTEND_MANUAL,
    H_SLIDE_RETRACT_MANUAL,
}

enum class ActionAnalog2 : ActionEnumAnalog2 {
    MOVEMENT
}