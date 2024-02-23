package org.firstinspires.ftc.teamcode

import computer.living.gamepadyn.ActionEnumDigital
import computer.living.gamepadyn.ActionEnumAnalog1
import computer.living.gamepadyn.ActionEnumAnalog2

enum class ActionDigital : ActionEnumDigital {
    // toggles
    TOGGLE_DRIVER_RELATIVITY,
    TOGGLE_INTAKE_HEIGHT,
    TOGGLE_CARDINALITY_SNAP,

    // one-shots
    TRUSS_CYCLE,
    DRONE_LAUNCH,
    CLAW_LEFT_OPEN,
    CLAW_LEFT_CLOSE,
    CLAW_RIGHT_OPEN,
    CLAW_RIGHT_CLOSE,

    // MACRO NAMES ARE MISLEADING BECAUSE THEY DO MULTIPLE THINGS!!! SEE THE DRIVER CONTROL FILE FOR MORE

    SLIDE_ADJUST_UP,
    SLIDE_ADJUST_DOWN,

    // one-shot
    MACRO_SLIDE_UP,
    MACRO_SLIDE_DOWN,
    MACRO_PLACE_PIXEL,

    CUSTOM_ACTION_DIGITAL_A,
    CUSTOM_ACTION_DIGITAL_B,
    CUSTOM_ACTION_DIGITAL_X,
    CUSTOM_ACTION_DIGITAL_Y,
}

enum class ActionAnalog1 : ActionEnumAnalog1 {
    // X = spin power (+inwards, -outwards)
    INTAKE_SPIN,
    // X = clockwise yaw
    ROTATION,
    // X = open..closed
    CLAW,
    // hold it down (+1 = pull, -1 = release)
    TRUSS_PULL,

    // testing
    CUSTOM_ACTION_ANALOG_A,
    CUSTOM_ACTION_ANALOG_B,
    CUSTOM_ACTION_ANALOG_X,
    CUSTOM_ACTION_ANALOG_Y,
}

enum class ActionAnalog2 : ActionEnumAnalog2 {
    // Y = run, X = strafe
    MOVEMENT,
}