package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.Gamepadyn
import computer.living.gamepadyn.InputDataDigital
import org.firstinspires.ftc.teamcode.ActionDigital
import org.firstinspires.ftc.teamcode.ActionDigital.*
import org.firstinspires.ftc.teamcode.idc

/**
 * Truss module.
 *
 * Why is it called September?
 * Truss -> Trust -> Trust Fall -> Fall -> September.
 */
class September(cfg: ModuleConfig) : BotModule(cfg) {

    private val trussPull: DcMotorEx?   = idc {   hardwareMap[DcMotorEx    ::class.java,   "hang"      ] }
    private val trussLeft: Servo?       = idc {   hardwareMap[Servo        ::class.java,   "trussl"    ] }
    private val trussRight: Servo?      = idc {   hardwareMap[Servo        ::class.java,   "trussr"    ] }

    override fun modStart() {
        trussPull?.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        if (isTeleOp && gamepadyn != null) {
            val trussHandler: (InputDataDigital) -> Unit = {
                trussLeft?.position = 1.0
                trussRight?.position = 0.0
            }

            gamepadyn.players[1].getEvent(TRUSS_HANG)!!.addListener(trussHandler)
        }
    }

    override fun modUpdate() {
        if (isTeleOp && gamepadyn != null) {
            trussPull?.power = 1.0 * ((if (opMode.gamepad1.b) 1.0 else 0.0) + (if (opMode.gamepad1.a) -1.0 else 0.0))



//            gamepadyn.players[0].getEvent(ActionDigital.TRUSS_HANG)!!.addListener {
//                if (it()) {
//                    shared.servoTrussLeft?.position = 0.5
//                    shared.servoTrussRight?.position = 0.5
//                    shared.servoTrussLeft?.position = 0.0
//                    shared.servoTrussRight?.position = 0.0
//                }
//            }
        }
    }
}
