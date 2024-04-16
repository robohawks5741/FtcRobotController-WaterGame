package org.firstinspires.ftc.teamcode.component

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.TrussPosition
import org.firstinspires.ftc.teamcode.search

class Truss(manager: ComponentManager) : Component(manager) {
    private val trussPull: DcMotorEx?   = hardwareMap.search("hang")
    private val trussLeft: Servo?       = hardwareMap.search("trussL")
    private val trussRight: Servo?      = hardwareMap.search("trussR")

    override val status: Status

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
        val functionality = if (trussLeft == null || trussRight == null || trussPull == null) Functionality.NONE else Functionality.FULL

        val hardwareSet: Set<HardwareUsage> = setOf(
            HardwareUsage("trussR", DcMotorEx::class, trussRight != null),
            HardwareUsage("trussL", DcMotorEx::class, trussLeft != null),
            HardwareUsage("hang", Servo::class, trussPull != null),
        )
        status = Status(
            functionality,
            hardwareSet
        )

        trussPull?.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        position = TrussPosition.DOWN
    }
}