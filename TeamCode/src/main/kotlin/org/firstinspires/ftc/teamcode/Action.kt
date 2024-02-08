package org.firstinspires.ftc.teamcode

import computer.living.gamepadyn.ActionEnumDigital
import computer.living.gamepadyn.ActionEnumAnalog1
import computer.living.gamepadyn.ActionEnumAnalog2

enum class ActionDigital : ActionEnumDigital {
    TOGGLE_DRIVER_RELATIVITY,   // toggle
    TOGGLE_INTAKE_HEIGHT,       // toggle

    TRUSS_CYCLE,
    DRONE_LAUNCH,               // one-shot
    SLIDE_UP,
    CLAW_LEFT_OPEN,
    CLAW_LEFT_CLOSE,
    CLAW_RIGHT_OPEN,
    CLAW_RIGHT_CLOSE,


    CUSTOM_ACTION_DIGITAL_A,
    CUSTOM_ACTION_DIGITAL_B,
    CUSTOM_ACTION_DIGITAL_X,
    CUSTOM_ACTION_DIGITAL_Y,
}

enum class ActionAnalog1 : ActionEnumAnalog1 {
    INTAKE_SPIN,                // X = spin power (+inwards, -outwards)
    ROTATION,                   // X = clockwise yaw       
    CLAW,                       // X = open..closed

    TRUSS_PULL,                 // hold it down (+1 = pull, -1 = release)

    CUSTOM_ACTION_ANALOG_A,
    CUSTOM_ACTION_ANALOG_B,
    CUSTOM_ACTION_ANALOG_X,
    CUSTOM_ACTION_ANALOG_Y,
}

enum class ActionAnalog2 : ActionEnumAnalog2 {
    MOVEMENT                    // Y = run, X = strafe
}