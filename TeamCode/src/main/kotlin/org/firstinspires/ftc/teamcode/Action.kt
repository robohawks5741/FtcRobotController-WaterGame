package org.firstinspires.ftc.teamcode

enum class ActionDigital : computer.living.gamepadyn.ActionEnumDigital {
    TOGGLE_DRIVER_RELATIVITY,   // toggle
    TOGGLE_INTAKE_HEIGHT,       // toggle
    TRUSS_HANG                  // one-shot
}
enum class ActionAnalog1 : computer.living.gamepadyn.ActionEnumAnalog1 {
    INTAKE_SPIN,                // X = spin power (+inwards, -outwards)
    ROTATION,                   // X = clockwise yaw       
    CLAW                        // X = open..closed
}




enum class ActionAnalog2 : computer.living.gamepadyn.ActionEnumAnalog2 {
    MOVEMENT                    // Y = run, X = strafe
}