package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.InputDataAnalog1
import computer.living.gamepadyn.InputDataDigital
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.TrussPosition
import org.firstinspires.ftc.teamcode.idc

/**
 * Truss module.
 *
 * Used to be called "September," then spelled with an EL instead of LE
 */
class Trussle(cfg: ModuleConfig) : BotModule(cfg) {

    private val trussPull: DcMotorEx?   = idc {   hardwareMap[DcMotorEx    ::class.java,   "hang"      ] }
    private val trussLeft: Servo?       = idc {   hardwareMap[Servo        ::class.java,   "trussL"    ] }
    private val trussRight: Servo?      = idc {   hardwareMap[Servo        ::class.java,   "trussR"    ] }

    private var position: TrussPosition = TrussPosition.DOWN

    override fun modStart() {
        trussPull?.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        trussLeft?.position = TrussPosition.DOWN.leftPos
        trussRight?.position = TrussPosition.DOWN.rightPos
    }

    override fun modStartTeleOp() {
        if (gamepadyn == null) {
            telemetry.addLine("(Trussle Module) TeleOp was enabled but Gamepadyn was null!")
            return
        }
        // cycle the truss hanger positions when the button is pressed
        val cycleHandler: (InputDataDigital) -> Unit = {
            if (it()) {
                position = when (position) {
                    TrussPosition.UP -> TrussPosition.DOWN
                    TrussPosition.DOWN -> TrussPosition.UP
                }
                trussLeft?.position = position.leftPos
                trussRight?.position = position.rightPos
            }
        }

        // pull in the motors if the button is being held down
        val hangHandler: (InputDataAnalog1) -> Unit = {
            trussPull?.power = it.x.toDouble()
        }

        gamepadyn.players[0].getEvent(TRUSS_CYCLE).addListener(cycleHandler)
        gamepadyn.players[1].getEvent(TRUSS_CYCLE).addListener(cycleHandler)

        gamepadyn.players[0].getEvent(TRUSS_PULL).addListener(hangHandler)
        gamepadyn.players[1].getEvent(TRUSS_PULL).addListener(hangHandler)
    }
    override fun modUpdate() {
        trussLeft?.position = position.leftPos
        trussRight?.position = position.rightPos
    }
}
