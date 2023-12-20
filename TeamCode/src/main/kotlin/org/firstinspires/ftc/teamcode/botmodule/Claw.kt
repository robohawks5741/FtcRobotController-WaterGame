package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.teamcode.Action

class Claw(
    private val servoLeft: Servo,
    private val servoRight: Servo,
    opMode: OpMode, isTeleOp: Boolean, gamepadyn: Gamepadyn<Action>?
) : BotModule(opMode, isTeleOp, gamepadyn) {

    init {
        servoLeft.direction = Servo.Direction.FORWARD
        servoRight.direction = Servo.Direction.FORWARD
    }

    private var position: Double = 0.0

    companion object {
        /**
         * minimums and maximums
         */
        @JvmStatic private val CLAW_LEFT_OPEN: Double = 0.0
        @JvmStatic private val CLAW_LEFT_CLOSED: Double = 0.3
        @JvmStatic private val CLAW_RIGHT_OPEN: Double = 0.0
        @JvmStatic private val CLAW_RIGHT_CLOSED: Double = 0.3
    }

    /**
     * 1 is fully closed,
     * 0 is fully opened.
     */
    var state: Double
        get() = position
        set(x) {
            val y = if (x > 1.0) 1.0; else if (x < 0.0) 0.0; else x
            position = y
            val interpLeftPos = (((1.0 - position) * CLAW_LEFT_OPEN) + (position * CLAW_LEFT_CLOSED))
            val interpRightPos = (((1.0 - position) * CLAW_RIGHT_OPEN) + (position * CLAW_RIGHT_CLOSED))
            servoLeft.position = interpLeftPos
            servoRight.position = interpRightPos
        }
}