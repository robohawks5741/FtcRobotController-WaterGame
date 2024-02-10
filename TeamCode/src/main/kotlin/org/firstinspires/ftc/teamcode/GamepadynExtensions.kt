package org.firstinspires.ftc.teamcode

import computer.living.gamepadyn.ActionBind
import computer.living.gamepadyn.InputData
import computer.living.gamepadyn.InputDataAnalog1
import computer.living.gamepadyn.InputDataDigital
import computer.living.gamepadyn.RawInputAnalog1


// TODO: implement all of these this in Gamepadyn
/**
 * @param keepState If the threshold isn't met and [keepState] is true, it will just output the previous state (instead of `true`).
 */
class ActionBindAnalog1Threshold(targetAction: ActionDigital, input: RawInputAnalog1, private val threshold: Float, private val keepState: Boolean = false) : ActionBind<ActionDigital>(
    targetAction, input
) {
    override fun transform(
        inputState: InputData,
        targetActionState: InputData,
        delta: Double
    ): InputData = when {
        (inputState !is InputDataAnalog1) -> targetActionState
        (inputState.x > threshold) -> InputDataDigital(true)
        keepState -> targetActionState
        else -> InputDataDigital(false)
    }
}

/**
 * If the analog input is above a certain threshold, output a certain analog value.
 * @param activeValue The value to output if the input is above a certain threshold.
 * @param inactiveValue The value to output if the input is not above a certain threshold. If [Float.NaN], it will output the previous state (good for actions with multiple binds).
 */
class ActionBindAnalog1SnapToAnalog1(
    targetAction: ActionAnalog1,
    input: RawInputAnalog1,
    private val activeValue: Float = 1f,
    private val inactiveValue: Float = Float.NaN,
    private val threshold: Float = 0.5f,
) : ActionBind<ActionAnalog1>(
    targetAction, input
) {
    override fun transform(
        inputState: InputData,
        targetActionState: InputData,
        delta: Double
    ): InputData = when {
        (inputState !is InputDataAnalog1) -> targetActionState
        (inputState.x > threshold) -> InputDataAnalog1(activeValue)
        inactiveValue.isNaN() -> targetActionState
        else -> InputDataAnalog1(inactiveValue)
    }
}