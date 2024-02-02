package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.InputDataDigital
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.idc

/**
 * Truss module.
 *
 * Used to be called "September."
 */
class Trussel(cfg: ModuleConfig) : BotModule(cfg) {

    private val trussPull: DcMotorEx?   = idc {   hardwareMap[DcMotorEx    ::class.java,   "hang"      ] }
    private val trussLeft: Servo?       = idc {   hardwareMap[Servo        ::class.java,   "trussL"    ] }
    private val trussRight: Servo?      = idc {   hardwareMap[Servo        ::class.java,   "trussR"    ] }

    override fun modStart() {
        trussPull?.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        trussRight?.position = 0.32
        trussLeft?.position = 0.3
        if (isTeleOp && gamepadyn != null) {

            // move the truss hangers into position when the button is pressed
            val trussHandler: (InputDataDigital) -> Unit = {
                trussLeft?.position = 0.65
                trussRight?.position = 0.0
            }

            // pull in the motors if the button is being held down
            val hangHandler: (InputDataDigital) -> Unit = {
                trussPull?.power = if (it()) 1.0 else 0.0
            }

            gamepadyn.players[0].getEvent(TRUSS_MOVE).addListener(trussHandler)
            gamepadyn.players[1].getEvent(TRUSS_MOVE).addListener(trussHandler)

            gamepadyn.players[0].getEvent(TRUSS_PULL).addListener(hangHandler)
            gamepadyn.players[1].getEvent(TRUSS_PULL).addListener(hangHandler)
        }
    }

//    override fun modUpdate() {
//        if (isTeleOp && gamepadyn != null) {
//        }
//    }
}
