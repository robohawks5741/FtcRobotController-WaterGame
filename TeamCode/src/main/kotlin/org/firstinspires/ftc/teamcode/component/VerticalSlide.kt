package org.firstinspires.ftc.teamcode.component

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Servo

class VerticalSlide(manager: ComponentManager) : Component(manager) {
    override val status: Status

    private val vSlideLeft: DcMotorEx?      = getHardware("vslideLeft")
    private val vSlideRight: DcMotorEx?     = getHardware("vslideRight")

    override fun start() {
        if (vSlideLeft != null && vSlideRight != null) {
            vSlideLeft.power = 1.0
            vSlideRight.power = 1.0

            vSlideLeft.direction = DcMotorSimple.Direction.FORWARD
            vSlideRight.direction = DcMotorSimple.Direction.REVERSE

            vSlideLeft.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            vSlideRight.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

//            vSlideLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
//            vSlideRight.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

            vSlideLeft.targetPositionTolerance = 60
            vSlideRight.targetPositionTolerance = 60

            vSlideLeft.targetPosition = 0
            vSlideRight.targetPosition = 0

            vSlideLeft.mode = DcMotor.RunMode.RUN_TO_POSITION
            vSlideRight.mode = DcMotor.RunMode.RUN_TO_POSITION
        }
    }

    override fun loop() {
        telemetry.addLine("V. Slide Left Current Pos: ${vSlideLeft?.currentPosition}")
        telemetry.addLine("V. Slide Left Target Pos: ${vSlideLeft?.targetPosition}")
        telemetry.addLine("V. Slide Right Current Pos: ${vSlideRight?.currentPosition}")
        telemetry.addLine("V. Slide Right Target Pos: ${vSlideRight?.targetPosition}")
    }

    fun retract() { position = 0 }

    fun extend() { position = V_SLIDE_MAX }

    var position: Int = 0
        set(targetPos) {
            field = targetPos.coerceIn(0..V_SLIDE_MAX)
            vSlideLeft?.targetPosition = field
            vSlideRight?.targetPosition = field
        }

    init {
        val functionality = when {
            vSlideLeft == null || vSlideRight == null -> Functionality.NONE
            else -> Functionality.FULL
        }
        val hardwareSet: Set<HardwareUsage> = setOf(
            HardwareUsage("vslideRight", Servo::class, vSlideRight != null),
            HardwareUsage("vslideLeft", Servo::class, vSlideLeft != null)
        )
        status = Status(
            functionality,
            hardwareSet
        )
    }

    companion object {
        const val V_SLIDE_MAX: Int = 288 * 4
    }

}