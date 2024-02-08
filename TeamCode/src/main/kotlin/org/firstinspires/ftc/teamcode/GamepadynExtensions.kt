package org.firstinspires.ftc.teamcode

import computer.living.gamepadyn.ActionBind
import computer.living.gamepadyn.InputData
import computer.living.gamepadyn.InputDataAnalog1
import computer.living.gamepadyn.InputDataDigital
import computer.living.gamepadyn.RawInputAnalog1


// TODO: implement this in Gamepadyn
class ActionBindAnalog1Threshold(targetAction: ActionDigital, input: RawInputAnalog1, private val threshold: Float) : ActionBind<ActionDigital>(
    targetAction, input
) {
    override fun transform(
        inputState: InputData,
        targetActionState: InputData,
        delta: Double
    ): InputData {
        return InputDataDigital((inputState as InputDataAnalog1).x > threshold)
    }
}

class ActionBindAnalog1Snap(targetAction: ActionAnalog1, input: RawInputAnalog1, private val activeValue: Float = 1f, private val inactiveValue: Float = 0f, private val threshold: Float = 0.5f) : ActionBind<ActionAnalog1>(
    targetAction, input
) {
    override fun transform(
        inputState: InputData,
        targetActionState: InputData,
        delta: Double
    ): InputData {
        return InputDataAnalog1(if ((inputState as InputDataAnalog1).x > threshold) activeValue else inactiveValue)
    }
}