package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.InputDataAnalog1
import computer.living.gamepadyn.InputDataDigital
import computer.living.gamepadyn.Player
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.PlayerRH
import org.firstinspires.ftc.teamcode.TrussPosition
import org.firstinspires.ftc.teamcode.idc
import org.firstinspires.ftc.teamcode.stickCurve
import kotlin.math.abs

/**
 * Truss module.
 *
 * Used to be called "September," then spelled with an EL instead of LE
 */
class Trussle(cfg: ModuleConfig) : BotModule(cfg) {

    private val trussPull: DcMotorEx?   = idc {   hardwareMap[DcMotorEx    ::class.java,   "hang"      ] }
    private val trussLeft: Servo?       = idc {   hardwareMap[Servo        ::class.java,   "trussL"    ] }
    private val trussRight: Servo?      = idc {   hardwareMap[Servo        ::class.java,   "trussR"    ] }

    var position: TrussPosition = TrussPosition.DOWN
        set(pos) {
            field = pos
            trussLeft?.position = position.leftPos
            trussRight?.position = position.rightPos
        }

    override fun modStart() {
        trussPull?.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        trussLeft?.position = position.leftPos
        trussRight?.position = position.rightPos
    }

    override fun modStartTeleOp() {
        if (gamepadyn == null) {
            telemetry.addLine("(Trussle Module) TeleOp was enabled but Gamepadyn was null!")
            return
        }

        // cycle the truss hanger positions when the button is pressed
        gamepadyn.addListener(TRUSS_CYCLE) {
            if (it.data()) {
                position = when (position) {
                    TrussPosition.UP -> TrussPosition.DOWN
                    TrussPosition.DOWN -> TrussPosition.UP
                }
            }
        }

        // pull in the motors if the button is being held down
        gamepadyn.addListener(TRUSS_PULL) {
            trussPull?.power = it.data.x.toDouble()
        }
    }

    override fun modUpdateTeleOp() {
        if (gamepadyn == null) {
            TODO("cry about it")
        }
        val p0 = gamepadyn.players[0]
        val p1 = gamepadyn.players[1]
        val power = p0.getState(TRUSS_PULL).x + p1.getState(TRUSS_PULL).x
        trussPull?.power = power.toDouble().stickCurve()
    }

    override fun modUpdate() {
        trussLeft?.position = position.leftPos
        trussRight?.position = position.rightPos
        telemetry.addData("Truss position", position)
    }
}
