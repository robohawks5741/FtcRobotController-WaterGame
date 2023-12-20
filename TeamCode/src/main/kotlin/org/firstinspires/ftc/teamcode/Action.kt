package org.firstinspires.ftc.teamcode

import computer.living.gamepadyn.GDesc
import computer.living.gamepadyn.InputType
import computer.living.gamepadyn.InputType.ANALOG
import computer.living.gamepadyn.InputType.DIGITAL

enum class Action {
    MOVEMENT,                   // Y = run, X = strafe
    ROTATION,                   // X = clockwise yaw
    SPIN_INTAKE,                // X = spin power (+inwards, -outwards)
    CLAW,                       // X = open..closed
    TOGGLE_DRIVER_RELATIVITY,   // toggle
    TOGGLE_INTAKE_HEIGHT,       // toggle
    TRUSS_HANG;                 // one-shot

    companion object {
        val actionMap = mapOf(
            MOVEMENT                    to GDesc(ANALOG, 2),
            ROTATION                    to GDesc(ANALOG, 1),
            SPIN_INTAKE                 to GDesc(ANALOG, 1),
            CLAW                        to GDesc(ANALOG, 1),
            TOGGLE_DRIVER_RELATIVITY    to GDesc(DIGITAL),
            TOGGLE_INTAKE_HEIGHT        to GDesc(DIGITAL),
            TRUSS_HANG                  to GDesc(DIGITAL)
        )
    }
}