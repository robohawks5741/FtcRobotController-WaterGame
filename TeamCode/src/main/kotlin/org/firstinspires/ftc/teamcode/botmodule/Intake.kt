package org.firstinspires.ftc.teamcode.botmodule

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import computer.living.gamepadyn.InputDataAnalog1
import org.firstinspires.ftc.teamcode.ActionAnalog1.*
import org.firstinspires.ftc.teamcode.idc

/**
 * Intake controls
 */
@Suppress("unused")
class Intake(config: ModuleConfig) : BotModule(config) {
    private val motorLiftName = "inlift"
    private val motorSpinName = "inspin"
    private val motorSpin: DcMotorEx?   = idc { hardwareMap[DcMotorEx::class.java,   motorSpinName    ] }
    private val motorLift: Servo?       = idc { hardwareMap[Servo    ::class.java,   motorLiftName    ] }

    init {
        if (motorSpin == null && motorLift == null) {
            status = Status(StatusEnum.MISSING_HARDWARE, hardwareMissing = setOf(motorLiftName, motorSpinName))
        } else {
//            motorLift?.position = 0
//            motorLift?.mode = DcMotor.RunMode.RUN_TO_POSITION
            motorSpin?.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            motorSpin?.power = 0.0
//            motorLift?.power = 0.2
        }
    }

    // 0.0 is off, 1.0 is inwards, -1.0 is outwards
    @Suppress("MemberVisibilityCanBePrivate")
    var power: Double = 0.0
        set(status) {
            if (motorSpin == null) field = 0.0
            else {
                field = status
                motorSpin.power = if (field > 1.0) 1.0 else if (field < -1.0) -1.0 else field
            }
        }
    var raised: Boolean = false
        private set(height) {
//            motorLift?.position = if (height) 0 else 10
            field = height
        }

    override fun modStartTeleOp() {
        if (gamepadyn == null) {
            telemetry.addLine("(Intake Module) TeleOp was enabled but Gamepadyn was null!")
            return
        }
        val spinFunc: (InputDataAnalog1) -> Unit = {
            val d = it.x.toDouble()
            motorSpin?.power = if (d > 1.0) 1.0 else if (d < -1.0) -1.0 else d
        }
        gamepadyn.players[0].getEvent(INTAKE_SPIN, spinFunc)
        gamepadyn.players[1].getEvent(INTAKE_SPIN, spinFunc)
    }

    override fun modUpdate() {
        telemetry.addLine("Intake Spin = $power")
    }
}