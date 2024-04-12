package org.firstinspires.ftc.teamcode.components

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.TrussPosition
import org.firstinspires.ftc.teamcode.search

class Truss(manager: ComponentManager) : Component(manager) {
    private val trussPull: DcMotorEx?   = hardwareMap.search("hang")
    private val trussLeft: Servo?       = hardwareMap.search("trussL")
    private val trussRight: Servo?      = hardwareMap.search("trussR")

    var position: TrussPosition = TrussPosition.DOWN
        set(pos) {
            field = pos
            trussLeft?.position = field.leftPos
            trussRight?.position = field.rightPos
        }

    var hangPower: Double = 0.0
        set(power) {
            if (trussPull != null) {
                field = power.coerceIn(-1.0..1.0)
                trussPull.power = field
            }
        }

    init {
        trussPull?.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        position = TrussPosition.DOWN
    }
}