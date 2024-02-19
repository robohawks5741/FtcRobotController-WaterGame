package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.InputDataAnalog1
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.idc
import org.firstinspires.ftc.teamcode.search
import org.firstinspires.ftc.teamcode.toDouble

/**
 * Intake controls
 */
@Suppress("unused")
class Intake(config: ModuleConfig) : BotModule(config) {
    private val motorSpin: DcMotorEx?   = hardwareMap.search("inspin")
    private val servoLift: Servo?       = hardwareMap.search("inlift")

    init {
        if (motorSpin == null && servoLift == null) {
            status = Status(StatusEnum.BAD, hardwareMissing = setOf("inspin", "inlift"))
        } else {
//            motorLift?.position = 0
//            motorLift?.mode = DcMotor.RunMode.RUN_TO_POSITION
            motorSpin?.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            motorSpin?.power = 0.0
//            motorLift?.power = 0.2
        }
    }

    // 0.0 is off, 1.0 is inwards, -1.0 is outwards
    var power: Double = 0.0
        set(status) {
            // if the motor doesn't exist, we can't really change its power
            if (motorSpin == null) {
                field = Double.NaN
                return
            }
            field = if (config.parent.lsd.currentHeight >= 20) 0.0 else status
            motorSpin.power = field.coerceIn(-1.0..1.0)
        }

    var raised: Boolean = false
        set(height) {
            servoLift?.position = height.toDouble()
            field = height
        }

    override fun modStartTeleOp() {
        if (gamepadyn == null) {
            telemetry.addLine("(Intake Module) TeleOp was enabled but Gamepadyn was null!")
            return
        }
        val spinFunc: (InputDataAnalog1) -> Unit = {
            motorSpin?.power = it.x.toDouble().coerceIn(0.0..1.0)
        }
        gamepadyn.players[0].getEvent(INTAKE_SPIN, spinFunc)
        gamepadyn.players[1].getEvent(INTAKE_SPIN, spinFunc)
    }

    override fun modUpdate() {
        if (config.parent.lsd.currentHeight >= 20) {
            telemetry.addLine("Intake spin power overridden by slide height!")
            power = 0.0
        }
        telemetry.addData("Intake Spin", power)
        telemetry.addData("Intake raised", raised)
    }
}