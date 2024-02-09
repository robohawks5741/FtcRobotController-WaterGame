package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.InputDataDigital
import org.firstinspires.ftc.teamcode.*

class Claw(config: ModuleConfig) : BotModule(config) {
    private val clawLeft: Servo?    =   idc {   hardwareMap[Servo  ::class.java,   "clawL"     ] }
    private val clawRight: Servo?   =   idc {   hardwareMap[Servo  ::class.java,   "clawR"     ] }

    companion object {
        /**
         * minimums and maximums
         */
        const val CLAW_LEFT_OPEN: Double       = 0.0
        const val CLAW_LEFT_CLOSED: Double     = 0.29
        const val CLAW_RIGHT_OPEN: Double      = 0.36
        const val CLAW_RIGHT_CLOSED: Double    = 0.07
    }

    /**
     * Whether the left claw is open. The setter moves the servos.
     */
    var leftOpen: Boolean = true
        set(state) {
            field = state
            clawLeft?.position = if (state) CLAW_LEFT_OPEN else CLAW_LEFT_CLOSED
        }
    /**
     * Whether the right claw is open. The setter moves the servos.
     */
    var rightOpen: Boolean = true
        set(state) {
            field = state
            clawRight?.position = if (state) CLAW_RIGHT_OPEN else CLAW_RIGHT_CLOSED
        }

    init {
        if (clawLeft == null || clawRight == null) {
            val missing = mutableSetOf<String>()
            if (clawLeft == null) missing.add("clawL")
            if (clawRight == null) missing.add("clawR")

            status = Status(StatusEnum.MISSING_HARDWARE, hardwareMissing = missing)
        } else {
            clawLeft.direction = Servo.Direction.FORWARD
            clawRight.direction = Servo.Direction.FORWARD
        }
    }

    override fun modStartTeleOp() {
        if (gamepadyn == null) {
            telemetry.addLine("(Claw Module) TeleOp was enabled but Gamepadyn was null!")
            return
        }
        val openLeftClaw =      { it: InputDataDigital -> if (it()) leftOpen = true }
        val closeLeftClaw =     { it: InputDataDigital -> if (it()) leftOpen = false }
        val openRightClaw =     { it: InputDataDigital -> if (it()) rightOpen = true }
        val closeRightClaw =    { it: InputDataDigital -> if (it()) rightOpen = false }
        
        // TODO: add an API in Gamepadyn to do this more easily
        gamepadyn.players[0].getEvent(ActionDigital.CLAW_LEFT_OPEN,     openLeftClaw)
        gamepadyn.players[1].getEvent(ActionDigital.CLAW_LEFT_OPEN,     openLeftClaw)
        gamepadyn.players[0].getEvent(ActionDigital.CLAW_LEFT_CLOSE,    closeLeftClaw)
        gamepadyn.players[1].getEvent(ActionDigital.CLAW_LEFT_CLOSE,    closeLeftClaw)
        gamepadyn.players[0].getEvent(ActionDigital.CLAW_RIGHT_OPEN,    openRightClaw)
        gamepadyn.players[1].getEvent(ActionDigital.CLAW_RIGHT_OPEN,    openRightClaw)
        gamepadyn.players[0].getEvent(ActionDigital.CLAW_RIGHT_CLOSE,   closeRightClaw)
        gamepadyn.players[1].getEvent(ActionDigital.CLAW_RIGHT_CLOSE,   closeRightClaw)
    }

}