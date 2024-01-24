package org.firstinspires.ftc.teamcode

import computer.living.gamepadyn.ActionEnumDigital
import computer.living.gamepadyn.ActionEnumAnalog1
import computer.living.gamepadyn.ActionEnumAnalog2

enum class ActionDigital : ActionEnumDigital {
    TOGGLE_DRIVER_RELATIVITY,   // toggle
    TOGGLE_INTAKE_HEIGHT,       // toggle
    DRONE_LAUNCH,               // one-shot
    TRUSS_MOVE,                 // one-shot
    TRUSS_PULL,                 // hold it down,

    PIXEL_START,                // one-shot
    PIXEL_END,                  // one-shot
    PIXEL_MOVE_UP,              // one-shot
    PIXEL_MOVE_DOWN,            // one-shot
    PIXEL_COMMIT_LEFT,          // one-shot
    PIXEL_COMMIT_RIGHT,         // one-shot

    CUSTOM_ACTION_DIGITAL_A,
    CUSTOM_ACTION_DIGITAL_B,
    CUSTOM_ACTION_DIGITAL_X,
    CUSTOM_ACTION_DIGITAL_Y,
}

enum class ActionAnalog1 : ActionEnumAnalog1 {
    INTAKE_SPIN,                // X = spin power (+inwards, -outwards)
    ROTATION,                   // X = clockwise yaw       
    CLAW,                       // X = open..closed
    SLIDE_MANUAL,               // X = move some (-1 down, +1 up)

    CUSTOM_ACTION_ANALOG_A,
    CUSTOM_ACTION_ANALOG_B,
    CUSTOM_ACTION_ANALOG_X,
    CUSTOM_ACTION_ANALOG_Y,
}

enum class ActionAnalog2 : ActionEnumAnalog2 {
    MOVEMENT                    // Y = run, X = strafe
}