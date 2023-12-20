package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.Gamepadyn
import org.firstinspires.ftc.teamcode.Action
import org.firstinspires.ftc.teamcode.Action.*

typealias TrussMod = September

/**
 * Truss module.
 *
 * Why is it called September?
 * Truss -> Trust -> Trust Fall -> Fall -> September.
 */
class September(
    private val trussPull: DcMotorEx,
    private val trussLeft: Servo,
    private val trussRight: Servo,
    opMode: OpMode, isTeleOp: Boolean, gamepadyn: Gamepadyn<Action>? = null
) : BotModule(opMode, isTeleOp, gamepadyn) {
    override fun modUpdate() {
        if (isTeleOp && gamepadyn != null) {
            // TODO: gamepadyn implementation
            trussPull.power = 1.0 * ((if (opMode.gamepad1.b) 1.0 else 0.0) + (if (opMode.gamepad1.a) -1.0 else 0.0))

            gamepadyn.players[1].getEventDigital(TRUSS_HANG)!!.addListener {
                trussLeft.position = 1.0
                trussRight.position = 0.0
            }
        }
    }
}
