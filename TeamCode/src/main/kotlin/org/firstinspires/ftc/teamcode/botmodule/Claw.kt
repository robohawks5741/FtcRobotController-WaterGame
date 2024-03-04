package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.InputDataDigital
import org.firstinspires.ftc.teamcode.*
import org.firstinspires.ftc.teamcode.ActionDigital.*

class Claw(config: ModuleConfig) : BotModule(config) {
    private val clawLeft: Servo?            =   hardwareMap.search("clawL")
    private val clawRight: Servo?           =   hardwareMap.search("clawR")

    companion object {
        /**
         * minimums and maximums
         */
        const val CLAW_LEFT_POS_OPEN: Double       = 0.0
        const val CLAW_LEFT_POS_CLOSED: Double     = 0.29
        const val CLAW_RIGHT_POS_OPEN: Double      = 0.36
        const val CLAW_RIGHT_POS_CLOSED: Double    = 0.07
    }

    /**
     * Whether the left claw is open. The setter moves the servos.
     */
    var leftOpen: Boolean = true
        set(state) {
            field = state
            clawLeft?.position = if (state) CLAW_LEFT_POS_OPEN else CLAW_LEFT_POS_CLOSED
        }
    /**
     * Whether the right claw is open. The setter moves the servos.
     */
    var rightOpen: Boolean = true
        set(state) {
            field = state
            clawRight?.position = if (state) CLAW_RIGHT_POS_OPEN else CLAW_RIGHT_POS_CLOSED
        }

    init {
        if (clawLeft == null || clawRight == null) {
            val missing = mutableSetOf<String>()
            if (clawLeft == null) missing.add("clawL")
            if (clawRight == null) missing.add("clawR")

            status = Status(StatusEnum.BAD, hardwareMissing = missing)
        } else {
            clawLeft.direction = Servo.Direction.FORWARD
            clawRight.direction = Servo.Direction.FORWARD
        }
    }
}