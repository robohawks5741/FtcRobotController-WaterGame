package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.*

class Claw(config: ModuleConfig) : BotModule(config) {
    private val servoLeft: Servo?    =   idc {   hardwareMap[Servo  ::class.java,   "clawL"     ] }
    private val servoRight: Servo?   =   idc {   hardwareMap[Servo  ::class.java,   "clawR"     ] }
    private var position: Double = 0.0

    companion object {
        /**
         * minimums and maximums
         */
        @JvmStatic private val CLAW_LEFT_OPEN: Double       = 0.29
        @JvmStatic private val CLAW_LEFT_CLOSED: Double     = 0.0
        @JvmStatic private val CLAW_RIGHT_OPEN: Double      = 0.07
        @JvmStatic private val CLAW_RIGHT_CLOSED: Double    = 0.36
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
            servoLeft?.position = interpLeftPos
            servoRight?.position = interpRightPos
        }

    init {
        if (servoLeft == null || servoRight == null) {
            val missing = mutableSetOf<String>()
            if (servoLeft != null) missing.add("clawl")
            if (servoRight != null) missing.add("clawr")

            status = Status(StatusEnum.MISSING_HARDWARE, hardwareMissing = missing)
        } else {
            servoLeft.direction = Servo.Direction.FORWARD
            servoRight.direction = Servo.Direction.FORWARD
        }
    }

//    override fun modStart() {
//        if (isTeleOp) {
//            if (gamepadyn == null) {
//                telemetry.addLine("(Claw Module) TeleOp was enabled but Gamepadyn was null!")
//                return
//            }
//
//            gamepadyn.players[0].getEvent(ActionAnalog1.CLAW)!! {
//                if (it()) useBotRelative = !useBotRelative
//            }
//        }
//    }

//    override fun <T : Enum<T>> bindTeleOp(gamepadyn: Gamepadyn<T>) {
//        super.bindTeleOp(gamepadyn)
//    }

}