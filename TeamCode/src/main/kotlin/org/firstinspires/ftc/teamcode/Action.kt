package org.firstinspires.ftc.teamcode

import computer.living.gamepadyn.GDesc
import computer.living.gamepadyn.InputType

enum class Action {
    MOVEMENT,
    ROTATION,
    SPIN_INTAKE,
    CLAW,
    TOGGLE_DRIVER_RELATIVITY,
    TOGGLE_INTAKE_HEIGHT,
    TRUSS_HANG;

    companion object {
        @JvmStatic val actionMap = mapOf(
            MOVEMENT                    to GDesc(InputType.ANALOG, 2),
            ROTATION                    to GDesc(InputType.ANALOG, 1),
            SPIN_INTAKE                 to GDesc(InputType.ANALOG, 1),
            CLAW                        to GDesc(InputType.ANALOG, 1),
            TOGGLE_DRIVER_RELATIVITY    to GDesc(InputType.DIGITAL),
            TOGGLE_INTAKE_HEIGHT        to GDesc(InputType.DIGITAL),
            TRUSS_HANG                  to GDesc(InputType.DIGITAL)
        )
    }
}