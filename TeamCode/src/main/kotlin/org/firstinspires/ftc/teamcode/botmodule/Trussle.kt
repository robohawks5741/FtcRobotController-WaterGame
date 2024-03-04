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

    var pullPower: Double = 0.0
        set(power) {
            field = power.coerceIn(-1.0..1.0)
            trussPull?.power = field
        }

    override fun modStart() {
        trussPull?.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        trussLeft?.position = position.leftPos
        trussRight?.position = position.rightPos
    }

    override fun modUpdate() {
        trussLeft?.position = position.leftPos
        trussRight?.position = position.rightPos
        telemetry.addData("Truss position", position)
    }
}
